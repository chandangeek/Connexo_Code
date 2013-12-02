package com.energyict.mdc.rest.impl;

import com.energyict.mdc.services.LicensedProtocolService;
import com.energyict.mdw.core.LicensedProtocol;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 09:46
 */
@Path("/licensedprotocols")
public class LicensedProtocolResource {

    @Inject
    private LicensedProtocolService licensedProtocolService;

    public LicensedProtocolResource() {
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
