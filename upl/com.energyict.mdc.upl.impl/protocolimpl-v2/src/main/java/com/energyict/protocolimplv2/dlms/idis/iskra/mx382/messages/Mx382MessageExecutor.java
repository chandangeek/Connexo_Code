package com.energyict.protocolimplv2.dlms.idis.iskra.mx382.messages;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;

import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am130.messages.AM130MessageExecutor;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;

import java.io.IOException;

/**
 * Created by cisac on 1/26/2016.
 */
public class Mx382MessageExecutor extends AM130MessageExecutor {

    public Mx382MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected CollectedMessage executeMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(DeviceActionMessage.BILLING_RESET)) {
            resetBilling();
        } else {
            super.executeMessage(pendingMessage, collectedMessage);
        }
        return collectedMessage;
    }

    public void resetBilling() throws IOException {
        ScriptTable demandResetScriptTable = getCosemObjectFactory().getScriptTable(ObisCode.fromString("0.0.10.0.1.255"));
        demandResetScriptTable.execute(1);
        demandResetScriptTable.execute(2);
    }

}
