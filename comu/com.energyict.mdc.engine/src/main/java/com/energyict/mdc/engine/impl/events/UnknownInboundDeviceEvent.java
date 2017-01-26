package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

/**
 * Models the event that occurs when an inbound device opens a communication channel
 * to the data collection engine, provides its unique identifier to the discover protocol
 * that is responsible for the communication channel but the platform fails to find
 * an existing device with the specified identifier. Therefore, the device that opened
 * the communication channel is not recognized.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (14:33)
 * @see com.energyict.mdc.engine.impl.EventType#UNKNOWN_INBOUND_DEVICE
 */
public class UnknownInboundDeviceEvent {

    private final String comPortName;
    private final String comServerName;
    private final String deviceIdentifier;
    private final long discoveryProtocolId;

    public UnknownInboundDeviceEvent(ComPort comPort, DeviceIdentifier deviceIdentifier, InboundDeviceProtocolPluggableClass discoveryProtocolPluggableClass) {
        super();
        this.comPortName = comPort.getName();
        this.comServerName = comPort.getComServer().getName();
        this.deviceIdentifier = deviceIdentifier.toString();
        this.discoveryProtocolId = discoveryProtocolPluggableClass.getId();
    }

    public String getComPortName() {
        return comPortName;
    }

    public String getComServerName() {
        return comServerName;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public long getDiscoveryProtocolId() {
        return discoveryProtocolId;
    }

}