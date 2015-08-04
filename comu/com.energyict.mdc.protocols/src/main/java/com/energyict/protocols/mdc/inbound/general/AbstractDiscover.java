package com.energyict.protocols.mdc.inbound.general;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.InboundFrameException;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.inbound.BinaryInboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.DiscoverInfo;
import com.energyict.mdc.protocol.api.inbound.IdentificationFactory;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocols.mdc.inbound.general.frames.AbstractInboundFrame;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;
import com.energyict.protocols.util.ProtocolImplFactory;
import com.energyict.protocols.util.ProtocolInstantiator;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract super class containing common elements (properties, connection,...) for the 3 discover protocols.
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/06/12
 * Time: 16:50
 * Author: khe
 */
public abstract class AbstractDiscover implements BinaryInboundDeviceProtocol {

    private static final int TIMEOUT_DEFAULT = 10000;          //TODO are these defaults OK ?
    private static final int RETRIES_DEFAULT = 2;
    private ComChannel comChannel;
    private TypedProperties typedProperties;
    private DeviceIdentifier deviceIdentifier = null;
    private List<CollectedData> collectedDatas = null;
    private InboundConnection inboundConnection = null;
    private InboundDiscoveryContext context;
    private final PropertySpecService propertySpecService;
    private final IssueService issueService;
    private final MdcReadingTypeUtilService readingTypeUtilService;
    private final Thesaurus thesaurus;
    private final IdentificationService identificationService;

    protected AbstractDiscover(PropertySpecService propertySpecService, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, Thesaurus thesaurus, IdentificationService identificationService) {
        super();
        this.propertySpecService = propertySpecService;
        this.issueService = issueService;
        this.readingTypeUtilService = readingTypeUtilService;
        this.thesaurus = thesaurus;
        this.identificationService = identificationService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected IssueService getIssueService() {
        return issueService;
    }

    protected MdcReadingTypeUtilService getReadingTypeUtilService() {
        return readingTypeUtilService;
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

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
        setDeviceIdentifier(this.identificationService.createDeviceIdentifierByCallHomeId(serialNumber));
    }

    protected void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public List<CollectedData> getCollectedData(OfflineDevice device) {
        return collectedDatas;
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        this.typedProperties = TypedProperties.copyOf(properties);
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(this.getPropertySpecService().basicPropertySpec(this.thesaurus.getString(MessageSeeds.TIMEOUT.getKey(), "Timeout"), false, new BigDecimalFactory()));
        propertySpecs.add(this.getPropertySpecService().basicPropertySpec(this.thesaurus.getString(MessageSeeds.RETRIES.getKey(), "Retries"), false, new BigDecimalFactory()));
        return propertySpecs;
    }

    @Override
    public PropertySpec getPropertySpec (String name) {
        for (PropertySpec propertySpec : this.getPropertySpecs()) {
            if (name.equals(propertySpec)) {
                return propertySpec;
            }
        }
        return null;
    }

    public int getTimeOutProperty() {
        return getTypedProperties().getIntegerProperty(this.thesaurus.getString(MessageSeeds.TIMEOUT.getKey(), "Timeout"), new BigDecimal(TIMEOUT_DEFAULT)).intValue();
    }

    public int getRetriesProperty() {
        return getTypedProperties().getIntegerProperty(this.thesaurus.getString(MessageSeeds.RETRIES.getKey(), "Retries"), new BigDecimal(RETRIES_DEFAULT)).intValue();
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
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    protected ProtocolInstantiator processProtocolInstantiator(String meterProtocolClass) {
        try {
            return ProtocolImplFactory.getProtocolInstantiator(meterProtocolClass);
        } catch (IOException e) {
            throw new ProtocolCreationException(MessageSeeds.UNSUPPORTED_LEGACY_PROTOCOL_TYPE, meterProtocolClass);
        }
    }

    protected String processMeterProtocolClass(String identificationFrame, IdentificationFactory identificationFactory) {
        try {
            return identificationFactory.getMeterProtocolClass(identificationFrame);
        } catch (IOException e) {
            throw new InboundFrameException(MessageSeeds.INBOUND_UNEXPECTED_FRAME, e, identificationFrame, e.getMessage());
        }
    }

    protected IdentificationFactory processIdentificationFactory() {
        try {
            return ProtocolImplFactory.getIdentificationFactory();
        } catch (IOException e) {
            throw new ProtocolCreationException(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, IdentificationFactory.class.getName());
        }
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        //No responses for the I and DoubleI Discover
    }

    public IdentificationService getIdentificationService() {
        return identificationService;
    }
}