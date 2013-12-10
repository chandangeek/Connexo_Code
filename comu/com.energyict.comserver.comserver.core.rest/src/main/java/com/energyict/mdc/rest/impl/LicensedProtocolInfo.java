package com.energyict.mdc.rest.impl;

import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.ProtocolFamily;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 09:37
 */
@XmlRootElement
public class LicensedProtocolInfo {

    public String protocolName;
    public String protocolJavaClassName;
    public int licensedProtocolRuleCode;
    public LicensedProtocolFamilyInfo[] protocolFamilies;

    public LicensedProtocolInfo() {
    }

    public LicensedProtocolInfo(LicensedProtocol licensedProtocolRule) {
        protocolName = licensedProtocolRule.getName();
        protocolJavaClassName = licensedProtocolRule.getClassName();
        protocolFamilies = new LicensedProtocolFamilyInfo[licensedProtocolRule.getFamilies().size()];
        int counter = 0;
        for (ProtocolFamily protocolFamily : licensedProtocolRule.getFamilies()) {
            protocolFamilies[counter++] = new LicensedProtocolFamilyInfo(protocolFamily);
        }
        licensedProtocolRuleCode = licensedProtocolRule.getCode();
    }
}
