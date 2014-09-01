package com.energyict.mdc.masterdata.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.masterdata.rest.EndDeviceEventTypeInfo.EndDeviceDomainInfo;

@Path("/enddevicedomains")
public class EndDeviceDomainResource {
    
    private final Thesaurus thesaurus;
    
    @Inject
    public EndDeviceDomainResource(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllEndDeviceDomains(@BeanParam QueryParameters queryParameters) {
        List<EndDeviceDomainInfo> infos = new ArrayList<>();
        for (EndDeviceDomain domain : EndDeviceDomain.values()) {
            infos.add(EndDeviceDomainInfo.from(domain, thesaurus));
        }
        return PagedInfoList.asJson("data", infos, queryParameters);
    }
}
