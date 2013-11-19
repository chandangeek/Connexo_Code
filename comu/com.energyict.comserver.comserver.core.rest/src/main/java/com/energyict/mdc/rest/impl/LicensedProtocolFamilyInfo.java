package com.energyict.mdc.rest.impl;

import com.energyict.mdw.core.ProtocolFamily;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 09:38
 */
@XmlRootElement
public class LicensedProtocolFamilyInfo {

    public final String protocolFamilyName;
    public final int protocolFamilyCode;

    public LicensedProtocolFamilyInfo(ProtocolFamily familyRule) {
        protocolFamilyName = familyRule.getName();
        protocolFamilyCode = familyRule.getCode();
    }
}
