package com.github.electroluxv2.utils;

import com.github.electroluxv2.BackupScriptsMod;
import net.minecraft.server.MinecraftServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

import static com.github.electroluxv2.BackupScriptsMod.LOGGER;

public class ScriptsManager {
    private static final Path serverRootDirectory = Paths
            .get("")
            .toAbsolutePath();

    private static final Path scriptsTargetDirectory = serverRootDirectory
            .resolve("mods/backup-scripts");

    private static final Path initScriptPath = scriptsTargetDirectory.resolve("init.sh");
    private static final Path initLastRunPath = scriptsTargetDirectory.resolve(".last-init-run");
    private static final Path onSaveScriptPath = scriptsTargetDirectory.resolve("on-save.sh");
    private static String shellPath;

    public static void initialize() throws URISyntaxException, IOException {
        initializeDefaultBackupScripts();
        shellPath = new String(Files.readAllBytes(scriptsTargetDirectory.resolve("shell")));

        if (Files.notExists(Path.of(shellPath))) {
            throw new RuntimeException("Invalid shell provided: '%s'".formatted(shellPath));
        }
    }

    private static void initializeDefaultBackupScripts() throws IOException, URISyntaxException {
        if (Files.exists(scriptsTargetDirectory)) {
            LOGGER.debug("Scripts directory already exists");
            return;
        }

        final var scriptsSourceDirectory = Path.of(Objects.requireNonNull(BackupScriptsMod.class.getResource("/backup-scripts")).toURI());
        FileUtils.copyDirectory(scriptsSourceDirectory, scriptsTargetDirectory);
    }

    private static boolean shouldRunInitScript() throws IOException {
        var lastRun = Instant.MIN;
        if (Files.exists(initLastRunPath)) {
            var contents = new String(Files.readAllBytes(initLastRunPath));
            lastRun = Instant.ofEpochSecond(Long.parseLong(contents));
        }

        Files.write(initLastRunPath, String.valueOf(Instant.now().getEpochSecond()).getBytes());

        var initLastModified = Files.getLastModifiedTime(initScriptPath).toInstant();

        LOGGER.debug("Last init run: %s".formatted(lastRun));
        LOGGER.debug("Last init mod: %s".formatted(initLastModified));

        return lastRun.isBefore(initLastModified);
    }

    public static void runInitScript(BackupScriptParameters parameters) {
        try {
            if (!shouldRunInitScript()) {
                LOGGER.info("Skipping init script execution as it was not modified since last run");
                return;
            }

            runScript(initScriptPath, parameters);
        } catch (Exception e) {
            LOGGER.error("Failed to execute init script", e);
        }
    }

    public static void runOnSaveScript(boolean parallel, MinecraftServer server) {
        final var parameters = BackupScriptParameters.fromServer(server);

        if (!parallel) {
            runScript(onSaveScriptPath, parameters);
            return;
        }

        LOGGER.info("Running scripts off main thread");
        final var thread = new Thread(() -> runScript(onSaveScriptPath, parameters));
        thread.start();

        server.submit(() -> {
            try {
                thread.join();
                LOGGER.info("Shutdown of off main thread");
            } catch (InterruptedException e) {
                LOGGER.error("Failed to wait for script thread", e);
            }
        });
    }

    private static void runScript(final Path scriptPath, BackupScriptParameters parameters) {
        final var cmd = new ArrayList<String>();

        cmd.add(shellPath);
        cmd.add(scriptPath.toString());
        cmd.addAll(parameters.toArguments());

        try {
            var builder = new ProcessBuilder();
            builder.directory(serverRootDirectory.toFile());
            builder.command(cmd);

            var process = builder.start();

            BufferedReader lineReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            lineReader.lines().forEach(line -> LOGGER.info("(%s): %s".formatted(scriptPath.getFileName(), line)));

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            errorReader.lines().forEach(line -> LOGGER.warn("(%s): %s".formatted(scriptPath.getFileName(), line)));

            process.waitFor();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to execute (%s):".formatted(scriptPath), e);
        }
    }
}
