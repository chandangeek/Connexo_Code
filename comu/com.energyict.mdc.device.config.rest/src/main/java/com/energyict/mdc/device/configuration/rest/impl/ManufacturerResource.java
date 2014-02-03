package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.SortOrder;
import com.energyict.mdc.protocol.api.Manufacturer;
import com.energyict.mdc.services.DeviceConfigurationService;
import com.energyict.mdw.core.DeviceType;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/manufacturers")
public class ManufacturerResource {


    public ManufacturerResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ManufacturerInfos getAllManufacturers() {
        ManufacturerInfos manufacturerInfos = new ManufacturerInfos();
        manufacturerInfos.manufacturerInfos = new ArrayList<>();
        for (Manufacturer manufacturer : Manufacturer.values()) {
            manufacturerInfos.manufacturerInfos.add(new ManufacturerInfo(manufacturer));
        }
        return manufacturerInfos;
    }

}
