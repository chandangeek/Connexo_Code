package com.energyict.mdc.rest.impl;

import com.energyict.mdc.protocol.DeviceProtocolPluggableClass;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 05/11/13
 * Time: 11:21
 */
@XmlRootElement
public class DeviceCommunicationProtocolInfos {
    public int total;

    public List<DeviceCommunicationProtocolInfo> deviceCommunicationProtocols = new ArrayList<>();

    DeviceCommunicationProtocolInfos() {
    }

    DeviceCommunicationProtocolInfos(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        add(deviceProtocolPluggableClass);
    }

    DeviceCommunicationProtocolInfos(Iterable<? extends DeviceProtocolPluggableClass> deviceCommunicationProtocols) {
        addAll(deviceCommunicationProtocols);
    }

    DeviceCommunicationProtocolInfo add(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        DeviceCommunicationProtocolInfo result = new DeviceCommunicationProtocolInfo(deviceProtocolPluggableClass);
        deviceCommunicationProtocols.add(result);
        total++;
        return result;
    }

    void addAll(Iterable<? extends DeviceProtocolPluggableClass> deviceProtocolPluggableClasses) {
        for (DeviceProtocolPluggableClass each : deviceProtocolPluggableClasses) {
            add(each);
        }
    }
}
