package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/22/15.
 */
public class ConfigurationSecurityPropertySetInfoFactory extends SelectableFieldFactory<ConfigurationSecurityPropertySetInfo, SecurityPropertySet> {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final Provider<AuthenticationDeviceAccessLevelInfoFactory> authenticationDeviceAccessLevelInfoFactoryProvider;
    private final Provider<EncryptionDeviceAccessLevelInfoFactory> encryptionDeviceAccessLevelInfoFactoryProvider;

    @Inject
    public ConfigurationSecurityPropertySetInfoFactory(
            MdcPropertyUtils mdcPropertyUtils,
            Provider<AuthenticationDeviceAccessLevelInfoFactory> authenticationDeviceAccessLevelInfoFactoryProvider,
            Provider<EncryptionDeviceAccessLevelInfoFactory> encryptionDeviceAccessLevelInfoFactoryProvider) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.authenticationDeviceAccessLevelInfoFactoryProvider = authenticationDeviceAccessLevelInfoFactoryProvider;
        this.encryptionDeviceAccessLevelInfoFactoryProvider = encryptionDeviceAccessLevelInfoFactoryProvider;
    }

    public LinkInfo asLink(SecurityPropertySet securityPropertySet, Relation relation, UriInfo uriInfo) {
        return asLink(securityPropertySet, relation, getUriBuilder(uriInfo));
    }

    public List<LinkInfo> asLink(Collection<SecurityPropertySet> securityPropertySets, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return securityPropertySets.stream().map(i-> asLink(i, relation, uriBuilder)).collect(toList());
    }

    private LinkInfo asLink(SecurityPropertySet securityPropertySet, Relation relation, UriBuilder uriBuilder) {
        LinkInfo info = new LinkInfo();
        info.id = securityPropertySet.getId();
        info.link = Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Configuration security set")
                .build(securityPropertySet.getDeviceConfiguration().getDeviceType().getId(),
                        securityPropertySet.getDeviceConfiguration().getId(),
                        securityPropertySet.getId());
        return info;
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(ConfigurationSecurityPropertySetResource.class)
                .path(ConfigurationSecurityPropertySetResource.class, "getSecurityPropertySet");
    }

    public ConfigurationSecurityPropertySetInfo from(SecurityPropertySet securityPropertySet, UriInfo uriInfo, Collection<String> fields) {
        ConfigurationSecurityPropertySetInfo info = new ConfigurationSecurityPropertySetInfo();
        copySelectedFields(info, securityPropertySet, uriInfo, fields);
        return info;
    }


    @Override
    protected Map<String, PropertyCopier<ConfigurationSecurityPropertySetInfo, SecurityPropertySet>> buildFieldMap() {
        Map<String, PropertyCopier<ConfigurationSecurityPropertySetInfo, SecurityPropertySet>> map = new HashMap<>();
        map.put("id", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) -> configurationSecurityPropertySetInfo.id = securityPropertySet.getId());
        map.put("name", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) -> configurationSecurityPropertySetInfo.name = securityPropertySet.getName());
        map.put("authenticationAccessLevel", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) ->
            configurationSecurityPropertySetInfo.authenticationAccessLevel = authenticationDeviceAccessLevelInfoFactoryProvider.get().asLink(
                    securityPropertySet.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass(),
                    securityPropertySet.getAuthenticationDeviceAccessLevel(),
                    Relation.REF_RELATION,
                    uriInfo));
        map.put("encryptionAccessLevel", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) ->
            configurationSecurityPropertySetInfo.encryptionAccessLevel = encryptionDeviceAccessLevelInfoFactoryProvider.get().asLink(
                    securityPropertySet.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass(),
                    securityPropertySet.getEncryptionDeviceAccessLevel(),
                    Relation.REF_RELATION,
                    uriInfo
            ));
        map.put("properties", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) -> configurationSecurityPropertySetInfo.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(securityPropertySet.getPropertySpecs(), TypedProperties.empty()));
        map.put("link", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) ->
            configurationSecurityPropertySetInfo.link = asLink(securityPropertySet, Relation.REF_SELF, uriInfo).link);
        return map;
    }

}
