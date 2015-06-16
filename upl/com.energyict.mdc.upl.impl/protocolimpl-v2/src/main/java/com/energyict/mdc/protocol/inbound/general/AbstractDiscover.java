package com.energyict.mdc.protocol.inbound.general;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.BinaryInboundDeviceProtocol;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.inbound.general.frames.AbstractInboundFrame;
import com.energyict.protocol.ProtocolImplFactory;
import com.energyict.protocol.ProtocolInstantiator;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.meteridentification.IdentificationFactory;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract super class containing common elements (properties, connection,...) for the 3 discover protocols
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/06/12
 * Time: 16:50
 * Author: khe
 */
public abstract class AbstractDiscover implements BinaryInboundDeviceProtocol {

    protected static final String TIMEOUT_KEY = "Timeout";
    protected static final String RETRIES_KEY = "Retries";

    private static final TimeDuration TIMEOUT_DEFAULT = TimeDuration.seconds(10);
    private static final BigDecimal RETRIES_DEFAULT = new BigDecimal(2);
    private ComChannel comChannel;
    private TypedProperties typedProperties;
    private DeviceIdentifier deviceIdentifier = null;
    private List<CollectedData> collectedDatas = null;
    private InboundConnection inboundConnection = null;
    private InboundDiscoveryContext context;

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
    public void addProperties(TypedProperties properties) {
        this.typedProperties = properties;
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
                new ArrayList<String>());

        try {
            return protocolInstantiator.getSerialNumber().getSerialNumber(discoverInfo);
        } catch (IOException e) {
            throw MdcManager.getComServerExceptionFactory().createUnExpectedProtocolError(e);
        }
    }

    protected ProtocolInstantiator processProtocolInstantiator(String meterProtocolClass) {
        try {
            return ProtocolImplFactory.getProtocolInstantiator(meterProtocolClass);
        } catch (IOException e) {
            throw MdcManager.getComServerExceptionFactory().createGenericReflectionError(e, IdentificationFactory.class);
        }
    }

    protected String processMeterProtocolClass(String identificationFrame, IdentificationFactory identificationFactory) {
        try {
            return identificationFactory.getMeterProtocolClass(identificationFrame);
        } catch (IOException e) {
            throw MdcManager.getComServerExceptionFactory().createUnExpectedInboundFrame(e, identificationFrame, e.getMessage());
        }
    }

    protected IdentificationFactory processIdentificationFactory() {
        try {
            return ProtocolImplFactory.getIdentificationFactory();
        } catch (IOException e) {
            throw MdcManager.getComServerExceptionFactory().createGenericReflectionError(e, IdentificationFactory.class);
        }
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        //No responses for the I and DoubleI Discover
    }
}