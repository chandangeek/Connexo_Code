/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/22/15.
 */
public class ConfigurationSecurityPropertySetInfoFactory extends SelectableFieldFactory<ConfigurationSecurityPropertySetInfo, SecurityPropertySet> {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final Provider<SecuritySuiteDeviceAccessLevelInfoFactory> securitySuiteDeviceAccessLevelInfoFactoryProvider;
    private final Provider<AuthenticationDeviceAccessLevelInfoFactory> authenticationDeviceAccessLevelInfoFactoryProvider;
    private final Provider<EncryptionDeviceAccessLevelInfoFactory> encryptionDeviceAccessLevelInfoFactoryProvider;
    private final Provider<RequestSecurityDeviceAccessLevelInfoFactory> requestSecurityDeviceAccessLevelInfoFactoryProvider;
    private final Provider<ResponseSecurityDeviceAccessLevelInfoFactory> responseSecurityDeviceAccessLevelInfoFactoryProvider;
    private final Provider<KeyAccessorTypeInfoFactory> keyAccessorTypeInfoFactoryProvider;

    @Inject
    public ConfigurationSecurityPropertySetInfoFactory(
            MdcPropertyUtils mdcPropertyUtils,
            Provider<SecuritySuiteDeviceAccessLevelInfoFactory> securitySuiteDeviceAccessLevelInfoFactoryProvider, Provider<AuthenticationDeviceAccessLevelInfoFactory> authenticationDeviceAccessLevelInfoFactoryProvider,
            Provider<EncryptionDeviceAccessLevelInfoFactory> encryptionDeviceAccessLevelInfoFactoryProvider, Provider<RequestSecurityDeviceAccessLevelInfoFactory> requestSecurityDeviceAccessLevelInfoFactoryProvider, Provider<ResponseSecurityDeviceAccessLevelInfoFactory> responseSecurityDeviceAccessLevelInfoFactoryProvider, Provider<KeyAccessorTypeInfoFactory> keyAccessorTypeInfoFactoryProvider) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.securitySuiteDeviceAccessLevelInfoFactoryProvider = securitySuiteDeviceAccessLevelInfoFactoryProvider;
        this.authenticationDeviceAccessLevelInfoFactoryProvider = authenticationDeviceAccessLevelInfoFactoryProvider;
        this.encryptionDeviceAccessLevelInfoFactoryProvider = encryptionDeviceAccessLevelInfoFactoryProvider;
        this.requestSecurityDeviceAccessLevelInfoFactoryProvider = requestSecurityDeviceAccessLevelInfoFactoryProvider;
        this.responseSecurityDeviceAccessLevelInfoFactoryProvider = responseSecurityDeviceAccessLevelInfoFactoryProvider;
        this.keyAccessorTypeInfoFactoryProvider = keyAccessorTypeInfoFactoryProvider;
    }

    public LinkInfo asLink(SecurityPropertySet configurationSecurityPropertySet, Relation relation, UriInfo uriInfo) {
        ConfigurationSecurityPropertySetInfo info = new ConfigurationSecurityPropertySetInfo();
        copySelectedFields(info, configurationSecurityPropertySet, uriInfo, Arrays.asList("id", "version"));
        info.link = link(configurationSecurityPropertySet, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<SecurityPropertySet> configurationSecurityPropertySets, Relation relation, UriInfo uriInfo) {
        return configurationSecurityPropertySets.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(SecurityPropertySet securityPropertySet, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Configuration security set")
                .build(securityPropertySet.getDeviceConfiguration().getDeviceType().getId(),
                        securityPropertySet.getDeviceConfiguration().getId(),
                        securityPropertySet.getId());
    }


    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(ConfigurationSecurityPropertySetResource.class)
                .path(ConfigurationSecurityPropertySetResource.class, "getSecurityPropertySet");
    }

    public ConfigurationSecurityPropertySetInfo from(SecurityPropertySet securityPropertySet, UriInfo uriInfo, Collection<String> fields) {
        ConfigurationSecurityPropertySetInfo info = new ConfigurationSecurityPropertySetInfo();
        copySelectedFields(info, securityPropertySet, uriInfo, fields);
        return info;
    }


    @Override
    protected Map<String, PropertyCopier<ConfigurationSecurityPropertySetInfo, SecurityPropertySet>> buildFieldMap() {
        Map<String, PropertyCopier<ConfigurationSecurityPropertySetInfo, SecurityPropertySet>> map = new HashMap<>();
        map.put("id", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) -> configurationSecurityPropertySetInfo.id = securityPropertySet.getId());
        map.put("version", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) -> configurationSecurityPropertySetInfo.version = securityPropertySet.getVersion());
        map.put("name", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) -> configurationSecurityPropertySetInfo.name = securityPropertySet.getName());
        map.put("client", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) -> configurationSecurityPropertySetInfo.client = getClientAsPropertyInfo(securityPropertySet));
        map.put("securitySuite", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) ->
                configurationSecurityPropertySetInfo.securitySuite =
                        securityPropertySet.getSecuritySuite().getId() >= 0
                                ? securitySuiteDeviceAccessLevelInfoFactoryProvider.get().asLink(
                                securityPropertySet.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass().get(),
                                securityPropertySet.getSecuritySuite(),
                                Relation.REF_RELATION,
                                uriInfo)
                                : null);
        map.put("authenticationAccessLevel", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) ->
                configurationSecurityPropertySetInfo.authenticationAccessLevel =
                        securityPropertySet.getAuthenticationDeviceAccessLevel().getId() >= 0
                                ? authenticationDeviceAccessLevelInfoFactoryProvider.get().asLink(
                                securityPropertySet.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass().get(),
                                securityPropertySet.getAuthenticationDeviceAccessLevel(),
                                Relation.REF_RELATION,
                                uriInfo)
                                : null);
        map.put("encryptionAccessLevel", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) ->
                configurationSecurityPropertySetInfo.encryptionAccessLevel =
                        securityPropertySet.getEncryptionDeviceAccessLevel().getId() >= 0
                                ? encryptionDeviceAccessLevelInfoFactoryProvider.get().asLink(
                                securityPropertySet.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass().get(),
                                securityPropertySet.getEncryptionDeviceAccessLevel(),
                                Relation.REF_RELATION,
                                uriInfo)
                                : null);
        map.put("requestSecurityLevel", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) ->
                configurationSecurityPropertySetInfo.requestSecurityLevel =
                        securityPropertySet.getRequestSecurityLevel().getId() >= 0
                                ? requestSecurityDeviceAccessLevelInfoFactoryProvider.get().asLink(
                                securityPropertySet.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass().get(),
                                securityPropertySet.getRequestSecurityLevel(),
                                Relation.REF_RELATION,
                                uriInfo)
                                : null);
        map.put("responseSecurityLevel", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) ->
                configurationSecurityPropertySetInfo.responseSecurityLevel =
                        securityPropertySet.getResponseSecurityLevel().getId() >= 0
                                ? responseSecurityDeviceAccessLevelInfoFactoryProvider.get().asLink(
                                securityPropertySet.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass().get(),
                                securityPropertySet.getResponseSecurityLevel(),
                                Relation.REF_RELATION,
                                uriInfo)
                                : null);

        map.put("properties", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) -> {
            List<ConfigurationSecurityProperty> securityProperties = securityPropertySet.getConfigurationSecurityProperties();
            configurationSecurityPropertySetInfo.properties = new ArrayList<>(securityProperties.size());
            for (ConfigurationSecurityProperty securityProperty : securityProperties) {
                configurationSecurityPropertySetInfo.properties.add(
                        keyAccessorTypeInfoFactoryProvider.get().asLink(
                                securityPropertySet.getDeviceConfiguration().getDeviceType(),
                                securityProperty.getSecurityAccessorType(),
                                Relation.REF_RELATION,
                                uriInfo
                        )
                );
            }
        });
        map.put("link", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) ->
                configurationSecurityPropertySetInfo.link = link(securityPropertySet, Relation.REF_SELF, uriInfo));
        return map;
    }

    private PropertyInfo getClientAsPropertyInfo(SecurityPropertySet securityPropertySet) {
        if (securityPropertySet.getClientSecurityPropertySpec().isPresent()) {
            return mdcPropertyUtils.convertPropertySpecToPropertyInfo(securityPropertySet.getClientSecurityPropertySpec().get(), securityPropertySet.getClient());
        } else {
            return null;
        }
    }

}
