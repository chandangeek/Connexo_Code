/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.resource;

import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.system.RuntimeComponent;
import com.elster.jupiter.system.SubsystemService;
import com.elster.jupiter.system.security.Privileges;
import com.elster.jupiter.systemadmin.rest.imp.response.ComponentInfo;
import com.elster.jupiter.systemadmin.rest.imp.response.ComponentInfoFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/components")
public class ComponentResource {
    private ComponentInfoFactory componentInfoFactory;
    private SubsystemService subsystemService;

    @Inject
    public ComponentResource(ComponentInfoFactory componentInfoFactory, SubsystemService subsystemService) {
        this.componentInfoFactory = componentInfoFactory;
        this.subsystemService = subsystemService;
    }

    @GET
    @RolesAllowed(Privileges.Constants.VIEW_DEPLOYMENT_INFORMATION)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getComponentsList(@BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParams) {
        Stream<RuntimeComponent> stream = subsystemService.getRuntimeComponents().stream();
        Optional<Predicate<RuntimeComponent>> filterByApplication = buildFilterByApplication(filter);
        if (filterByApplication.isPresent()) {
            stream = stream.filter(filterByApplication.get());
        }
        Optional<Predicate<RuntimeComponent>> filterByBundleType = buildFilterByBundleType(filter);
        if (filterByBundleType.isPresent()) {
            stream = stream.filter(filterByBundleType.get());
        }
        Optional<Predicate<RuntimeComponent>> filterByBundleStatus = buildFilterByBundleStatus(filter);
        if (filterByBundleStatus.isPresent()) {
            stream = stream.filter(filterByBundleStatus.get());
        }
        List<ComponentInfo> components = stream.map(componentInfoFactory::asInfo).sorted(this::comparator).collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("components", components, queryParams);
    }

    private Optional<Predicate<RuntimeComponent>> buildFilterByApplication(JsonQueryFilter filter) {
        if (filter.hasProperty("application")) {
            List<String> applications = filter.getStringList("application");
            return Optional.of(rc -> applications.contains(rc.getSubsystem().getId()));
        }
        return Optional.empty();
    }

    private Optional<Predicate<RuntimeComponent>> buildFilterByBundleType(JsonQueryFilter filter) {
        if (filter.hasProperty("bundleType")) {
            List<String> bundleTypes = filter.getStringList("bundleType");
            return Optional.of(rc -> bundleTypes.contains(rc.getComponent().getBundleType().getId()));
        }
        return Optional.empty();
    }

    private Optional<Predicate<RuntimeComponent>> buildFilterByBundleStatus(JsonQueryFilter filter) {
        if (filter.hasProperty("status")) {
            List<String> bundleTypes = filter.getStringList("status");
            return Optional.of(rc -> bundleTypes.contains(rc.getStatus().getId()));
        }
        return Optional.empty();
    }

    private int comparator(ComponentInfo rc1, ComponentInfo rc2) {
        int byApplication = rc1.application.compareTo(rc2.application);
        if (byApplication != 0) {
            return byApplication;
        }
        int byBundleType = rc1.bundleType.compareTo(rc2.bundleType);
        if (byBundleType != 0) {
            return byBundleType;
        }
        return rc1.name.compareTo(rc2.name);
    }
}
