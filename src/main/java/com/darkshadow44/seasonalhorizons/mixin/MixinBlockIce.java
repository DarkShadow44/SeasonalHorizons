package com.darkshadow44.seasonalhorizons.mixin;

import net.minecraft.block.BlockIce;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(BlockIce.class)
public class MixinBlockIce {
    @Inject(method = "updateTick", at = @At("TAIL"))
    private void seasonalhorizons$meltSnow(World world, int x, int y, int z, Random random, CallbackInfo ci) {
        BiomeGenBase biomegenbase = world.getBiomeGenForCoords(x, z);
        float temperature = biomegenbase.getFloatTemperature(x, y, z);
        boolean canSeeSky = world.canBlockSeeTheSky(x, y, z);

        if (canSeeSky && temperature >= 0.15f) {
            world.setBlock(x, y, z, Blocks.water);
        }
    }
}
