package com.darkshadow44.seasonalhorizons.mixin;

import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.darkshadow44.seasonalhorizons.save.IMixinChunk;

@Mixin(Chunk.class)
public class MixinChunk implements IMixinChunk {

    @Unique
    private long seasonalHorizons$lastSaveTime;

    @Override
    public void seasonalHorizons$setLastSaveTime(long time) {
        seasonalHorizons$lastSaveTime = time;
    }

    @Override
    public long seasonalHorizons$getLastSaveTime() {
        return 0;
    }
}
