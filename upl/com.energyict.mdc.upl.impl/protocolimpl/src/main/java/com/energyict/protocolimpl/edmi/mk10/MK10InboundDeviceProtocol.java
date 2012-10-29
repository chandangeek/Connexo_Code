package com.energyict.protocolimpl.edmi.mk10;

import com.energyict.genericprotocolimpl.edmi.mk10.packets.PushPacket;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.exceptions.CommunicationException;
import com.energyict.mdc.protocol.inbound.AbstractDiscover;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.SerialNumberDeviceIdentifier;
import com.energyict.mdc.protocol.inbound.core.InboundConnection;

import java.io.IOException;

/**
 * Inbound device discovery created for the MK10 protocol
 * In this case, a meter opens an inbound connection to the comserver and pushes a 'HEARTBEAT' packet, using the UDP protocol.
 * We should take in the 'HEARTBEAT' packet and parse it to know which device is knocking. No response from comserver is required.
 * All requests are sent in the normal protocol session (e.g. fetch meter data).
 * <p/>
 * @author: sva
 * @since: 29/10/12 (10:34)
 */
public class MK10InboundDeviceProtocol extends AbstractDiscover {

    SerialNumberDeviceIdentifier deviceIdentifier;

    @Override
    public DiscoverResultType doDiscovery() {
        try {
            ComChannel comChannel = this.getComChannel();
            this.setInboundConnection(new InboundConnection(comChannel, getTimeOutProperty(), getRetriesProperty()));
            byte[] packetBytes = getInboundConnection().readVariableFrameAsByteArray();
            PushPacket packet = PushPacket.getPushPacket(packetBytes);
            switch (packet.getPushPacketType()) {
                case README:
                case HEARTBEAT:
                    setDeviceIdentifier(packet.getSerial());
                    return DiscoverResultType.IDENTIFIER;
                default:
                    throw new IOException("The received packet is unsupported in the current protocol [" + packet.toString() + "].");
            }
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        // not needed
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String serialNumber) {
        this.deviceIdentifier = new SerialNumberDeviceIdentifier(serialNumber);
    }

    @Override
    public String getVersion() {
        return "$Date: 2012-10-26 14:42:27 +0200 (vr, 26 okt 2012) $";
    }
}
