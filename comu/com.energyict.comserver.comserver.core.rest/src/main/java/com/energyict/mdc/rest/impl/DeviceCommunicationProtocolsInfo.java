package com.energyict.mdc.rest.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;
import java.util.HashSet;

/**
 * Copyrights EnergyICT
 * Date: 05/11/13
 * Time: 11:21
 */
@XmlRootElement
public class DeviceCommunicationProtocolsInfo {
    @XmlElement
    @XmlElementWrapper(name = "DeviceProtocolPluggableClass")
    public Collection<? super DeviceCommunicationProtocolInfo> deviceCommunicationProtocolInfos = new HashSet<>();
}
