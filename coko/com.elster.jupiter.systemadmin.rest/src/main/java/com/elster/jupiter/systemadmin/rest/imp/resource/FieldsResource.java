package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.systemadmin.rest.imp.response.ApplicationInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.ApplicationInfoFactory;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/fields")
public class FieldsResource {
    private ApplicationInfoFactory applicationInfoFactory;
    private SubsystemService subsystemService;

    @Inject
    public FieldsResource(ApplicationInfoFactory applicationInfoFactory, SubsystemService subsystemService) {
        this.applicationInfoFactory = applicationInfoFactory;
        this.subsystemService = subsystemService;
    }

    @GET
    @Path("/applications")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public PagedInfoList getSystemInformation(@BeanParam JsonQueryParameters queryParams) {
        List<ApplicationInfo> infos = subsystemService.getSubsystems().stream().map(applicationInfoFactory::asInfo).sorted((o1, o2) -> o1.name.compareTo(o2.name)).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("applications", infos, queryParams);
    }
}
