package com.github.electroluxv2.interfaces;

import net.minecraft.world.level.ServerWorldProperties;

import java.util.Optional;

public interface WorldPropertiesGetter {
    default Optional<ServerWorldProperties> git_backup_template_1_20_1$getWorldProperties() {
        return Optional.empty();
    }
}
