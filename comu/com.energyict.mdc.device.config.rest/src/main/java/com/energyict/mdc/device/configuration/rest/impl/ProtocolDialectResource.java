package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

public class ProtocolDialectResource {

    private final DeviceConfigurationService deviceConfigurationService;
    private final ResourceHelper resourceHelper;

    @Inject
    public ProtocolDialectResource(DeviceConfigurationService deviceConfigurationService, ResourceHelper resourceHelper) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getProtocolDialects(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<ProtocolDialectConfigurationProperties> pagedDialectProtocols = ListPager.of(deviceConfiguration.getProtocolDialectConfigurationPropertiesList(), new ProtocolDialectComparator()).from(queryParameters).find();
        List<ProtocolDialectInfo> protocolDialectInfos = ProtocolDialectInfo.from(pagedDialectProtocols, uriInfo);
        return PagedInfoList.asJson("protocolDialects", protocolDialectInfos, queryParameters);
    }

    @GET
    @Path("/{protocolDialectId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProtocolDialectInfo getProtocolDialects(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("protocolDialectId") long protocolDialectId, @Context UriInfo uriInfo) {
        return ProtocolDialectInfo.from(findProtocolDialectOrThrowException(deviceTypeId, deviceConfigurationId, protocolDialectId), uriInfo);
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
        return protocolDialectInfo.from(protocolDialect, uriInfo);
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
                Object propertyValue = MdcPropertyUtils.findPropertyValue(propertySpec, protocolDialectInfo.properties);
                if (propertyValue != null) {
                    protocolDialectConfigurationProperties.setProperty(propertySpec.getName(), propertyValue);
                } else {
                    protocolDialectConfigurationProperties.removeProperty(propertySpec.getName());
                }
            }
        }
    }

}
