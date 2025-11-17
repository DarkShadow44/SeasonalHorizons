package com.darkshadow44.seasonalhorizons.color;

import net.minecraft.util.MathHelper;

public class SeasonColorMap {

    private final int[] colorMap;

    public SeasonColorMap(int[] pixels) {
        this.colorMap = pixels;
    }

    public int getColor(float temperature, float humidity) {
        temperature = MathHelper.clamp_float(temperature, 0, 1);
        humidity = MathHelper.clamp_float(humidity, 0, 1);
        humidity *= temperature;
        int i = (int) ((1.0f - temperature) * 255);
        int j = (int) ((1.0f - humidity) * 255);
        int k = j << 8 | i;
        return colorMap[k];
    }
}
