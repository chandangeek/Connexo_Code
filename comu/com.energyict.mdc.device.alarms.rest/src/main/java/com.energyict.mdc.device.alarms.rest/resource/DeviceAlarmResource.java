package com.energyict.mdc.device.alarms.rest.resource;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Created by albertv on 11/29/2016.
 */

@Path("/alarms")
public class DeviceAlarmResource{

    @Inject
    public DeviceAlarmResource(){

    }

    @GET
    public Response testGetAlarm(){
        return Response.ok().build();
    }
}
