package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.accesslevel.UPLAuthenticationLevelAdapter;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class DeviceProtocolPluggableClassInfoFactory extends SelectableFieldFactory<DeviceProtocolPluggableClassInfo, DeviceProtocolPluggableClass> {

    private final Provider<AuthenticationDeviceAccessLevelInfoFactory> authenticationDeviceAccessLevelInfoFactoryProvider;
    private final Provider<EncryptionDeviceAccessLevelInfoFactory> encryptionDeviceAccessLevelInfoFactoryProvider;

    @Inject
    public DeviceProtocolPluggableClassInfoFactory(Provider<AuthenticationDeviceAccessLevelInfoFactory> authenticationDeviceAccessLevelInfoFactory,
                                                   Provider<EncryptionDeviceAccessLevelInfoFactory> encryptionDeviceAccessLevelInfoFactory) {
        this.authenticationDeviceAccessLevelInfoFactoryProvider = authenticationDeviceAccessLevelInfoFactory;
        this.encryptionDeviceAccessLevelInfoFactoryProvider = encryptionDeviceAccessLevelInfoFactory;
    }

    public DeviceProtocolPluggableClassInfo from(DeviceProtocolPluggableClass deviceProtocolPluggableClass, UriInfo uriInfo, Collection<String> fields) {
        DeviceProtocolPluggableClassInfo info = new DeviceProtocolPluggableClassInfo();
        copySelectedFields(info, deviceProtocolPluggableClass, uriInfo, fields);
        return info;
    }

    public LinkInfo asLink(DeviceProtocolPluggableClass deviceProtocolPluggableClass, Relation relation, UriInfo uriInfo) {
        DeviceProtocolPluggableClassInfo info = new DeviceProtocolPluggableClassInfo();
        copySelectedFields(info,deviceProtocolPluggableClass,uriInfo, Arrays.asList("id"));
        info.link = link(deviceProtocolPluggableClass,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<DeviceProtocolPluggableClass> deviceProtocolPluggableClasss, Relation relation, UriInfo uriInfo) {
        return deviceProtocolPluggableClasss.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
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
        map.put("authenticationAccessLevels", ((deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) ->
            deviceProtocolPluggableClassInfo.authenticationAccessLevels = deviceProtocolPluggableClass
                    .getDeviceProtocol()
                    .getAuthenticationAccessLevels()
                    .stream()
                    .map(UPLAuthenticationLevelAdapter::new)
                    .sorted((aa1, aa2) -> aa1.getTranslation().compareTo(aa2.getTranslation()))
                    .map(aal -> authenticationDeviceAccessLevelInfoFactoryProvider.get().asLink(deviceProtocolPluggableClass, aal, Relation.REF_RELATION, uriInfo))
                    .collect(toList())
        ));
        map.put("encryptionAccessLevels", ((deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) ->
            deviceProtocolPluggableClassInfo.encryptionAccessLevels = deviceProtocolPluggableClass
                    .getDeviceProtocol()
                    .getEncryptionAccessLevels()
                    .stream()
                    .map(UPLAuthenticationLevelAdapter::new)
                    .sorted((aa1, aa2) -> aa1.getTranslation().compareTo(aa2.getTranslation()))
                    .map(eal -> encryptionDeviceAccessLevelInfoFactoryProvider.get().asLink(deviceProtocolPluggableClass, eal, Relation.REF_RELATION, uriInfo))
                    .collect(toList())
        ));

        return map;
    }
}
