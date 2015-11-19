package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class ProtocolDialectConfigurationPropertiesInfoFactory extends SelectableFieldFactory<ProtocolDialectConfigurationPropertiesInfo, ProtocolDialectConfigurationProperties> {

    public LinkInfo asLink(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, Relation relation, UriInfo uriInfo) {
        return asLink(protocolDialectConfigurationProperties, relation, getUriBuilder(uriInfo));
    }

    public List<LinkInfo> asLink(Collection<ProtocolDialectConfigurationProperties> protocolDialectConfigurationPropertiess, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return protocolDialectConfigurationPropertiess.stream().map(i-> asLink(i, relation, uriBuilder)).collect(toList());
    }

    private LinkInfo asLink(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, Relation relation, UriBuilder uriBuilder) {
        LinkInfo info = new LinkInfo();
        info.id = protocolDialectConfigurationProperties.getId();
        info.link = Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Protocol dialect configuration properties").
                build(protocolDialectConfigurationProperties.getDeviceConfiguration().getDeviceType().getId(),
                        protocolDialectConfigurationProperties.getDeviceConfiguration().getId(),
                        protocolDialectConfigurationProperties.getId());
        return info;
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(ProtocolDialectConfigurationPropertiesResource.class)
                .path(ProtocolDialectConfigurationPropertiesResource.class, "getProtocolDialectConfigurationProperty");
    }

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
        map.put("link", ((protocolDialectConfigurationPropertiesInfo, protocolDialectConfigurationProperties, uriInfo) ->
                protocolDialectConfigurationPropertiesInfo.link = asLink(protocolDialectConfigurationProperties, Relation.REF_SELF, uriInfo).link));
        return map;
    }
}
