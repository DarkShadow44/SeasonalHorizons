package com.darkshadow44.seasonalhorizons.block;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.darkshadow44.seasonalhorizons.SeasonalHorizons;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

// A flower that blooms in spring and is removed in the other seasons. One block per flower block, metadata matches
// the flower metadata so looks, drops and pick block can be taken from the flower directly. Harvesting it gives the
// real flower, so only flowers that haven't been picked up are seasonal. Has no item of its own
public class BlockSpringFlower extends BlockBush {

    private final Block flower;
    // Flower metadata that bloom in spring
    private final boolean[] enabledMeta = new boolean[16];

    public BlockSpringFlower(Block flower) {
        super(flower.getMaterial());
        this.flower = flower;
        setBlockName(SeasonalHorizons.MODID + ".spring_flower");
        setCreativeTab(null);
        setStepSound(flower.stepSound);
    }

    public void enableMeta(int meta) {
        enabledMeta[meta] = true;
    }

    public boolean isEnabledMeta(int meta) {
        return enabledMeta[meta];
    }

    // The flower's own placement rules; the position is still air when this is asked
    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return flower.canPlaceBlockAt(world, x, y, z);
    }

    @Override
    public boolean canBlockStay(World world, int x, int y, int z) {
        return flower.canBlockStay(world, x, y, z);
    }

    @Override
    public float getBlockHardness(World world, int x, int y, int z) {
        return flower.getBlockHardness(world, x, y, z);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        return flower.getDrops(world, x, y, z, metadata, fortune);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return flower.getPickBlock(target, world, x, y, z, player);
    }

    @Override
    public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return flower.getFlammability(world, x, y, z, face);
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return flower.getFireSpreadSpeed(world, x, y, z, face);
    }

    @Override
    public int getRenderType() {
        return flower.getRenderType();
    }

    // Icons belong to the flower
    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {}

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return flower.getIcon(side, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBlockColor() {
        return flower.getBlockColor();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderColor(int meta) {
        return flower.getRenderColor(meta);
    }

    // Flowers read the metadata at the position, which matches theirs
    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        return flower.colorMultiplier(world, x, y, z);
    }
}
