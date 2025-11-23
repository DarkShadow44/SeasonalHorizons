package com.darkshadow44.seasonalhorizons.mixin;

import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.darkshadow44.seasonalhorizons.save.IMixinWorldServer;
import com.darkshadow44.seasonalhorizons.save.SeasonWorldData;
import com.darkshadow44.seasonalhorizons.season.SnowHandler;

@Mixin(WorldServer.class)
public abstract class MixinWorldServer extends World implements IMixinWorldServer {

    @Unique
    private SnowHandler seasonalHorizons$snowHandler;

    @Unique
    private SeasonWorldData seasonalHorizons$seasonWorldData;

    public MixinWorldServer(ISaveHandler p_i45368_1_, String p_i45368_2_, WorldProvider p_i45368_3_,
        WorldSettings p_i45368_4_, Profiler p_i45368_5_) {
        super(p_i45368_1_, p_i45368_2_, p_i45368_3_, p_i45368_4_, p_i45368_5_);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void constructor(MinecraftServer p_i45284_1_, ISaveHandler p_i45284_2_, String p_i45284_3_, int p_i45284_4_,
        WorldSettings p_i45284_5_, Profiler p_i45284_6_, CallbackInfo ci) {
        seasonalHorizons$seasonWorldData = (SeasonWorldData) this
            .loadItemData(SeasonWorldData.class, "seasonalhorizons");
        seasonalHorizons$snowHandler = new SnowHandler((World) (Object) this, seasonalHorizons$seasonWorldData);
    }

    @Redirect(
        method = "func_147456_g",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;func_147478_e(IIIZ)Z"))
    private boolean stopSnowing(WorldServer instance, int x, int y, int z, boolean checkLight) {
        return false;
    }

    @Redirect(
        method = "func_147456_g",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/WorldProvider;canDoRainSnowIce(Lnet/minecraft/world/chunk/Chunk;)Z",
            remap = false))
    private boolean handleSnow(WorldProvider instance, Chunk chunk) {
        seasonalHorizons$snowHandler.handleSnowServerTick(chunk);
        return instance.canDoRainSnowIce(chunk);
    }

    @Inject(method = "func_147456_g", at = @At("HEAD"))
    private void handleSnowGlobal(CallbackInfo ci) {
        seasonalHorizons$snowHandler.handleSnowServerGlobal();
    }

    @Override
    public SeasonWorldData seasonalHorizons$getSeasonWorldData() {
        return seasonalHorizons$seasonWorldData;
    }

    @Override
    public SnowHandler seasonalHorizons$getSnowHandler() {
        return seasonalHorizons$snowHandler;
    }
}
