package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

public class WebRTUKPDsmr23MessageExecutor extends Dsmr23MessageExecutor {

    public WebRTUKPDsmr23MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, keyAccessorTypeExtractor);
    }

    @Override
    protected ActivityCalendarController newActivityCalendarController() {
        return new WebRTUKPDLMSActivityCalendarController(getCosemObjectFactory(), getProtocol().getDlmsSession().getTimeZone(), false);
    }

}
