package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import java.util.List;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@DeviceStatesRestricted(value = {DefaultState.DECOMMISSIONED}, methods = {HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE})
public class ProtocolDialectResource {

    private final ResourceHelper resourceHelper;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public ProtocolDialectResource(ResourceHelper resourceHelper, MdcPropertyUtils mdcPropertyUtils) {
        this.resourceHelper = resourceHelper;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getProtocolDialects(@PathParam("mRID") String mRID, @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<ProtocolDialectConfigurationProperties> pagedDialectProtocols = ListPager.of(device.getProtocolDialects(), new ProtocolDialectComparator()).from(queryParameters).find();
        List<ProtocolDialectInfo> protocolDialectInfos = ProtocolDialectInfo.from(device, pagedDialectProtocols, uriInfo, mdcPropertyUtils);
        return PagedInfoList.fromPagedList("protocolDialects", protocolDialectInfos, queryParameters);
    }

    @GET
    @Path("/{protocolDialectId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public ProtocolDialectInfo getProtocolDialects(@PathParam("mRID") String mRID, @PathParam("protocolDialectId") long protocolDialectId, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        ProtocolDialectConfigurationProperties protocolDialect = findProtocolDialectOrThrowException(mRID, protocolDialectId);
        Optional<ProtocolDialectProperties> protocolDialectProperties = device.getProtocolDialectProperties(protocolDialect.getDeviceProtocolDialectName());
        return ProtocolDialectInfo.from(protocolDialect, protocolDialectProperties , uriInfo, mdcPropertyUtils);
    }

    @PUT
    @Path("/{protocolDialectId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION)
    public ProtocolDialectInfo updateProtocolDialect(@PathParam("mRID") String mRID,
                                                      @PathParam("protocolDialectId") long protocolDialectId,
                                                      @Context UriInfo uriInfo,
                                                      ProtocolDialectInfo protocolDialectInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        ProtocolDialectConfigurationProperties protocolDialect = findProtocolDialectOrThrowException(mRID, protocolDialectId);
        updateProperties(protocolDialectInfo, protocolDialect, device);
        device.save();
        return ProtocolDialectInfo.from(protocolDialect, device.getProtocolDialectProperties(protocolDialect.getDeviceProtocolDialectName()), uriInfo, mdcPropertyUtils);
    }


    private ProtocolDialectConfigurationProperties findProtocolDialectOrThrowException(String mRID, long protocolDialectId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        for (ProtocolDialectConfigurationProperties protocolDialectProperty : device.getProtocolDialects()) {
            if (protocolDialectProperty.getId() == protocolDialectId) {
                return protocolDialectProperty;
            }
        }
        throw new WebApplicationException("No such protocol dialect for the device", Response.status(Response.Status.NOT_FOUND).entity("No such protocol dialect for the device").build());
    }

    /**
     * Add new properties, update existing and remove properties no longer listed.
     * Converts String values to correct type
     * Discards properties if there is no matching propertySpec
     */
    private void updateProperties(ProtocolDialectInfo protocolDialectInfo, ProtocolDialectConfigurationProperties protocolDialectProperties, Device device) {
        if (protocolDialectInfo.properties != null) {
            for (PropertySpec propertySpec : protocolDialectProperties.getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, protocolDialectInfo.properties);
                if (propertyValue != null) {
                    device.setProtocolDialectProperty(protocolDialectProperties.getDeviceProtocolDialectName(), propertySpec.getName(), propertyValue);
                } else {
                    device.removeProtocolDialectProperty(protocolDialectProperties.getDeviceProtocolDialectName(), propertySpec.getName());
                }
            }
        }
    }

}
