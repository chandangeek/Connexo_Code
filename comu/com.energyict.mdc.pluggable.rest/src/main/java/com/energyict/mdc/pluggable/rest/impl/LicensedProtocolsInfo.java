/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.pluggable.rest.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;

@XmlRootElement
public class LicensedProtocolsInfo {
    @XmlElement
    @XmlElementWrapper(name = "LicensedProtocol")
    public Collection<? super LicensedProtocolInfo> licensedProtocolInfos = new ArrayList<>();
}
