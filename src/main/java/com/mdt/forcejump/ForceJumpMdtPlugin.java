package com.mdt.forcejump;

import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import java.io.File;
import java.lang.reflect.Field;
import mindustry.Vars;
import mindustry.game.EventType.PlayerJoin;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.mod.Plugin;

public final class ForceJumpMdtPlugin extends Plugin {
    private static final String CONFIG_DIR_NAME = "mdt-force-jump-mdt";

    private File dataRoot;
    private ForceJumpConfig config;

    @Override
    public void init() {
        try {
            dataRoot = resolveDataRoot();
            config = ForceJumpConfig.load(dataRoot);
            Events.on(PlayerJoin.class, event -> handleJoin(event.player));
            Log.info("MDT Force Jump loaded. target=@:@", config.host(), config.port());
        } catch (Exception exception) {
            throw new RuntimeException("MDT Force Jump init failed.", exception);
        }
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("force-jump-reload", "Reload force jump config.", args -> {
            try {
                config = ForceJumpConfig.load(dataRoot);
                Log.info("MDT Force Jump reloaded. target=@:@ enabled=@", config.host(), config.port(), config.enabled());
            } catch (Exception exception) {
                Log.err("MDT Force Jump reload failed: @", exception.getMessage());
            }
        });

        handler.register("force-jump-status", "Show current redirect target.", args -> {
            Log.info(
                "enabled=@ target=@:@ name=@ chat=@ info=@ kick=@",
                config.enabled(),
                config.host(),
                config.port(),
                config.targetName(),
                config.sendChat(),
                config.sendInfo(),
                config.kick()
            );
        });

        handler.register("force-jump-send", "<player>", "Trigger redirect payload for an online player.", args -> {
            Player player = Groups.player.find(p -> p != null && p.name != null && p.name.equalsIgnoreCase(args[0]));
            if (player == null) {
                Log.info("Player not found: @", args[0]);
                return;
            }
            dispatch(player);
        });
    }

    private void handleJoin(Player player) {
        if (!config.enabled()) {
            return;
        }
        dispatch(player);
    }

    private void dispatch(Player player) {
        if (player == null || player.con == null) {
            return;
        }
        if (isSelfTarget()) {
            if (config.debug()) {
                Log.info("Skip redirect for @ because target points to current server: @:@", player.name, config.host(), config.port());
            }
            return;
        }

        String uri = config.buildRedirectUri();
        if (config.sendChat()) {
            player.sendMessage(config.buildChatMessage(uri));
        }
        if (config.sendInfo()) {
            Call.infoMessage(player.con, config.buildInfoMessage(uri));
        }
        if (config.debug()) {
            Log.info("Dispatch redirect payload to @ -> @", player.name, uri);
        }
        if (config.kick()) {
            player.con.kick(config.buildKickMessage(uri), config.kickDuration());
        }
    }

    private boolean isSelfTarget() {
        if (!isLocalTargetHost(config.host())) {
            return false;
        }
        int currentPort = resolveCurrentServerPort();
        return currentPort > 0 && currentPort == config.port();
    }

    private static boolean isLocalTargetHost(String host) {
        if (host == null) {
            return false;
        }
        String normalized = host.trim().toLowerCase();
        return normalized.equals("127.0.0.1")
            || normalized.equals("localhost")
            || normalized.equals("0.0.0.0")
            || normalized.equals("::1");
    }

    private static int resolveCurrentServerPort() {
        try {
            Field field = Vars.class.getDeclaredField("port");
            field.setAccessible(true);
            return field.getInt(null);
        } catch (Exception ignored) {
            return -1;
        }
    }

    private File resolveDataRoot() {
        File modsRoot = new File(Vars.dataDirectory.absolutePath(), "mods");
        return new File(new File(modsRoot, "config"), CONFIG_DIR_NAME);
    }
}
