package com.energyict.protocols.mdc.inbound.general;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.exceptions.InboundFrameException;
import com.energyict.mdc.protocol.api.inbound.IdentificationFactory;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;
import com.energyict.protocols.util.ProtocolInstantiator;

import javax.inject.Inject;
import java.time.Clock;

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

    private final Clock clock;
    private final CollectedDataFactory collectedDataFactory;
    private final MeteringService meteringService;

    @Inject
    public IframeDiscover(PropertySpecService propertySpecService, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, Thesaurus thesaurus, IdentificationService identificationService, Clock clock, CollectedDataFactory collectedDataFactory, MeteringService meteringService) {
        super(propertySpecService, issueService, readingTypeUtilService, thesaurus, identificationService);
        this.clock = clock;
        this.collectedDataFactory = collectedDataFactory;
        this.meteringService = meteringService;
    }

    @Override
    public DiscoverResultType doDiscovery ()  {
        try {
            ComChannel comChannel = this.getComChannel();
            this.setInboundConnection(new InboundConnection(comChannel, getTimeOutProperty(), getRetriesProperty(), this.clock, this.getIssueService(), this.getReadingTypeUtilService(), this.collectedDataFactory, meteringService, this.getThesaurus(), getIdentificationService()));
            String identificationFrame = getInboundConnection().sendIRequestAndReadResponse();
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