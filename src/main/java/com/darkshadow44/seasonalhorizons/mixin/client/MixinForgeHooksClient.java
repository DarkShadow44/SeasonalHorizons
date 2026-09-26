package com.darkshadow44.seasonalhorizons.mixin.client;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.ForgeHooksClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.darkshadow44.seasonalhorizons.season.SeasonHandler;

@Mixin(value = ForgeHooksClient.class, remap = false)
public class MixinForgeHooksClient {

    @Redirect(
        method = "getSkyBlendColour",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/biome/BiomeGenBase;getFloatTemperature(III)F",
            remap = true))
    private static float getTemperature(BiomeGenBase biome, int x, int y, int z, World world, int playerX, int playerY,
        int playerZ) {
        return SeasonHandler.getAdjustedTemperature(world, biome, x, y, z);
    }
}
