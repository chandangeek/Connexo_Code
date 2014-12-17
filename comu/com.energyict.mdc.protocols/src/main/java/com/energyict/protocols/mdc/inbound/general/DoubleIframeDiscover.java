package com.energyict.protocols.mdc.inbound.general;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.exceptions.InboundFrameException;
import com.energyict.mdc.protocol.api.inbound.IdentificationFactory;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;
import com.energyict.protocols.util.ProtocolInstantiator;

import javax.inject.Inject;

/**
 * In the case of DoubleIframeDiscover, a meter opens an inbound connection to the comserver but it doesn't send any frames.
 * We send a request (II frame) for identification to know which Device and schedule has to be executed.
 * Extra requests are sent in the normal protocol session to fetch meter data.
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/06/12
 * Time: 14:50
 */
public class DoubleIframeDiscover extends AbstractDiscover {

    @Inject
    public DoubleIframeDiscover(PropertySpecService propertySpecService, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, Thesaurus thesaurus, IdentificationService identificationService) {
        super(propertySpecService, issueService, readingTypeUtilService, thesaurus, identificationService);
    }

    @Override
    public DiscoverResultType doDiscovery() {
        try {
            ComChannel comChannel = this.getComChannel();
            this.setInboundConnection(new InboundConnection(comChannel, getTimeOutProperty(), getRetriesProperty(), this.getIssueService(), this.getReadingTypeUtilService(), this.getThesaurus(), getIdentificationService()));
            String identificationFrame = getInboundConnection().sendDoubleIRequestAndReadResponse();
            IdentificationFactory identificationFactory = processIdentificationFactory();
            String meterProtocolClass = processMeterProtocolClass(identificationFrame, identificationFactory);
            ProtocolInstantiator protocolInstantiator = processProtocolInstantiator(meterProtocolClass);
            this.setSerialNumber(requestSerialNumber(comChannel, protocolInstantiator));
            return DiscoverResultType.IDENTIFIER;
        } catch (InboundTimeOutException e) {
            throw new InboundFrameException(MessageSeeds.INBOUND_TIMEOUT, e.getMessage());
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-08-27 11:11:04 +0200 (Tue, 27 Aug 2013) $";
    }

}