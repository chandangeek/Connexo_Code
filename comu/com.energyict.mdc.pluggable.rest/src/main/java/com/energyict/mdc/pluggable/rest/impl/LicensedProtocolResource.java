package com.energyict.mdc.pluggable.rest.impl;

import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.google.common.base.Optional;

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
    private  LicenseService licenseService;
    @Inject
    private LicensedProtocolService licensedProtocolService;

    public LicensedProtocolResource() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public LicensedProtocolsInfo getLicensedProtocolInfos(){
        LicensedProtocolsInfo licensedProtocolsInfo = new LicensedProtocolsInfo();
        Optional<License> licenseForApplicationMdc = licenseService.getLicenseForApplication("MDC");
        if(licenseForApplicationMdc.isPresent()){
            for (LicensedProtocol licensedProtocol : this.licensedProtocolService.getAllLicensedProtocols(licenseForApplicationMdc.get())) {
                licensedProtocolsInfo.licensedProtocolInfos.add(new LicensedProtocolInfo(licensedProtocol));
            }
        }
        return licensedProtocolsInfo;
    }

}