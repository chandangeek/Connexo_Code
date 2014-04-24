package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.protocol.api.timezones.TimeZoneInUse;
import com.energyict.mdc.protocol.api.timezones.TimeZoneInUseFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 20/11/13
 * Time: 15:20
 */
@Path("/timezoneinuses")
public class TimeZoneInUseResource {

    public TimeZoneInUseResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TimeZoneInUseInfos getTimeZoneInUsePropertyContext(@Context UriInfo uriInfo){
        TimeZoneInUseInfos timeZoneInUseInfos = new TimeZoneInUseInfos();
        List<TimeZoneInUseFactory> timeZoneInUseFactories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(TimeZoneInUseFactory.class);
        for (TimeZoneInUseFactory timeZoneInUseFactory : timeZoneInUseFactories) {
            for (TimeZoneInUse timeZoneInUse : timeZoneInUseFactory.findAllTimeZoneInUses()) {
                timeZoneInUseInfos.timeZonesInUse.add(new TimeZoneInUseInfo(timeZoneInUse));
            }
        }
        return timeZoneInUseInfos;
    }
}
