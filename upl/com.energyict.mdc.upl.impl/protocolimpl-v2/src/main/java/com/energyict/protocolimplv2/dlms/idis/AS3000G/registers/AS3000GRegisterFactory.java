package com.energyict.protocolimplv2.dlms.idis.AS3000G.registers;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;
import com.energyict.protocolimplv2.dlms.idis.am540.registers.AM540RegisterFactory;

public class AS3000GRegisterFactory extends AM540RegisterFactory {
    public AS3000GRegisterFactory(AbstractDlmsProtocol as3000g, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super((AM540) as3000g, collectedDataFactory, issueFactory);
    }
}
