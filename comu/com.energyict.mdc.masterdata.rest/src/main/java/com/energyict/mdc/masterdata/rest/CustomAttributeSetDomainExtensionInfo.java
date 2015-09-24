package com.energyict.mdc.masterdata.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomAttributeSetDomainExtensionInfo {

    public String value;
    public String localizedValue;

    public CustomAttributeSetDomainExtensionInfo() {
    }

    public CustomAttributeSetDomainExtensionInfo(String value, String localizedValue) {
        this.value = value;
        this.localizedValue = localizedValue;
    }

    public static List<CustomAttributeSetDomainExtensionInfo> from(Set<String> domainExtensions, Thesaurus thesaurus) {
        List<CustomAttributeSetDomainExtensionInfo> customAttributeSetDomainExtensionInfos = new ArrayList<>();
        for (String domainExtension : domainExtensions) {
            customAttributeSetDomainExtensionInfos.add(new CustomAttributeSetDomainExtensionInfo(domainExtension, thesaurus.getString(domainExtension, domainExtension)));
        }
        return customAttributeSetDomainExtensionInfos;
    }
}