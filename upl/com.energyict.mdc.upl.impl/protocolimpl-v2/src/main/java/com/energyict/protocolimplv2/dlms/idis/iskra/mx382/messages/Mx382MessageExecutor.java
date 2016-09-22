package com.energyict.protocolimplv2.dlms.idis.iskra.mx382.messages;

import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am130.messages.AM130MessageExecutor;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by cisac on 1/26/2016.
 */
public class Mx382MessageExecutor extends AM130MessageExecutor {

    public Mx382MessageExecutor(AbstractDlmsProtocol protocol) {
        super(protocol);
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
