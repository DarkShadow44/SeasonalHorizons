package com.darkshadow44.seasonalhorizons.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockLeafPile extends ItemBlock {

    private final BlockLeafPile leafPile;

    public ItemBlockLeafPile(Block block) {
        super(block);
        leafPile = (BlockLeafPile) block;
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        Item leavesItem = Item.getItemFromBlock(leafPile.getLeaves());
        if (leavesItem == null) {
            return StatCollector.translateToLocal("tile.seasonalhorizons.leaf_pile.name");
        }

        String leavesName = new ItemStack(leavesItem, 1, stack.getItemDamage()).getDisplayName();
        String genericLeavesName = StatCollector.translateToLocal("tile.leaves.name");
        String suffix = " " + genericLeavesName;
        if (leavesName.endsWith(suffix) && leavesName.length() > suffix.length()) {
            leavesName = leavesName.substring(0, leavesName.length() - suffix.length());
        }
        return StatCollector.translateToLocalFormatted("tile.seasonalhorizons.leaf_pile.named", leavesName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        return leafPile.getRenderColor(stack.getItemDamage());
    }
}
