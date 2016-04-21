package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bvn on 7/22/15.
 */
public class ConfigurationSecurityPropertySetFactory extends SelectableFieldFactory<ConfigurationSecurityPropertySetInfo, SecurityPropertySet> {

    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public ConfigurationSecurityPropertySetFactory(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
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
        map.put("authenticationAccessLevel", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(AuthenticationDeviceAccessLevelResource.class)
                    .path(AuthenticationDeviceAccessLevelResource.class, "getAuthenticationDeviceAccessLevel")
                    .resolveTemplate("deviceProtocolPluggableClassId", securityPropertySet.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass().getId());
            LinkInfo linkInfo = new LinkInfo();
            linkInfo.id = (long)securityPropertySet.getAuthenticationDeviceAccessLevel().getId();
            linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_RELATION).build(securityPropertySet.getAuthenticationDeviceAccessLevel().getId());
            configurationSecurityPropertySetInfo.authenticationAccessLevel = linkInfo;
        });
        map.put("encryptionAccessLevel", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(EncryptionDeviceAccessLevelResource.class)
                    .path(EncryptionDeviceAccessLevelResource.class, "getEncryptionDeviceAccessLevel")
                    .resolveTemplate("deviceProtocolPluggableClassId", securityPropertySet.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass().getId());
            LinkInfo linkInfo = new LinkInfo();
            linkInfo.id = (long)securityPropertySet.getEncryptionDeviceAccessLevel().getId();
            linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_RELATION).build(securityPropertySet.getEncryptionDeviceAccessLevel().getId());
            configurationSecurityPropertySetInfo.encryptionAccessLevel = linkInfo;

        });
        map.put("properties", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) -> configurationSecurityPropertySetInfo.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(securityPropertySet.getPropertySpecs(), TypedProperties.empty()));
        map.put("link", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(ConfigurationSecurityPropertySetResource.class)
                    .path(ConfigurationSecurityPropertySetResource.class, "getSecurityPropertySet")
                    .resolveTemplate("deviceTypeId", securityPropertySet.getDeviceConfiguration().getDeviceType().getId())
                    .resolveTemplate("deviceConfigId", securityPropertySet.getDeviceConfiguration().getId())
                    .resolveTemplate("securityPropertySetId", securityPropertySet.getId());
            configurationSecurityPropertySetInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_SELF).build();
        });
        return map;
    }

}
