package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

public class DeviceProtocolPluggableClassInfoFactory extends SelectableFieldFactory<DeviceProtocolPluggableClassInfo, DeviceProtocolPluggableClass> {

    public DeviceProtocolPluggableClassInfo from(DeviceProtocolPluggableClass deviceProtocolPluggableClass, UriInfo uriInfo, Collection<String> fields) {
        DeviceProtocolPluggableClassInfo info = new DeviceProtocolPluggableClassInfo();
        copySelectedFields(info, deviceProtocolPluggableClass, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<DeviceProtocolPluggableClassInfo, DeviceProtocolPluggableClass>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceProtocolPluggableClassInfo, DeviceProtocolPluggableClass>> map = new HashMap<>();
        map.put("id", (deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) -> deviceProtocolPluggableClassInfo.id = deviceProtocolPluggableClass.getId());
        map.put("name", (deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) -> deviceProtocolPluggableClassInfo.name = deviceProtocolPluggableClass.getName());
        map.put("javaClassName", (deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) -> deviceProtocolPluggableClassInfo.javaClassName = deviceProtocolPluggableClass.getJavaClassName());
        map.put("version", (deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) -> deviceProtocolPluggableClassInfo.version = deviceProtocolPluggableClass.getVersion());
        map.put("link", ((deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) ->
            deviceProtocolPluggableClassInfo.link = Link.fromUriBuilder(uriInfo.
                    getBaseUriBuilder().
                    path(DeviceProtocolPluggableClassResource.class).
                    path(DeviceProtocolPluggableClassResource.class, "getDeviceProtocolPluggableClass")).
                    rel(LinkInfo.REF_SELF).
                    title("pluggable class").
                    build(deviceProtocolPluggableClass.getId())
        ));
        map.put("authenticationAccessLevels", ((deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(AuthenticationDeviceAccessLevelResource.class)
                    .path(AuthenticationDeviceAccessLevelResource.class, "getAuthenticationDeviceAccessLevel")
                    .resolveTemplate("deviceProtocolPluggableClassId", deviceProtocolPluggableClass.getId());
            deviceProtocolPluggableClassInfo.authenticationAccessLevels = deviceProtocolPluggableClass
                    .getDeviceProtocol()
                    .getAuthenticationAccessLevels()
                    .stream()
                    .sorted((aa1, aa2) -> aa1.getTranslationKey().compareTo(aa2.getTranslationKey()))
                    .map(aal -> {
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = (long)aal.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).
                                rel("related").
                                title("Authentication access level").
                                build(aal.getId());

                        return linkInfo;
                    }).collect(toList());
        }));
        map.put("encryptionAccessLevels", ((deviceProtocolPluggableClassInfo, deviceProtocolPluggableClass, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(EncryptionDeviceAccessLevelResource.class)
                    .path(EncryptionDeviceAccessLevelResource.class, "getEncryptionDeviceAccessLevel")
                    .resolveTemplate("deviceProtocolPluggableClassId", deviceProtocolPluggableClass.getId());
            deviceProtocolPluggableClassInfo.encryptionAccessLevels = deviceProtocolPluggableClass
                    .getDeviceProtocol()
                    .getEncryptionAccessLevels()
                    .stream()
                    .sorted((aa1, aa2) -> aa1.getTranslationKey().compareTo(aa2.getTranslationKey()))
                    .map(aal -> {
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = (long)aal.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).
                                rel("related").
                                title("Encryption access level").
                                build(aal.getId());

                        return linkInfo;
                    }).collect(toList());
        }));

        return map;
    }
}
