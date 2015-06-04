package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.protocol.api.Manufacturer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@Path("/manufacturers")
public class ManufacturerResource {

    public ManufacturerResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public ManufacturerInfos getAllManufacturers() {
        ManufacturerInfos manufacturerInfos = new ManufacturerInfos();
        manufacturerInfos.manufacturerInfos = new ArrayList<>();
        for (Manufacturer manufacturer : Manufacturer.values()) {
            manufacturerInfos.manufacturerInfos.add(new ManufacturerInfo(manufacturer));
        }
        return manufacturerInfos;
    }

}