package com.darkshadow44.seasonalhorizons.save;

public interface IMixinChunk {

    void seasonalHorizons$setLastSaveTime(long time);

    long seasonalHorizons$getLastSaveTime();
}
