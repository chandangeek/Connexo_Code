package com.energyict.mdc.masterdata.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.elster.jupiter.cbo.EndDeviceSubDomain;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.masterdata.rest.EndDeviceEventTypeInfo.EndDeviceSubDomainInfo;

@Path("/enddevicesubdomains")
public class EndDeviceSubDomainResource {

    private final Thesaurus thesaurus;
    
    @Inject
    public EndDeviceSubDomainResource(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllEndDeviceSubDomains(@BeanParam QueryParameters queryParameters) {
        List<EndDeviceSubDomainInfo> infos = new ArrayList<>();
        for (EndDeviceSubDomain subDomain : EndDeviceSubDomain.values()) {
            infos.add(EndDeviceSubDomainInfo.from(subDomain, thesaurus));
        }
        return PagedInfoList.asJson("data", infos, queryParameters);
    }
}
