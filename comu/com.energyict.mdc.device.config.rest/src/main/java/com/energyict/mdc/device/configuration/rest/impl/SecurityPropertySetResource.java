/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.device.configuration.rest.SecurityLevelInfo;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.RequestSecurityLevel;
import com.energyict.mdc.protocol.api.security.ResponseSecurityLevel;
import com.energyict.mdc.protocol.api.security.SecuritySuite;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities;

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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class SecurityPropertySetResource {

    private final ResourceHelper resourceHelper;
    private final UserService userService;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final SecurityPropertySetInfoFactory securityPropertySetInfoFactory;
    private final ProtocolPluggableService protocolPluggableService;

    @Inject
    public SecurityPropertySetResource(ResourceHelper resourceHelper, UserService userService, MdcPropertyUtils mdcPropertyUtils, SecurityPropertySetInfoFactory securityPropertySetInfoFactory, ProtocolPluggableService protocolPluggableService) {
        this.resourceHelper = resourceHelper;
        this.userService = userService;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.securityPropertySetInfoFactory = securityPropertySetInfoFactory;
        this.protocolPluggableService = protocolPluggableService;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getSecurityPropertySets(@PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam JsonQueryParameters queryParameters) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        List<Group> groups = this.userService.getGroups();
        List<SecurityPropertySetInfo> securityPropertySetInfos =
                ListPager.of(deviceConfiguration.getSecurityPropertySets(), new SecurityPropertySetComparator())
                        .from(queryParameters)
                        .find()
                        .stream()
                        .map(set -> securityPropertySetInfoFactory.from(set, groups))
                        .collect(toList());

        return PagedInfoList.fromPagedList("data", securityPropertySetInfos, queryParameters);
    }

    @GET
    @Transactional
    @Path("/{securityPropertySetId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getSecurityPropertySet(@PathParam("securityPropertySetId") long securityPropertySetId) {
        SecurityPropertySet securityPropertySet = resourceHelper.findSecurityPropertySetByIdOrThrowException(securityPropertySetId);

        List<Group> groups = this.userService.getGroups();
        return Response.status(Response.Status.OK).entity(securityPropertySetInfoFactory.from(securityPropertySet, groups)).build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response createSecurityPropertySet(@PathParam("deviceConfigurationId") long deviceConfigurationId, SecurityPropertySetInfo info) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);

        if (info.authenticationLevelId == null) {
            info.authenticationLevelId = DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }
        if (info.encryptionLevelId == null) {
            info.encryptionLevelId = DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }
        if (info.securitySuiteId == null) {
            info.securitySuiteId = DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }
        if (info.requestSecurityLevelId == null) {
            info.requestSecurityLevelId = DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }
        if (info.responseSecurityLevelId == null) {
            info.responseSecurityLevelId = DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID;
        }
        Optional<PropertySpec> clientPropertySpec = getClientPropertySpec(deviceConfiguration.getDeviceType());
        SecurityPropertySetBuilder builder = deviceConfiguration
                .createSecurityPropertySet(info.name)
                .client(info.client != null && clientPropertySpec.isPresent() ? mdcPropertyUtils.findPropertyValue(clientPropertySpec.get(), Collections.singletonList(info.client)) : null)
                .authenticationLevel(info.authenticationLevelId)
                .encryptionLevel(info.encryptionLevelId)
                .securitySuite(info.securitySuiteId)
                .requestSecurityLevel(info.requestSecurityLevelId)
                .responseSecurityLevel(info.responseSecurityLevelId);
        if (info.properties != null && !info.properties.isEmpty()) {
            for (PropertySpec propertySpec : builder.getPropertySpecs()) {
                SecurityAccessorType newKeyAccessor = (SecurityAccessorType) mdcPropertyUtils.findPropertyValue(propertySpec, info.properties);   // Cast to KeyAccessorType should work fine
                if (newKeyAccessor != null) {                                                                                           // unless front-end has send wrong data, but then it's ok to throw an error
                    builder.addConfigurationSecurityProperty(propertySpec.getName(), newKeyAccessor);
                }
            }
        }

        SecurityPropertySet securityPropertySet = builder.build();
        List<Group> groups = this.userService.getGroups();
        return Response.status(Response.Status.CREATED).entity(securityPropertySetInfoFactory.from(securityPropertySet, groups)).build();
    }

    @PUT
    @Transactional
    @Path("/{securityPropertySetId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response updateSecurityPropertySet(@PathParam("deviceConfigurationId") long deviceConfigurationId, @PathParam("securityPropertySetId") long securityPropertySetId, SecurityPropertySetInfo info) {
        info.id = securityPropertySetId;
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        SecurityPropertySet securityPropertySet = resourceHelper.lockSecurityPropertySetOrThrowException(info);
        Optional<PropertySpec> clientPropertySpec = getClientPropertySpec(deviceConfiguration.getDeviceType());
        Object propertyValue = null;
        if(clientPropertySpec.isPresent()) {
            propertyValue = mdcPropertyUtils.findPropertyValue(clientPropertySpec.get(), Collections.singletonList(info.client));
        }
        info.writeTo(securityPropertySet, propertyValue);

        List<ConfigurationSecurityProperty> configurationSecurityProperties = securityPropertySet.getConfigurationSecurityProperties();
        if (info.properties != null && !info.properties.isEmpty()) {
            for (PropertySpec propertySpec : securityPropertySet.getPropertySpecs()) {
                SecurityAccessorType keyAccessor = (SecurityAccessorType) mdcPropertyUtils.findPropertyValue(propertySpec, info.properties);  // Cast to KeyAccessorType should work fine
                // unless front-end has send wrong data, but then it's ok to throw an error
                Optional<ConfigurationSecurityProperty> existingSecurityProperty = configurationSecurityProperties.stream()
                        .filter(property -> property.getName().equals(propertySpec.getName()))
                        .findFirst();
                if (existingSecurityProperty.isPresent()) {
                    securityPropertySet.updateConfigurationSecurityProperty(propertySpec.getName(), keyAccessor);
                } else if (keyAccessor != null) {
                    securityPropertySet.addConfigurationSecurityProperty(propertySpec.getName(), keyAccessor);
                }
            }
            // Remove the no longer used configuration security properties
            List<ConfigurationSecurityProperty> toRemove = securityPropertySet.getConfigurationSecurityProperties()
                    .stream()
                    .filter(property -> securityPropertySet.getPropertySpecs().stream().noneMatch(spec -> spec.getName().equals(property.getName())))
                    .collect(Collectors.toList());
            toRemove.forEach(property -> securityPropertySet.removeConfigurationSecurityProperty(property.getName()));
        } else {
            // Remove all properties
            securityPropertySet.getConfigurationSecurityProperties().forEach(property -> securityPropertySet.removeConfigurationSecurityProperty(property.getName()));
        }
        securityPropertySet.update();

        List<Group> groups = this.userService.getGroups();
        return Response.status(Response.Status.OK).entity(securityPropertySetInfoFactory.from(securityPropertySet, groups)).build();
    }

    @DELETE
    @Transactional
    @Path("/{securityPropertySetId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteSecurityPropertySet(@PathParam("securityPropertySetId") long securityPropertySetId, SecurityPropertySetInfo info) {
        info.id = securityPropertySetId;
        SecurityPropertySet securityPropertySet = resourceHelper.lockSecurityPropertySetOrThrowException(info);
        securityPropertySet.getDeviceConfiguration().removeSecurityPropertySet(securityPropertySet);

        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Transactional
    @Path("/securitysuites")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getSecurityPropertySetSecuritySuites(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        List<SecurityLevelInfo> securityLevelInfos = deviceType.getDeviceProtocolPluggableClass().map(deviceProtocolPluggableClass ->
                SecurityLevelInfo.from(supportedSecuritySuites(deviceProtocolPluggableClass.getDeviceProtocol()))).orElse(Collections.emptyList());
        return PagedInfoList.fromPagedList("data", securityLevelInfos, queryParameters);
    }

    private List<SecuritySuite> supportedSecuritySuites(DeviceProtocol deviceProtocol) {
        List<SecuritySuite> securitySuites = Collections.emptyList();
        if (isAdvancedSecurity(deviceProtocol)) {
            securitySuites = castToAdvancedSecurity(deviceProtocol).getSecuritySuites()
                    .stream()
                    .map(this.protocolPluggableService::adapt)
                    .collect(Collectors.toList());
        }
        return securitySuites;
    }

    private boolean isAdvancedSecurity(DeviceProtocol deviceProtocol) {
        if (deviceProtocol instanceof UPLProtocolAdapter) {
            return ((UPLProtocolAdapter) deviceProtocol).getActual() instanceof AdvancedDeviceProtocolSecurityCapabilities;
        } else {
            return deviceProtocol instanceof com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities;
        }
    }

    private AdvancedDeviceProtocolSecurityCapabilities castToAdvancedSecurity(DeviceProtocol deviceProtocol) {
        if (deviceProtocol instanceof UPLProtocolAdapter) {
            return (AdvancedDeviceProtocolSecurityCapabilities) ((UPLProtocolAdapter) deviceProtocol).getActual();
        } else {
            return (AdvancedDeviceProtocolSecurityCapabilities) deviceProtocol;
        }
    }

    @GET
    @Transactional
    @Path("/authlevels")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getSecurityPropertySetAuthLevels(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        Optional<Integer> securitySuiteIdOptional = getIdParameterFromUriParams(uriInfo, "securitySuiteId");

        List<AuthenticationDeviceAccessLevel> deviceAccessLevels = deviceType.getDeviceProtocolPluggableClass().map(deviceProtocolPluggableClass -> {
            DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
            List<com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel> authenticationDeviceAccessLevels =
                    (isAdvancedSecurity(deviceProtocol) && securitySuiteIdOptional.isPresent())
                            ? findSecuritySuiteByIdOrThrowException(castToAdvancedSecurity(deviceProtocol), securitySuiteIdOptional.get()).getAuthenticationAccessLevels()
                            : deviceProtocol.getAuthenticationAccessLevels();
            return authenticationDeviceAccessLevels.stream().map(this.protocolPluggableService::adapt).collect(Collectors.toList());
        }).orElse(Collections.emptyList());
        return PagedInfoList.fromPagedList("data", SecurityLevelInfo.from(deviceAccessLevels), queryParameters);
    }

    @GET
    @Transactional
    @Path("/enclevels")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getSecurityPropertySetEncLevels(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        Optional<Integer> securitySuiteIdOptional = getIdParameterFromUriParams(uriInfo, "securitySuiteId");

        List<EncryptionDeviceAccessLevel> deviceAccessLevels = deviceType.getDeviceProtocolPluggableClass().map(deviceProtocolPluggableClass -> {
            DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
            List<com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel> encryptionDeviceAccessLevels =
                    (isAdvancedSecurity(deviceProtocol) && securitySuiteIdOptional.isPresent())
                            ? findSecuritySuiteByIdOrThrowException(castToAdvancedSecurity(deviceProtocol), securitySuiteIdOptional.get()).getEncryptionAccessLevels()
                            : deviceProtocol.getEncryptionAccessLevels();
            return encryptionDeviceAccessLevels.stream().map(this.protocolPluggableService::adapt).collect(Collectors.toList());
        }).orElse(Collections.emptyList());
        return PagedInfoList.fromPagedList("data", SecurityLevelInfo.from(deviceAccessLevels), queryParameters);
    }

    @GET
    @Transactional
    @Path("/reqsecuritylevels")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getSecurityPropertySetRequestSecurityLevels(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        Optional<Integer> securitySuiteIdOptional = getIdParameterFromUriParams(uriInfo, "securitySuiteId");

        List<RequestSecurityLevel> deviceAccessLevels = deviceType.getDeviceProtocolPluggableClass().map(deviceProtocolPluggableClass -> {
            DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
            List<com.energyict.mdc.upl.security.RequestSecurityLevel> requestSecurityLevels =
                    isAdvancedSecurity(deviceProtocol)
                            ? securitySuiteIdOptional.isPresent()
                            ? findSecuritySuiteByIdOrThrowException(castToAdvancedSecurity(deviceProtocol), securitySuiteIdOptional.get()).getRequestSecurityLevels()
                            : castToAdvancedSecurity(deviceProtocol).getRequestSecurityLevels()
                            : Collections.<com.energyict.mdc.upl.security.RequestSecurityLevel>emptyList();
            return requestSecurityLevels.stream().map(this.protocolPluggableService::adapt).collect(Collectors.toList());
        }).orElse(Collections.emptyList());
        return PagedInfoList.fromPagedList("data", SecurityLevelInfo.from(deviceAccessLevels), queryParameters);
    }

    @GET
    @Transactional
    @Path("/respsecuritylevels")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getSecurityPropertySetResponseSecurityLevels(@PathParam("deviceTypeId") long deviceTypeId, @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        Optional<Integer> securitySuiteIdOptional = getIdParameterFromUriParams(uriInfo, "securitySuiteId");

        List<ResponseSecurityLevel> deviceAccessLevels = deviceType.getDeviceProtocolPluggableClass().map(deviceProtocolPluggableClass -> {
            DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
            List<com.energyict.mdc.upl.security.ResponseSecurityLevel> responseSecurityLevels =
                    isAdvancedSecurity(deviceProtocol)
                            ? securitySuiteIdOptional.isPresent()
                            ? findSecuritySuiteByIdOrThrowException(castToAdvancedSecurity(deviceProtocol), securitySuiteIdOptional.get()).getResponseSecurityLevels()
                            : castToAdvancedSecurity(deviceProtocol).getResponseSecurityLevels()
                            : Collections.<com.energyict.mdc.upl.security.ResponseSecurityLevel>emptyList();
            return responseSecurityLevels.stream().map(this.protocolPluggableService::adapt).collect(Collectors.toList());
        }).orElse(Collections.emptyList());
        return PagedInfoList.fromPagedList("data", SecurityLevelInfo.from(deviceAccessLevels), queryParameters);
    }

    @GET
    @Transactional
    @Path("/confsecurityproperties")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getSecurityPropertySetConfigurationSecurityProperties(@PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam JsonQueryParameters queryParameters, @Context UriInfo uriInfo) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        SecurityPropertySetBuilder builder = deviceConfiguration
                .createSecurityPropertySet("builder")
                .authenticationLevel(getIdParameterFromUriParams(uriInfo, "authenticationLevelId").orElse(-1))
                .encryptionLevel(getIdParameterFromUriParams(uriInfo, "encryptionLevelId").orElse(-1))
                .securitySuite(getIdParameterFromUriParams(uriInfo, "securitySuiteId").orElse(-1))
                .requestSecurityLevel(getIdParameterFromUriParams(uriInfo, "requestSecurityLevelId").orElse(-1))
                .responseSecurityLevel(getIdParameterFromUriParams(uriInfo, "responseSecurityLevelId").orElse(-1));
        List<PropertyInfo> propertyInfos = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(builder.getPropertySpecs(), TypedProperties.empty());
        propertyInfos.sort(Comparator.comparing(propertyInfo -> propertyInfo.name));
        return PagedInfoList.fromPagedList("data", propertyInfos, queryParameters);
    }

    private com.energyict.mdc.upl.security.SecuritySuite findSecuritySuiteByIdOrThrowException(AdvancedDeviceProtocolSecurityCapabilities advancedDeviceProtocolSecurityCapabilities, long id) {
        return advancedDeviceProtocolSecurityCapabilities.getSecuritySuites().stream()
                .filter(level -> level.getId() == id)
                .findFirst()
                .orElseThrow(() -> new WebApplicationException("No security suite with id " + id, Response.Status.NOT_FOUND));
    }

    private Optional<Integer> getIdParameterFromUriParams(UriInfo uriInfo, String parameter) {
        MultivaluedMap<String, String> uriParams = uriInfo.getQueryParameters();
        if (uriParams.containsKey(parameter) && !uriParams.getFirst(parameter).isEmpty()) {
            String parameterString = uriParams.getFirst(parameter);
            return Optional.of(Integer.parseInt(parameterString));
        } else {
            return Optional.empty();
        }
    }

    @GET
    @Transactional
    @Path("/clienttype")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response getClientType(@PathParam("deviceTypeId") long deviceTypeId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        PropertyInfo clientType = null;
        Optional<PropertySpec> clientPropertySpec = getClientPropertySpec(deviceType);
        if (clientPropertySpec.isPresent()) {
            clientType = mdcPropertyUtils.convertPropertySpecToPropertyInfo(clientPropertySpec.get(), null);
        }
        return Response.ok(clientType).build();
    }

    private Optional<PropertySpec> getClientPropertySpec(DeviceType deviceType) {
        if (deviceType.getDeviceProtocolPluggableClass().isPresent()) {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = deviceType.getDeviceProtocolPluggableClass().get();
            if (deviceProtocolPluggableClass.getDeviceProtocol().getClientSecurityPropertySpec().isPresent()) {
                return Optional.of(UPLToConnexoPropertySpecAdapter.adaptTo(deviceProtocolPluggableClass.getDeviceProtocol().getClientSecurityPropertySpec().get()));
            }
        }
        return Optional.empty();
    }
}