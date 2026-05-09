package com.mdt.forcejump;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public final class ForceJumpConfig {
    private static final String RESOURCE_NAME = "force-jump-mdt.properties";

    private final boolean enabled;
    private final boolean debug;
    private final String host;
    private final int port;
    private final String targetName;
    private final boolean sendChat;
    private final boolean sendInfo;
    private final boolean kick;
    private final long kickDuration;
    private final String uriTemplate;
    private final String chatTemplate;
    private final String infoTemplate;
    private final String kickTemplate;

    private ForceJumpConfig(
        boolean enabled,
        boolean debug,
        String host,
        int port,
        String targetName,
        boolean sendChat,
        boolean sendInfo,
        boolean kick,
        long kickDuration,
        String uriTemplate,
        String chatTemplate,
        String infoTemplate,
        String kickTemplate
    ) {
        this.enabled = enabled;
        this.debug = debug;
        this.host = host;
        this.port = port;
        this.targetName = targetName;
        this.sendChat = sendChat;
        this.sendInfo = sendInfo;
        this.kick = kick;
        this.kickDuration = kickDuration;
        this.uriTemplate = uriTemplate;
        this.chatTemplate = chatTemplate;
        this.infoTemplate = infoTemplate;
        this.kickTemplate = kickTemplate;
    }

    public static ForceJumpConfig load(File dataRoot) throws IOException {
        if (!dataRoot.exists() && !dataRoot.mkdirs() && !dataRoot.isDirectory()) {
            throw new IOException("Unable to create config directory: " + dataRoot.getAbsolutePath());
        }

        File configFile = new File(dataRoot, RESOURCE_NAME);
        if (!configFile.exists()) {
            try (InputStream inputStream = ForceJumpConfig.class.getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
                if (inputStream == null) {
                    throw new IOException("Missing default resource: " + RESOURCE_NAME);
                }
                Files.copy(inputStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        Properties properties = new Properties();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            properties.load(reader);
        }

        return new ForceJumpConfig(
            readBoolean(properties, "enabled", true),
            readBoolean(properties, "debug", false),
            readString(properties, "target.host", "127.0.0.1"),
            readInt(properties, "target.port", 6567),
            readString(properties, "target.name", "MDT Target Server"),
            readBoolean(properties, "dispatch.chat", true),
            readBoolean(properties, "dispatch.info", true),
            readBoolean(properties, "dispatch.kick", true),
            readLong(properties, "dispatch.kickDuration", 0L),
            readString(properties, "dispatch.uriTemplate", "mdt://redirect?host=%s&port=%d&name=%s"),
            readString(properties, "dispatch.chatTemplate", "[accent][MDT Redirect][] %s"),
            readString(properties, "dispatch.infoTemplate", "[accent]Server redirect prepared[]\n%s"),
            readString(properties, "dispatch.kickTemplate", "MDT_REDIRECT %s")
        );
    }

    private static String readString(Properties properties, String key, String fallback) {
        String value = properties.getProperty(key);
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static boolean readBoolean(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        return value == null || value.trim().isEmpty() ? fallback : Boolean.parseBoolean(value.trim());
    }

    private static int readInt(Properties properties, String key, int fallback) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static long readLong(Properties properties, String key, long fallback) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) return fallback;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean debug() {
        return debug;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String targetName() {
        return targetName;
    }

    public boolean sendChat() {
        return sendChat;
    }

    public boolean sendInfo() {
        return sendInfo;
    }

    public boolean kick() {
        return kick;
    }

    public long kickDuration() {
        return kickDuration;
    }

    public String buildRedirectUri() {
        return String.format(uriTemplate, host, Integer.valueOf(port), targetName);
    }

    public String buildChatMessage(String uri) {
        return String.format(chatTemplate, uri);
    }

    public String buildInfoMessage(String uri) {
        return String.format(infoTemplate, uri);
    }

    public String buildKickMessage(String uri) {
        return String.format(kickTemplate, uri);
    }
}
