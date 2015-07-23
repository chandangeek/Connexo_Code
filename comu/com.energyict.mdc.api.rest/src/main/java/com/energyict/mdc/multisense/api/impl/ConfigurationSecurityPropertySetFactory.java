package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 7/22/15.
 */
public class ConfigurationSecurityPropertySetFactory extends SelectableFieldFactory<ConfigurationSecurityPropertySetInfo, SecurityPropertySet> {

    public ConfigurationSecurityPropertySetInfo asInfo(SecurityPropertySet securityPropertySet, UriInfo uriInfo, Collection<String> fields) {
        ConfigurationSecurityPropertySetInfo info = new ConfigurationSecurityPropertySetInfo();
        copySelectedFields(info, securityPropertySet, uriInfo, fields);
        return info;
    }


    @Override
    protected Map<String, PropertyCopier<ConfigurationSecurityPropertySetInfo, SecurityPropertySet>> buildFieldMap() {
        Map<String, PropertyCopier<ConfigurationSecurityPropertySetInfo, SecurityPropertySet>> map = new HashMap<>();
        map.put("id", (configurationSecurityPropertySetInfo, securityPropertySet, uriInfo) -> configurationSecurityPropertySetInfo.id = securityPropertySet.getId());
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
