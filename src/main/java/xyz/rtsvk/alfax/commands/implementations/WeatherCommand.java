package xyz.rtsvk.alfax.commands.implementations;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import xyz.rtsvk.alfax.util.guildstate.GuildState;
import xyz.rtsvk.alfax.commands.ICommand;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.chatcontext.IChatContext;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;

public class WeatherCommand implements ICommand {

	private final String apiKey;
	private final String lang;

	public WeatherCommand(Config config) {
		this.apiKey = config.getString("weather-api-key");
		this.lang = config.getString("weather-lang");
	}

	@Override
	public void handle(User user, IChatContext chat, List<String> args, GuildState guildState, GatewayDiscordClient bot, MessageManager language) {
		DecimalFormat f = new DecimalFormat("##.00");

		try {
			String cityName = String.join(" ", args);

			// fetch the weather data
			URL url = new URL("https://api.openweathermap.org/data/2.5/weather?appid=" + this.apiKey + "&q=" + cityName + "&units=metric&lang=" + this.lang);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			BufferedInputStream input = new BufferedInputStream(conn.getInputStream());

			StringBuilder response = new StringBuilder();
			byte[] buffer = new byte[2048];
			while (input.available() > 0) {
				int read = input.read(buffer);
				response.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
			}
			JSONObject json = (JSONObject) (new JSONParser().parse(response.toString()));

			JSONObject main = (JSONObject) json.get("main");
			JSONObject wind = (JSONObject) json.get("wind");
			JSONObject weather = (JSONObject) ((JSONArray) json.get("weather")).get(0);
			double temp = Double.parseDouble(String.valueOf(main.get("temp")));
			double feelsLike = Double.parseDouble(String.valueOf(main.get("feels_like")));
			int humidity = Integer.parseInt(String.valueOf(main.get("humidity")));
			double windSpeed = Double.parseDouble(String.valueOf(wind.get("speed")));
			int windHeading = Integer.parseInt(String.valueOf(wind.get("deg")));

			input.close();
			conn.disconnect();

			EmbedCreateSpec table = EmbedCreateSpec.builder()
					.author("Powered by OpenWeatherMap.com", "https://www.openweathermap.org/", "http://openweathermap.org/img/w/" + weather.get("icon") + ".png")
					.title("Aktualne pocasie pre " + cityName)
					.addField("Stav:", weather.get("main").toString(), true)
					.addField("Popis:", weather.get("description").toString(), true)
					.addField("Teplota:", temp + " °C (pocitovo " + feelsLike + ")", false)
					.addField("Najvyssia denna teplota: ", main.get("temp_max") + " °C", false)
					.addField("Najnizsia denna teplota: ", main.get("temp_min") + " °C", false)
					.addField("Tlak:", main.get("pressure") + " hPa", false)
					.addField("Vlhkosť:", humidity + "%", false)
					.addField("Vietor:", windHeading + "@" + windSpeed + " m/s (" + f.format(kphToKts(windSpeed)) + " kts)", false)
					.timestamp(Instant.now())
					.build();

			chat.sendMessage(table);
		} catch (Exception e) {
			chat.sendMessage("**Chyba pri ziskavani udajov o pocasi.**");
			e.printStackTrace(System.out);
		}
	}

	@Override
	public String getName() {
		return "weather";
	}

	@Override
	public String getDescription() {
		return "Zobrazi aktualne pocasie pre zadanu oblast.";
	}

	private double kphToKts(double mps) {
		return (mps * 3.6) / 1.852;
	}

	@Override
	public String getUsage() {
		return "weather <mesto>";
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
