/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.device.data.rest.SecurityAccessorInfoFactory;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class SecurityAccessorResource {
    private final Set<CryptographicType> CERTIFICATES = EnumSet.of(CryptographicType.Certificate, CryptographicType.ClientCertificate, CryptographicType.TrustedCertificate);
    private final Set<CryptographicType> KEYS = EnumSet.of(CryptographicType.SymmetricKey, CryptographicType.Passphrase);

    private final SecurityAccessorInfoFactory securityAccessorInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public SecurityAccessorResource(ResourceHelper resourceHelper, SecurityAccessorInfoFactory securityAccessorInfoFactory) {
        this.securityAccessorInfoFactory = securityAccessorInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Transactional
    @Path("/keys")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
//            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
//            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public PagedInfoList getKeys(@PathParam("name") String name, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<SecurityAccessorInfo> collect = getSecurityAccessorInfos(device, ka -> KEYS.contains(ka.getKeyAccessorType().getKeyType().getCryptographicType()));
        return PagedInfoList.fromCompleteList("keys", collect, queryParameters);
    }

    @GET
    @Transactional
    @Path("/certificates")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
//    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA,
//            com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_2, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_3, com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_SECURITY_PROPERTIES_4,
//            com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_1, com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_2,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_3,com.energyict.mdc.device.config.security.Privileges.Constants.EDIT_DEVICE_SECURITY_PROPERTIES_4,})
    public PagedInfoList getCertificates(@PathParam("name") String name, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByNameOrThrowException(name);
        List<SecurityAccessorInfo> collect = getSecurityAccessorInfos(device, ka -> CERTIFICATES.contains(ka.getKeyAccessorType().getKeyType().getCryptographicType()));
        return PagedInfoList.fromCompleteList("certificates", collect, queryParameters);
    }

    private List<SecurityAccessorInfo> getSecurityAccessorInfos(Device device, Predicate<KeyAccessor> keyAccessorPredicate) {
        return device.getKeyAccessors()
                .stream()
                .filter(keyAccessorPredicate)
                .map(securityAccessorInfoFactory::from)
                .sorted(Comparator.comparing(ka -> ka.name.toLowerCase()))
                .collect(toList());
    }


}
