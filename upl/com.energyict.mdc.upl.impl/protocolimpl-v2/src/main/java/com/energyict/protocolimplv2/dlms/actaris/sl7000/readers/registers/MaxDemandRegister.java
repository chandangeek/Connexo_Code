package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.ActarisSl7000;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;

import java.io.IOException;
import java.util.Date;

public class MaxDemandRegister implements ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> {

    private final CollectedRegisterBuilder collectedRegisterBuilder;
    private final ObisCode o;

    public MaxDemandRegister(CollectedRegisterBuilder collectedRegisterBuilder, ObisCode obisCode) {
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.o = obisCode;
    }

    @Override
    public CollectedRegister read(ActarisSl7000 protocol, OfflineRegister readingSpecs) {
        try {
            ProfileGeneric profileGeneric = protocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(o);
            DataContainer buffer = profileGeneric.getBuffer();
            long value = buffer.getRoot().getStructure(0).getValue(0);
            ScalerUnit scalerUnit = new ScalerUnit(buffer.getRoot().getStructure(0).getStructure(1).getInteger(0), buffer.getRoot().getStructure(0).getStructure(1).getInteger(1));
            Date date = buffer.getRoot().getStructure(0).getOctetString(2).toDate(protocol.getTimeZone());
            return collectedRegisterBuilder.createCollectedRegister(readingSpecs, new RegisterValue(readingSpecs, new Quantity(value, scalerUnit.getEisUnit()), date));
        } catch (IOException e) {
            return collectedRegisterBuilder.createCollectedRegister(readingSpecs, ResultType.DataIncomplete, e.getMessage());
        }
    }

    @Override
    public boolean isApplicable(ObisCode obisCode) {
        return o.equals(obisCode);
    }
}
