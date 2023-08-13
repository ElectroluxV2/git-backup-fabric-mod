package com.github.electroluxv2.utils;

import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public record BackupScriptParameters(
        List<String> levelNames,
        int playerCount,
        float tps
) {
    public static BackupScriptParameters fromServer(final MinecraftServer server) {
        var playerCount = server.getCurrentPlayerCount();
        var tps = server.getTickTime();

        var levelNames = StreamSupport
                .stream(server.getWorlds().spliterator(), false)
                .map(world -> world.toServerWorld().backup_scripts_1_20_1$getWorldProperties().orElseThrow().getLevelName())
                .distinct()
                .toList();

        return new BackupScriptParameters(levelNames, playerCount, tps);
    }

    public List<String> toArguments() {
        final var arguments = new ArrayList<String>();

        arguments.add(String.valueOf(playerCount));

        arguments.add(String.valueOf(tps));

        arguments.add(String.valueOf(levelNames.size()));
        arguments.addAll(levelNames);

        return arguments;
    }
}
