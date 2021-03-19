package com.energyict.protocolimplv2.dlms.common.obis.readers.register;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.AbstractObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.Matcher;

import java.io.IOException;

public class DefaultExtendedRegister<T, K extends AbstractDlmsProtocol> extends AbstractObisReader<CollectedRegister, OfflineRegister, T, K> {

    private final CollectedRegisterBuilder collectedRegisterBuilder;
    private final boolean readCaptureTime;

    public DefaultExtendedRegister(Matcher<T> matcher, CollectedRegisterBuilder collectedRegisterBuilder, boolean readCaptureTime) {
        super(matcher);
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.readCaptureTime = readCaptureTime;
    }

    @Override
    public CollectedRegister read(K protocol, OfflineRegister offlineRegister) {
        try {
            ObisCode cxoObisCode = offlineRegister.getObisCode();
            ExtendedRegister extendedRegister = protocol.getDlmsSession().getCosemObjectFactory().getExtendedRegister(super.map(cxoObisCode));
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, map(extendedRegister, cxoObisCode));
        } catch (IOException e) {
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
        }
    }

    protected RegisterValue map(ExtendedRegister extendedRegister, ObisCode obisCode) throws IOException {
        AbstractDataType valueAttr = extendedRegister.getValueAttr();
        if (valueAttr.isNumerical()) {
            if (readCaptureTime) {
                return new RegisterValue(obisCode, extendedRegister.getQuantityValue(), extendedRegister.getCaptureTime());
            }
            return new RegisterValue(obisCode, extendedRegister.getQuantityValue());
        } else {
            return new RegisterValue(obisCode,valueAttr.toString());
        }
    }
}
