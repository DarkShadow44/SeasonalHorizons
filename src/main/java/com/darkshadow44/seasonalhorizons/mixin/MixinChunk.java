package com.darkshadow44.seasonalhorizons.mixin;

import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.darkshadow44.seasonalhorizons.save.IMixinChunk;

@Mixin(Chunk.class)
public class MixinChunk implements IMixinChunk {

    @Unique
    private long seasonalHorizons$lastUpdateTime;

    @Override
    public void seasonalHorizons$setLastUpdateTime(long time) {
        seasonalHorizons$lastUpdateTime = time;
    }

    @Override
    public long seasonalHorizons$getLastUpdateTime() {
        return seasonalHorizons$lastUpdateTime;
    }
}
