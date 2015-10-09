package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ComTaskEnablementInfoFactory extends SelectableFieldFactory<ComTaskEnablementInfo, ComTaskEnablement> {

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
                    rel(LinkInfo.REF_SELF).
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
                        rel(LinkInfo.REF_RELATION).
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
                        .rel(LinkInfo.REF_RELATION)
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
                        .rel(LinkInfo.REF_RELATION)
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
                        .rel(LinkInfo.REF_RELATION)
                        .title("Protocol dialect configuration properties")
                        .build(comTaskEnablement.getProtocolDialectConfigurationProperties().getId());
        }));
        return map;
    }

}
