package com.energyict.protocolimpl.edmi.mk10;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.exceptions.ComServerExecutionException;
import com.energyict.mdc.protocol.api.exceptions.InboundFrameException;
import com.energyict.mdc.protocol.api.inbound.BinaryInboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.genericprotocolimpl.edmi.mk10.packets.PushPacket;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.protocols.mdc.services.impl.Bus;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

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
 * @author sva
 * @since 29/10/12 (10:34)
 */
public class MK10InboundDeviceProtocol implements BinaryInboundDeviceProtocol {

    private static final int TIMEOUT_DEFAULT = 10000;
    private static final int RETRIES_DEFAULT = 2;
    private static final String TIMEOUT_KEY = Bus.getThesaurus().getString(MessageSeeds.TIMEOUT.getKey(), "Timeout");
    private static final String RETRIES_KEY = Bus.getThesaurus().getString(MessageSeeds.RETRIES.getKey(), "Retries");

    private DeviceIdentifierBySerialNumber deviceIdentifier;
    private InboundDiscoveryContext context;
    private ComChannel comChannel;
    private TypedProperties typedProperties;

    @Override
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        // No usage for the PropertySpecService so far
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
    public DiscoverResultType doDiscovery() {
        PushPacket packet = PushPacket.getPushPacket(readFrame());
        switch (packet.getPushPacketType()) {
            case README:
            case HEARTBEAT:
                setDeviceIdentifier(packet.getSerial());
                return DiscoverResultType.IDENTIFIER;
            default:
                throw new InboundFrameException(MessageSeeds.INBOUND_UNEXPECTED_FRAME, packet.toString(), "The received packet is unsupported in the current protocol");
        }
    }

    /**
     * Read in a frame,
     * implemented by reading bytes until a timeout occurs.
     *
     * @return the partial frame
     * @throws ComServerExecutionException
     *          in case of timeout after x retries
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
                    throw new InboundFrameException(MessageSeeds.INBOUND_TIMEOUT, String.format("Timeout while waiting for inbound frame, after %d ms, using %d retries.", getTimeOutProperty(), getRetriesProperty()));
                }
            }
        }
    }

    private byte readByte() {
        return (byte) getComChannel().read();
    }

    private void delay() {
        this.delay(TIMEOUT_DEFAULT);
    }

    private void delay(int millis) {
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

    @Override
    public List<CollectedData> getCollectedData() {
        return Collections.emptyList();
    }

    public void setDeviceIdentifier(String serialNumber) {
        this.deviceIdentifier = new DeviceIdentifierBySerialNumber(serialNumber);
    }

    @Override
    public String getVersion() {
        return "$Date: 2012-10-26 14:42:27 +0200 (vr, 26 okt 2012) $";
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        this.typedProperties = properties;
    }

    @Override
    public void initComChannel(ComChannel comChannel) {
        this.comChannel = comChannel;
    }

    protected ComChannel getComChannel () {
        return comChannel;
    }

    @Override
    public PropertySpec getPropertySpec (String name) {
        return null;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        PropertySpecService propertySpecService = Bus.getPropertySpecService();
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(propertySpecService.basicPropertySpec(TIMEOUT_KEY, false, new BigDecimalFactory()));
        propertySpecs.add(propertySpecService.basicPropertySpec(RETRIES_KEY, false, new BigDecimalFactory()));
        return propertySpecs;
    }

    public int getTimeOutProperty() {
        return getTypedProperties().getIntegerProperty(TIMEOUT_KEY, new BigDecimal(TIMEOUT_DEFAULT)).intValue();
    }

    public int getRetriesProperty() {
        return getTypedProperties().getIntegerProperty(RETRIES_KEY, new BigDecimal(RETRIES_DEFAULT)).intValue();
    }

    public TypedProperties getTypedProperties() {
        if (typedProperties == null) {
            typedProperties = TypedProperties.empty();
        }
        return typedProperties;
    }
}
