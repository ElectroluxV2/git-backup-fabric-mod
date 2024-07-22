package com.github.electroluxv2.utils;

import com.github.electroluxv2.mixin.ServerLevelAccessor;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public record BackupScriptParameters(
        List<String> levelNames,
        int playerCount,
        float avgTickTime,
        boolean running
) {
    public static BackupScriptParameters fromServer(final MinecraftServer server, boolean running) {
        var playerCount = server.getPlayerCount();
        var avgTickTime = server.getAverageTickTimeNanos();

        var levelNames = StreamSupport
                .stream(server.getAllLevels().spliterator(), false)
                .map(level -> (ServerLevelAccessor) level)
                .map(level -> level.getServerLevelData().getLevelName())
                .distinct()
                .toList();

        return new BackupScriptParameters(levelNames, playerCount, avgTickTime, running);
    }

    public List<String> toArguments() {
        final var arguments = new ArrayList<String>();

        arguments.add(String.valueOf(playerCount));

        arguments.add(String.valueOf(avgTickTime));

        arguments.add(String.valueOf(running));

        arguments.add(String.valueOf(levelNames.size()));
        arguments.addAll(levelNames);

        return arguments;
    }
}
