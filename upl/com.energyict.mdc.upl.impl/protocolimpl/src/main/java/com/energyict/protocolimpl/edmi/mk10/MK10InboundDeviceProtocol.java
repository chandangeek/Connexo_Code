package com.energyict.protocolimpl.edmi.mk10;

import com.energyict.genericprotocolimpl.edmi.mk10.packets.PushPacket;
import com.energyict.mdc.protocol.exceptions.CommunicationException;
import com.energyict.mdc.protocol.exceptions.InboundFrameException;
import com.energyict.mdc.protocol.inbound.AbstractDiscover;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.SerialNumberDeviceIdentifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Inbound device discovery created for the MK10 protocol
 * In this case, a meter opens an inbound connection to the comserver and pushes a 'HEARTBEAT' packet, using the UDP protocol.
 * We should take in the 'HEARTBEAT' packet and parse it to know which device is knocking. No response from comserver is required.
 * All requests are sent in the normal protocol session (e.g. fetch meter data).
 * <p/>
 *
 * @author: sva
 * @since: 29/10/12 (10:34)
 */
public class MK10InboundDeviceProtocol extends AbstractDiscover {

    private static final int DEFAULT_DELAY_MILLIS = 10;

    SerialNumberDeviceIdentifier deviceIdentifier;

    @Override
    public DiscoverResultType doDiscovery() {
        try {
            PushPacket packet = PushPacket.getPushPacket(readFrame());
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

    /**
     * Read in a frame,
     * implemented by reading bytes until a timeout occurs.
     *
     * @return the partial frame
     * @throws com.energyict.mdc.protocol.exceptions.InboundFrameException
     *          in case of timeout after x retries
     */
    private byte[] readFrame() throws InboundFrameException {
        getComChannel().startReading();
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        long timeoutMoment = System.currentTimeMillis() + getTimeOutProperty();
        int retryCount = 0;

        while (true) {    //Read until a timeout occurs
            if (getComChannel().available() > 0) {
                byteStream.write(readByte());
            } else {
                delay();
            }

            if (System.currentTimeMillis() > timeoutMoment) {
                if (byteStream.size() != 0) {
                    return byteStream.toByteArray();    //Stop listening, return the result
                }
                retryCount++;
                timeoutMoment = System.currentTimeMillis() + getTimeOutProperty();
                if (retryCount > getRetriesProperty()) {
                    throw InboundFrameException.timeout("Timeout while waiting for inbound frame, after " + getTimeOutProperty() + " ms, using " + getRetriesProperty() + " retries.");
                }
            }
        }
    }

    private byte readByte() {
        return (byte) getComChannel().read();
    }

    private void delay() {
        this.delay(DEFAULT_DELAY_MILLIS);
    }

    private void delay(int millis) throws InboundFrameException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
