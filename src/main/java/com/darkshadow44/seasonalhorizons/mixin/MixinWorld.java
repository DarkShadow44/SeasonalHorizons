package com.darkshadow44.seasonalhorizons.mixin;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.darkshadow44.seasonalhorizons.season.Season;
import com.darkshadow44.seasonalhorizons.season.SeasonHandler;

@Mixin(World.class)
public class MixinWorld {

    @Redirect(
        method = "canSnowAtBody",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/BiomeGenBase;getFloatTemperature(III)F"))
    private float getTemperature(BiomeGenBase biome, int x, int y, int z) {
        Season season = SeasonHandler.getSeasonForWorld((World) (Object) this);
        if (season == null) {
            return biome.getFloatTemperature(x, y, z);
        }
        return season.getAdjustedTemperatureFloat(biome, x, y, z);
    }
}
