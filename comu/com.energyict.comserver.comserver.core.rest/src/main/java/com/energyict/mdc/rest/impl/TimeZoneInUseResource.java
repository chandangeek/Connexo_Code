package com.energyict.mdc.rest.impl;

import com.energyict.mdc.rest.impl.properties.propertycontexts.TimeZoneInUseInfo;
import com.energyict.mdc.rest.impl.properties.propertycontexts.TimeZoneInUseInfos;
import com.energyict.mdw.core.TimeZoneInUse;
import com.energyict.mdw.core.TimeZoneInUseFactoryProvider;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

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
        for (TimeZoneInUse timeZoneInUse : TimeZoneInUseFactoryProvider.instance.get().getTimeZoneInUseFactory().findAll()) {
            timeZoneInUseInfos.timeZonesInUse.add(new TimeZoneInUseInfo(timeZoneInUse));
        }
        return timeZoneInUseInfos;
    }
}
