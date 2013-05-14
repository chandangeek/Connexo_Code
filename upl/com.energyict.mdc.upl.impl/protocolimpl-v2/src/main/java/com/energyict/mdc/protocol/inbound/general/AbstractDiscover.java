package com.energyict.mdc.protocol.inbound.general;

import com.energyict.cpo.Environment;
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
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierBySerialNumber;

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

    private static final String TIMEOUT_KEY = Environment.getDefault().getTranslation("protocol.timeout");
    private static final String RETRIES_KEY = Environment.getDefault().getTranslation("protocol.retries");

    private static final int TIMEOUT_DEFAULT = 10000;          //TODO are these defaults OK ?
    private static final int RETRIES_DEFAULT = 2;
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
        return deviceIdentifier;
    }

    protected void setSerialNumber (String serialNumber) {
        setDeviceIdentifier(new DeviceIdentifierBySerialNumber(serialNumber));
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
        propertySpecs.add(PropertySpecFactory.bigDecimalPropertySpec(TIMEOUT_KEY));
        propertySpecs.add(PropertySpecFactory.bigDecimalPropertySpec(RETRIES_KEY));
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