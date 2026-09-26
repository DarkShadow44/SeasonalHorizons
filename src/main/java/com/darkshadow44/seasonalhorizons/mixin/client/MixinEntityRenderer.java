package com.darkshadow44.seasonalhorizons.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.world.biome.BiomeGenBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.darkshadow44.seasonalhorizons.season.SeasonHandler;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow
    private Minecraft mc;

    @Redirect(
        method = { "addRainParticles", "renderRainSnow" },
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/BiomeGenBase;getFloatTemperature(III)F"))
    private float getTemperature(BiomeGenBase biome, int x, int y, int z) {
        return SeasonHandler.getAdjustedTemperature(mc.theWorld, biome, x, y, z);
    }
}
