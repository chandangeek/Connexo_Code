package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;

public class DeviceAccessLevelInfoFactory extends SelectableFieldFactory<DeviceAccessLevelInfo, DeviceAccessLevel> {

    private final Thesaurus thesaurus;

    @Inject
    public DeviceAccessLevelInfoFactory(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }


    public DeviceAccessLevelInfo from(DeviceAccessLevel deviceAccessLevel, UriInfo uriInfo, Collection<String> fields) {
        DeviceAccessLevelInfo info = new DeviceAccessLevelInfo();
        copySelectedFields(info, deviceAccessLevel, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<DeviceAccessLevelInfo, DeviceAccessLevel>> buildFieldMap() {
        Map<String, PropertyCopier<DeviceAccessLevelInfo, DeviceAccessLevel>> map = new HashMap<>();
        map.put("id", (deviceAccessLevelInfo, deviceAccessLevel, uriInfo) -> deviceAccessLevelInfo.id = (long)deviceAccessLevel.getId());
//        map.put("name", (deviceAccessLevelInfo, deviceAccessLevel, uriInfo) -> deviceAccessLevelInfo.name = thesaurus.getFormat(deviceAccessLevel.getTranslationKey()));
        map.put("link", ((deviceAccessLevelInfo, deviceAccessLevel, uriInfo) ->
            deviceAccessLevelInfo.link = Link.fromUriBuilder(uriInfo.
                    getBaseUriBuilder().
                    path(DeviceAccessLevelResource.class).
                    path(DeviceAccessLevelResource.class, "getDeviceAccessLevel")).
                    rel(LinkInfo.REF_SELF).
                    title("yyy").
                    build(deviceAccessLevel.getId())
        ));

        return map;
    }
}
