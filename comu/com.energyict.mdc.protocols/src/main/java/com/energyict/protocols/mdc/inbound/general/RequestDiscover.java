package com.energyict.protocols.mdc.inbound.general;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.elster.jupiter.nls.Thesaurus;

import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocols.mdc.inbound.general.frames.AbstractInboundFrame;

import javax.inject.Inject;

/**
 * In the case of RequestDiscover, a meter starts an inbound session and pushes its serial number and meter data.
 * There are no extra requests sent by the comserver.
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/06/12
 * Time: 14:50
 */
public class RequestDiscover extends AbstractDiscover {

    private final CollectedDataFactory collectedDataFactory;

    @Inject
    public RequestDiscover(PropertySpecService propertySpecService, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, Thesaurus thesaurus, IdentificationService identificationService, CollectedDataFactory collectedDataFactory) {
        super(propertySpecService, issueService, readingTypeUtilService, thesaurus, identificationService);
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public DiscoverResultType doDiscovery() {
        ComChannel comChannel = this.getComChannel();
        this.setInboundConnection(new InboundConnection(comChannel, getTimeOutProperty(), getRetriesProperty(), this.getIssueService(), this.getReadingTypeUtilService(), this.collectedDataFactory, this.getThesaurus(), getIdentificationService()));
        boolean notTimedOut = true;
        while (notTimedOut) {
            try {
                AbstractInboundFrame fullFrame = getInboundConnection().readAndAckInboundFrame();
                this.setSerialNumber(fullFrame.getSerialNumber());
                addCollectedData(fullFrame);

                if (fullFrame.isRequest() && fullFrame.getInboundParameters().getComPort() != null) {
                    //TODO send ESC commands?
                }
            } catch (InboundTimeOutException e) {
                notTimedOut = false;
            }
        }
        return DiscoverResultType.DATA;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-08-27 11:11:04 +0200 (Tue, 27 Aug 2013) $";
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        if (responseType == DiscoverResponseType.SUCCESS) {
            getInboundConnection().ackFrames();
        }
    }

}