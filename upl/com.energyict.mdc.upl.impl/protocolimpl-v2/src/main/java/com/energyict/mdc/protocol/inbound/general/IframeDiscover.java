package com.energyict.mdc.protocol.inbound.general;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.protocol.ProtocolInstantiator;
import com.energyict.protocol.exceptions.InboundFrameException;
import com.energyict.protocol.meteridentification.IdentificationFactory;

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

    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public IframeDiscover(CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
    }

    @Override
    public DiscoverResultType doDiscovery ()  {
        try {
            ComChannel comChannel = this.getComChannel();
            this.setInboundConnection(new InboundConnection(comChannel, getTimeOutProperty(), getRetriesProperty(), this.collectedDataFactory, this.issueFactory));
            String identificationFrame = getInboundConnection().sendIRequestAndReadResponse();
            IdentificationFactory identificationFactory = processIdentificationFactory();
            String meterProtocolClass = processMeterProtocolClass(identificationFrame, identificationFactory);
            ProtocolInstantiator protocolInstantiator = processProtocolInstantiator(meterProtocolClass);
            this.setSerialNumber(requestSerialNumber(comChannel, protocolInstantiator));

            return DiscoverResultType.IDENTIFIER;
        } catch (InboundTimeOutException e) {
            throw InboundFrameException.timeoutException(e, e.getMessage());
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

}