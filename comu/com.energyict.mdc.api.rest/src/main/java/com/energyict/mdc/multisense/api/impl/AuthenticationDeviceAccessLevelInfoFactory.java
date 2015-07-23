package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;

public class AuthenticationDeviceAccessLevelInfoFactory extends SelectableFieldFactory<DeviceAccessLevelInfo, Pair<DeviceProtocolPluggableClass, DeviceAccessLevel>> {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final Thesaurus thesaurus;

    @Inject
    public AuthenticationDeviceAccessLevelInfoFactory(MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.thesaurus = thesaurus;
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
        map.put("name", (deviceAccessLevelInfo, pair, uriInfo) -> deviceAccessLevelInfo.name = thesaurus.getStringBeyondComponent(pair.getLast().getTranslationKey(), pair.getLast().getTranslationKey()));
        map.put("properties", (deviceAccessLevelInfo, pair, uriInfo) -> deviceAccessLevelInfo.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(pair.getLast().getSecurityProperties(), TypedProperties.empty()));
        map.put("link", ((deviceAccessLevelInfo, deviceAccessLevel, uriInfo) ->
            deviceAccessLevelInfo.link = Link.fromUriBuilder(uriInfo.
                    getBaseUriBuilder().
                    path(AuthenticationDeviceAccessLevelResource.class).
                    path(AuthenticationDeviceAccessLevelResource.class, "getAuthenticationDeviceAccessLevel")).
                    rel(LinkInfo.REF_SELF).
                    title("Authentication access level").
                    build(deviceAccessLevel.getFirst().getId(),deviceAccessLevel.getLast().getId())
        ));

        return map;
    }
}
