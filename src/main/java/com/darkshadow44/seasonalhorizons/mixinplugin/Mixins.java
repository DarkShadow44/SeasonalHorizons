package com.darkshadow44.seasonalhorizons.mixinplugin;

import javax.annotation.Nonnull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    // COMMON
    MIXIN_CHUNK(new MixinBuilder().addCommonMixins("MixinChunk")),
    MIXIN_WORLD(new MixinBuilder().addCommonMixins("MixinWorld")),
    MIXIN_WORLD_SERVER(new MixinBuilder().addCommonMixins("MixinWorldServer")),
    MIXIN_ENTITY_SNOWMAN(new MixinBuilder().addCommonMixins("MixinEntitySnowman")),

    // CLIENT
    ACCESSOR_FORGE_HOOKS_CLIENT(new MixinBuilder().addClientMixins("client.AccessorForgeHooksClient")),
    MIXIN_ENTITY_RENDERER_RAIN_SNOW(new MixinBuilder("Season-adjusted temperature for rain/snow rendering")
        .addClientMixins("client.MixinEntityRenderer_RainSnow")
        .addExcludedMod(TargetedMod.DynamicSurroundings)),
    MIXIN_FORGE_HOOKS_CLIENT(new MixinBuilder().addClientMixins("client.MixinForgeHooksClient"));

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Override
    public @Nonnull MixinBuilder getBuilder() {
        return builder;
    }
}
