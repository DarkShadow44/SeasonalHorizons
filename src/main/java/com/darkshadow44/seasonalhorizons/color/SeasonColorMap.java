package com.darkshadow44.seasonalhorizons.color;

public class SeasonColorMap {

    /*
    Logic adapted from https://github.com/lucaargolo/fabric-seasons/blob/9e721b2e501741ad337c1e9d102d046e6809174c/src/main/java/io/github/lucaargolo/seasons/colors/SeasonFoliageColors.java
    */
    private int[] colorMap;

    public SeasonColorMap(int[] pixels) {
        this.colorMap = pixels;
    }

    public int getColor(double temperature, double humidity) {
        humidity *= temperature;
        int i = (int) ((1.0D - temperature) * 255.0D);
        int j = (int) ((1.0D - humidity) * 255.0D);
        int k = j << 8 | i;
        return colorMap[k];
    }
}
