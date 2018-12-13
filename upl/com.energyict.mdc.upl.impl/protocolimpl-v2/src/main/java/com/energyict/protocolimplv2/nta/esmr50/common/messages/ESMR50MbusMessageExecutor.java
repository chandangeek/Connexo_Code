package com.energyict.protocolimplv2.nta.esmr50.common.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40MbusMessageExecutor;

public class ESMR50MbusMessageExecutor extends Dsmr40MbusMessageExecutor {


    public ESMR50MbusMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }
}
