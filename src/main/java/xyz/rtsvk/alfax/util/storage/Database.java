package xyz.rtsvk.alfax.util.storage;

import discord4j.common.util.Snowflake;
import xyz.rtsvk.alfax.services.scheduler.Task;
import xyz.rtsvk.alfax.tasks.Event;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.util.text.FormattedString;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
	private static boolean initialized;
	private static Logger logger;
	private static Connection conn;
	private static Config botConfig;
	private static Map<Snowflake, MessageManager> languageCache = new HashMap<>();
	public static final byte PERMISSION_NONE = 0;
	public static final byte PERMISSION_ADMIN = 0x01;
	public static final byte PERMISSION_API_CHANNEL = 1 << 1;
	public static final byte PERMISSION_API_DM = 1 << 2;
	public static final byte PERMISSION_MQTT = 1 << 3;
	public static final byte PERMISSION_RATE_LIMIT_BYPASS = 1 << 4;
	public static final byte PERMISSION_API_GET_FILE = 1 << 5;
	public static final byte PERMISSION_API = PERMISSION_API_CHANNEL | PERMISSION_API_DM | PERMISSION_API_GET_FILE;

	public static void init(Config config) {
		init(
				config.getString("db-host"),
				config.getString("db-user"),
				config.getString("db-password"),
				config.getString("db-name")
		);
		botConfig = config;
	}

	public static synchronized void init(String host, String user, String password, String db) {
		logger = new Logger(Database.class);
		initialized = false;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			String url = "jdbc:mysql://" + user + ":" + password + "@" + host;
			logger.info("Connecting to database '" + db +"' at " + host + " as user '" + user + "'...");

			conn = DriverManager.getConnection(url);
			Statement st = conn.createStatement();

			st.addBatch("CREATE DATABASE IF NOT EXISTS `" + db + "`;");
			st.addBatch("USE `" + db + "`;");
			st.executeBatch();

			st.addBatch("CREATE TABLE IF NOT EXISTS `system_info` (`vkey` varchar(64), `value` varchar(128), PRIMARY KEY(`vkey`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `guilds` (`guild_id` varchar(128), `announcement_channel` varchar(128), `gpt_tokens_used` bigint, PRIMARY KEY(`guild_id`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `schedule` (`id` int AUTO_INCREMENT, `command` varchar(32), `description` text, `channel` varchar(128), `guild` varchar(128), `exec_date` date, `exec_time` varchar(8), `days` varchar(16), PRIMARY KEY(`id`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `events`(`id` int AUTO_INCREMENT, `name` varchar(128), `description` text, `time` long, `guild` varchar(128), PRIMARY KEY(`id`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `auth` (`id` varchar(128), `auth_key` varchar(128), `permissions` int, `credits` long, `tokens_used` bigint, `language` varchar(4), PRIMARY KEY(`id`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `polls` (`id` int AUTO_INCREMENT, `channel` varchar(128), `question` text, is_closed int, PRIMARY KEY(`id`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `poll_options` (`id` int AUTO_INCREMENT, `poll_id` int, `option` varchar(128), PRIMARY KEY(`id`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `poll_votes` (`id` int AUTO_INCREMENT, `poll_id` int, `user_id` varchar(128), `option_id` int, PRIMARY KEY(`id`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `sensors`(`id` int AUTO_INCREMENT, `key` varchar(128), `description` text, `type` varchar(32), `unit` varchar(16), `min` float, `max` float, `value` float, `last_updated` datetime, PRIMARY KEY(`id`));");
			st.executeBatch();

			st.close();
			initialized = true;
			logger.info("Database wrapper class initialized successfully.");
		}
		catch (Exception e){
			logger.error("An error occured while trying to initialize the database wrapper class.");
			throw new RuntimeException(e);
		}
	}

	public static synchronized boolean close() {
		if (!initialized) return false;

		try {
			logger.info("Closing database connection...");
			conn.close();
			return true;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean schedule(String commandName, String description, String channelId, String guildId, LocalDate execDate, LocalTime execTime, String days) {
		if (!initialized) return false;

		try {
			String sql = "INSERT INTO `schedule`(`command`, `description`, `channel`, `guild`, `exec_date`, `exec_time`, `days`) VALUES (";
			sql += "'" + commandName + "'," +
					"'" + description + "'," +
					"'" + channelId +"'," +
					"'" + guildId + "'," +
					"'" + execDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "'," +
					"'" + execTime.format(DateTimeFormatter.ISO_LOCAL_TIME) + "'," +
					"'" + days + "'";
			sql += ");";
			Statement st = conn.createStatement();
			st.execute(sql);
			st.close();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized List<Task> getScheduleFor(LocalDate date) {
		List<Task> tasks = new ArrayList<>();
		if(!initialized) return tasks;
		try (Statement st = conn.createStatement();
			ResultSet result = st.executeQuery("SELECT * FROM `schedule` WHERE `exec_date`='" + date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "';")
		) {
			if (result.isBeforeFirst())
				while (result.next())
					tasks.add(new Task(
							result.getInt("id"),
							result.getString("command"),
							result.getString("channel"),
							result.getString("guild"),
							result.getString("exec_date"),
							result.getString("exec_time"),
							result.getString("days")
					));

		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return tasks;
	}

	@Deprecated
	public static boolean addAPIUser(String id, String hash) {
		return addUser(id, hash, PERMISSION_API);
	}

	public static synchronized boolean addUser(String id, String hash, int permissions) {
		if (!initialized) return false;

		try {
			String sql = FormattedString.create()
					.setFormat("INSERT INTO `auth`(`id`, `auth_key`, `permissions`, `credits`, `language`, `tokens_used`) VALUES ('${id}', '${key}', '${perms}', '${creds}', '${lang}', '${tokens_used}');")
					.addParam("id", id)
					.addParam("key", hash)
					.addParam("perms", permissions)
					.addParam("creds", 3000)
					.addParam("lang", "legacy")
					.addParam("tokens_used", 0)
					.build();

			Statement st = conn.createStatement();
			st.execute(sql);
			st.close();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized MessageManager getUserLanguage(Snowflake id) {
		MessageManager defaultLanguage = null;
		try {
			defaultLanguage = MessageManager.getDefaultLanguage();
			if (!initialized) return defaultLanguage;

			if (languageCache.containsKey(id))
				return languageCache.get(id);

			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT `language` FROM `auth` WHERE `id`='" + id.asString() + "';");
			if (set.next()) {
				String lang = set.getString("language");
				set.close();
				st.close();
				MessageManager language = MessageManager.getMessages(lang);
				languageCache.put(id, language);
				return language;
			}
			else {
				set.close();
				st.close();
				return defaultLanguage;
			}
		}
		catch (SQLException | IOException e) {
			e.printStackTrace();
			return defaultLanguage;
		}

	}

	public static synchronized boolean setUserLanguage(Snowflake id, String lang) {
		if (!initialized) return false;

		try {
			Statement st = conn.createStatement();
			String sql = "UPDATE `auth` SET `language`='" + lang + "' WHERE `id`='" + id.asString() + "';";
			st.execute(sql);
			st.close();
			languageCache.put(id, MessageManager.getMessages(lang));
			return true;
		}
		catch (SQLException | IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean setUserCredits(Snowflake id, long credits) {
		if (!initialized) return false;

		try {
			Statement st = conn.createStatement();  // update permissions
			String sql = "UPDATE `auth` SET `credits`='" + credits + "' WHERE `id`='" + id + "';";
			st.execute(sql);
			st.close();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean addUserCredits(Snowflake id, long credits) {
		if (!initialized) return false;

		try {
			Statement st = conn.createStatement();

			// update permissions
			String sql = "UPDATE `auth` SET `credits`=`credits`+'" + credits + "' WHERE `id`='" + id.asString() + "';";
			st.execute(sql);
			st.close();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean subtractUserCredits(Snowflake id, long amount) {
		if (!initialized) return false;

		try {
			Statement st = conn.createStatement();

			String query = FormattedString
					.create("UPDATE `auth` SET `credits`=`credits`-${amount}, `tokens_used`=`tokens_used`+${amount} WHERE `id`='${id}';")
					.addParam("amount", amount)
					.addParam("id", id.asString())
					.build();
			st.execute(query);
			st.close();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized long getUserCredits(Snowflake id) {
		if (!initialized) return -1;

		try {
			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT `credits` FROM `auth` WHERE `id`='" + id.asString() + "';");
			if (set.next()) {
				long credits = set.getLong("credits");
				set.close();
				st.close();
				return credits;
			}
			else {
				set.close();
				st.close();
				return -1;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static synchronized UserInfo getUserInfo(Snowflake id) {
		if (!initialized) return null;

		try {
			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT `auth_key`, `permissions`, `credits`, `language` FROM `auth` WHERE `id`='" + id.asString() + "';");
			if (set.next()) {
				UserInfo info = new UserInfo(
						id.asString(),
						set.getString("auth_key"),
						set.getInt("permissions"),
						set.getLong("credits"),
						set.getString("language")
				);
				set.close();
				st.close();
				return info;
			}
			else {
				set.close();
				st.close();
				return null;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static synchronized boolean createPoll(Snowflake channelId, String question, List<String> options) {
		if (!initialized) return false;

		try {
			Statement st = conn.createStatement();
			String sql = "INSERT INTO `polls`(`channel`, `question`, `is_closed`) VALUES ('" + channelId.asString() + "', '" + question + "', 0);";
			st.execute(sql);
			ResultSet set = st.executeQuery("SELECT `id` FROM `polls` WHERE `channel`='" + channelId.asString() + "' AND `question`='" + question + "' AND is_closed=0;");
			if (set.next()) {
				int pollId = set.getInt("id");
				set.close();
				for (String option : options) {
					sql = "INSERT INTO `poll_options`(`poll_id`, `option`) VALUES ('" + pollId + "', '" + option + "');";
					st.execute(sql);
				}
				st.close();
				return true;
			}
			else {
				set.close();
				st.close();
				return false;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean endPoll(Snowflake channelId) {
		if (!initialized) return false;

		try {
			Statement st = conn.createStatement();
			String sql = "UPDATE `polls` SET `is_closed`=1 WHERE `channel`='" + channelId.asString() + "';";
			st.execute(sql);
			st.close();
			return true;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean votePoll(Snowflake channelId, String option) {
		if (!initialized) return false;

		try {
			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT `id` FROM `polls` WHERE `channel`='" + channelId.asString() + "' AND is_closed=0;");
			if (set.next()) {
				int pollId = set.getInt("id");
				set.close();
				set = st.executeQuery("SELECT `id` FROM `poll_options` WHERE `poll_id`='" + pollId + "' AND `option`='" + option + "';");
				if (set.next()) {
					int optionId = set.getInt("id");
					set.close();
					String sql = "INSERT INTO `poll_votes`(`poll_id`, `user_id`, `option_id`) VALUES ('" + pollId + "', '" + channelId.asString() + "', '" + optionId + "');";
					st.execute(sql);
					st.close();
					return true;
				}
				else {
					set.close();
					st.close();
					return false;
				}
			}
			else {
				set.close();
				st.close();
				return false;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean addPollOption(Snowflake channelId, String option) {
		if (!initialized) return false;

		try {
			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT `id` FROM `polls` WHERE `channel`='" + channelId.asString() + "' AND is_closed=0;");
			if (set.next()) {
				int pollId = set.getInt("id");
				set.close();
				String sql = "INSERT INTO `poll_options`(`poll_id`, `option`) VALUES ('" + pollId + "', '" + option + "');";
				st.execute(sql);
				st.close();
				return true;
			}
			else {
				set.close();
				st.close();
				return false;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized Map<String, Integer> getLastPollResults(Snowflake channelId) {
		if (!initialized) return new HashMap<>();

		Map<String, Integer> results = new HashMap<>();
		try {
			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT `id` FROM `polls` WHERE `channel`='" + channelId.asString() + "' AND is_closed=0;");
			if (set.next()) {
				int pollId = set.getInt("id");
				set.close();
				set = st.executeQuery("SELECT `option`, COUNT(`option_id`) AS `votes` FROM `poll_options` LEFT JOIN `poll_votes` ON `poll_options`.`id`=`poll_votes`.`option_id` WHERE `poll_id`='" + pollId + "' GROUP BY `option_id`;");
				while (set.next()) {
					results.put(set.getString("option"), set.getInt("votes"));
				}
				set.close();
				st.close();
				return results;
			}
			else {
				set.close();
				st.close();
				return results;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return results;
		}
	}

	public static synchronized boolean checkPermissions(Snowflake id, byte permissions) {
		return checkPermissions(id.asString(), permissions);
	}

	public static synchronized boolean checkPermissions(String id, byte permissions) {
		if (!initialized) return false;

		try {
			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT `permissions` FROM `auth` WHERE `id`='" + id + "';");
			if (set.next()) {
				byte userPermissions = set.getByte("permissions");
				logger.info("checkin permissions for " + id);
				set.close();
				st.close();
				return (userPermissions & permissions) == permissions
						|| (userPermissions & PERMISSION_ADMIN) == PERMISSION_ADMIN;
			}
			else {
				set.close();
				st.close();
				logger.warn("User " + id + " not found in database.");
				return false;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean checkPermissionsByKey(String key, byte permissions) {
		if (!initialized) return false;

		try {
			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT `permissions` FROM `auth` WHERE `auth_key`='" + key + "';");
			if (set.next()) {
				byte userPermissions = set.getByte("permissions");
				set.close();
				st.close();
				return (userPermissions & permissions) == permissions
						|| (userPermissions & PERMISSION_ADMIN) == PERMISSION_ADMIN;
			}
			else {
				set.close();
				st.close();
				logger.warn("User not found in database.");
				return false;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean updateUserPermissions(String id, int permissions) {
		if (!initialized) return false;

		try {
			Statement st = conn.createStatement();

			// check if user exists
			ResultSet set = st.executeQuery("SELECT `permissions` FROM `auth` WHERE `id`='" + id + "';");
			if (!set.next()) {
				set.close();
				st.close();
				return false;
			}

			// update permissions
			String sql = "UPDATE `auth` SET `permissions`='" + permissions + "' WHERE `id`='" + id + "';";
			st.execute(sql);

			set.close();
			st.close();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean setAnnouncementChannel(Snowflake guild, Snowflake channel) {
		if (!initialized) return false;

		try {
			Statement st = conn.createStatement();

			// check if guild exists
			ResultSet set = st.executeQuery("SELECT `announcement_channel` FROM `guilds` WHERE `guild_id`='" + guild.asString() + "';");
			if (!set.next()) {
				st.execute("INSERT INTO `guilds`(`guild_id`, `announcement_channel`) VALUES ('" + guild.asString() + "','" + channel.asString() + "');");
			}
			else {
				st.execute("UPDATE `guilds` SET `announcement_channel`='" + channel.asString() + "' WHERE `guild_id`='" + guild.asString() + "';");
			}
			set.close();
			st.close();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized Snowflake getAnnouncementChannel(Snowflake guild) {
		if (!initialized) return null;

		try {
			Statement st = conn.createStatement();

			// check if guild exists
			ResultSet set = st.executeQuery("SELECT `announcement_channel` FROM `guilds` WHERE `guild_id`='" + guild.asString() + "';");
			if (!set.next()) {
				set.close();
				st.close();
				return null;
			}

			// update permissions
			String channel = set.getString("announcement_channel");

			set.close();
			st.close();
			return Snowflake.of(channel);

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static synchronized boolean registerSensor(String key, String description, String type, String unit, float min, float max) {
		if (!initialized) return false;

		try {
			String sql = "INSERT INTO `sensors`(`key`, `description`, `type`, `unit`, `min`, `max`, `value`, `last_updated`) VALUES (";
			sql += "'" + key + "'," +
					"'" + description + "'," +
					"'" + type + "'," +
					"'" + unit + "'," +
					"'" + min + "'," +
					"'" + max + "'," +
					"'0'," +
					"'" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + " " + LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME) + "'";
			sql += ");";

			Statement st = conn.createStatement();
			st.execute(sql);
			st.close();

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean addEvent(String name, String description, String time, Snowflake guild) {
		if (!initialized) return false;

		try {
			String sql = "INSERT INTO `events`(`name`, `description`, `time`, `guild`) VALUES (";
			sql += "'" + name + "'," +
					"'" + description + "'," +
					"'" + time + "'," +
					"'" + guild.asString() + "'";
			sql += ");";

			Statement st = conn.createStatement();
			st.execute(sql);
			st.close();

			return true;
		}
		catch (Exception e) {
			e.printStackTrace(System.out);
			return false;
		}
	}

	public static synchronized boolean updateSensorData(SensorData data) {
		return false;
	}

	public static synchronized int getAdminCount() {
		if (!initialized) return -1;

		try {
			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT COUNT(*) FROM `auth` WHERE `permissions` & " + PERMISSION_ADMIN + " = " + PERMISSION_ADMIN + ";");
			if (set.next()) {
				int count = set.getInt(1);
				set.close();
				st.close();
				return count;
			}
			else {
				set.close();
				st.close();
				return -1;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static synchronized List<Event> getEvents() {
		if (!initialized) return null;

		List<Event> events = new ArrayList<>();
		try (Statement st = conn.createStatement();
			ResultSet result = st.executeQuery("SELECT * FROM `events`;")
		) {
			if (result.isBeforeFirst())
				while (result.next())
					events.add(new Event(
							result.getInt("id"),
							result.getString("name"),
							result.getString("description"),
							result.getString("time"),
							result.getString("guild")
					));
			return events;
		}
		catch (SQLException e) {
			e.printStackTrace(System.out);
			return null;
		}
	}

	public static synchronized boolean userExists(String userId) {
		if (!initialized) return false;

		try {
			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT COUNT(*) FROM `auth` WHERE `id`='" + userId + "';");
			if (set.next()) {
				int count = set.getInt(1);
				set.close();
				st.close();
				return count > 0;
			}
			else {
				set.close();
				st.close();
				return false;
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean addTokenUsage(Snowflake guildId, long tokenAmt) {
		if (!initialized) return false;

		try {

			String select = FormattedString
					.create("SELECT `gpt_tokens_used` FROM `guilds` WHERE `guild_id`='${id}';")
					.addParam("id", guildId.asString())
					.build();
			String update = FormattedString
					.create("UPDATE `guilds` SET `gpt_tokens_used` = `gpt_tokens_used` + ${amount} WHERE `guild_id`='${id}';")
					.addParam("amount", tokenAmt)
					.addParam("id", guildId.asString())
					.build();
			String insert = FormattedString
					.create("INSERT INTO `guilds`(`guild_id`, `gpt_tokens_used`) VALUES ('${id}', '${amount}');")
					.addParam("amount", tokenAmt)
					.addParam("id", guildId.asString())
					.build();

			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery(select);
			if (set.next()) {
				st.execute(update);
			} else {
				st.execute(insert);
			}

			set.close();
			st.close();
			return true;

		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public record UserInfo(String id, String authKey, int permissions, long credits, String language) {}

	public static class SensorData {

	}
}
