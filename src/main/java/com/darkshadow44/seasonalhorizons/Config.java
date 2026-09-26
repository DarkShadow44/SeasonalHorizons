package com.darkshadow44.seasonalhorizons;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    private static int[] seasonDimensions = { 0 };
    private static boolean walkCanopies = true;
    private static boolean snowUnderCanopies = true;
    private static boolean icicles = true;
    private static float icicleChance = 0.02F;
    private static final String[] DEFAULT_LEAF_PILE_LEAVES = { "minecraft:leaves:0", "minecraft:leaves:2",
        "minecraft:leaves2:0", "minecraft:leaves2:1" };
    private static boolean leafPiles = true;
    private static float leafPileChance = 0.02F;
    private static String[] leafPileLeaves = DEFAULT_LEAF_PILE_LEAVES;
    private static int subseasonLength = 10000;
    private static int snowScheduleLength = 1000;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        seasonDimensions = configuration
            .get(
                Configuration.CATEGORY_GENERAL,
                "seasonDimensions",
                new int[] { 0 },
                "Dimension IDs that have seasons. Each dimension keeps and advances its own season.")
            .getIntList();

        walkCanopies = configuration.get(
            Configuration.CATEGORY_GENERAL,
            "walkCanopies",
            true,
            "Whether columns beneath leaf canopies are processed at all. Required by snowUnderCanopies, icicles and leafPiles. Disabling improves performance; everything beneath canopies then stays as it is.")
            .getBoolean();

        snowUnderCanopies = configuration.get(
            Configuration.CATEGORY_GENERAL,
            "snowUnderCanopies",
            true,
            "Whether snow and ice form on the ground beneath leaf canopies. Requires walkCanopies. Existing snow and ice there still thaw when disabled.")
            .getBoolean();

        icicles = configuration.get(
            Configuration.CATEGORY_GENERAL,
            "icicles",
            true,
            "Whether icicles form below leaves where snow accumulates. Requires walkCanopies. Existing icicles still thaw when disabled.")
            .getBoolean();

        icicleChance = configuration.getFloat(
            "icicleChance",
            Configuration.CATEGORY_GENERAL,
            0.02F,
            0.0F,
            1.0F,
            "Fraction of columns in which icicles form below leaves.");

        leafPiles = configuration.get(
            Configuration.CATEGORY_GENERAL,
            "leafPiles",
            true,
            "Whether leaf piles form beneath leaf canopies in autumn. Requires walkCanopies. Existing piles are still removed outside autumn when disabled.")
            .getBoolean();

        leafPileChance = configuration.getFloat(
            "leafPileChance",
            Configuration.CATEGORY_GENERAL,
            0.02F,
            0.0F,
            1.0F,
            "Fraction of columns in which leaf piles form beneath leaves.");

        leafPileLeaves = configuration
            .get(
                Configuration.CATEGORY_GENERAL,
                "leafPileLeaves",
                DEFAULT_LEAF_PILE_LEAVES,
                "Leaves that produce leaf piles, as modid:name:meta. Only vanilla leaves are supported for now.")
            .getStringList();

        subseasonLength = configuration.getInt(
            "subseasonLength",
            Configuration.CATEGORY_GENERAL,
            10000,
            1,
            Integer.MAX_VALUE,
            "Duration of each subseason in ticks.");

        snowScheduleLength = configuration.getInt(
            "snowScheduleLength",
            Configuration.CATEGORY_GENERAL,
            1000,
            1,
            100000,
            "Maximum number of ticks a snow/thaw schedule spans before every column has been updated.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static boolean isSeasonDimension(int dimension) {
        for (int seasonDimension : seasonDimensions) {
            if (seasonDimension == dimension) {
                return true;
            }
        }
        return false;
    }

    public static boolean isWalkCanopies() {
        return walkCanopies;
    }

    public static boolean isSnowUnderCanopies() {
        return snowUnderCanopies;
    }

    public static boolean isIcicles() {
        return icicles;
    }

    public static float getIcicleChance() {
        return icicleChance;
    }

    public static boolean isLeafPiles() {
        return leafPiles;
    }

    public static float getLeafPileChance() {
        return leafPileChance;
    }

    public static String[] getLeafPileLeaves() {
        return leafPileLeaves;
    }

    public static int getSubseasonLength() {
        return subseasonLength;
    }

    public static int getSnowScheduleLength() {
        return snowScheduleLength;
    }
}
