package com.darkshadow44.seasonalhorizons;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    private static int[] seasonDimensions = { 0 };
    private static boolean snowUnderCanopies = true;
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

        snowUnderCanopies = configuration
            .get(
                Configuration.CATEGORY_GENERAL,
                "snowUnderCanopies",
                true,
                "Whether snow and ice form and thaw on the ground beneath leaf canopies. Disabling improves performance; existing snow and ice there then stays.")
            .getBoolean();

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

    public static boolean isSnowUnderCanopies() {
        return snowUnderCanopies;
    }

    public static int getSubseasonLength() {
        return subseasonLength;
    }

    public static int getSnowScheduleLength() {
        return snowScheduleLength;
    }
}
