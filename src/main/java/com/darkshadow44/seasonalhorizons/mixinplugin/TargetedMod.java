package com.darkshadow44.seasonalhorizons.mixinplugin;

import javax.annotation.Nonnull;

import com.gtnewhorizon.gtnhmixins.builders.ITargetMod;
import com.gtnewhorizon.gtnhmixins.builders.TargetModBuilder;

public enum TargetedMod implements ITargetMod {

    DynamicSurroundings("dsurround", "org.blockartistry.mod.DynSurround.Module");

    private final TargetModBuilder builder;

    TargetedMod(String modId, String targetClass) {
        this.builder = new TargetModBuilder().setModId(modId)
            .setTargetClass(targetClass)
            .testModID(modId);
    }

    @Override
    public @Nonnull TargetModBuilder getBuilder() {
        return builder;
    }
}
