package com.energyict.protocols.mdc.inbound.general;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocol.ProtocolInstantiator;
import com.energyict.protocol.meteridentification.IdentificationFactory;
import com.energyict.protocolimplv2.MdcManager;

/**
 * In the case of IDiscover, a meter opens an inbound connection to the comserver but it doesn't send any frames.
 * We send a request (I frame) for identification to know which Device and schedule has to be executed.
 * Extra requests are sent in the normal protocol session to fetch meter data.
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/06/12
 * Time: 14:50
 */
public class IframeDiscover extends AbstractDiscover {

    @Override
    public DiscoverResultType doDiscovery ()  {
        try {
            ComChannel comChannel = this.getComChannel();
            this.setInboundConnection(new InboundConnection(comChannel, getTimeOutProperty(), getRetriesProperty()));
            String identificationFrame = getInboundConnection().sendIRequestAndReadResponse();
            IdentificationFactory identificationFactory = processIdentificationFactory();
            String meterProtocolClass = processMeterProtocolClass(identificationFrame, identificationFactory);
            ProtocolInstantiator protocolInstantiator = processProtocolInstantiator(meterProtocolClass);
            this.setSerialNumber(requestSerialNumber(comChannel, protocolInstantiator));

            return DiscoverResultType.IDENTIFIER;
        } catch (InboundTimeOutException e) {
            throw MdcManager.getComServerExceptionFactory().createInboundTimeOutException(e.getMessage());
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-08-27 11:11:04 +0200 (Tue, 27 Aug 2013) $";
    }

}