package com.darkshadow44.seasonalhorizons.mixin;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.darkshadow44.seasonalhorizons.season.SnowHandler;

@Mixin(WorldServer.class)
public class MixinWorldServer {

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
            target = "Lnet/minecraft/world/WorldProvider;canDoRainSnowIce(Lnet/minecraft/world/chunk/Chunk;)Z"))
    private boolean handleSnow(WorldProvider instance, Chunk chunk) {
        SnowHandler.handleSnowServer(chunk);
        return instance.canDoRainSnowIce(chunk);
    }

    @Inject(method = "func_147456_g", at = @At("HEAD"))
    private void handleSnowGlobal(CallbackInfo ci) {
        SnowHandler.handleSnowServerGlobal((WorldServer) (Object) this);
    }
}
