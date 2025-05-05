package com.darkshadow44.seasonalhorizons.mixin;

import com.darkshadow44.seasonalhorizons.season.Season;
import com.darkshadow44.seasonalhorizons.season.SeasonHandlerClient;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BiomeGenBase.class)
public class MixinBiomeGenBase {

    @Shadow
    public int color;

    @WrapMethod(method = "getFloatRainfall")
    public final float getFloatRainfall(Operation<Float> original) {
        Season season = SeasonHandlerClient.getCurrentSeason();
        float originalRainfall = original.call();
        return season.getAdjustedRainfall(originalRainfall);
    }

    @WrapMethod(method = "getFloatTemperature")
    public final float getFloatTemperature(int p_150564_1_, int p_150564_2_, int p_150564_3_, Operation<Float> original) {
        Season season = SeasonHandlerClient.getCurrentSeason();
        float originalTemperature = original.call(p_150564_1_, p_150564_2_, p_150564_3_);
        return season.getAdjustedTemperature(originalTemperature);
    }

}
