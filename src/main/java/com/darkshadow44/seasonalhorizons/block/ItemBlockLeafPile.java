package com.darkshadow44.seasonalhorizons.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBlockLeafPile extends ItemBlock {

    public ItemBlockLeafPile(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }
}
