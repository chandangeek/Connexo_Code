package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

public class ProtocolDialectConfigurationPropertiesInfoFactory extends SelectableFieldFactory<ProtocolDialectConfigurationPropertiesInfo, ProtocolDialectConfigurationProperties> {

    public ProtocolDialectConfigurationPropertiesInfo from(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, UriInfo uriInfo, Collection<String> fields) {
        ProtocolDialectConfigurationPropertiesInfo info = new ProtocolDialectConfigurationPropertiesInfo();
        copySelectedFields(info, protocolDialectConfigurationProperties, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<ProtocolDialectConfigurationPropertiesInfo, ProtocolDialectConfigurationProperties>> buildFieldMap() {
        Map<String, PropertyCopier<ProtocolDialectConfigurationPropertiesInfo, ProtocolDialectConfigurationProperties>> map = new HashMap<>();
        map.put("id", (protocolDialectConfigurationPropertiesInfo, protocolDialectConfigurationProperties, uriInfo) -> protocolDialectConfigurationPropertiesInfo.id = protocolDialectConfigurationProperties.getId());
        map.put("name", (protocolDialectConfigurationPropertiesInfo, protocolDialectConfigurationProperties, uriInfo) -> protocolDialectConfigurationPropertiesInfo.name = protocolDialectConfigurationProperties.getName());
        map.put("link", ((protocolDialectConfigurationPropertiesInfo, protocolDialectConfigurationProperties, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo
                    .getBaseUriBuilder()
                    .path(ProtocolDialectConfigurationPropertiesResource.class)
                    .path(ProtocolDialectConfigurationPropertiesResource.class, "getProtocolDialectConfigurationProperty")
                    .resolveTemplate("deviceTypeId", protocolDialectConfigurationProperties.getDeviceConfiguration().getDeviceType().getId())
                    .resolveTemplate("deviceConfigId", protocolDialectConfigurationProperties.getDeviceConfiguration().getId());
            protocolDialectConfigurationPropertiesInfo.link = Link.fromUriBuilder(uriBuilder).
                    rel(LinkInfo.REF_SELF).
                    title("Protocol dialect configuration properties").
                    build(protocolDialectConfigurationProperties.getId());
        }));

        return map;
    }
}
