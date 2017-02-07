/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.protocol.api.Manufacturer;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@Path("/manufacturers")
public class ManufacturerResource {

    public ManufacturerResource() {
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE,
            com.elster.jupiter.validation.security.Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION,
            com.elster.jupiter.validation.security.Privileges.Constants.VIEW_VALIDATION_CONFIGURATION,
            com.elster.jupiter.validation.security.Privileges.Constants.FINE_TUNE_VALIDATION_CONFIGURATION_ON_DEVICE_CONFIGURATION})
    public ManufacturerInfos getAllManufacturers() {
        ManufacturerInfos manufacturerInfos = new ManufacturerInfos();
        manufacturerInfos.manufacturerInfos = new ArrayList<>();
        for (Manufacturer manufacturer : Manufacturer.values()) {
            manufacturerInfos.manufacturerInfos.add(new ManufacturerInfo(manufacturer));
        }
        return manufacturerInfos;
    }

}