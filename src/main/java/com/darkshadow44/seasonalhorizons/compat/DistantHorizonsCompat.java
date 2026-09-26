package com.darkshadow44.seasonalhorizons.compat;

import com.seibel.distanthorizons.api.DhApi;

// Client-only; only instantiated when DH is installed, so the DH API can be used freely here
public class DistantHorizonsCompat {

    // Rebuilds DH render data from its stored terrain so LODs pick up the new grass and foliage colors
    public void refreshLods() {
        // Null until DH has finished initializing
        if (DhApi.Delayed.renderProxy != null) {
            DhApi.Delayed.renderProxy.clearRenderDataCache();
        }
    }
}
