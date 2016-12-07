package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.protocol.ProtocolFamily;
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
