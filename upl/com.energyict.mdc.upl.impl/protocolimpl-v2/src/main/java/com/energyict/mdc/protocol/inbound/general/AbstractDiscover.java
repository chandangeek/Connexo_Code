package com.energyict.mdc.protocol.inbound.general;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.general.frames.AbstractInboundFrame;
import com.energyict.mdc.upl.BinaryInboundDeviceProtocol;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.ProtocolImplFactory;
import com.energyict.protocol.ProtocolInstantiator;
import com.energyict.protocol.exceptions.CodingException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.InboundFrameException;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.meteridentification.IdentificationFactory;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract super class containing common elements (properties, connection,...)
 * for the 3 discover protocols.
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/06/12
 * Time: 16:50
 * Author: khe
 */
public abstract class AbstractDiscover implements BinaryInboundDeviceProtocol {

    protected static final String TIMEOUT_KEY = "Timeout";
    protected static final String RETRIES_KEY = "Retries";

    private static final Duration TIMEOUT_DEFAULT = Duration.ofSeconds(10);
    private static final BigDecimal RETRIES_DEFAULT = new BigDecimal(2);
    private final PropertySpecService propertySpecService;
    private ComChannel comChannel;
    private TypedProperties typedProperties;
    private DeviceIdentifier deviceIdentifier = null;
    private List<CollectedData> collectedDatas = null;
    private InboundConnection inboundConnection = null;
    private InboundDiscoveryContext context;

    protected AbstractDiscover(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
    }

    @Override
    public String getAdditionalInformation() {
        return ""; //No additional info available
    }

    @Override
    public InboundDiscoveryContext getContext () {
        return this.context;
    }

    @Override
    public void initComChannel(ComChannel comChannel) {
        this.comChannel = comChannel;
    }

    protected ComChannel getComChannel () {
        return comChannel;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier () {
        if (deviceIdentifier == null) {
            if (getInboundConnection() != null &&       // As fall-back try to use the serialNumber from inboundConnection
                    getInboundConnection().getSerialNumberPlaceHolder() != null &&
                    getInboundConnection().getSerialNumberPlaceHolder().getSerialNumber() != null) {
                setDeviceIdentifier(new DialHomeIdDeviceIdentifier(getInboundConnection().getSerialNumberPlaceHolder().getSerialNumber()));
            }
        }
        return deviceIdentifier;
    }

    protected void setSerialNumber (String serialNumber) {
        setDeviceIdentifier(new DialHomeIdDeviceIdentifier(serialNumber));
        getInboundConnection().updateSerialNumberPlaceHolder(serialNumber);
    }

    protected void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public List<CollectedData> getCollectedData() {
        return collectedDatas;
    }

    @Override
    public void setProperties(com.energyict.mdc.upl.properties.TypedProperties properties) {
        this.typedProperties = TypedProperties.copyOf(properties);
    }

    public void addProperties(TypedProperties properties) {
        this.typedProperties = properties;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
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

    public InboundConnection getInboundConnection() {
        return inboundConnection;
    }

    protected void setInboundConnection (InboundConnection inboundConnection) {
        this.inboundConnection = inboundConnection;
    }

    protected void addCollectedData(AbstractInboundFrame fullFrame) {
        if (collectedDatas == null) {
            collectedDatas = new ArrayList<>();
        }
        collectedDatas.addAll(fullFrame.getCollectedDatas());
    }

    //Methods used by both the DoubleIframeDiscover and the IframeDiscover
    protected String requestSerialNumber(ComChannel comChannel, ProtocolInstantiator protocolInstantiator) {
        DiscoverInfo discoverInfo = new DiscoverInfo(
                new SerialCommunicationChannelImpl(comChannel),
                null,
                -1,
                new ArrayList<>());

        try {
            return protocolInstantiator.getSerialNumber().getSerialNumber(discoverInfo);
        } catch (IOException e) {
            throw ConnectionCommunicationException.unExpectedProtocolError(e);
        }
    }

    protected ProtocolInstantiator processProtocolInstantiator(String meterProtocolClass) {
        try {
            return ProtocolImplFactory.getProtocolInstantiator(meterProtocolClass);
        } catch (IOException e) {
            throw CodingException.genericReflectionError(e, IdentificationFactory.class);
        }
    }

    protected String processMeterProtocolClass(String identificationFrame, IdentificationFactory identificationFactory) {
        try {
            return identificationFactory.getMeterProtocolClass(identificationFrame);
        } catch (IOException e) {
            throw InboundFrameException.unexpectedFrame(e, identificationFrame, e.getMessage());
        }
    }

    protected IdentificationFactory processIdentificationFactory() {
        try {
            return ProtocolImplFactory.getIdentificationFactory();
        } catch (IOException e) {
            throw CodingException.genericReflectionError(e, IdentificationFactory.class);
        }
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        //No responses for the I and DoubleI Discover
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }
}