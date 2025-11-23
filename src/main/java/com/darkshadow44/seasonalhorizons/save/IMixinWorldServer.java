package com.darkshadow44.seasonalhorizons.save;

import com.darkshadow44.seasonalhorizons.season.SnowHandler;

public interface IMixinWorldServer {

    SeasonWorldData seasonalHorizons$getSeasonWorldData();

    SnowHandler seasonalHorizons$getSnowHandler();
}
