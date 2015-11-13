package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.system.security.Privileges;
import com.elster.jupiter.systemadmin.rest.imp.response.ComponentInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.ComponentInfoFactory;
import org.osgi.framework.BundleContext;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/components")
public class ComponentResource {
    private ComponentInfoFactory componentInfoFactory;
    private SubsystemService subsystemService;
    private BundleContext bundleContext;

    @Inject
    public ComponentResource(ComponentInfoFactory componentInfoFactory, SubsystemService subsystemService, BundleContext bundleContext) {
        this.componentInfoFactory = componentInfoFactory;
        this.subsystemService = subsystemService;
        this.bundleContext = bundleContext;
    }

    @GET
    @RolesAllowed(Privileges.Constants.VIEW_DEPLOYMENT_INFORMATION)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public PagedInfoList getSystemInformation(@BeanParam JsonQueryParameters queryParams, @BeanParam JsonQueryFilter filter) {
        List<ComponentInfo> components = subsystemService.getComponents(bundleContext).
                stream().
                filter(runtimeComponent -> {
                    boolean statusFilter = true;
                    boolean applicationFilter = true;
                    boolean bundleTypeFilter = true;
                    if (filter.hasProperty("status")) {
                        statusFilter = filter.getStringList("status").contains(runtimeComponent.getStatus().getId());
                    }
                    if (filter.hasProperty("application")) {
                        applicationFilter = filter.getStringList("application").contains(runtimeComponent.getComponent().getSubsystem().getId());
                    }
                    if (filter.hasProperty("bundleType")) {
                        bundleTypeFilter = filter.getStringList("bundleType").contains(runtimeComponent.getComponent().getBundleType().getId());
                    }
                    return statusFilter && applicationFilter && bundleTypeFilter;
                }).
                map(componentInfoFactory::asInfo).
                sorted((o1, o2) -> {
                    int result = o1.application.compareTo(o2.application);
                    if (result != 0) {
                        return result;
                    }
                    result = o1.bundleType.compareTo(o2.bundleType);
                    if (result != 0) {
                        return result;
                    }
                    result = o1.name.compareTo(o2.name);
                    return (result != 0) ? result : 0;
                }).
                collect(Collectors.toList());
        return PagedInfoList.fromPagedList("components", components, queryParams);
    }
}
