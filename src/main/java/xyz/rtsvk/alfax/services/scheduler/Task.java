package xyz.rtsvk.alfax.services.scheduler;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.MessageChannel;

import java.time.LocalDate;
import java.time.LocalTime;

public class Task {

	private int id;
	private String command;

	private Snowflake channel;
	private LocalDate execDate;
	private LocalTime execTime;
	private String days;
	private Snowflake guild;

	public Task(int id, String command, String channel, String guild, String execDate, String execTime, String days) {
		this.id = id;
		this.command = command;
		this.days = days;

		this.channel = Snowflake.of(channel);
		this.guild = Snowflake.of(guild);

		this.execDate = LocalDate.parse(execDate);
		this.execTime = LocalTime.parse(execTime);
	}

	public int getId() {
		return id;
	}

	public String getCommand() {
		return command;
	}

	public LocalDate getExecDate() {
		return execDate;
	}

	public LocalTime getExecTime() {
		return execTime;
	}

	public String getDays() {
		return days;
	}

	public Snowflake getChannel() {
		return this.channel;
	}

	public Snowflake getGuild() {
		return this.guild;
	}
}
