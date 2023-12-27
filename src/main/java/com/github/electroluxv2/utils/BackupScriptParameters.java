package com.github.electroluxv2.utils;

import com.github.electroluxv2.mixin.ServerWorldAccessor;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public record BackupScriptParameters(
        List<String> levelNames,
        int playerCount,
        float avgTickTime
) {
    public static BackupScriptParameters fromServer(final MinecraftServer server) {
        var playerCount = server.getCurrentPlayerCount();
        var avgTickTime = server.getAverageTickTime();

        var levelNames = StreamSupport
                .stream(server.getWorlds().spliterator(), false)
                .map(world -> (ServerWorldAccessor) world.toServerWorld())
                .map(world -> world.getWorldProperties().getLevelName())
                .distinct()
                .toList();

        return new BackupScriptParameters(levelNames, playerCount, avgTickTime);
    }

    public List<String> toArguments() {
        final var arguments = new ArrayList<String>();

        arguments.add(String.valueOf(playerCount));

        arguments.add(String.valueOf(avgTickTime));

        arguments.add(String.valueOf(levelNames.size()));
        arguments.addAll(levelNames);

        return arguments;
    }
}
