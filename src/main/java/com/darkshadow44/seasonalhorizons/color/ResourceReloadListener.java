package com.darkshadow44.seasonalhorizons.color;

import com.darkshadow44.seasonalhorizons.SeasonalHorizons;
import com.darkshadow44.seasonalhorizons.season.Season;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class ResourceReloadListener implements IResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

        for (Season season : Season.values()) {
            String mainPath = "textures/colormap/" + season.getMainSeason().getId() + "/" + season.getId();
            String pathFoliage = mainPath + "_foliage.png";
            String pathGrass = mainPath + "_grass.png";
            season.setFoliageColorMap(getRawPixelData(pathFoliage));
            season.setGrassColorMap(getRawPixelData(pathGrass));
        }
    }


    public static int[] getRawPixelData(String path) {
        try {
            ResourceLocation location = new ResourceLocation(SeasonalHorizons.MODID, path);
            return TextureUtil.readImageData(Minecraft.getMinecraft().getResourceManager(), location);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
