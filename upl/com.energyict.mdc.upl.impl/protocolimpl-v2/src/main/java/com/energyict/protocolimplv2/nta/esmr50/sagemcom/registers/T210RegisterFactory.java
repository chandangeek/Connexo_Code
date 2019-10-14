package com.energyict.protocolimplv2.nta.esmr50.sagemcom.registers;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.ESMR50RegisterFactory;

public class T210RegisterFactory extends ESMR50RegisterFactory {

    public T210RegisterFactory(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }
}
