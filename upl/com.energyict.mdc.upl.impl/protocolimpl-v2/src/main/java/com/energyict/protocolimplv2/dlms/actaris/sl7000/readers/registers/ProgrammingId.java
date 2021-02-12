package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers;

import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.mdc.upl.NotInObjectListException;
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

public class ProgrammingId implements ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> {

    private static final ObisCode o = ObisCode.fromString("0.0.96.2.0.255");
    private final CollectedRegisterBuilder collectedRegisterBuilder;

    public ProgrammingId(CollectedRegisterBuilder collectedRegisterBuilder) {
        this.collectedRegisterBuilder = collectedRegisterBuilder;
    }

    @Override
    public CollectedRegister read(ActarisSl7000 protocol, OfflineRegister readingSpecs) {
        try {
            ExtendedRegister extendedRegister = protocol.getDlmsSession().getCosemObjectFactory().getExtendedRegister(readingSpecs.getObisCode());
            return collectedRegisterBuilder.createCollectedRegister(readingSpecs, new RegisterValue(readingSpecs, null, extendedRegister.getCaptureTime(), null, null, new Date(), 0, extendedRegister.getStatusText().trim()));
        } catch (IOException e) {
            return collectedRegisterBuilder.createCollectedRegister(readingSpecs, ResultType.DataIncomplete, e.getMessage());
        }
    }

    @Override
    public boolean isApplicable(ObisCode obisCode) {
        return o.equals(obisCode);
    }
}
