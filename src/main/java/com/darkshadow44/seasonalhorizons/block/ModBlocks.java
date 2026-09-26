package com.darkshadow44.seasonalhorizons.block;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static BlockIcicle icicle;

    public static void register() {
        icicle = new BlockIcicle();
        GameRegistry.registerBlock(icicle, "icicle");
    }
}
