package com.darkshadow44.seasonalhorizons.save;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

import com.darkshadow44.seasonalhorizons.SeasonalHorizons;
import com.falsepattern.chunk.api.DataManager;

public class AdditionalChunkData implements DataManager.ChunkDataManager {

    @Override
    public String domain() {
        return SeasonalHorizons.MODID;
    }

    @Override
    public String id() {
        return "chunk_data";
    }

    @Override
    public void writeChunkToNBT(Chunk chunk, NBTTagCompound nbt) {
        long time = ((IMixinChunk) chunk).seasonalHorizons$getLastSaveTime();
        nbt.setLong("seasonLastSaveTime", time);
    }

    @Override
    public void readChunkFromNBT(Chunk chunk, NBTTagCompound nbt) {
        long time = nbt.getLong("seasonLastSaveTime");
        ((IMixinChunk) chunk).seasonalHorizons$setLastSaveTime(time);
    }

    @Override
    public void cloneChunk(Chunk from, Chunk to) {
        long time = ((IMixinChunk) from).seasonalHorizons$getLastSaveTime();
        ((IMixinChunk) to).seasonalHorizons$setLastSaveTime(time);
    }

    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public String newInstallDescription() {
        return "Seasonal Horizons Season support";
    }

    @Override
    public String uninstallMessage() {
        return "Seasonal Horizons  Season support";
    }

    @Override
    public String versionChangeMessage(String priorVersion) {
        return null;
    }
}
