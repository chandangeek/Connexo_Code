package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class ProtocolDialectConfigurationPropertiesInfoFactory extends SelectableFieldFactory<ProtocolDialectConfigurationPropertiesInfo, ProtocolDialectConfigurationProperties> {

    public LinkInfo asLink(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, Relation relation, UriInfo uriInfo) {
        ProtocolDialectConfigurationPropertiesInfo info = new ProtocolDialectConfigurationPropertiesInfo();
        copySelectedFields(info,protocolDialectConfigurationProperties,uriInfo, Arrays.asList("id","version"));
        info.link = link(protocolDialectConfigurationProperties,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<ProtocolDialectConfigurationProperties> protocolDialectConfigurationPropertiess, Relation relation, UriInfo uriInfo) {
        return protocolDialectConfigurationPropertiess.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Protocol dialect configuration properties").
                build(protocolDialectConfigurationProperties.getDeviceConfiguration().getDeviceType().getId(),
                        protocolDialectConfigurationProperties.getDeviceConfiguration().getId(),
                        protocolDialectConfigurationProperties.getId());
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
        map.put("id", (protocolDialectConfigurationPropertiesInfo, protocolDialectConfigurationProperties, uriInfo) ->
                protocolDialectConfigurationPropertiesInfo.id = protocolDialectConfigurationProperties.getId());
        map.put("version", (protocolDialectConfigurationPropertiesInfo, protocolDialectConfigurationProperties, uriInfo) ->
                protocolDialectConfigurationPropertiesInfo.version = protocolDialectConfigurationProperties.getVersion());
        map.put("name", (protocolDialectConfigurationPropertiesInfo, protocolDialectConfigurationProperties, uriInfo) ->
                protocolDialectConfigurationPropertiesInfo.name = protocolDialectConfigurationProperties.getName());
        map.put("link", ((protocolDialectConfigurationPropertiesInfo, protocolDialectConfigurationProperties, uriInfo) ->
                protocolDialectConfigurationPropertiesInfo.link = link(protocolDialectConfigurationProperties, Relation.REF_SELF, uriInfo)));
        return map;
    }
}
