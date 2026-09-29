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
    private static float leafPileChance = 0.2F;
    private static String[] leafPileLeaves = DEFAULT_LEAF_PILE_LEAVES;
    private static final String[] DEFAULT_SPRING_FLOWER_BLOCKS = { "minecraft:yellow_flower:0",
        "minecraft:red_flower:0", "minecraft:red_flower:2", "minecraft:red_flower:3", "minecraft:red_flower:4",
        "minecraft:red_flower:5", "minecraft:red_flower:6", "minecraft:red_flower:7", "minecraft:red_flower:8" };
    private static boolean springFlowers = true;
    private static float springFlowerChance = 0.05F;
    private static String[] springFlowerBlocks = DEFAULT_SPRING_FLOWER_BLOCKS;
    private static int subseasonLength = 120000;
    private static int snowScheduleLength = 20480;
    private static boolean sleepAdvancesSeason = true;
    private static boolean pauseWithoutPlayers = true;

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
            0.2F,
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

        springFlowers = configuration.get(
            Configuration.CATEGORY_GENERAL,
            "springFlowers",
            true,
            "Whether flowers bloom on open ground in spring. Existing seasonal flowers are still removed outside spring when disabled.")
            .getBoolean();

        springFlowerChance = configuration.getFloat(
            "springFlowerChance",
            Configuration.CATEGORY_GENERAL,
            0.05F,
            0.0F,
            1.0F,
            "Fraction of columns in which flowers bloom in spring.");

        springFlowerBlocks = configuration.get(
            Configuration.CATEGORY_GENERAL,
            "springFlowerBlocks",
            DEFAULT_SPRING_FLOWER_BLOCKS,
            "Flowers that bloom in spring, as modid:name:meta. Each column picks one of them. Seasonal flowers look like the listed block and turn into it when harvested; they are removed in the other seasons, while real flowers are never touched. Only single-block plants are supported.")
            .getStringList();

        subseasonLength = configuration.getInt(
            "subseasonLength",
            Configuration.CATEGORY_GENERAL,
            120000,
            12000,
            Integer.MAX_VALUE,
            "Duration of each subseason in ticks.");

        snowScheduleLength = configuration.getInt(
            "snowScheduleLength",
            Configuration.CATEGORY_GENERAL,
            20480,
            1000,
            100000,
            "Maximum number of ticks a snow/thaw schedule spans before every column has been updated.");

        sleepAdvancesSeason = configuration.get(
            Configuration.CATEGORY_GENERAL,
            "sleepAdvancesSeason",
            true,
            "Whether sleeping through the night also advances the season and its snow and thaw by the skipped ticks. When disabled, the season does not advance while the night is skipped.")
            .getBoolean();

        pauseWithoutPlayers = configuration.get(
            Configuration.CATEGORY_GENERAL,
            "pauseWithoutPlayers",
            true,
            "Whether seasons, snow and thaw pause while no players are online. When disabled, they keep advancing in every loaded dimension with seasons, as the world time does.")
            .getBoolean();

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

    public static boolean isSpringFlowers() {
        return springFlowers;
    }

    public static float getSpringFlowerChance() {
        return springFlowerChance;
    }

    public static String[] getSpringFlowerBlocks() {
        return springFlowerBlocks;
    }

    public static int getSubseasonLength() {
        return subseasonLength;
    }

    public static int getSnowScheduleLength() {
        return snowScheduleLength;
    }

    public static boolean isSleepAdvancesSeason() {
        return sleepAdvancesSeason;
    }

    public static boolean isPauseWithoutPlayers() {
        return pauseWithoutPlayers;
    }
}
