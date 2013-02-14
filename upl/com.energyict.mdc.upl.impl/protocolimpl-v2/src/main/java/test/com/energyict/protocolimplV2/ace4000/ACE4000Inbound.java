package test.com.energyict.protocolimplV2.ace4000;

import com.energyict.mdc.meterdata.CollectedData;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.identifiers.LogBookIdentifierByDeviceAndObisCodeImpl;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.BinaryInboundDeviceProtocol;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdw.core.LogBookTypeFactory;
import test.com.energyict.protocolimplV2.ace4000.objects.ObjectFactory;

import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 16/10/12
 * Time: 10:22
 * Author: khe
 */
public class ACE4000Inbound extends ACE4000 implements BinaryInboundDeviceProtocol {

    private InboundDiscoveryContext context;

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
        List<CollectedData> collectedDatas = new ArrayList<CollectedData>();
        collectedDatas.addAll(getCollectedRegisters());
        collectedDatas.addAll(getObjectFactory().createCollectedLoadProfiles());

        CollectedLogBook deviceLogBook = getObjectFactory().getDeviceLogBook(new LogBookIdentifierByDeviceAndObisCodeImpl(getDeviceIdentifier(), LogBookTypeFactory.GENERIC_LOGBOOK_TYPE_OBISCODE));
        collectedDatas.add(deviceLogBook);

        return collectedDatas;
    }

    public String getVersion() {
        return "$Date$";
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
            objectFactory = new ObjectFactory(this);
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