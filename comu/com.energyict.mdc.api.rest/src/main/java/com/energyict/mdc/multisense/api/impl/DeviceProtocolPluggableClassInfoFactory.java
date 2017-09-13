/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.UPLConnectionFunction;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class DeviceProtocolPluggableClassInfoFactory extends SelectableFieldFactory<DeviceProtocolPluggableClassInfo, DeviceProtocolPluggableClass> {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final Provider<SecuritySuiteDeviceAccessLevelInfoFactory> securitySuiteDeviceAccessLevelInfoFactoryProvider;
    private final Provider<AuthenticationDeviceAccessLevelInfoFactory> authenticationDeviceAccessLevelInfoFactoryProvider;
    private final Provider<EncryptionDeviceAccessLevelInfoFactory> encryptionDeviceAccessLevelInfoFactoryProvider;
    private final Provider<RequestSecurityDeviceAccessLevelInfoFactory> requestSecurityDeviceAccessLevelInfoFactoryProvider;
    private final Provider<ResponseSecurityDeviceAccessLevelInfoFactory> responseSecurityDeviceAccessLevelInfoFactoryProvider;
    private final Provider<ConnectionFunctionInfoFactory> connectionFunctionInfoFactoryProvider;
    private final ProtocolPluggableService protocolPluggableService;

    @Inject
    public DeviceProtocolPluggableClassInfoFactory(
            MdcPropertyUtils mdcPropertyUtils, Provider<SecuritySuiteDeviceAccessLevelInfoFactory> securitySuiteDeviceAccessLevelInfoFactoryProvider,
            Provider<RequestSecurityDeviceAccessLevelInfoFactory> requestSecurityDeviceAccessLevelInfoFactoryProvider,
            Provider<ResponseSecurityDeviceAccessLevelInfoFactory> responseSecurityDeviceAccessLevelInfoFactoryProvider,
            ProtocolPluggableService protocolPluggableService,
            Provider<AuthenticationDeviceAccessLevelInfoFactory> authenticationDeviceAccessLevelInfoFactory,
            Provider<EncryptionDeviceAccessLevelInfoFactory> encryptionDeviceAccessLevelInfoFactory,
            Provider<ConnectionFunctionInfoFactory> connectionFunctionInfoFactoryProvider) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.securitySuiteDeviceAccessLevelInfoFactoryProvider = securitySuiteDeviceAccessLevelInfoFactoryProvider;
        this.authenticationDeviceAccessLevelInfoFactoryProvider = authenticationDeviceAccessLevelInfoFactory;
        this.encryptionDeviceAccessLevelInfoFactoryProvider = encryptionDeviceAccessLevelInfoFactory;
        this.requestSecurityDeviceAccessLevelInfoFactoryProvider = requestSecurityDeviceAccessLevelInfoFactoryProvider;
        this.responseSecurityDeviceAccessLevelInfoFactoryProvider = responseSecurityDeviceAccessLevelInfoFactoryProvider;
        this.protocolPluggableService = protocolPluggableService;
        this.connectionFunctionInfoFactoryProvider = connectionFunctionInfoFactoryProvider;
    }

    public DeviceProtocolPluggableClassInfo from(DeviceProtocolPluggableClass deviceProtocolPluggableClass, UriInfo uriInfo, Collection<String> fields) {
        DeviceProtocolPluggableClassInfo info = new DeviceProtocolPluggableClassInfo();
        copySelectedFields(info, deviceProtocolPluggableClass, uriInfo, fields);
        return info;
    }

    public LinkInfo asLink(DeviceProtocolPluggableClass deviceProtocolPluggableClass, Relation relation, UriInfo uriInfo) {
        DeviceProtocolPluggableClassInfo info = new DeviceProtocolPluggableClassInfo();
        copySelectedFields(info, deviceProtocolPluggableClass, uriInfo, Collections.singletonList("id"));
        info.link = link(deviceProtocolPluggableClass, relation, uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<DeviceProtocolPluggableClass> deviceProtocolPluggableClasss, Relation relation, UriInfo uriInfo) {
        return deviceProtocolPluggableClasss.stream().map(i -> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(DeviceProtocolPluggableClass deviceProtocolPluggableClass, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Pluggable class")
                .build(deviceProtocolPluggableClass.getId());
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(DeviceProtocolPluggableClassResource.class)
                .path(DeviceProtocolPluggableClassResource.class, "getDeviceProtocolPluggableClass");
    }

    @Override
    protected Map<String, PropertyCopier<DeviceProtocolPluggableClassInfo, DeviceProtocolPluggableClass>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceProtocolPluggableClassInfo, DeviceProtocolPluggableClass>> map = new HashMap<>();
        map.put("id", (deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) -> deviceProtocolPluggableClassInfo.id = deviceProtocolPluggableClass.getId());
        map.put("name", (deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) -> deviceProtocolPluggableClassInfo.name = deviceProtocolPluggableClass.getName());
        map.put("javaClassName", (deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) -> deviceProtocolPluggableClassInfo.javaClassName = deviceProtocolPluggableClass.getJavaClassName());
        map.put("version", (deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) -> deviceProtocolPluggableClassInfo.version = deviceProtocolPluggableClass.getVersion());
        map.put("link", ((deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) ->
                deviceProtocolPluggableClassInfo.link = link(deviceProtocolPluggableClass, Relation.REF_SELF, uriInfo)));
        map.put("client", (deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo)
                -> deviceProtocolPluggableClassInfo.client = getClientAsPropertyInfo(getActualDeviceProtocol(deviceProtocolPluggableClass).getClientSecurityPropertySpec()));
        map.put("securitySuites", ((deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) ->
                deviceProtocolPluggableClassInfo.securitySuites = getActualDeviceProtocol(deviceProtocolPluggableClass) instanceof AdvancedDeviceProtocolSecurityCapabilities
                        ? ((AdvancedDeviceProtocolSecurityCapabilities) getActualDeviceProtocol(deviceProtocolPluggableClass)).getSecuritySuites()
                        .stream()
                        .map(this.protocolPluggableService::adapt)
                        .sorted(Comparator.comparing(DeviceAccessLevel::getTranslation))
                        .map(aal -> securitySuiteDeviceAccessLevelInfoFactoryProvider.get().asLink(deviceProtocolPluggableClass, aal, Relation.REF_RELATION, uriInfo))
                        .collect(toList())
                        : null));
        map.put("authenticationAccessLevels", ((deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) ->
                deviceProtocolPluggableClassInfo.authenticationAccessLevels = getActualDeviceProtocol(deviceProtocolPluggableClass)
                        .getAuthenticationAccessLevels()
                        .stream()
                        .map(this.protocolPluggableService::adapt)
                        .sorted(Comparator.comparing(DeviceAccessLevel::getTranslation))
                        .map(aal -> authenticationDeviceAccessLevelInfoFactoryProvider.get().asLink(deviceProtocolPluggableClass, aal, Relation.REF_RELATION, uriInfo))
                        .collect(toList())
        ));
        map.put("encryptionAccessLevels", ((deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) ->
                deviceProtocolPluggableClassInfo.encryptionAccessLevels = getActualDeviceProtocol(deviceProtocolPluggableClass)
                        .getEncryptionAccessLevels()
                        .stream()
                        .map(this.protocolPluggableService::adapt)
                        .sorted(Comparator.comparing(DeviceAccessLevel::getTranslation))
                        .map(eal -> encryptionDeviceAccessLevelInfoFactoryProvider.get().asLink(deviceProtocolPluggableClass, eal, Relation.REF_RELATION, uriInfo))
                        .collect(toList())
        ));
        map.put("requestSecurityAccessLevels", ((deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) ->
                deviceProtocolPluggableClassInfo.requestSecurityAccessLevels = getActualDeviceProtocol(deviceProtocolPluggableClass) instanceof AdvancedDeviceProtocolSecurityCapabilities
                        ? ((AdvancedDeviceProtocolSecurityCapabilities) getActualDeviceProtocol(deviceProtocolPluggableClass)).getRequestSecurityLevels()
                        .stream()
                        .map(this.protocolPluggableService::adapt)
                        .sorted(Comparator.comparing(DeviceAccessLevel::getTranslation))
                        .map(aal -> requestSecurityDeviceAccessLevelInfoFactoryProvider.get().asLink(deviceProtocolPluggableClass, aal, Relation.REF_RELATION, uriInfo))
                        .collect(toList())
                        : null));
        map.put("responseSecurityAccessLevels", ((deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) ->
                deviceProtocolPluggableClassInfo.responseSecurityAccessLevels = getActualDeviceProtocol(deviceProtocolPluggableClass) instanceof AdvancedDeviceProtocolSecurityCapabilities
                        ? ((AdvancedDeviceProtocolSecurityCapabilities) getActualDeviceProtocol(deviceProtocolPluggableClass)).getResponseSecurityLevels()
                        .stream()
                        .map(this.protocolPluggableService::adapt)
                        .sorted(Comparator.comparing(DeviceAccessLevel::getTranslation))
                        .map(aal -> responseSecurityDeviceAccessLevelInfoFactoryProvider.get().asLink(deviceProtocolPluggableClass, aal, Relation.REF_RELATION, uriInfo))
                        .collect(toList())
                        : null));
        map.put("providedConnectionFunctions", ((deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) ->
                deviceProtocolPluggableClassInfo.providedConnectionFunctions = getActualDeviceProtocol(deviceProtocolPluggableClass).getProvidedConnectionFunctions()
                        .stream()
                        .sorted(Comparator.comparing(UPLConnectionFunction::getId))
                        .map(function -> connectionFunctionInfoFactoryProvider.get().asLink(deviceProtocolPluggableClass, function, Relation.REF_RELATION, uriInfo))
                        .collect(toList())));
        map.put("consumableConnectionFunctions", ((deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) ->
                deviceProtocolPluggableClassInfo.consumableConnectionFunctions = getActualDeviceProtocol(deviceProtocolPluggableClass).getConsumableConnectionFunctions()
                        .stream()
                        .sorted(Comparator.comparing(UPLConnectionFunction::getId))
                        .map(function -> connectionFunctionInfoFactoryProvider.get().asLink(deviceProtocolPluggableClass, function, Relation.REF_RELATION, uriInfo))
                        .collect(toList())));
        return map;
    }

    private com.energyict.mdc.upl.DeviceProtocol getActualDeviceProtocol(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
        if (deviceProtocol instanceof UPLProtocolAdapter) {
            return (com.energyict.mdc.upl.DeviceProtocol) ((UPLProtocolAdapter) deviceProtocol).getActual();
        }
        return deviceProtocol;
    }

    private PropertyInfo getClientAsPropertyInfo(Optional<PropertySpec> clientPropertySpec) {
        if (clientPropertySpec.isPresent()) {
            return mdcPropertyUtils.convertPropertySpecToPropertyInfo(UPLToConnexoPropertySpecAdapter.adaptTo(clientPropertySpec.get()), "");
        } else {
            return null;
        }
    }
}
