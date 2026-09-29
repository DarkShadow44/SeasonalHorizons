package com.darkshadow44.seasonalhorizons.block;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import com.darkshadow44.seasonalhorizons.Config;
import com.darkshadow44.seasonalhorizons.SeasonalHorizons;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static BlockIcicle icicle;

    private static final Map<Block, BlockLeafPile> leafPiles = new IdentityHashMap<>();

    private static final Map<Block, BlockSpringFlower> springFlowers = new IdentityHashMap<>();
    // Every listed flower as its spring flower block and metadata, in list order
    private static final List<BlockSpringFlower> springFlowerBlocks = new ArrayList<>();
    private static final List<Integer> springFlowerMetas = new ArrayList<>();

    public static void register() {
        icicle = new BlockIcicle();
        GameRegistry.registerBlock(icicle, "icicle");

        registerLeafPiles();
        registerSpringFlowers();
    }

    // Entries are "modid:name:meta"; one pile block per leaf block, shared by its listed metadata
    private static void registerLeafPiles() {
        for (String entry : Config.getLeafPileLeaves()) {
            int split = entry.lastIndexOf(':');
            Block leaves = null;
            int meta = -1;
            if (split > 0) {
                leaves = Block.getBlockFromName(entry.substring(0, split));
                try {
                    meta = Integer.parseInt(entry.substring(split + 1));
                } catch (NumberFormatException ignored) {}
            }
            if (leaves == null || meta < 0 || meta > 3) {
                SeasonalHorizons.LOG.warn("Ignoring invalid leaf pile entry '{}'", entry);
                continue;
            }

            BlockLeafPile pile = leafPiles.get(leaves);
            if (pile == null) {
                pile = new BlockLeafPile(leaves);
                String name = Block.blockRegistry.getNameForObject(leaves)
                    .replace(':', '_');
                GameRegistry.registerBlock(pile, ItemBlockLeafPile.class, "leaf_pile_" + name);
                leafPiles.put(leaves, pile);
            }
            pile.enableMeta(meta);
        }
    }

    // Entries are "modid:name:meta"; one spring flower block per flower block, shared by its listed metadata
    private static void registerSpringFlowers() {
        for (String entry : Config.getSpringFlowerBlocks()) {
            int split = entry.lastIndexOf(':');
            Block flower = null;
            int meta = -1;
            if (split > 0) {
                flower = Block.getBlockFromName(entry.substring(0, split));
                try {
                    meta = Integer.parseInt(entry.substring(split + 1));
                } catch (NumberFormatException ignored) {}
            }
            // The block registry returns air for unknown names
            if (flower == null || flower == Blocks.air || meta < 0 || meta > 15) {
                SeasonalHorizons.LOG.warn("Ignoring invalid spring flower entry '{}'", entry);
                continue;
            }

            BlockSpringFlower springFlower = springFlowers.get(flower);
            if (springFlower == null) {
                springFlower = new BlockSpringFlower(flower);
                String name = Block.blockRegistry.getNameForObject(flower)
                    .replace(':', '_');
                // No item: harvesting gives the real flower
                GameRegistry.registerBlock(springFlower, null, "spring_flower_" + name);
                springFlowers.put(flower, springFlower);
            }
            if (!springFlower.isEnabledMeta(meta)) {
                springFlower.enableMeta(meta);
                springFlowerBlocks.add(springFlower);
                springFlowerMetas.add(meta);
            }
        }
    }

    public static int getSpringFlowerCount() {
        return springFlowerBlocks.size();
    }

    public static BlockSpringFlower getSpringFlower(int index) {
        return springFlowerBlocks.get(index);
    }

    public static int getSpringFlowerMeta(int index) {
        return springFlowerMetas.get(index);
    }

    // The pile for the given leaves, or null if they don't produce piles
    public static BlockLeafPile getLeafPile(Block leaves, int meta) {
        BlockLeafPile pile = leafPiles.get(leaves);
        return pile != null && pile.isEnabledMeta(meta) ? pile : null;
    }
}
