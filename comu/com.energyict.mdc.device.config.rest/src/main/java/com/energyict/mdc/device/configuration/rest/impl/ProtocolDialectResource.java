package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.common.rest.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;

public class ProtocolDialectResource {

    private final ResourceHelper resourceHelper;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public ProtocolDialectResource(ResourceHelper resourceHelper, MdcPropertyUtils mdcPropertyUtils) {
        this.resourceHelper = resourceHelper;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getProtocolDialects(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        List<ProtocolDialectConfigurationProperties> pagedDialectProtocols = ListPager.of(deviceConfiguration.getProtocolDialectConfigurationPropertiesList(), new ProtocolDialectComparator()).from(queryParameters).find();
        List<ProtocolDialectInfo> protocolDialectInfos = ProtocolDialectInfo.from(pagedDialectProtocols, uriInfo, mdcPropertyUtils);
        return PagedInfoList.fromPagedList("protocolDialects", protocolDialectInfos, queryParameters);
    }

    @GET @Transactional
    @Path("/{protocolDialectId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public ProtocolDialectInfo getProtocolDialects(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("protocolDialectId") long protocolDialectId, @Context UriInfo uriInfo) {
        ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties = resourceHelper.findProtocolDialectConfigurationPropertiesByIdOrThrowException(protocolDialectId);
        return ProtocolDialectInfo.from(protocolDialectConfigurationProperties, uriInfo, mdcPropertyUtils);
    }

    @PUT @Transactional
    @Path("/{protocolDialectId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public ProtocolDialectInfo updateConnectionMethod(@PathParam("deviceTypeId") long deviceTypeId,
                                                          @PathParam("deviceConfigurationId") long deviceConfigurationId,
                                                          @PathParam("protocolDialectId") long protocolDialectId,
                                                          @Context UriInfo uriInfo,
                                                          ProtocolDialectInfo protocolDialectInfo) {
        ProtocolDialectConfigurationProperties protocolDialect = resourceHelper.lockProtocolDialectConfigurationPropertiesOrThrowException(protocolDialectInfo);
        updateProperties(protocolDialectInfo, protocolDialect);
        protocolDialect.save();
        return protocolDialectInfo.from(protocolDialect, uriInfo, mdcPropertyUtils);
    }


    /**
     * Add new properties, update existing and remove properties no longer listed
     * Converts String values to correct type
     * Discards properties if there is no matching propertySpec
     */
    private void updateProperties(ProtocolDialectInfo protocolDialectInfo, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
        if (protocolDialectInfo.properties != null) {
            for (PropertySpec propertySpec : protocolDialectConfigurationProperties.getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, protocolDialectInfo.properties);
                if (propertyValue != null) {
                    protocolDialectConfigurationProperties.setProperty(propertySpec.getName(), propertyValue);
                } else {
                    protocolDialectConfigurationProperties.removeProperty(propertySpec.getName());
                }
            }
        }
    }
}
