package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chat.Chat;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class MathExpressionCommand implements ICommand {

	private final List<Symbol> symtable = new ArrayList<>();

	public MathExpressionCommand() {
		// basic math operators
		this.symtable.add(new Symbol("+", 2, e -> e[0] + e[1]));
		this.symtable.add(new Symbol("-", 2, e -> e[0] - e[1]));
		this.symtable.add(new Symbol("×", 2, e -> e[0] * e[1]));
		this.symtable.add(new Symbol("*", 2, e -> e[0] * e[1]));
		this.symtable.add(new Symbol("÷", 2, e -> e[0] / e[1]));
		this.symtable.add(new Symbol("/", 2, e -> e[0] / e[1]));
		this.symtable.add(new Symbol("%", 2, e -> e[0] % e[1]));
		this.symtable.add(new Symbol("^", 2, e -> Math.pow(e[0], e[1])));
		this.symtable.add(new Symbol("root", 2, e -> Math.pow(e[0], 1 / e[1])));

		// constants
		this.symtable.add(new Symbol("pi", 0, e -> Math.PI));
		this.symtable.add(new Symbol("e",0, e -> Math.E));
		this.symtable.add(new Symbol("phi", 0, e -> 1.61803398874989484820458683436563811772030917980576286213544862270526046281890));
		this.symtable.add(new Symbol("inf", 0, e -> Double.POSITIVE_INFINITY));

		// functions
		this.symtable.add(new Symbol("ln", 1, e -> Math.log(e[0])));
		this.symtable.add(new Symbol("log", 1, e -> Math.log10(e[0])));
		this.symtable.add(new Symbol("sqrt", 1, e -> Math.sqrt(e[0])));
		this.symtable.add(new Symbol("cbrt", 1, e -> Math.cbrt(e[0])));

		// trigonometry
		this.symtable.add(new Symbol("sin", 1, e -> Math.sin(e[0])));
		this.symtable.add(new Symbol("cos", 1, e -> Math.cos(e[0])));
		this.symtable.add(new Symbol("tan", 1, e -> Math.tan(e[0])));
		this.symtable.add(new Symbol("asin", 1, e -> Math.asin(e[0])));
		this.symtable.add(new Symbol("acos", 1, e -> Math.acos(e[0])));
		this.symtable.add(new Symbol("atan", 1, e -> Math.atan(e[0])));
		this.symtable.add(new Symbol("atan2", 1, e -> Math.atan2(e[0], e[1])));
	}

	@Override
	public void handle(User user, Chat chat, List<String> args, Snowflake guildId, GatewayDiscordClient bot, MessageManager language) throws Exception {
		String rawExpression = args.stream().filter(s -> !s.contains("=")).collect(Collectors.joining(""));
		Stack<Token> stack = new Stack<>();
		Stack<Double> evalStack = new Stack<>();
		List<Token> tokens = tokenize(rawExpression);
		List<Token> postfix = new ArrayList<>();
		List<Symbol> symtable = new ArrayList<>(this.symtable);

		args = args.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
		args.stream().filter(s -> s.contains("=")).forEach(s -> {
			String[] parts = s.split("=");

			String name = parts[0];
			String value = parts[1];
			if (this.symtable.stream().anyMatch(symbol -> symbol.getName().equals(name))) {
				chat.sendMessage("Symbol '" + name + "' already exists");
				return;
			}
			try {
				double val = Double.parseDouble(value);
				symtable.add(new Symbol(name, 0, e -> val));
			} catch (NumberFormatException e) {
				chat.sendMessage("Invalid symbol value `" + value + "`");
			}
		});

		// convert the expression to postfix notation
		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			Token nextToken = i + 1 < tokens.size() ? tokens.get(i + 1) : null;
			switch (token.type) {
				case INTEGER_LITERAL:
				case DOUBLE_LITERAL:
					postfix.add(token);
					break;

				case SYMBOL:
					if (nextToken != null && nextToken.type == TokenType.OPEN_BRACKET) {
						stack.push(token);
					}
					else {
						postfix.add(token);
					}
					break;

				case OPERATOR:
					while (!stack.empty() && stack.peek().type == TokenType.OPERATOR && getPriority(stack.peek().value.charAt(0)) >= getPriority(token.value.charAt(0))) {
						postfix.add(stack.pop());
					}
					stack.push(token);
					break;

				case OPEN_BRACKET:
					stack.push(token);
					break;

				case CLOSE_BRACKET:
					while (!stack.empty() && stack.peek().type != TokenType.OPEN_BRACKET) {
						postfix.add(stack.pop());
					}
					stack.pop();
					break;

				default:
					break;
			}
		}

		while (!stack.empty()) {
			postfix.add(stack.pop());
		}

		// evaluate the expression
		for (Token token : postfix) {
			if (token.getType() == TokenType.INTEGER_LITERAL || token.getType() == TokenType.DOUBLE_LITERAL) {
				evalStack.push(Double.parseDouble(token.getValue()));
			}
			else if (token.getType() == TokenType.SYMBOL) {
				Symbol symbol = symtable.stream().filter(s -> s.getName().equals(token.getValue())).findFirst().orElse(null);
				if (symbol == null) {
					chat.sendMessage(language.getFormattedString("command.math.unknown-symbol")
							.addParam("symbol", token.getValue()).build());
					return;
				}
				evalStack.push(symbol.getValue());
			}
			else if (token.getType() == TokenType.OPERATOR) {
				String operator = token.getValue();
				Symbol symbol = symtable.stream().filter(s -> s.getName().equals(operator)).findFirst().orElse(null);
				if (symbol == null) {
					chat.sendMessage(language.getFormattedString("command.math.unknown-operator")
							.addParam("operator", operator).build());
					return;
				}
				if (!symbol.eval(evalStack)) {
					chat.sendMessage(language.getFormattedString("command.math.invalid-num-args")
							.addParam("operator", operator).build());
					return;
				}
			}
		}

		chat.sendMessage("```\n" + rawExpression  + " = " + evalStack.pop() + "\n```");
	}

	private List<Token> tokenize(String expression) {   // lexer (tokenizer)
		List<Token> tokens = new ArrayList<>();
		StringBuilder token = new StringBuilder();

		int index = 0;
		while (index < expression.length()) {
			if (isOperator(expression.charAt(index))) {
				if (!token.isEmpty()) {
					tokens.add(new Token(token.toString(), TokenType.SYMBOL));
					token = new StringBuilder();
				}
				tokens.add(new Token(String.valueOf(expression.charAt(index)), TokenType.OPERATOR));
				index++;
			}
			else if (expression.charAt(index) == '(') {
				if (!token.isEmpty()) {
					tokens.add(new Token(token.toString(), TokenType.SYMBOL));
					token = new StringBuilder();
				}
				tokens.add(new Token(String.valueOf(expression.charAt(index)), TokenType.OPEN_BRACKET));
				index++;
			}
			else if (expression.charAt(index) == ')') {
				if (!token.isEmpty()) {
					tokens.add(new Token(token.toString(), TokenType.SYMBOL));
					token = new StringBuilder();
				}
				tokens.add(new Token(String.valueOf(expression.charAt(index)), TokenType.CLOSE_BRACKET));
				index++;
			}
			else if (Character.isDigit(expression.charAt(index))) {
				while (index < expression.length() && (Character.isDigit(expression.charAt(index)) || expression.charAt(index) == '.')) {
					token.append(expression.charAt(index));
					index++;
				}
				tokens.add(new Token(token.toString(), token.toString().contains(".") ? TokenType.DOUBLE_LITERAL : TokenType.INTEGER_LITERAL));
				token = new StringBuilder();
			}
			else {
				token.append(expression.charAt(index));
				index++;
			}
		}

		if (!token.isEmpty()) {
			tokens.add(new Token(token.toString(), TokenType.SYMBOL));
		}

		return tokens;
	}

	private boolean isOperator(char c) {
		return List.of('+', '-', '×', '*', '÷', '/', '%', '^').contains(c);
	}

	private int getPriority(char c) {
		switch (c) {
			case '+':
			case '-':
				return 1;
			case '*':
			case '/':
			case '%':
			case '×':
			case '÷':
				return 2;
			case '^':
				return 3;
			default:
				return 0;
		}
	}

	@Override
	public String getName() {
		return "calculate";
	}

	@Override
	public String getDescription() {
		return "command.math.description";
	}

	@Override
	public String getUsage() {
		return "calculate <expression>";
	}

	@Override
	public List<String> getAliases() {
		return List.of("calc", "math");
	}

	@Override
	public int getCooldown() {
		return 0;
	}

	private static class Token {
		private final String value;
		private final TokenType type;

		public Token(String value, TokenType type) {
			this.value = value;
			this.type = type;
		}

		public String getValue() {
			return value;
		}

		public TokenType getType() {
			return type;
		}
	}

	private static class Symbol {

		private final String name;
		private final int arity;
		private final Function valueSupplier;
		public Symbol(String name, int arity, Function valueSupplier) {
			this.name = name;
			this.arity = arity;
			this.valueSupplier = valueSupplier;
		}

		public boolean eval(Stack<Double> stack) {
			if (stack.size() < arity) {
				return false;
			}
			double[] args = new double[arity];
			for (int i = arity - 1; i >= 0; i--) {
				args[i] = stack.pop();
			}
			stack.push(this.getValue(args));
			return true;
		}

		public String getName() {
			return this.name;
		}

		public Double getValue() {
			return this.valueSupplier.apply(0.0);
		}

		public Double getValue(double... args) {
			return this.valueSupplier.apply(args);
		}

		public int getArity() {
			return arity;
		}
	}
	private interface Function {
		double apply(double... args);
	}

	private enum TokenType {
		DOUBLE_LITERAL,
		INTEGER_LITERAL,
		SYMBOL,
		OPERATOR,
		OPEN_BRACKET,
		CLOSE_BRACKET
	}

	public enum CalculatorMode {
		DEGREES,
		RADIANS;
	}
}
