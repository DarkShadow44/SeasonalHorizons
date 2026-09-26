package com.darkshadow44.seasonalhorizons.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.world.biome.BiomeGenBase;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.darkshadow44.seasonalhorizons.season.SeasonHandler;

@Mixin(EntitySnowman.class)
public class MixinEntitySnowman {

    @Redirect(
        method = "onLivingUpdate",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/BiomeGenBase;getFloatTemperature(III)F"))
    private float getTemperature(BiomeGenBase biome, int x, int y, int z) {
        return SeasonHandler.getAdjustedTemperature(((Entity) (Object) this).worldObj, biome, x, y, z);
    }
}
