package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import xyz.rtsvk.alfax.commands.Command;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public class TodayCommand implements Command {

	private JSONObject names;

	public TodayCommand() throws IOException, ParseException {
		File calendar = new File("calendar.json");
		this.names = (JSONObject) (new JSONParser().parse(new FileReader(calendar)));
	}

	@Override
	public void handle(User user, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) {
		String displayDate = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL));

		int month = LocalDate.now().getMonthValue();
		int dayofmonth = LocalDate.now().getDayOfMonth();
		String[] names = ((JSONObject)this.names.get(month + "")).get(dayofmonth + "").toString().split(" ");

		StringBuilder name = new StringBuilder("```\n");
		for (String n : names)
			name.append(n.replace(",", "")).append("\n");
		name.append("```");

		channel.createMessage("**Dnes je " + displayDate + ". Meniny majú:** " + name.toString()).block();
	}

	@Override
	public String getDescription() {
		return "Vypise dnesny den v tyzdni, datum a meniny.";
	}
}