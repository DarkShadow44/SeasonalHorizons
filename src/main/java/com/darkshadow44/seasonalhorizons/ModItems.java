package com.darkshadow44.seasonalhorizons;

import net.minecraft.item.Item;

import cpw.mods.fml.common.registry.GameRegistry;

public final class ModItems {

    public static Item creativeTabIcon;

    public static void register() {
        creativeTabIcon = new Item().setTextureName(SeasonalHorizons.MODID + ":creative_tab_icon");
        GameRegistry.registerItem(creativeTabIcon, "creative_tab_icon");
    }

    private ModItems() {}
}
