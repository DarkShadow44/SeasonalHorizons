package com.darkshadow44.seasonalhorizons.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.darkshadow44.seasonalhorizons.mixininterfaces.IMixinBiomeGenBase;
import com.darkshadow44.seasonalhorizons.season.Season;
import com.darkshadow44.seasonalhorizons.season.SeasonHandler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(BiomeGenBase.class)
public class MixinBiomeGenBase implements IMixinBiomeGenBase {

    @Shadow
    public float temperature;

    @Final
    @Shadow
    protected static NoiseGeneratorPerlin temperatureNoise;

    @WrapMethod(method = "getFloatRainfall")
    public final float seasonalhorizons$getFloatRainfall(Operation<Float> original) {
        float originalRainfall = original.call();
        if (Minecraft.getMinecraft().theWorld == null) {
            return originalRainfall;
        }
        Season season = SeasonHandler.getCurrentClientSeason();
        return season.getAdjustedRainfall(originalRainfall);
    }

    @WrapMethod(method = "getFloatTemperature")
    public final float seasonalhorizons$getFloatTemperature(int p_150564_1_, int p_150564_2_, int p_150564_3_,
        Operation<Float> original) {
        float originalTemperature = original.call(p_150564_1_, p_150564_2_, p_150564_3_);
        if (Minecraft.getMinecraft().theWorld == null) {
            return originalTemperature;
        }
        Season season = SeasonHandler.getCurrentClientSeason();
        return season.getAdjustedTemperature(originalTemperature);
    }

    @Unique
    @Override
    public final float seasonalHorizons$getAdjustedFloatTemperature(float temperature, int x, int y, int z) {
        if (y > 64) {
            float f = (float) temperatureNoise.func_151601_a((double) x / 8.0D, (double) z / 8.0D) * 4.0F;
            return temperature - (f + (float) y - 64.0F) * 0.05F / 30.0F;
        } else {
            return temperature;
        }
    }

}
