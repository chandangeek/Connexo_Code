package com.energyict.mdc.rest.impl;

import com.energyict.mdc.services.LicensedProtocolService;
import com.energyict.mdw.core.LicensedProtocol;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 09:46
 */
@Path("/licensedprotocols")
public class LicensedProtocolResource {

    private final LicensedProtocolService licensedProtocolService;

    public LicensedProtocolResource(@Context Application application) {
        licensedProtocolService = ((MdcApplication) ((ResourceConfig) application).getApplication()).getLicensedProtocolService();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public LicensedProtocolsInfo getLicensedProtocolInfos(){
        LicensedProtocolsInfo licensedProtocolsInfo = new LicensedProtocolsInfo();
        for (LicensedProtocol licensedProtocol : this.licensedProtocolService.getAllLicensedProtocols()) {
            licensedProtocolsInfo.licensedProtocolInfos.add(new LicensedProtocolInfo(licensedProtocol));
        }
        return licensedProtocolsInfo;
    }
}
