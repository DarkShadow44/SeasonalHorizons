package com.darkshadow44.seasonalhorizons;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public final class ModCreativeTabs {

    public static final CreativeTabs SEASONAL_HORIZONS = new CreativeTabs(SeasonalHorizons.MODID) {

        @Override
        public Item getTabIconItem() {
            return ModItems.creativeTabIcon;
        }
    };

    private ModCreativeTabs() {}
}
