package com.energyict.protocolimplv2.ace4000;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.BinaryInboundDeviceProtocol;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.ace4000.objects.ObjectFactory;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierByObisCodeAndDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 16/10/12
 * Time: 10:22
 * Author: khe
 */
public class ACE4000Inbound extends ACE4000 implements BinaryInboundDeviceProtocol {

    private ObisCode GENERIC_LOGBOOK_TYPE_OBISCODE = ObisCode.fromString("0.0.99.98.0.255");

    private InboundDiscoveryContext context;
    private final CollectedDataFactory collectedDataFactory;

    public ACE4000Inbound(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory) {
        super(propertySpecService);
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return context;
    }

    public Logger getLogger() {
        return getContext().getLogger();
    }

    @Override
    public String getAdditionalInformation() {
        return ""; //No additional info available
    }

    /**
     * Read in the inbound frames (don't ack them yet), parse them and return the result type.
     *
     * @return DiscoverResultType
     */
    @Override
    public DiscoverResultType doDiscovery() {
        List<String> messages = getAce4000Connection().readFrames(true);  //Keep reading frames until timeout occurs
        for (String message : messages) {
            getObjectFactory().parseXML(message);       //This parses the messages and adds the results to the collected data
        }
        // end of the inbound UDP listen loop. The outbound ACE4000 protocol does the rest of the requests

        if (getCollectedData().isEmpty()) {
            return DiscoverResultType.IDENTIFIER;
        } else {
            return DiscoverResultType.DATA;
        }
    }

    public List<CollectedData> getCollectedData() {
        List<CollectedData> collectedDatas = new ArrayList<CollectedData>();
        collectedDatas.addAll(getCollectedRegisters());
        if (!getObjectFactory().getLoadProfile().getProfileData().getIntervalDatas().isEmpty()) {
            collectedDatas.addAll(getObjectFactory().createCollectedLoadProfiles(DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE));
        }

        if (!getObjectFactory().getAllMeterEvents().isEmpty()) {
            CollectedLogBook deviceLogBook = getObjectFactory().getDeviceLogBook(new LogBookIdentifierByObisCodeAndDevice(getDeviceIdentifier(), GENERIC_LOGBOOK_TYPE_OBISCODE));
            collectedDatas.add(deviceLogBook);
        }

        return collectedDatas;
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return true;
    }

    public String getVersion() {
        return "$Date: 2016-06-29 13:42:56 +0200 (Wed, 29 Jun 2016)$";
    }

    /**
     * Send an ack for the received data, if it was successfully stored in EIServer
     */
    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        if (responseType == DiscoverResponseType.SUCCESS) {
            getObjectFactory().provideReponseAfterInbound();
        }
    }

    public ObjectFactory getObjectFactory() {
        if (objectFactory == null) {
            objectFactory = new ObjectFactory(this, this.collectedDataFactory);
            objectFactory.setInbound(true);  //Important to store the parsed data in the list of collecteddatas
        }
        return objectFactory;
    }

    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getDefault();   //Not used for inbound protocol
    }

    @Override
    public void initComChannel(ComChannel comChannel) {
        setAce4000Connection(new ACE4000Connection(comChannel, this, true));
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return new DialHomeIdDeviceIdentifier(serialNumber);
    }

    /**
     * No configured serial number available at this point (we're still discovering the device)
     * Return the serial number that was received from the device instead.
     */
    @Override
    public String getConfiguredSerialNumber() {
        return serialNumber;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return super.getRequiredProperties();
    }

    @Override
    public void addProperties(TypedProperties properties) {
    }

}