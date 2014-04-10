package com.energyict.mdc.pluggable.rest.impl;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 * Date: 19/11/13
 * Time: 10:18
 */
@XmlRootElement
public class LicensedProtocolsInfo {
    @XmlElement
    @XmlElementWrapper(name = "LicensedProtocol")
    public Collection<? super LicensedProtocolInfo> licensedProtocolInfos = new ArrayList<>();
}
