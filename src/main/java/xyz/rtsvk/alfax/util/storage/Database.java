package xyz.rtsvk.alfax.util.storage;

import discord4j.common.util.Snowflake;
import xyz.rtsvk.alfax.services.scheduler.Task;
import xyz.rtsvk.alfax.tasks.Event;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.Logger;
import xyz.rtsvk.alfax.util.storage.repos.DummyRepository;
import xyz.rtsvk.alfax.util.storage.repos.SqlRepository;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
	public static final byte PERMISSION_NONE = 0;
	public static final byte PERMISSION_ADMIN = 1 << 0;
	public static final byte PERMISSION_API_CHANNEL = 1 << 1;
	public static final byte PERMISSION_API_DM = 1 << 2;
	public static final byte PERMISSION_MQTT = 1 << 3;
	public static final byte PERMISSION_RATE_LIMIT_BYPASS = 1 << 4;
	public static final byte PERMISSION_API_GET_FILE = 1 << 5;
	public static final byte PERMISSION_API = PERMISSION_API_CHANNEL | PERMISSION_API_DM | PERMISSION_API_GET_FILE;

	private static final Map<Snowflake, MessageManager> languageCache = new HashMap<>();
	private static final Logger logger = new Logger(Database.class);
	private static IRepository repository;
	private static boolean initialized;

	private static IRepository getRepository(String type) {
		return switch (type) {
			case "sql" -> new SqlRepository();
			case "dummy" -> new DummyRepository();
			default -> throw new IllegalStateException("Invalid repository type: " + type);
		};
	}

	public static void init(Config config) {
		if (initialized) return;

		String repoType = config.getString("db-type");
		repository = getRepository(repoType);
		logger.info(String.format("Using %s repository", repoType));
		repository.init(config);
		initialized = true;
	}

	public static synchronized void close() {
		if (!initialized) return;
		logger.info("Closing database connection...");
		repository.close();
		initialized = false;
	}

	public static synchronized boolean schedule(String commandName, String description, String channelId, String guildId, LocalDate execDate, LocalTime execTime, String days) {
		if (!initialized) return false;
		return repository.schedule(commandName, description, channelId, guildId, execDate, execTime, days);
	}

	public static synchronized List<Task> getScheduleFor(LocalDate date) {
		if(!initialized) return new ArrayList<>();
		return repository.getScheduleFor(date);
	}

	@Deprecated
	public static boolean addAPIUser(String id, String hash) {
		return addUser(id, hash, PERMISSION_API);
	}

	public static synchronized boolean addUser(String id, String hash, int permissions) {
		if (!initialized) return false;
		return repository.addUser(id, hash, permissions);
	}

	public static synchronized MessageManager getUserLanguage(Snowflake id) {
        MessageManager defaultLanguage = MessageManager.getDefaultLanguage();
        if (!initialized) return defaultLanguage;

        if (languageCache.containsKey(id))
            return languageCache.get(id);

        MessageManager language = repository.getUserLanguage(id);
        if (language != null) {
            languageCache.put(id, language);
        }
        return language;
    }

	public static synchronized boolean setUserLanguage(Snowflake id, String lang) {
		if (!initialized) return false;

		try {
			boolean result = repository.setUserLanguage(id, lang);
			if (result) {
				languageCache.put(id, MessageManager.getMessages(lang));
			}
			return result;
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public static synchronized boolean setUserCredits(Snowflake id, long credits) {
		if (!initialized) return false;
		return repository.setUserCredits(id, credits);
	}

	public static synchronized boolean addUserCredits(Snowflake id, long credits) {
		if (!initialized) return false;
		return repository.addUserCredits(id, credits);
	}

	public static synchronized boolean subtractUserCredits(Snowflake id, long amount) {
		if (!initialized) return false;
		return repository.subtractUserCredits(id, amount);
	}

	public static synchronized long getUserCredits(Snowflake id) {
		if (!initialized) return -1;
		return repository.getUserCredits(id);
	}

	public static synchronized UserInfo getUserInfo(Snowflake id) {
		if (!initialized) return null;
		return repository.getUserInfo(id);
	}
	public static synchronized boolean createPoll(Snowflake channelId, String question, List<String> options) {
		if (!initialized) return false;
		return repository.createPoll(channelId, question, options);
	}

	public static synchronized boolean endPoll(Snowflake channelId) {
		if (!initialized) return false;
		return repository.endPoll(channelId);
	}

	public static synchronized boolean votePoll(Snowflake channelId, String option) {
		if (!initialized) return false;
		return repository.votePoll(channelId, option);
	}

	public static synchronized boolean addPollOption(Snowflake channelId, String option) {
		if (!initialized) return false;
		return repository.addPollOption(channelId, option);
	}

	public static synchronized Map<String, Integer> getLastPollResults(Snowflake channelId) {
		if (!initialized) return new HashMap<>();
		return repository.getLastPollResults(channelId);
	}

	public static synchronized boolean checkPermissions(Snowflake id, byte permissions) {
		if (!initialized) return false;
		return repository.checkPermissions(id, permissions);
	}

	public static synchronized boolean checkPermissions(String id, byte permissions) {
		if (!initialized) return false;
		return repository.checkPermissions(id, permissions);
	}

	public static synchronized boolean checkPermissionsByKey(String key, byte permissions) {
		if (!initialized) return false;
		return repository.checkPermissionsByKey(key, permissions);
	}

	public static synchronized boolean updateUserPermissions(String id, int permissions) {
		if (!initialized) return false;
		return repository.updateUserPermissions(id, permissions);
	}

	public static synchronized boolean setAnnouncementChannel(Snowflake guild, Snowflake channel) {
		if (!initialized) return false;
		return repository.setAnnouncementChannel(guild, channel);
	}

	public static synchronized Snowflake getAnnouncementChannel(Snowflake guild) {
		if (!initialized) return null;
		return repository.getAnnouncementChannel(guild);
	}

	public static synchronized boolean registerSensor(String key, String description, String type, String unit, float min, float max) {
		if (!initialized) return false;
		return repository.registerSensor(key, description, type, unit, min, max);
	}

	public static synchronized boolean addEvent(String name, String description, String time, Snowflake guild) {
		if (!initialized) return false;
		return repository.addEvent(name, description, time, guild);
	}

	public static synchronized boolean updateSensorData(SensorData data) {
		if (!initialized) return false;
		return repository.updateSensorData(data);
	}

	public static synchronized int getAdminCount() {
		if (!initialized) return -1;
		return repository.getAdminCount();
	}

	public static synchronized List<Event> getEvents() {
		if (!initialized) return null;
		return repository.getEvents();
	}

	public static synchronized boolean userExists(String userId) {
		if (!initialized) return false;
		return repository.userExists(userId);
	}

	public static boolean addTokenUsage(Snowflake guildId, long tokenAmt) {
		if (!initialized) return false;
		return repository.addTokenUsage(guildId, tokenAmt);
	}

	public static boolean isInitialized() {
		return initialized;
	}

	public static class SensorData {

	}
}
