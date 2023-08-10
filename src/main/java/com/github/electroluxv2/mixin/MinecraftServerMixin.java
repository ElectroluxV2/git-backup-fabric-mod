package com.github.electroluxv2.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

import static com.github.electroluxv2.ExampleMod.LOGGER;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantThreadExecutor<ServerTask> {
    public MinecraftServerMixin() {
        super("Server");
    }

    @Shadow public abstract Iterable<ServerWorld> getWorlds();

    @Shadow public abstract boolean runTask();

    @Shadow protected abstract ServerTask createTask(Runnable runnable);

    @Shadow private volatile boolean running;

    @Inject(at = @At(value = "TAIL"), method = "save")
    public void save(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        final var runtime = Runtime.getRuntime();

        final var done = new HashSet<String>();
        for (ServerWorld world : this.getWorlds()) {
            final var levelName = world.git_backup_template_1_20_1$getWorldProperties().orElseThrow().getLevelName();
            if (done.contains(levelName)) continue; // One backup per world, multiple worlds per dimension
            done.add(levelName);

            try {
                LOGGER.info("Executing git add \"%s\"".formatted(levelName));
                var process = runtime.exec(new String[]{"git", "add", levelName});
                var returnValue = process.waitFor();
                if (returnValue != 0) {
                    final var stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    throw new RuntimeException("Git command returned %d code: %s".formatted(returnValue, stdError.lines().toList()));
                }
            } catch (IOException | InterruptedException e) {
                LOGGER.error("Execution of git add \"%s\" failed".formatted(levelName), e);
            }
        }

        if (running) {
            final var task = createTask(this::commitAndPush);
            send(task); // Async execution, maybe execute forcefully on different thread?
            LOGGER.info("Submitted push task");
        } else {
            commitAndPush();
        }
    }

    @Unique
    private void commitAndPush() {
        final var runtime = Runtime.getRuntime();
        final var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
        final var message = "Automatic backup %s".formatted(formatter.format(Instant.now()));
        try {
            LOGGER.info("Committing world change: %s".formatted(message));
            {
                final var commitProcess = runtime.exec(new String[]{"git", "commit", "-m", message});
                final var returnValue = commitProcess.waitFor();
                if (returnValue != 0) {
                    final var stdError = new BufferedReader(new InputStreamReader(commitProcess.getErrorStream()));
                    throw new RuntimeException("Git command returned %d code: %s".formatted(returnValue, stdError.lines().toList()));
                }
            }

            {
                final var pushProcess = runtime.exec("git pushall");
                final var returnValue = pushProcess.waitFor();
                if (returnValue != 0) {
                    final var stdError = new BufferedReader(new InputStreamReader(pushProcess.getErrorStream()));
                    throw new RuntimeException("Git command returned %d code: %s".formatted(returnValue, stdError.lines().toList()));
                }
            }
        } catch (IOException | InterruptedException e) {
             LOGGER.error("Execution of git commit and push failed", e);
        }
    }
}
