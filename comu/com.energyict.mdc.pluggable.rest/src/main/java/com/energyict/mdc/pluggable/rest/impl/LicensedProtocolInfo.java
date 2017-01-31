/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.ProtocolFamily;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LicensedProtocolInfo {

    public String protocolName;
    public String protocolJavaClassName;
    public int licensedProtocolRuleCode;
    public LicensedProtocolFamilyInfo[] protocolFamilies;

    public LicensedProtocolInfo() {
    }

    public LicensedProtocolInfo(LicensedProtocol licensedProtocolRule, String description) {
        protocolName = description;
        protocolJavaClassName = licensedProtocolRule.getClassName();
        protocolFamilies = new LicensedProtocolFamilyInfo[licensedProtocolRule.getFamilies().size()];
        int counter = 0;
        for (ProtocolFamily protocolFamily : licensedProtocolRule.getFamilies()) {
            protocolFamilies[counter++] = new LicensedProtocolFamilyInfo(protocolFamily);
        }
        licensedProtocolRuleCode = licensedProtocolRule.getCode();
    }
}
