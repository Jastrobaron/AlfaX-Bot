package xyz.rtsvk.alfax.util;

import discord4j.common.util.Snowflake;
import xyz.rtsvk.alfax.scheduler.Task;
import xyz.rtsvk.alfax.tasks.Event;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Database {
	private static boolean initialized;
	private static String url;
	private static Logger logger;
	private static Connection conn;

	public static final byte PERMISSION_NONE = 0x00;
	public static final byte PERMISSION_ADMIN = 0x01;
	public static final byte PERMISSION_API_CHANNEL = 0x02;
	public static final byte PERMISSION_API_DM = 0x04;
	public static final byte PERMISSION_API = PERMISSION_API_CHANNEL | PERMISSION_API_DM;
	public static final byte PERMISSION_MQTT = 0x08;

	public static void init(String host, String user, String password, String db) {
		logger = new Logger(Database.class);
		initialized = false;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			url = "jdbc:mysql://" + user + ":" + password + "@" + host;
			logger.info("Connecting to database '" + db +"' at " + host + " as user '" + user + "'...");

			conn = DriverManager.getConnection(url);
			Statement st = conn.createStatement();

			st.addBatch("CREATE DATABASE IF NOT EXISTS `" + db + "`;");
			st.addBatch("USE `" + db + "`;");
			st.executeBatch();

			st.addBatch("CREATE TABLE IF NOT EXISTS `system_info` (`vkey` varchar(64), `value` varchar(128), PRIMARY KEY(`vkey`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `guilds` (`guild_id` varchar(128), `announcement_channel` varchar(128), PRIMARY KEY(`guild_id`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `schedule` (`id` int AUTO_INCREMENT, `command` varchar(32), `description` text, `channel` varchar(128), `guild` varchar(128), `exec_date` date, `exec_time` varchar(8), `days` varchar(16), PRIMARY KEY(`id`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `events`(`id` int AUTO_INCREMENT, `name` varchar(128), `description` text, `time` long, `guild` varchar(128), PRIMARY KEY(`id`));");
			st.addBatch("CREATE TABLE IF NOT EXISTS `auth` (`id` varchar(128), `auth_key` varchar(128), `permissions` int, PRIMARY KEY(`id`));");
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

	public static boolean close() {
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

	public static boolean schedule(String commandName, String description, String channelId, String guildId, LocalDate execDate, LocalTime execTime, String days) {
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

	public static List<Task> getScheduleFor(LocalDate date) {
		List<Task> tasks = new ArrayList<>();
		try (Statement st = conn.createStatement();
			ResultSet result = st.executeQuery("SELECT * FROM `schedule` WHERE `exec_date`='" + date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "';")
		) {
			if(!initialized) return tasks;

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

	public static boolean addUser(String id, String hash, int permissions) {
		if (!initialized) return false;

		try {
			String sql = "INSERT INTO `auth`(`id`, `auth_key`, `permissions`) VALUES (";
			sql += "'" + id + "'," +
					"'" + hash + "'," +
					"'" + permissions + "'";
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

	public static boolean checkPermissions(String id, byte permissions) {
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

	public static boolean checkPermissionsByKey(String key, byte permissions) {
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

	public static boolean updateUserPermissions(String id, int permissions) {
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

	public static boolean setAnnouncementChannel(Snowflake guild, Snowflake channel) {
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

	public static Snowflake getAnnouncementChannel(Snowflake guild) {
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

	public static boolean registerSensor(String key, String description, String type, String unit, float min, float max) {
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

	public static boolean addEvent(String name, String description, String time, Snowflake guild) {
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

	public static boolean updateSensorData(SensorData data) {
		return false;
	}

	public static int getAdminCount() {
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

	public static List<Event> getEvents() {
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

	public static boolean userExists(String userId) {
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


	public static class SensorData {

	}
}
