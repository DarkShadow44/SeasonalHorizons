package com.darkshadow44.seasonalhorizons;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    private static int[] seasonDimensions = { 0 };

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        seasonDimensions = configuration
            .get(
                Configuration.CATEGORY_GENERAL,
                "seasonDimensions",
                new int[] { 0 },
                "Dimension IDs that have seasons. Each dimension keeps and advances its own season.")
            .getIntList();

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
}
