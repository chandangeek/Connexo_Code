/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import com.energyict.mdc.protocol.api.ProtocolFamily;

import javax.xml.bind.annotation.XmlRootElement;

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
