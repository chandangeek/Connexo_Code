package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.ProtocolDialectProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
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
import java.util.List;

public class ProtocolDialectResource {

    private final DeviceDataService deviceDataService;
    private final ResourceHelper resourceHelper;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public ProtocolDialectResource(DeviceDataService deviceDataService, ResourceHelper resourceHelper, MdcPropertyUtils mdcPropertyUtils) {
        this.deviceDataService = deviceDataService;
        this.resourceHelper = resourceHelper;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getProtocolDialects(@PathParam("mRID") String mRID, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<ProtocolDialectConfigurationProperties> pagedDialectProtocols = ListPager.of(device.getProtocolDialects(), new ProtocolDialectComparator()).from(queryParameters).find();
        TypedProperties typedProperties = device.getDeviceProtocolProperties();
        List<ProtocolDialectInfo> protocolDialectInfos = ProtocolDialectInfo.from(pagedDialectProtocols, typedProperties,uriInfo, mdcPropertyUtils);
        return PagedInfoList.asJson("protocolDialects", protocolDialectInfos, queryParameters);
    }

    @GET
    @Path("/{protocolDialectId}")
    @Produces(MediaType.APPLICATION_JSON)
    public ProtocolDialectInfo getProtocolDialects(@PathParam("mRID") String mRID, @PathParam("protocolDialectId") long protocolDialectId, @Context UriInfo uriInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        TypedProperties typedProperties = device.getDeviceProtocolProperties();
        return ProtocolDialectInfo.from(findProtocolDialectOrThrowException(mRID, protocolDialectId),typedProperties , uriInfo, mdcPropertyUtils);
    }

    @PUT
    @Path("/{protocolDialectId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ProtocolDialectInfo updateProtocolDialect(@PathParam("mRID") String mRID,
                                                      @PathParam("protocolDialectId") long protocolDialectId,
                                                      @Context UriInfo uriInfo,
                                                      ProtocolDialectInfo protocolDialectInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        ProtocolDialectConfigurationProperties protocolDialect = findProtocolDialectOrThrowException(mRID, protocolDialectId);
        updateProperties(protocolDialectInfo, protocolDialect, device);
       // protocolDialect.save();
        return protocolDialectInfo.from(protocolDialect, device.getDeviceProtocolProperties(), uriInfo, mdcPropertyUtils);
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
     * Add new properties, update existing and remove properties no longer listed
     * Converts String values to correct type
     * Discards properties if there is no matching propertySpec
     */
    private void updateProperties(ProtocolDialectInfo protocolDialectInfo, ProtocolDialectConfigurationProperties protocolDialectProperties, Device device) {
        if (protocolDialectInfo.properties != null) {
            for (PropertySpec<?> propertySpec : protocolDialectProperties.getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, protocolDialectInfo.properties);
                if (propertyValue != null) {
                    device.setProperty(propertySpec.getName(), propertyValue);
                    //protocolDialectProperties.setProperty(propertySpec.getName(), propertyValue);
                } else {
                    device.removeProperty(propertySpec.getName());
                    //protocolDialectProperties.removeProperty(propertySpec.getName());
                }
            }
        }
    }

}
