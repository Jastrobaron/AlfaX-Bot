package xyz.rtsvk.alfax.util.storage;

import discord4j.common.util.Snowflake;
import xyz.rtsvk.alfax.services.scheduler.Task;
import xyz.rtsvk.alfax.tasks.Event;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface IRepository {

	// init/close operations
	void init(Config config);
	void close();

	// Schedule operations
	boolean schedule(String commandName, String description, String channelId, String guildId, LocalDate execDate, LocalTime execTime, String days);
	List<Task> getScheduleFor(LocalDate date);

	// User management
	boolean addUser(String id, String hash, int permissions);
	boolean userExists(String userId);

	// User language
	MessageManager getUserLanguage(Snowflake id);
	boolean setUserLanguage(Snowflake id, String lang);

	// User credits
	boolean setUserCredits(Snowflake id, long credits);
	boolean addUserCredits(Snowflake id, long credits);
	boolean subtractUserCredits(Snowflake id, long amount);
	long getUserCredits(Snowflake id);

	// User info
	UserInfo getUserInfo(Snowflake id);

	// Poll operations
	boolean createPoll(Snowflake channelId, String question, List<String> options);
	boolean endPoll(Snowflake channelId);
	boolean votePoll(Snowflake channelId, String option);
	boolean addPollOption(Snowflake channelId, String option);
	Map<String, Integer> getLastPollResults(Snowflake channelId);

	// Permission operations
	default boolean checkPermissions(Snowflake id, byte permissions) {
		return checkPermissions(id.asString(), permissions);
	}
	boolean checkPermissions(String id, byte permissions);
	boolean checkPermissionsByKey(String key, byte permissions);
	boolean updateUserPermissions(String id, int permissions);
	int getAdminCount();

	// Guild operations
	boolean setAnnouncementChannel(Snowflake guild, Snowflake channel);
	Snowflake getAnnouncementChannel(Snowflake guild);
	boolean addTokenUsage(Snowflake guildId, long tokenAmt);

	// Sensor operations
	boolean registerSensor(String key, String description, String type, String unit, float min, float max);
	default boolean updateSensorData(Database.SensorData data) {
		return false;
	}

	// Event operations
	boolean addEvent(String name, String description, String time, Snowflake guild);
	List<Event> getEvents();
}
