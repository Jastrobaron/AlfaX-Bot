package xyz.rtsvk.alfax.util.storage.repos;

import discord4j.common.util.Snowflake;
import xyz.rtsvk.alfax.services.scheduler.Task;
import xyz.rtsvk.alfax.tasks.Event;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.util.storage.Database;
import xyz.rtsvk.alfax.util.storage.IRepository;
import xyz.rtsvk.alfax.util.storage.UserInfo;
import xyz.rtsvk.alfax.util.text.FormattedString;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SqlRepository implements IRepository {

	private static final String SQL_QUOTE = "'";

	private final Logger logger;
	private Connection conn;

	public SqlRepository() {
		this.logger = new Logger(SqlRepository.class);
	}

	private String quoted(String s) {
		if (!s.startsWith(SQL_QUOTE)) s = SQL_QUOTE + s;
		if (!s.endsWith(SQL_QUOTE)) s = s + SQL_QUOTE;
		return s;
	}

	private synchronized boolean insert(String tableName, String[] columns, String[] values) {
		try {
			String _columns = String.join(", ", columns);
			String _values = String.join(", ", Arrays.stream(values).map(this::quoted).toList());
			String sql = String.format("INSERT INTO `%s`(%s) VALUES (%s);", tableName, _columns, _values);
			Statement st = this.conn.createStatement();
			st.execute(sql);
			st.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void init(Config config) {
		try {
			String url = config.getString("db-url");
			this.logger.info(String.format("Connecting to database at %s", url));
			this.conn = DriverManager.getConnection(url);
			Statement st = this.conn.createStatement();

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
			logger.info("SQL repository initialized successfully.");
		}
		catch (Exception e){
			logger.error("An error occured while trying to initialize the database wrapper class.");
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
        try {
            this.conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public synchronized boolean schedule(String commandName, String description, String channelId, String guildId, LocalDate execDate, LocalTime execTime, String days) {
		return insert("schedule",
				new String[]{"command", "description", "channel", "guild", "exec_date", "exec_time", "days"},
				new String[]{commandName, description, channelId, guildId, execDate.format(DateTimeFormatter.ISO_LOCAL_DATE), execTime.format(DateTimeFormatter.ISO_LOCAL_TIME), days});
	}

	@Override
	public synchronized List<Task> getScheduleFor(LocalDate date) {
		List<Task> tasks = new ArrayList<>();
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

	@Override
	public synchronized boolean addUser(String id, String hash, int permissions) {
		return insert("auth",
				new String[]{"id", "auth_key", "permissions", "credits", "language", "tokens_used"},
				new String[]{id, hash, String.valueOf(permissions), "3000", "legacy", "0"});
	}

	@Override
	public synchronized boolean userExists(String userId) {
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

	@Override
	public synchronized MessageManager getUserLanguage(Snowflake id) {
		MessageManager defaultLanguage = null;
		try {
			defaultLanguage = MessageManager.getDefaultLanguage();

			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT `language` FROM `auth` WHERE `id`='" + id.asString() + "';");
			if (set.next()) {
				String lang = set.getString("language");
				set.close();
				st.close();
				return MessageManager.getMessages(lang);
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

	@Override
	public synchronized boolean setUserLanguage(Snowflake id, String lang) {
		try {
			Statement st = conn.createStatement();
			String sql = "UPDATE `auth` SET `language`='" + lang + "' WHERE `id`='" + id.asString() + "';";
			st.execute(sql);
			st.close();
			return true;
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public synchronized boolean setUserCredits(Snowflake id, long credits) {
		try {
			Statement st = conn.createStatement();
			String sql = "UPDATE `auth` SET `credits`='" + credits + "' WHERE `id`='" + id + "';";
			st.execute(sql);
			st.close();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public synchronized boolean addUserCredits(Snowflake id, long credits) {
		try {
			Statement st = conn.createStatement();
			String sql = "UPDATE `auth` SET `credits`=`credits`+'" + credits + "' WHERE `id`='" + id.asString() + "';";
			st.execute(sql);
			st.close();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public synchronized boolean subtractUserCredits(Snowflake id, long amount) {
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

	@Override
	public synchronized long getUserCredits(Snowflake id) {
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

	@Override
	public synchronized UserInfo getUserInfo(Snowflake id) {
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

	@Override
	public synchronized boolean createPoll(Snowflake channelId, String question, List<String> options) {
		try {
			if (!insert("polls", new String[]{"channel", "question", "is_closed"}, new String[]{channelId.asString(), question, "0"})) {
				return false;
			}

			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT `id` FROM `polls` WHERE `channel`='" + channelId.asString() + "' AND `question`='" + question + "' AND is_closed=0;");
			if (set.next()) {
				int pollId = set.getInt("id");
				set.close();
				for (String option : options) {
					insert("poll_options", new String[]{"poll_id", "option"}, new String[]{String.valueOf(pollId), option});
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

	@Override
	public synchronized boolean endPoll(Snowflake channelId) {
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

	@Override
	public synchronized boolean votePoll(Snowflake channelId, String option) {
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
					st.close();
					return insert("poll_votes", new String[]{"poll_id", "user_id", "option_id"}, new String[]{String.valueOf(pollId), channelId.asString(), String.valueOf(optionId)});
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

	@Override
	public synchronized boolean addPollOption(Snowflake channelId, String option) {
		try {
			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT `id` FROM `polls` WHERE `channel`='" + channelId.asString() + "' AND is_closed=0;");
			if (set.next()) {
				int pollId = set.getInt("id");
				set.close();
				st.close();
				return insert("poll_options", new String[]{"poll_id", "option"}, new String[]{String.valueOf(pollId), option});
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

	@Override
	public synchronized Map<String, Integer> getLastPollResults(Snowflake channelId) {
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

	@Override
	public synchronized boolean checkPermissions(String id, byte permissions) {
		try {
			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT `permissions` FROM `auth` WHERE `id`='" + id + "';");
			if (set.next()) {
				byte userPermissions = set.getByte("permissions");
				logger.info("checkin permissions for " + id);
				set.close();
				st.close();
				return (userPermissions & permissions) == permissions
						|| (userPermissions & Database.PERMISSION_ADMIN) == Database.PERMISSION_ADMIN;
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

	@Override
	public synchronized boolean checkPermissionsByKey(String key, byte permissions) {
		try {
			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT `permissions` FROM `auth` WHERE `auth_key`='" + key + "';");
			if (set.next()) {
				byte userPermissions = set.getByte("permissions");
				set.close();
				st.close();
				return (userPermissions & permissions) == permissions
						|| (userPermissions & Database.PERMISSION_ADMIN) == Database.PERMISSION_ADMIN;
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

	@Override
	public synchronized boolean updateUserPermissions(String id, int permissions) {
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

	@Override
	public synchronized int getAdminCount() {
		try {
			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery("SELECT COUNT(*) FROM `auth` WHERE `permissions` & " + Database.PERMISSION_ADMIN + " = " + Database.PERMISSION_ADMIN + ";");
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

	@Override
	public synchronized boolean setAnnouncementChannel(Snowflake guild, Snowflake channel) {
		try {
			Statement st = conn.createStatement();

			// check if guild exists
			ResultSet set = st.executeQuery("SELECT `announcement_channel` FROM `guilds` WHERE `guild_id`='" + guild.asString() + "';");
			if (!set.next()) {
				set.close();
				st.close();
				return insert("guilds", new String[]{"guild_id", "announcement_channel"}, new String[]{guild.asString(), channel.asString()});
			}
			else {
				st.execute("UPDATE `guilds` SET `announcement_channel`='" + channel.asString() + "' WHERE `guild_id`='" + guild.asString() + "';");
				set.close();
				st.close();
				return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public synchronized Snowflake getAnnouncementChannel(Snowflake guild) {
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

	@Override
	public synchronized boolean addTokenUsage(Snowflake guildId, long tokenAmt) {
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

			Statement st = conn.createStatement();
			ResultSet set = st.executeQuery(select);
			if (set.next()) {
				st.execute(update);
				set.close();
				st.close();
				return true;
			} else {
				set.close();
				st.close();
				return insert("guilds", new String[]{"guild_id", "gpt_tokens_used"}, new String[]{guildId.asString(), String.valueOf(tokenAmt)});
			}

		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public synchronized boolean registerSensor(String key, String description, String type, String unit, float min, float max) {
		String lastUpdated = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + " " + LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
		return insert("sensors",
				new String[]{"key", "description", "type", "unit", "min", "max", "value", "last_updated"},
				new String[]{key, description, type, unit, String.valueOf(min), String.valueOf(max), "0", lastUpdated});
	}

	@Override
	public synchronized boolean addEvent(String name, String description, String time, Snowflake guild) {
		return insert("events",
				new String[]{"name", "description", "time", "guild"},
				new String[]{name, description, time, guild.asString()});
	}

	@Override
	public synchronized List<Event> getEvents() {
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
}
