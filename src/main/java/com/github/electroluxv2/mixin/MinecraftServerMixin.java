package com.github.electroluxv2.mixin;

import com.github.electroluxv2.utils.ScriptsManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Shadow private volatile boolean running;

    @Inject(at = @At(value = "TAIL"), method = "saveEverything")
    public void save(boolean suppressLogs, boolean flush, boolean force, CallbackInfoReturnable<Boolean> cir) {
        ScriptsManager.runOnSaveScript(false, (MinecraftServer) (Object) this, running);
    }

    @Inject(at = @At(value = "TAIL"), method = "stopServer")
    public void onStopServer(CallbackInfo ci) {
        ScriptsManager.runOnSaveScript(true, (MinecraftServer) (Object) this, running);
    }
}
