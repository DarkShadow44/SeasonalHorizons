package com.darkshadow44.seasonalhorizons.mixin;

import net.minecraft.world.biome.BiomeGenBase;

import org.spongepowered.asm.mixin.Mixin;

import com.darkshadow44.seasonalhorizons.season.Season;
import com.darkshadow44.seasonalhorizons.season.SeasonHandler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(BiomeGenBase.class)
public class MixinBiomeGenBase {

    @WrapMethod(method = "getFloatRainfall")
    public final float seasonalhorizons$getFloatRainfall(Operation<Float> original) {
        Season season = SeasonHandler.getCurrentClientSeason();
        float originalRainfall = original.call();
        return season.getAdjustedRainfall(originalRainfall);
    }

    @WrapMethod(method = "getFloatTemperature")
    public final float seasonalhorizons$getFloatTemperature(int p_150564_1_, int p_150564_2_, int p_150564_3_,
        Operation<Float> original) {
        Season season = SeasonHandler.getCurrentClientSeason();
        float originalTemperature = original.call(p_150564_1_, p_150564_2_, p_150564_3_);
        return season.getAdjustedTemperature(originalTemperature);
    }

}
