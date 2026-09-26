package com.darkshadow44.seasonalhorizons.block;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.block.Block;

import com.darkshadow44.seasonalhorizons.Config;
import com.darkshadow44.seasonalhorizons.SeasonalHorizons;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static BlockIcicle icicle;

    private static final Map<Block, BlockLeafPile> leafPiles = new IdentityHashMap<>();

    public static void register() {
        icicle = new BlockIcicle();
        GameRegistry.registerBlock(icicle, "icicle");

        registerLeafPiles();
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
                GameRegistry.registerBlock(pile, "leaf_pile_" + name);
                leafPiles.put(leaves, pile);
            }
            pile.enableMeta(meta);
        }
    }

    // The pile for the given leaves, or null if they don't produce piles
    public static BlockLeafPile getLeafPile(Block leaves, int meta) {
        BlockLeafPile pile = leafPiles.get(leaves);
        return pile != null && pile.isEnabledMeta(meta) ? pile : null;
    }
}
