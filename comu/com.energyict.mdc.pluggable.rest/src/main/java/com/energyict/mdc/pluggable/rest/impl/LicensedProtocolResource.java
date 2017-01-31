/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.tasks.support.DeviceDescriptionSupport;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/licensedprotocols")
public class LicensedProtocolResource {

    @Inject
    private ProtocolPluggableService protocolPluggableService;

    public LicensedProtocolResource() {
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA,
            com.energyict.mdc.engine.config.security.Privileges.Constants.VIEW_COMMUNICATION_ADMINISTRATION,
            com.energyict.mdc.engine.config.security.Privileges.Constants.ADMINISTRATE_COMMUNICATION_ADMINISTRATION,
            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE,
            com.energyict.mdc.device.config.security.Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public LicensedProtocolsInfo getLicensedProtocolInfos() {
        LicensedProtocolsInfo licensedProtocolsInfo = new LicensedProtocolsInfo();
        for (LicensedProtocol licensedProtocol : this.protocolPluggableService.getAllLicensedProtocols()) {
            Object protocol = protocolPluggableService.createProtocol(licensedProtocol.getClassName());
            String description;
            if (protocol instanceof DeviceDescriptionSupport) {
                description = ((DeviceDescriptionSupport) protocol).getProtocolDescription();
            } else {
                description = licensedProtocol.getName();
            }

            licensedProtocolsInfo.licensedProtocolInfos.add(new LicensedProtocolInfo(licensedProtocol, description));
        }
        return licensedProtocolsInfo;
    }

}