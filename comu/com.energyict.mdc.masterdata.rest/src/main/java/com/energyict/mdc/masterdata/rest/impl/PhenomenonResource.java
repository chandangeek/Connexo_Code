package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.rest.PhenomenonInfo;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/phenomena")
public class PhenomenonResource {
    private final MasterDataService masterDataService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Thesaurus thesaurus;

    @Inject
    public PhenomenonResource(MasterDataService masterDataService, DeviceConfigurationService deviceConfigurationService, Thesaurus thesaurus) {
        this.masterDataService = masterDataService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.thesaurus = thesaurus;
    }

    @GET
    public Response getAllPhenomenons(@BeanParam QueryParameters queryParameters){
        List<Phenomenon> phenomenons = masterDataService.findAllPhenomena();
        return Response.ok(PagedInfoList.asJson("data", PhenomenonInfo.from(phenomenons), queryParameters)).build();
    }
}
