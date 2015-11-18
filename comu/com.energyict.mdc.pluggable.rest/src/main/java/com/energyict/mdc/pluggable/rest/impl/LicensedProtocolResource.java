package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

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
    private ProtocolPluggableService protocolPluggableService;

    public LicensedProtocolResource() {
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public LicensedProtocolsInfo getLicensedProtocolInfos() {
        LicensedProtocolsInfo licensedProtocolsInfo = new LicensedProtocolsInfo();
        for (LicensedProtocol licensedProtocol : this.protocolPluggableService.getAllLicensedProtocols()) {
            licensedProtocolsInfo.licensedProtocolInfos.add(new LicensedProtocolInfo(licensedProtocol));
        }
        return licensedProtocolsInfo;
    }

}