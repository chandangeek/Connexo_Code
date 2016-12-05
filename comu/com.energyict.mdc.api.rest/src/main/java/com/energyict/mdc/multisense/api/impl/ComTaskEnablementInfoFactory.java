package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.device.config.ComTaskEnablement;

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

public class ComTaskEnablementInfoFactory extends SelectableFieldFactory<ComTaskEnablementInfo, ComTaskEnablement> {

    private final Provider<PartialConnectionTaskInfoFactory> partialConnectionTaskInfoFactoryProvider;
    private final Provider<ComTaskInfoFactory> comTaskInfoFactoryProvider;
    private final Provider<ConfigurationSecurityPropertySetInfoFactory> configurationSecurityPropertySetInfoFactoryProvider;
    private final Provider<ProtocolDialectConfigurationPropertiesInfoFactory> protocolDialectConfigurationPropertiesInfoFactoryProvider;

    @Inject
    public ComTaskEnablementInfoFactory(Provider<PartialConnectionTaskInfoFactory> partialConnectionTaskInfoFactoryProvider,
                                        Provider<ComTaskInfoFactory> comTaskInfoFactoryProvider,
                                        Provider<ConfigurationSecurityPropertySetInfoFactory> configurationSecurityPropertySetInfoFactoryProvider,
                                        Provider<ProtocolDialectConfigurationPropertiesInfoFactory> protocolDialectConfigurationPropertiesInfoFactoryProvider) {
        this.partialConnectionTaskInfoFactoryProvider = partialConnectionTaskInfoFactoryProvider;
        this.comTaskInfoFactoryProvider = comTaskInfoFactoryProvider;
        this.configurationSecurityPropertySetInfoFactoryProvider = configurationSecurityPropertySetInfoFactoryProvider;
        this.protocolDialectConfigurationPropertiesInfoFactoryProvider = protocolDialectConfigurationPropertiesInfoFactoryProvider;
    }

    public LinkInfo asLink(ComTaskEnablement comTaskEnablement, Relation relation, UriInfo uriInfo) {
        ComTaskEnablementInfo info = new ComTaskEnablementInfo();
        copySelectedFields(info, comTaskEnablement, uriInfo, Arrays.asList("id", "version"));
        info.link = link(comTaskEnablement,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<ComTaskEnablement> comTaskEnablements, Relation relation, UriInfo uriInfo) {
        return comTaskEnablements.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(ComTaskEnablement comTaskEnablement, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Communication task enablement")
                .build(comTaskEnablement.getDeviceConfiguration().getDeviceType().getId(),
                        comTaskEnablement.getDeviceConfiguration().getId(),
                        comTaskEnablement.getId());
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
        map.put("version", (comTaskEnablementInfo, comTaskEnablement, uriInfo) -> comTaskEnablementInfo.version = comTaskEnablement.getVersion());
        map.put("priority", (comTaskEnablementInfo, comTaskEnablement, uriInfo) -> comTaskEnablementInfo.priority = comTaskEnablement.getPriority());
        map.put("suspended", (comTaskEnablementInfo, comTaskEnablement, uriInfo) -> comTaskEnablementInfo.suspended = comTaskEnablement.isSuspended());
        map.put("link", ((comTaskEnablementInfo, comTaskEnablement, uriInfo) -> comTaskEnablementInfo.link = link(comTaskEnablement, Relation.REF_SELF,uriInfo)));
        map.put("partialConnectionTask", ((comTaskEnablementInfo, comTaskEnablement, uriInfo) -> {
                    if (comTaskEnablement.hasPartialConnectionTask()) {
                        comTaskEnablementInfo.partialConnectionTask = partialConnectionTaskInfoFactoryProvider.get().asLink(comTaskEnablement.getPartialConnectionTask().get(), Relation.REF_RELATION, uriInfo);
                    }
                }));
        map.put("comTask", ((comTaskEnablementInfo, comTaskEnablement, uriInfo) -> comTaskEnablementInfo.comTask = comTaskInfoFactoryProvider.get().asLink(comTaskEnablement.getComTask(), Relation.REF_RELATION, uriInfo)));
        map.put("securityPropertySet", ((comTaskEnablementInfo, comTaskEnablement, uriInfo) ->
            comTaskEnablementInfo.securityPropertySet = configurationSecurityPropertySetInfoFactoryProvider.get().asLink(comTaskEnablement.getSecurityPropertySet(), Relation.REF_RELATION, uriInfo)));
        map.put("protocolDialectConfigurationProperties", ((comTaskEnablementInfo, comTaskEnablement, uriInfo) ->
            comTaskEnablementInfo.protocolDialectConfigurationProperties = protocolDialectConfigurationPropertiesInfoFactoryProvider.get().asLink(comTaskEnablement.getProtocolDialectConfigurationProperties(), Relation.REF_RELATION, uriInfo)));
        return map;
    }

}
