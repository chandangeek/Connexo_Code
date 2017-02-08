package com.energyict.protocolimpl.edmi.mk10;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.BinaryInboundDeviceProtocol;
import com.energyict.mdc.upl.InboundDeviceProtocol;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.InboundFrameException;
import com.energyict.protocol.exception.ProtocolRuntimeException;
import com.energyict.protocolimpl.edmi.mk10.packets.PushPacket;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Inbound device discovery created for the MK10 protocol
 * In this case, a meter opens an inbound connection to the comserver and pushes a 'HEARTBEAT' packet, using the UDP protocol.
 * We should take in the 'HEARTBEAT' packet and parse it to know which device is knocking. No response from comserver is required.
 * All requests are sent in the normal protocol session (e.g. fetch meter data).
 * <p/>
 *
 * @author sva
 * @since 29/10/12 (10:34)
 */
public class MK10InboundDeviceProtocol implements BinaryInboundDeviceProtocol {

    private static final String TIMEOUT_KEY = "Timeout";
    private static final String RETRIES_KEY = "Retries";

    private static final Duration TIMEOUT_DEFAULT = Duration.ofSeconds(10);
    private static final BigDecimal RETRIES_DEFAULT = new BigDecimal(2);

    private final PropertySpecService propertySpecService;
    private DeviceIdentifier deviceIdentifier;
    private InboundDiscoveryContext context;
    private ComChannel comChannel;
    private TypedProperties typedProperties;

    public MK10InboundDeviceProtocol(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return context;
    }

    @Override
    public String getAdditionalInformation() {
        return ""; //No additional info available
    }

    @Override
    public InboundDeviceProtocol.DiscoverResultType doDiscovery() {
        PushPacket packet = PushPacket.getPushPacket(readFrame());
        switch (packet.getPushPacketType()) {
            case README:
            case HEARTBEAT:
                setDeviceIdentifier(packet.getSerial());
                return InboundDeviceProtocol.DiscoverResultType.IDENTIFIER;
            default:
                throw InboundFrameException.unexpectedFrame(packet.toString(), "The received packet is unsupported in the current protocol");
        }
    }

    /**
     * Read in a frame,
     * implemented by reading bytes until a timeout occurs.
     *
     * @return the partial frame
     * @throws ProtocolRuntimeException in case of timeout after x retries
     */
    private byte[] readFrame() {
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
                    throw InboundFrameException.timeoutException(String.format("Timeout while waiting for inbound frame, after %d ms, using %d retries.", getTimeOutProperty(), getRetriesProperty()));
                }
            }
        }
    }

    private byte readByte() {
        return (byte) getComChannel().read();
    }

    private void delay() {
        this.delay(100);
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
    }

    @Override
    public void provideResponse(InboundDeviceProtocol.DiscoverResponseType responseType) {
        // not needed
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        return Collections.emptyList();
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }

    public void setDeviceIdentifier(String serialNumber) {
        this.deviceIdentifier = new DialHomeIdDeviceIdentifier(serialNumber);
    }

    @Override
    public String getVersion() {
        return "$Date: Mon Nov 14 14:20:36 2016 +0100 $";
    }

    @Override
    public void setUPLProperties(com.energyict.mdc.upl.properties.TypedProperties properties) {
        this.typedProperties = TypedProperties.copyOf(properties);
    }

    @Override
    public void initComChannel(ComChannel comChannel) {
        this.comChannel = comChannel;
    }

    protected ComChannel getComChannel() {
        return comChannel;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                UPLPropertySpecFactory
                        .specBuilder(TIMEOUT_KEY, false, this.propertySpecService::durationSpec)
                        .setDefaultValue(TIMEOUT_DEFAULT)
                        .finish(),
                UPLPropertySpecFactory
                        .specBuilder(RETRIES_KEY, false, this.propertySpecService::bigDecimalSpec)
                        .setDefaultValue(RETRIES_DEFAULT)
                        .finish());
    }

    public int getTimeOutProperty() {
        return (int) getTypedProperties().getTypedProperty(TIMEOUT_KEY, TIMEOUT_DEFAULT).toMillis();
    }

    public int getRetriesProperty() {
        return getTypedProperties().getIntegerProperty(RETRIES_KEY, RETRIES_DEFAULT).intValue();
    }

    public TypedProperties getTypedProperties() {
        if (typedProperties == null) {
            typedProperties = TypedProperties.empty();
        }
        return typedProperties;
    }

}