package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class EncryptionDeviceAccessLevelInfoFactory extends SelectableFieldFactory<DeviceAccessLevelInfo, Pair<DeviceProtocolPluggableClass, DeviceAccessLevel>> {

    private final MdcPropertyUtils mdcPropertyUtils;

    public LinkInfo asLink(DeviceProtocolPluggableClass protocolPluggableClass, DeviceAccessLevel deviceAccessLevel, Relation relation, UriInfo uriInfo) {
        return asLink(protocolPluggableClass, deviceAccessLevel, relation, getUriBuilder(uriInfo));
    }

    public List<LinkInfo> asLink(Collection<Pair<DeviceProtocolPluggableClass, DeviceAccessLevel>> pairs, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return pairs.stream().map(i-> asLink(i.getFirst(), i.getLast(), relation, uriBuilder)).collect(toList());
    }

    private LinkInfo asLink(DeviceProtocolPluggableClass protocolPluggableClass, DeviceAccessLevel deviceAccessLevel, Relation relation, UriBuilder uriBuilder) {
        LinkInfo info = new LinkInfo();
        info.id = (long)deviceAccessLevel.getId();
        info.link = Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Encryption access level").
                build(protocolPluggableClass.getId(),deviceAccessLevel.getId());
        return info;
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(EncryptionDeviceAccessLevelResource.class)
                .path(EncryptionDeviceAccessLevelResource.class, "getEncryptionDeviceAccessLevel");
    }


    @Inject
    public EncryptionDeviceAccessLevelInfoFactory(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public DeviceAccessLevelInfo from(DeviceProtocolPluggableClass pluggableClass, DeviceAccessLevel authenticationDeviceAccessLevel, UriInfo uriInfo, Collection<String> fields) {
        DeviceAccessLevelInfo info = new DeviceAccessLevelInfo();
        copySelectedFields(info, Pair.of(pluggableClass,authenticationDeviceAccessLevel), uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<DeviceAccessLevelInfo, Pair<DeviceProtocolPluggableClass, DeviceAccessLevel>>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceAccessLevelInfo, Pair<DeviceProtocolPluggableClass, DeviceAccessLevel>>> map = new HashMap<>();
        map.put("id", (deviceAccessLevelInfo, pair, uriInfo) -> deviceAccessLevelInfo.id = (long) pair.getLast().getId());
        map.put("name", (deviceAccessLevelInfo, pair, uriInfo) -> deviceAccessLevelInfo.name = pair.getLast().getTranslation());
        map.put("properties", (deviceAccessLevelInfo, pair, uriInfo) -> deviceAccessLevelInfo.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(pair.getLast().getSecurityProperties(), TypedProperties.empty()));
        map.put("link", ((deviceAccessLevelInfo, deviceAccessLevel, uriInfo) ->
            deviceAccessLevelInfo.link = asLink(deviceAccessLevel.getFirst(), deviceAccessLevel.getLast(), Relation.REF_SELF, uriInfo).link));

        return map;
    }
}
