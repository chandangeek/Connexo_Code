package com.energyict.mdc.masterdata.rest.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.elster.jupiter.cbo.EndDeviceEventorAction;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.masterdata.rest.EndDeviceEventTypeInfo.EndDeviceEventOrActionInfo;

@Path("/enddeviceeventoractions")
public class EndDeviceEventOrActionResource {
    
    private final Thesaurus thesaurus;
    
    @Inject
    public EndDeviceEventOrActionResource(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getAllEndDeviceSubDomains(@BeanParam QueryParameters queryParameters) {
        List<EndDeviceEventOrActionInfo> infos = new ArrayList<>();
        for (EndDeviceEventorAction eventOrAction : EndDeviceEventorAction.values()) {
            infos.add(EndDeviceEventOrActionInfo.from(eventOrAction, thesaurus));
        }
        return PagedInfoList.asJson("data", infos, queryParameters);
    }
}
