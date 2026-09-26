package com.darkshadow44.seasonalhorizons.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

// Renders a pile as a few scattered, nearly flat leaf patches with gaps between them.
// The layout is chosen pseudo-randomly by position, so it is stable across re-renders
@SideOnly(Side.CLIENT)
public class RenderLeafPile implements ISimpleBlockRenderingHandler {

    private static final int MIN_PATCHES = 5;
    private static final int MAX_PATCHES = 8;
    // Patch edge lengths in pixels
    private static final int MIN_SIZE = 3;
    private static final int MAX_SIZE = 8;
    // Height of the lowest patch; each further patch is raised slightly so overlaps don't z-fight
    private static final double BASE_HEIGHT = 1.0 / 128.0;
    private static final double HEIGHT_STEP = 1.0 / 512.0;

    public static void register() {
        BlockLeafPile.renderType = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(BlockLeafPile.renderType, new RenderLeafPile());
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        IIcon icon = renderer.hasOverrideBlockTexture() ? renderer.overrideBlockTexture
            : renderer.getBlockIcon(block, world, x, y, z, 1);

        int color = block.colorMultiplier(world, x, y, z);
        Tessellator tessellator = Tessellator.instance;
        tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        tessellator.setColorOpaque_F((color >> 16 & 255) / 255.0F, (color >> 8 & 255) / 255.0F, (color & 255) / 255.0F);

        Random random = new Random((long) x * 3129871L ^ (long) z * 116129781L ^ (long) y * 42317861L);
        int patches = MIN_PATCHES + random.nextInt(MAX_PATCHES - MIN_PATCHES + 1);
        for (int i = 0; i < patches; i++) {
            int sizeX = MIN_SIZE + random.nextInt(MAX_SIZE - MIN_SIZE + 1);
            int sizeZ = MIN_SIZE + random.nextInt(MAX_SIZE - MIN_SIZE + 1);
            int minX = random.nextInt(17 - sizeX);
            int minZ = random.nextInt(17 - sizeZ);
            double height = BASE_HEIGHT + i * HEIGHT_STEP;

            // Texture coordinates follow the patch position; rotation varies the pattern between patches
            renderer.uvRotateTop = random.nextInt(4);
            renderer.setRenderBounds(
                minX / 16.0,
                0.0,
                minZ / 16.0,
                (minX + sizeX) / 16.0,
                height,
                (minZ + sizeZ) / 16.0);
            renderer.renderFaceYPos(block, x, y, z, icon);
        }

        renderer.uvRotateTop = 0;
        renderer.setRenderBoundsFromBlock(block);
        return true;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {}

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }

    @Override
    public int getRenderId() {
        return BlockLeafPile.renderType;
    }
}
