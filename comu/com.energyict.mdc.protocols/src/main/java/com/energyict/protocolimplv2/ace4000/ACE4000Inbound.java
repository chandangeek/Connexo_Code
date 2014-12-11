package com.energyict.protocolimplv2.ace4000;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.LogBookFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.inbound.BinaryInboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.InboundDiscoveryContext;
import com.energyict.protocolimplv2.ace4000.objects.ObjectFactory;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierByObisCodeAndDevice;

import javax.inject.Inject;
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

    private final MdcReadingTypeUtilService readingTypeUtilService;
    private InboundDiscoveryContext context;

    @Inject
    public ACE4000Inbound(MdcReadingTypeUtilService readingTypeUtilService, PropertySpecService propertySpecService) {
        super(propertySpecService);
        this.readingTypeUtilService = readingTypeUtilService;
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
        List<CollectedData> collectedDatas = new ArrayList<>();
        collectedDatas.addAll(getCollectedRegisters());
        collectedDatas.addAll(getObjectFactory().createCollectedLoadProfiles());

        CollectedLogBook deviceLogBook = getObjectFactory().getDeviceLogBook(new LogBookIdentifierByObisCodeAndDevice(getDeviceIdentifier(), LogBookFactory.GENERIC_LOGBOOK_TYPE_OBISCODE));
        collectedDatas.add(deviceLogBook);

        return collectedDatas;
    }

    public String getVersion() {
        return "$Date: 2013-05-14 15:29:42 +0200 (Die, 14 Mai 2013) $";
    }

    /**
     * Send an ack for the received frames, if the device has been found in EiServer
     */
    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        if (responseType == DiscoverResponseType.SUCCESS) {
            getObjectFactory().provideReponseAfterInbound();
        } else {
            //TODO ?
        }
    }

    public ObjectFactory getObjectFactory() {
        if (objectFactory == null) {
            objectFactory = new ObjectFactory(this, this.readingTypeUtilService);
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
}