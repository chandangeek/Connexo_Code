package com.energyict.protocolimplv2.dlms.idis.aec3phase.registers;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.idis.aec.registers.AECRegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;

public class AEC3PhaseRegisterFactory extends AECRegisterFactory {

    public AEC3PhaseRegisterFactory(AM130 am130, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(am130, collectedDataFactory, issueFactory);
    }

    @Override
    protected RegisterValue getRegisterValueForAlarms(OfflineRegister offlineRegister, AbstractDataType dataValue) {
        RegisterValue registerValue;
        if (dataValue.getVisibleString() != null) {
            registerValue = new RegisterValue(offlineRegister, dataValue.getVisibleString().getStr().trim());
        } else {
            registerValue = new RegisterValue(offlineRegister, new Quantity(dataValue.toBigDecimal(), Unit.getUndefined()));
        }
        return registerValue;
    }
}
