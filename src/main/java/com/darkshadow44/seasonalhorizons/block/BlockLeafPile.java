package com.darkshadow44.seasonalhorizons.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.darkshadow44.seasonalhorizons.ModCreativeTabs;
import com.darkshadow44.seasonalhorizons.SeasonalHorizons;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

// Leaf litter: scattered, nearly flat patches of leaves (see RenderLeafPile); one block per leaf block, metadata
// matches the leaf metadata
// so textures and colors can be taken from the leaves directly. Breaks into nothing
public class BlockLeafPile extends Block {

    // Set by the client proxy
    public static int renderType = -1;

    private final Block leaves;
    // Leaf metadata (& 3) that produce piles
    private final boolean[] enabledMeta = new boolean[4];

    public BlockLeafPile(Block leaves) {
        // Vine material is replaceable, so snow and players can place into piles
        super(Material.vine);
        this.leaves = leaves;
        setBlockName(SeasonalHorizons.MODID + ".leaf_pile");
        setCreativeTab(ModCreativeTabs.SEASONAL_HORIZONS);
        setHardness(0.1F);
        setStepSound(soundTypeGrass);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0625F, 1.0F);
    }

    public void enableMeta(int meta) {
        enabledMeta[meta & 3] = true;
    }

    public boolean isEnabledMeta(int meta) {
        return enabledMeta[meta & 3];
    }

    public Block getLeaves() {
        return leaves;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubBlocks(Item item, CreativeTabs tab, List items) {
        for (int meta = 0; meta < enabledMeta.length; meta++) {
            if (enabledMeta[meta]) {
                items.add(new ItemStack(item, 1, meta));
            }
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return super.canPlaceBlockAt(world, x, y, z) && world.isSideSolid(x, y - 1, z, ForgeDirection.UP);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (!world.isSideSolid(x, y - 1, z, ForgeDirection.UP)) {
            world.setBlockToAir(x, y, z);
        }
    }

    // No collision
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

    @Override
    public int getRenderType() {
        return renderType;
    }

    // Icons belong to the leaves
    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {}

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return leaves.getIcon(side, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBlockColor() {
        return leaves.getBlockColor();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderColor(int meta) {
        return leaves.getRenderColor(meta);
    }

    // Leaves read the metadata at the position, which matches theirs
    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        return leaves.colorMultiplier(world, x, y, z);
    }
}
