package xyz.rtsvk.alfax.util.storage;

public record UserInfo(String id, String authKey, int permissions, long credits, String language) {}
