package xyz.rtsvk.alfax.util.storage.repos;

import discord4j.common.util.Snowflake;
import xyz.rtsvk.alfax.services.scheduler.Task;
import xyz.rtsvk.alfax.tasks.Event;
import xyz.rtsvk.alfax.util.Config;
import xyz.rtsvk.alfax.util.storage.IRepository;
import xyz.rtsvk.alfax.util.storage.UserInfo;
import xyz.rtsvk.alfax.util.text.MessageManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class DummyRepository implements IRepository {
    @Override
    public void init(Config config) {
        // do nothing
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public boolean schedule(String commandName, String description, String channelId, String guildId, LocalDate execDate, LocalTime execTime, String days) {
        return false;
    }

    @Override
    public List<Task> getScheduleFor(LocalDate date) {
        return List.of();
    }

    @Override
    public boolean addUser(String id, String hash, int permissions) {
        return false;
    }

    @Override
    public boolean userExists(String userId) {
        return false;
    }

    @Override
    public MessageManager getUserLanguage(Snowflake id) {
        return null;
    }

    @Override
    public boolean setUserLanguage(Snowflake id, String lang) {
        return false;
    }

    @Override
    public boolean setUserCredits(Snowflake id, long credits) {
        return false;
    }

    @Override
    public boolean addUserCredits(Snowflake id, long credits) {
        return false;
    }

    @Override
    public boolean subtractUserCredits(Snowflake id, long amount) {
        return false;
    }

    @Override
    public long getUserCredits(Snowflake id) {
        return 0;
    }

    @Override
    public UserInfo getUserInfo(Snowflake id) {
        return null;
    }

    @Override
    public boolean createPoll(Snowflake channelId, String question, List<String> options) {
        return false;
    }

    @Override
    public boolean endPoll(Snowflake channelId) {
        return false;
    }

    @Override
    public boolean votePoll(Snowflake channelId, String option) {
        return false;
    }

    @Override
    public boolean addPollOption(Snowflake channelId, String option) {
        return false;
    }

    @Override
    public Map<String, Integer> getLastPollResults(Snowflake channelId) {
        return Map.of();
    }

    @Override
    public boolean checkPermissions(String id, byte permissions) {
        return true;
    }

    @Override
    public boolean checkPermissionsByKey(String key, byte permissions) {
        return true;
    }

    @Override
    public boolean updateUserPermissions(String id, int permissions) {
        return false;
    }

    @Override
    public int getAdminCount() {
        return 0;
    }

    @Override
    public boolean setAnnouncementChannel(Snowflake guild, Snowflake channel) {
        return false;
    }

    @Override
    public Snowflake getAnnouncementChannel(Snowflake guild) {
        return null;
    }

    @Override
    public boolean addTokenUsage(Snowflake guildId, long tokenAmt) {
        return false;
    }

    @Override
    public boolean registerSensor(String key, String description, String type, String unit, float min, float max) {
        return false;
    }

    @Override
    public boolean addEvent(String name, String description, String time, Snowflake guild) {
        return false;
    }

    @Override
    public List<Event> getEvents() {
        return List.of();
    }
}
