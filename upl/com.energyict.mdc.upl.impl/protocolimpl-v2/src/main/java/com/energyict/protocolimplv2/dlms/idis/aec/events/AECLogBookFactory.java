package com.energyict.protocolimplv2.dlms.idis.aec.events;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.idis.aec.AEC;
import com.energyict.protocolimplv2.dlms.idis.am130.events.AM130LogBookFactory;

public class AECLogBookFactory extends AM130LogBookFactory<AEC> {

    public AECLogBookFactory(AEC protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        supportedLogBooks.remove(STANDARD_EVENT_LOG);
        STANDARD_EVENT_LOG = ObisCode.fromString("1.1.99.98.0.255");
        supportedLogBooks.add(STANDARD_EVENT_LOG);
    }

}
