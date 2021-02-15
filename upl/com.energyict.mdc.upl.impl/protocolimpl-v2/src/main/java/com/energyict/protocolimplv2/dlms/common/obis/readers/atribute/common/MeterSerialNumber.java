package com.energyict.protocolimplv2.dlms.common.obis.readers.atribute.common;

import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;

public class MeterSerialNumber<T extends AbstractDlmsProtocol> implements ObisReader<CollectedRegister, OfflineRegister, ObisCode, T> {

    private final CollectedRegisterBuilder collectedRegisterBuilder;
    private final ObisCode obisCode;

    public MeterSerialNumber(CollectedRegisterBuilder collectedRegisterBuilder, ObisCode obisCode) {
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.obisCode = obisCode;
    }

    @Override
    public CollectedRegister read(T protocol, OfflineRegister readingSpecs) {
        return collectedRegisterBuilder.createCollectedRegister(readingSpecs, new RegisterValue(readingSpecs, protocol.getSerialNumber()));
    }

    @Override
    public boolean isApplicable(ObisCode o) {
        return obisCode.equals(o);
    }
}
