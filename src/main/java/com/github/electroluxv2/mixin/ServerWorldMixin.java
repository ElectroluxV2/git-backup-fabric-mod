package com.github.electroluxv2.mixin;

import com.github.electroluxv2.interfaces.WorldPropertiesGetter;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements WorldPropertiesGetter {
    @Shadow @Final private ServerWorldProperties worldProperties;

    @Override
    public Optional<ServerWorldProperties> git_backup_template_1_20_1$getWorldProperties() {
        return Optional.ofNullable(worldProperties);
    }
}
