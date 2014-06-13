package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.SecurityPropertySet;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class SecurityPropertySetInfo {

    public Long id;
    public String name;
    @JsonProperty("authenticationLevelId")
    public Integer authenticationLevelId;
    @JsonProperty("encryptionLevelId")
    public Integer encryptionLevelId;
    @JsonProperty("authenticationLevel")
    public SecurityLevelInfo authenticationLevel;
    @JsonProperty("encryptionLevel")
    public SecurityLevelInfo encryptionLevel;

    public SecurityPropertySetInfo() {
    }

    public static SecurityPropertySetInfo from(SecurityPropertySet securityPropertySet, Thesaurus thesaurus) {
        SecurityPropertySetInfo securityPropertySetInfo = new SecurityPropertySetInfo();
        securityPropertySetInfo.id = securityPropertySet.getId();
        securityPropertySetInfo.name = securityPropertySet.getName();
        securityPropertySetInfo.authenticationLevelId = securityPropertySet.getAuthenticationDeviceAccessLevel().getId();
        securityPropertySetInfo.encryptionLevelId = securityPropertySet.getEncryptionDeviceAccessLevel().getId();
        securityPropertySetInfo.authenticationLevel = SecurityLevelInfo.from(securityPropertySet.getAuthenticationDeviceAccessLevel(), thesaurus);
        securityPropertySetInfo.encryptionLevel = SecurityLevelInfo.from(securityPropertySet.getEncryptionDeviceAccessLevel(), thesaurus);

        return securityPropertySetInfo;
    }

    public static List<SecurityPropertySetInfo> from(List<SecurityPropertySet> securityPropertySetList, Thesaurus thesaurus) {
        List<SecurityPropertySetInfo> securityPropertySetInfos = new ArrayList<>(securityPropertySetList.size());
        for (SecurityPropertySet securityPropertySet : securityPropertySetList) {
            securityPropertySetInfos.add(SecurityPropertySetInfo.from(securityPropertySet, thesaurus));
        }
        return securityPropertySetInfos;
    }

    public void writeTo(SecurityPropertySet securityPropertySet) {
        securityPropertySet.setName(this.name);
        securityPropertySet.setAuthenticationLevel(this.authenticationLevelId);
        securityPropertySet.setEncryptionLevelId(this.encryptionLevelId);
    }
}
