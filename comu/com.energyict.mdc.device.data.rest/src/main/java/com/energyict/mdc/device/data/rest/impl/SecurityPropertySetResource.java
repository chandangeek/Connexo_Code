package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceStatesRestricted;
import com.energyict.mdc.device.data.rest.SecurityPropertySetInfoFactory;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import java.util.Collection;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Handles SecurityPropertySets on devices
 * <p>
 * Created by bvn on 9/30/14.
 */
@DeviceStatesRestricted(value = {DefaultState.DECOMMISSIONED}, methods = {HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE})
public class SecurityPropertySetResource {
    private final ResourceHelper resourceHelper;
    private final SecurityPropertySetInfoFactory securityPropertySetInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public SecurityPropertySetResource(ResourceHelper resourceHelper, SecurityPropertySetInfoFactory securityPropertySetInfoFactory, ExceptionFactory exceptionFactory, DeviceConfigurationService deviceConfigurationService, MdcPropertyUtils mdcPropertyUtils) {
        this.resourceHelper = resourceHelper;
        this.securityPropertySetInfoFactory = securityPropertySetInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_4,
            com.energyict.mdc.device.config.security.Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public PagedInfoList getSecurityPropertySets(@PathParam("mRID") String mrid, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<SecurityPropertySetInfo> securityPropertySetInfos = securityPropertySetInfoFactory.asInfo(device, uriInfo);

        List<SecurityPropertySetInfo> pagedInfos = ListPager.of(securityPropertySetInfos).from(queryParameters).find();

        return PagedInfoList.fromPagedList("securityPropertySets", pagedInfos, queryParameters);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/{securityPropertySetId}")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_4,
            com.energyict.mdc.device.config.security.Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public Response getSecurityPropertySet(@PathParam("mRID") String mrid, @Context UriInfo uriInfo, @PathParam("securityPropertySetId") long securityPropertySetId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        SecurityPropertySet securityPropertySet = getSecurityPropertySetOrThrowException(securityPropertySetId, device);
        return Response.ok(securityPropertySetInfoFactory.asInfo(device, uriInfo, securityPropertySet)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{securityPropertySetId}")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_SECURITY_PROPERTIES_4,
            com.energyict.mdc.device.config.security.Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public Response updateSecurityPropertySet(@PathParam("mRID") String mrid, @Context UriInfo uriInfo, @PathParam("securityPropertySetId") long securityPropertySetId, SecurityPropertySetInfo securityPropertySetInfo) throws InvalidValueException {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        SecurityPropertySet securityPropertySet = getSecurityPropertySetOrThrowException(securityPropertySetId, device);
        //TypedProperties typedProperties = TypedProperties.empty();
        if (securityPropertySet.currentUserIsAllowedToEditDeviceProperties()) {
            boolean status = true;
            TypedProperties typedProperties = getTypedPropertiesForSecurityPropertySet(device, securityPropertySet);
            for (PropertySpec propertySpec : securityPropertySet.getPropertySpecs()) {
                Object newPropertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, securityPropertySetInfo.properties);
                if (newPropertyValue != null) {
                    // propertySpec.validateValue(newPropertyValue);
                    typedProperties.setProperty(propertySpec.getName(), newPropertyValue);
                } else {
                    if (!propertyHasValue(propertySpec, securityPropertySetInfo.properties)) {
                        typedProperties.removeProperty(propertySpec.getName());
                        status = false;
                    }
                }
            }

            if (status) {
                device.setSecurityProperties(securityPropertySet, typedProperties);
            } else {
                if (securityPropertySetInfo.saveAsIncomplete) {
                    device.setSecurityProperties(securityPropertySet, typedProperties);
                } else {
                    throw new LocalizedFieldValidationException(MessageSeeds.INCOMPLETE, "status");
                }
            }

            // Reload
            device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
            securityPropertySet = getSecurityPropertySetOrThrowException(securityPropertySetId, device);
            return Response.ok(securityPropertySetInfoFactory.asInfo(device, uriInfo, securityPropertySet)).build();
        }
        else {
            throw exceptionFactory.newException(MessageSeeds.UPDATE_SECURITY_PROPERTY_SET_NOT_ALLOWED);
        }
    }

    private SecurityPropertySet getSecurityPropertySetOrThrowException(long securityPropertySetId, Device device) {
        Optional<SecurityPropertySet> securityPropertySetOptional = deviceConfigurationService.findSecurityPropertySet(securityPropertySetId);
        if (!securityPropertySetOptional.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_SECURITY_PROPERTY_SET, securityPropertySetId);
        }
        if (securityPropertySetOptional.get().getDeviceConfiguration().getId() != device.getDeviceConfiguration().getId()) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_SECURITY_PROPERTY_SET_ON_DEVICE, securityPropertySetId, device.getmRID());
        }
        return securityPropertySetOptional.get();
    }

    private boolean propertyHasValue(PropertySpec propertySpec, Collection<PropertyInfo> propertyInfos) {
        return propertyHasValue(propertySpec, propertyInfos.toArray(new PropertyInfo[propertyInfos.size()]));
    }

    //find propertyValue in info
    public boolean propertyHasValue(PropertySpec propertySpec, PropertyInfo[] propertyInfos) {
        for (PropertyInfo propertyInfo : propertyInfos) {
            if (propertyInfo.key.equals(propertySpec.getName())) {
                if (propertyInfo.getPropertyValueInfo() != null && propertyInfo.getPropertyValueInfo().propertyHasValue!=null) {
                    return propertyInfo.getPropertyValueInfo().propertyHasValue;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private TypedProperties getTypedPropertiesForSecurityPropertySet(Device device, SecurityPropertySet securityPropertySet) {
        TypedProperties typedProperties = TypedProperties.empty();
        for (SecurityProperty securityProperty : device.getSecurityProperties(securityPropertySet)) {
            typedProperties.setProperty(securityProperty.getName(), securityProperty.getValue());
        }
        return typedProperties;
    }



}
