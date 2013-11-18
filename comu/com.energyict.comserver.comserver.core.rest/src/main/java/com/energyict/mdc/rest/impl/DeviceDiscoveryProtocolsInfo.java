package com.energyict.mdc.rest.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;

/**
 * Copyrights EnergyICT
 * Date: 15/11/13
 * Time: 12:16
 */
@XmlRootElement
public class DeviceDiscoveryProtocolsInfo {

    @XmlElement
    @XmlElementWrapper(name = "InboundDeviceProtocolPluggableClass")
    public Collection<? super DeviceDiscoveryProtocolInfo> deviceDiscoveryProtocolInfos = new HashSet<>();
}
