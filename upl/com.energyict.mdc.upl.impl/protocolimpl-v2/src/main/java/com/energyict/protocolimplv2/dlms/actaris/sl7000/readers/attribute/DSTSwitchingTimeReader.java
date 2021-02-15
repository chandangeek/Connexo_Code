package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.attribute;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.Data;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.ActarisSl7000;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;

import java.io.IOException;

public class DSTSwitchingTimeReader implements ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> {

    private static final ObisCode O1 = ObisCode.fromString("0.0.131.0.6.255");
    private static final ObisCode O2 = ObisCode.fromString("0.0.131.0.7.255");

    private final CollectedRegisterBuilder collectedRegisterBuilder;

    public DSTSwitchingTimeReader(CollectedRegisterBuilder collectedRegisterBuilder) {
        this.collectedRegisterBuilder = collectedRegisterBuilder;
    }

    @Override
    public CollectedRegister read(ActarisSl7000 protocol, OfflineRegister register) {
        try {
            int element = O1.equals(register.getObisCode()) ? 0 : 1;
            final Data data = protocol.getDlmsSession().getCosemObjectFactory().getData(O1);
            Structure dataSequence = (Structure) data.getValueAttr();
            Structure innerStruct = ((Array) dataSequence.getStructure().getDataType(1)).getDataType(element).getStructure();
            byte[] dateAndTime = innerStruct.getDataType(0).getOctetString().getOctetStr();

            String text = "";
            text += (dateAndTime[0] == 0x7F) ? "Year: *" : "Year: " + dateAndTime[0];
            text += " - ";
            text += (dateAndTime[1] == 0x7F) ? "month: *" : "month: " + dateAndTime[1];
            text += " - ";
            text += (dateAndTime[2] == 0x7F) ? "day of month: *" : "day of month: " + dateAndTime[2];
            text += " - ";
            text += (dateAndTime[3] == 0x7F) ? "day of week: *" : "day of week: " + (dateAndTime[3] == 7 ? 0 : dateAndTime[3]);
            text += " - ";
            text += "hour: " + dateAndTime[4];
            return collectedRegisterBuilder.createCollectedRegister(register, new RegisterValue(register,  text));
        } catch (IOException e) {
            return collectedRegisterBuilder.createCollectedRegister(register, ResultType.DataIncomplete, e.getMessage());
        }
    }

    @Override
    public boolean isApplicable(ObisCode obisCode) {
        return O1.equals(obisCode)| O2.equals(obisCode);
    }
}
