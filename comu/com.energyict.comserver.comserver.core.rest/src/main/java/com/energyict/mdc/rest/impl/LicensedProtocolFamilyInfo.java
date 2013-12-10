package com.energyict.mdc.rest.impl;

import com.energyict.mdc.protocol.api.ProtocolFamily;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 09:38
 */
@XmlRootElement
public class LicensedProtocolFamilyInfo {

    public String protocolFamilyName;
    public int protocolFamilyCode;

    public LicensedProtocolFamilyInfo() {
    }

    public LicensedProtocolFamilyInfo(ProtocolFamily familyRule) {
        protocolFamilyName = familyRule.getName();
        protocolFamilyCode = familyRule.getCode();
    }
}
