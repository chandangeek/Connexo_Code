package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
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

public class ComTaskEnablementInfoFactory extends SelectableFieldFactory<ComTaskEnablementInfo, ComTaskEnablement> {

    public LinkInfo asLink(ComTaskEnablement comTaskEnablement, Relation relation, UriInfo uriInfo) {
        return asLink(comTaskEnablement, relation, getUriBuilder(uriInfo));
    }

    public List<LinkInfo> asLink(Collection<ComTaskEnablement> comTaskEnablements, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return comTaskEnablements.stream().map(i-> asLink(i, relation, uriBuilder)).collect(toList());
    }

    private LinkInfo asLink(ComTaskEnablement comTaskEnablement, Relation relation, UriBuilder uriBuilder) {
        LinkInfo info = new LinkInfo();
        info.id = comTaskEnablement.getId();
        info.link = Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Communication task enablement")
                .build(comTaskEnablement.getDeviceConfiguration().getDeviceType().getId(),
                        comTaskEnablement.getDeviceConfiguration().getId(),
                        comTaskEnablement.getId());
        return info;
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(ComTaskEnablementResource.class)
                .path(ComTaskEnablementResource.class, "getComTaskEnablement");
    }


    public ComTaskEnablementInfo from(ComTaskEnablement comTaskEnablement, UriInfo uriInfo, Collection<String> fields) {
        ComTaskEnablementInfo info = new ComTaskEnablementInfo();
        copySelectedFields(info, comTaskEnablement, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<ComTaskEnablementInfo, ComTaskEnablement>> buildFieldMap() {
        Map<String, PropertyCopier<ComTaskEnablementInfo, ComTaskEnablement>> map = new HashMap<>();
        map.put("id", (comTaskEnablementInfo, comTaskEnablement, uriInfo) -> comTaskEnablementInfo.id = comTaskEnablement.getId());
        map.put("priority", (comTaskEnablementInfo, comTaskEnablement, uriInfo) -> comTaskEnablementInfo.priority = comTaskEnablement.getPriority());
        map.put("suspended", (comTaskEnablementInfo, comTaskEnablement, uriInfo) -> comTaskEnablementInfo.suspended = comTaskEnablement.isSuspended());
        map.put("link", ((comTaskEnablementInfo, comTaskEnablement, uriInfo) ->
            comTaskEnablementInfo.link = Link.fromUriBuilder(uriInfo.
                        getBaseUriBuilder().
                        path(ComTaskEnablementResource.class).
                        path(ComTaskEnablementResource.class, "getComTaskEnablement").
                        resolveTemplate("deviceTypeId", comTaskEnablement.getDeviceConfiguration().getDeviceType().getId()).
                        resolveTemplate("deviceConfigId", comTaskEnablement.getDeviceConfiguration().getId())).
                    rel(Relation.REF_SELF.rel()).
                    title("Communication task enablement").
                    build(comTaskEnablement.getId())
        ));
        map.put("partialConnectionTask", ((comTaskEnablementInfo, comTaskEnablement, uriInfo) -> {
            if (comTaskEnablement.hasPartialConnectionTask()) {
                comTaskEnablementInfo.partialConnectionTask = new LinkInfo();
                comTaskEnablementInfo.partialConnectionTask.id = comTaskEnablement.getPartialConnectionTask().get().getId();
                UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                        .path(PartialConnectionTaskResource.class)
                        .path(PartialConnectionTaskResource.class, "getPartialConnectionTask")
                        .resolveTemplate("deviceTypeId", comTaskEnablement.getDeviceConfiguration().getDeviceType().getId())
                        .resolveTemplate("deviceConfigId", comTaskEnablement.getDeviceConfiguration().getId());
                comTaskEnablementInfo.partialConnectionTask.link =
                        Link.fromUriBuilder(uriBuilder).
                        rel(Relation.REF_RELATION.rel()).
                        title("Partial connection task").
                        build(comTaskEnablement.getPartialConnectionTask().get().getId());
            }
        }));
        map.put("comTask", ((comTaskEnablementInfo, comTaskEnablement, uriInfo) -> {
                UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                        .path(ComTaskResource.class)
                        .path(ComTaskResource.class, "getComTask");
                comTaskEnablementInfo.comTask = new LinkInfo();
                comTaskEnablementInfo.comTask.id = comTaskEnablement.getComTask().getId();
                comTaskEnablementInfo.comTask.link = Link.fromUriBuilder(uriBuilder)
                        .rel(Relation.REF_RELATION.rel())
                        .title("Communication task")
                        .build(comTaskEnablement.getComTask().getId());
        }));
        map.put("securityPropertySet", ((comTaskEnablementInfo, comTaskEnablement, uriInfo) -> {
                UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                        .path(ConfigurationSecurityPropertySetResource.class)
                        .path(ConfigurationSecurityPropertySetResource.class, "getSecurityPropertySet")
                        .resolveTemplate("deviceTypeId", comTaskEnablement.getDeviceConfiguration().getDeviceType().getId())
                        .resolveTemplate("deviceConfigId", comTaskEnablement.getDeviceConfiguration().getId());
                comTaskEnablementInfo.securityPropertySet = new LinkInfo();
                comTaskEnablementInfo.securityPropertySet.id = comTaskEnablement.getSecurityPropertySet().getId();
                comTaskEnablementInfo.securityPropertySet.link = Link.fromUriBuilder(uriBuilder)
                        .rel(Relation.REF_RELATION.rel())
                        .title("Security property set")
                        .build(comTaskEnablement.getSecurityPropertySet().getId());
        }));
        map.put("protocolDialectConfigurationProperties", ((comTaskEnablementInfo, comTaskEnablement, uriInfo) -> {
                UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                        .path(ProtocolDialectConfigurationPropertiesResource.class)
                        .path(ProtocolDialectConfigurationPropertiesResource.class, "getProtocolDialectConfigurationProperty")
                        .resolveTemplate("deviceTypeId", comTaskEnablement.getDeviceConfiguration().getDeviceType().getId())
                        .resolveTemplate("deviceConfigId", comTaskEnablement.getDeviceConfiguration().getId());
                comTaskEnablementInfo.protocolDialectConfigurationProperties = new LinkInfo();
                comTaskEnablementInfo.protocolDialectConfigurationProperties.id = comTaskEnablement.getProtocolDialectConfigurationProperties().getId();
                comTaskEnablementInfo.protocolDialectConfigurationProperties.link = Link.fromUriBuilder(uriBuilder)
                        .rel(Relation.REF_RELATION.rel())
                        .title("Protocol dialect configuration properties")
                        .build(comTaskEnablement.getProtocolDialectConfigurationProperties().getId());
        }));
        return map;
    }

}
