package com.darkshadow44.seasonalhorizons.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import com.darkshadow44.seasonalhorizons.ModCreativeTabs;
import com.darkshadow44.seasonalhorizons.SeasonalHorizons;

// Hangs below leaves; breaks into nothing
public class BlockIcicle extends Block {

    public BlockIcicle() {
        super(Material.ice);
        setBlockName(SeasonalHorizons.MODID + ".icicle");
        setBlockTextureName(SeasonalHorizons.MODID + ":icicle");
        setHardness(0.0F);
        setStepSound(soundTypeGlass);
        setCreativeTab(ModCreativeTabs.SEASONAL_HORIZONS);
        setBlockBounds(0.3F, 0.2F, 0.3F, 0.7F, 1.0F, 0.7F);
    }

    public boolean canHangAt(World world, int x, int y, int z) {
        return world.getBlock(x, y + 1, z)
            .isLeaves(world, x, y + 1, z);
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return super.canPlaceBlockAt(world, x, y, z) && canHangAt(world, x, y, z);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (!canHangAt(world, x, y, z)) {
            world.setBlockToAir(x, y, z);
        }
    }

    // No collision, like plants
    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return null;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    // Crossed quads, like plants
    @Override
    public int getRenderType() {
        return 1;
    }
}
