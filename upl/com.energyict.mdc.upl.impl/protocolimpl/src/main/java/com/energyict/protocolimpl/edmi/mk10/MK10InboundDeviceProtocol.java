package com.energyict.protocolimpl.edmi.mk10;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.BinaryInboundDeviceProtocol;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.InboundFrameException;
import com.energyict.protocolimpl.edmi.mk10.packets.PushPacket;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class MK10InboundDeviceProtocol implements BinaryInboundDeviceProtocol {

    private static final String TIMEOUT_KEY = "Timeout";
    private static final String RETRIES_KEY = "Retries";

    private static final TimeDuration TIMEOUT_DEFAULT = TimeDuration.seconds(10);
    private static final BigDecimal RETRIES_DEFAULT = new BigDecimal(2);

    private DeviceIdentifier deviceIdentifier;
    private InboundDiscoveryContext context;
    private ComChannel comChannel;
    private TypedProperties typedProperties;


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
    public DiscoverResultType doDiscovery() {
        PushPacket packet = PushPacket.getPushPacket(readFrame());
        switch (packet.getPushPacketType()) {
            case README:
            case HEARTBEAT:
                setDeviceIdentifier(packet.getSerial());
                return DiscoverResultType.IDENTIFIER;
            default:
                throw InboundFrameException.unexpectedFrame(packet.toString(), "The received packet is unsupported in the current protocol");
        }
    }

    /**
     * Read in a frame,
     * implemented by reading bytes until a timeout occurs.
     *
     * @return the partial frame
     * @throws com.energyict.protocol.exceptions.ProtocolRuntimeException in case of timeout after x retries
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
    public void provideResponse(DiscoverResponseType responseType) {
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
        return "$Date: 2016-05-31 16:24:54 +0300 (Tue, 31 May 2016)$";
    }

    @Override
    public void addProperties(TypedProperties properties) {
        this.typedProperties = properties;
    }

    @Override
    public void initComChannel(ComChannel comChannel) {
        this.comChannel = comChannel;
    }

    protected ComChannel getComChannel() {
        return comChannel;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return new ArrayList<>();
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(TIMEOUT_KEY, TIMEOUT_DEFAULT));
        propertySpecs.add(PropertySpecFactory.bigDecimalPropertySpec(RETRIES_KEY, RETRIES_DEFAULT));
        return propertySpecs;
    }

    public int getTimeOutProperty() {
        return (int) getTypedProperties().getTypedProperty(TIMEOUT_KEY, TIMEOUT_DEFAULT).getMilliSeconds();
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
