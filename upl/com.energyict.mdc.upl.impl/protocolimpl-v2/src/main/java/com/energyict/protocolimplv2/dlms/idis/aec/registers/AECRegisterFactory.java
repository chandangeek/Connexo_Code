package com.energyict.protocolimplv2.dlms.idis.aec.registers;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;

import java.util.ArrayList;
import java.util.List;

public class AECRegisterFactory extends AM130RegisterFactory {
    public AECRegisterFactory(AM130 am130, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(am130, collectedDataFactory, issueFactory);
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> offlineRegisters) {
        List<OfflineRegister> copyOfOfflineRegisters = new ArrayList<>();
        copyOfOfflineRegisters.addAll( offlineRegisters );

        return super.readRegisters(copyOfOfflineRegisters);
    }
}
