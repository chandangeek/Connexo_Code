package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class SecurityPropertySetResource {

    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;

    @Inject
    public SecurityPropertySetResource(ResourceHelper resourceHelper, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_SECURITY_PROPERTY_SET)
    public PagedInfoList getSecurityPropertySets(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam QueryParameters queryParameters, @Context UriInfo uriInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<SecurityPropertySetInfo> securityPropertySetInfos = SecurityPropertySetInfo.from(ListPager.of(deviceConfiguration.getSecurityPropertySets(), new SecurityPropertySetComparator()).find(), thesaurus);

        return PagedInfoList.asJson("data", securityPropertySetInfos, queryParameters);
    }

    @GET
    @Path("/{securityPropertySetId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_SECURITY_PROPERTY_SET)
    public Response getSecurityPropertySet(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("securityPropertySetId") long securityPropertySetId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        SecurityPropertySet securityPropertySet = findSecurityPropertySetByIdOrThrowException(deviceConfiguration, securityPropertySetId);

        return Response.status(Response.Status.OK).entity(SecurityPropertySetInfo.from(securityPropertySet, thesaurus)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.CREATE_SECURITY_PROPERTY_SET)
    public Response createSecurityPropertySet(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, SecurityPropertySetInfo securityPropertySetInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);

        if (securityPropertySetInfo.authenticationLevelId == null) {
            securityPropertySetInfo.authenticationLevelId = -1;
        }
        if (securityPropertySetInfo.encryptionLevelId == null) {
            securityPropertySetInfo.encryptionLevelId = -1;
        }

        SecurityPropertySet securityPropertySet = deviceConfiguration.createSecurityPropertySet(securityPropertySetInfo.name)
                                                  .authenticationLevel(securityPropertySetInfo.authenticationLevelId)
                                                  .encryptionLevel(securityPropertySetInfo.encryptionLevelId)
                                                  .build();

        return Response.status(Response.Status.CREATED).entity(SecurityPropertySetInfo.from(securityPropertySet, thesaurus)).build();
    }

    @PUT
    @Path("/{securityPropertySetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.UPDATE_SECURITY_PROPERTY_SET)
    public Response updateSecurityPropertySet(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("securityPropertySetId") long securityPropertySetId, SecurityPropertySetInfo securityPropertySetInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        SecurityPropertySet securityPropertySet = findSecurityPropertySetByIdOrThrowException(deviceConfiguration, securityPropertySetId);
        securityPropertySetInfo.writeTo(securityPropertySet);
        securityPropertySet.update();

        return Response.status(Response.Status.OK).entity(SecurityPropertySetInfo.from(securityPropertySet, thesaurus)).build();
    }

    @DELETE
    @Path("/{securityPropertySetId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.DELETE_SECURITY_PROPERTY_SET)
    public Response deleteSecurityPropertySet(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("securityPropertySetId") long securityPropertySetId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        SecurityPropertySet securityPropertySet = findSecurityPropertySetByIdOrThrowException(deviceConfiguration, securityPropertySetId);
        deviceConfiguration.removeSecurityPropertySet(securityPropertySet);

        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/authlevels")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_SECURITY_PROPERTY_SET)
    public PagedInfoList getSecurityPropertySetAuthLevels(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        DeviceProtocolPluggableClass protocolPluggableClass = deviceType.getDeviceProtocolPluggableClass();
        DeviceProtocol deviceProtocol = protocolPluggableClass.getDeviceProtocol();
        List<AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels = deviceProtocol.getAuthenticationAccessLevels();
        List<SecurityLevelInfo> securityLevelInfos = SecurityLevelInfo.from(authenticationDeviceAccessLevels, thesaurus);

        return PagedInfoList.asJson("data", securityLevelInfos, queryParameters);
    }

    @GET
    @Path("/enclevels")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.VIEW_SECURITY_PROPERTY_SET)
    public PagedInfoList getSecurityPropertySetEncLevels(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        DeviceProtocolPluggableClass protocolPluggableClass = deviceType.getDeviceProtocolPluggableClass();
        DeviceProtocol deviceProtocol = protocolPluggableClass.getDeviceProtocol();
        List<EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels = deviceProtocol.getEncryptionAccessLevels();
        List<SecurityLevelInfo> securityLevelInfos = SecurityLevelInfo.from(encryptionDeviceAccessLevels, thesaurus);

        return PagedInfoList.asJson("data", securityLevelInfos, queryParameters);
    }

    private SecurityPropertySet findSecurityPropertySetByIdOrThrowException(DeviceConfiguration deviceConfiguration, long securityPropertySetId) {
        for(SecurityPropertySet securityPropertySet : deviceConfiguration.getSecurityPropertySets()) {
            if(securityPropertySet.getId() == securityPropertySetId) {
                return securityPropertySet;
            }
        }

        throw new WebApplicationException("No such security property set for the device configuration", Response.status(Response.Status.NOT_FOUND).entity("No such security property set for the device configuration").build());
    }
}
