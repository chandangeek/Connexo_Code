package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class ProtocolDialectResource {

    private final DeviceConfigurationService deviceConfigurationService;
    private final ResourceHelper resourceHelper;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public ProtocolDialectResource(DeviceConfigurationService deviceConfigurationService, ResourceHelper resourceHelper, MdcPropertyUtils mdcPropertyUtils) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.resourceHelper = resourceHelper;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getProtocolDialects(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<ProtocolDialectConfigurationProperties> pagedDialectProtocols = ListPager.of(deviceConfiguration.getProtocolDialectConfigurationPropertiesList(), new ProtocolDialectComparator()).from(queryParameters).find();
        List<ProtocolDialectInfo> protocolDialectInfos = ProtocolDialectInfo.from(pagedDialectProtocols, uriInfo, mdcPropertyUtils);
        return PagedInfoList.asJson("protocolDialects", protocolDialectInfos, queryParameters);
    }

    @GET
    @Path("/{protocolDialectId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProtocolDialectInfo getProtocolDialects(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("protocolDialectId") long protocolDialectId, @Context UriInfo uriInfo) {
        return ProtocolDialectInfo.from(findProtocolDialectOrThrowException(deviceTypeId, deviceConfigurationId, protocolDialectId), uriInfo, mdcPropertyUtils);
    }

    @PUT
    @Path("/{protocolDialectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ProtocolDialectInfo updateConnectionMethod(@PathParam("deviceTypeId") long deviceTypeId,
                                                          @PathParam("deviceConfigurationId") long deviceConfigurationId,
                                                          @PathParam("protocolDialectId") long protocolDialectId,
                                                          @Context UriInfo uriInfo,
                                                          ProtocolDialectInfo protocolDialectInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        ProtocolDialectConfigurationProperties protocolDialect = findProtocolDialectOrThrowException(deviceTypeId, deviceConfigurationId, protocolDialectId);
        updateProperties(protocolDialectInfo, protocolDialect);
        protocolDialect.save();
        return protocolDialectInfo.from(protocolDialect, uriInfo, mdcPropertyUtils);
    }


    private ProtocolDialectConfigurationProperties findProtocolDialectOrThrowException(long deviceTypeId, long deviceConfigId, long protocolDialectId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigId);
        for (ProtocolDialectConfigurationProperties protocolDialectConfigurationProperty : deviceConfiguration.getProtocolDialectConfigurationPropertiesList()) {
            if (protocolDialectConfigurationProperty.getId() == protocolDialectId) {
                return protocolDialectConfigurationProperty;
            }
        }
        throw new WebApplicationException("No such protocol dialect for the device configuration", Response.status(Response.Status.NOT_FOUND).entity("No such protocol dialect for the device configuration").build());
    }

    /**
     * Add new properties, update existing and remove properties no longer listed
     * Converts String values to correct type
     * Discards properties if there is no matching propertySpec
     */
    private void updateProperties(ProtocolDialectInfo protocolDialectInfo, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
        if (protocolDialectInfo.properties != null) {
            for (PropertySpec<?> propertySpec : protocolDialectConfigurationProperties.getPropertySpecs()) {
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
