package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public class TodayCommand implements ICommand {

	private final JSONObject names;

	public TodayCommand() throws IOException, ParseException {
		InputStream stream = this.getClass().getClassLoader().getResourceAsStream("calendar.json");
		if (stream == null) throw new FileNotFoundException("calendar.json not found!");
		Reader reader = new InputStreamReader(stream);
		this.names = (JSONObject) new JSONParser().parse(reader);
		reader.close();
		stream.close();
	}

	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) {
		String displayDate = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL));

		int month = LocalDate.now().getMonthValue()-1;
		int dayofmonth = LocalDate.now().getDayOfMonth();
		String[] names = ((JSONObject)this.names.get(month + "")).get(dayofmonth + "").toString().split(" ");

		StringBuilder name = new StringBuilder("```\n");
		for (String n : names)
			name.append(n.replace(",", "")).append("\n");
		name.append("```");

		chat.sendMessage("**Dnes je " + displayDate + ". Meniny majú:** " + name);
	}

	@Override
	public String getName() {
		return "today";
	}

	@Override
	public String getDescription() {
		return "Vypise dnesny den v tyzdni, datum a meniny.";
	}

	@Override
	public String getUsage() {
		return "today";
	}

	@Override
	public List<String> getAliases() {
		return List.of();
	}

	@Override
	public int getCooldown() {
		return 0;
	}
}
