package com.darkshadow44.seasonalhorizons.save;

public interface IMixinChunk {

    void seasonalHorizons$setLastUpdateTime(long time);

    long seasonalHorizons$getLastUpdateTime();
}
