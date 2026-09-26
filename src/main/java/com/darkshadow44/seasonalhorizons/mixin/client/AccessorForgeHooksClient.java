package com.darkshadow44.seasonalhorizons.mixin.client;

import net.minecraftforge.client.ForgeHooksClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ForgeHooksClient.class, remap = false)
public interface AccessorForgeHooksClient {

    // The sky color is cached until the player moves, so it must be invalidated when the season changes
    @Accessor("skyInit")
    static void setSkyInit(boolean skyInit) {
        throw new AssertionError();
    }
}
