package com.darkshadow44.seasonalhorizons;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {
    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
