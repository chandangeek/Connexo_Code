package com.energyict.protocolimplv2.dlms.common.obis.readers.register;

import com.energyict.cbo.Quantity;
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

public class DefaultExtendedRegisterClass<T> extends AbstractObisReader<CollectedRegister, OfflineRegister, T> {

    private final CollectedRegisterBuilder collectedRegisterBuilder;

    public DefaultExtendedRegisterClass(Matcher<T> matcher, CollectedRegisterBuilder collectedRegisterBuilder) {
        super(matcher);
        this.collectedRegisterBuilder = collectedRegisterBuilder;
    }

    @Override
    public CollectedRegister read(AbstractDlmsProtocol protocol, OfflineRegister offlineRegister) {
        try {
            ObisCode cxoObisCode = offlineRegister.getObisCode();
            ExtendedRegister extendedRegister = protocol.getDlmsSession().getCosemObjectFactory().getExtendedRegister(super.map(cxoObisCode));
            AbstractDataType valueAttr = extendedRegister.getValueAttr();
            if (valueAttr.isNumerical()) {
                Quantity quantity = new Quantity(valueAttr.toBigDecimal(), extendedRegister.getScalerUnit().getEisUnit());
                return collectedRegisterBuilder.createCollectedRegister(offlineRegister,new RegisterValue(cxoObisCode, quantity, extendedRegister.getCaptureTime()));
            } else {
                return collectedRegisterBuilder.createCollectedRegister(offlineRegister, new RegisterValue(cxoObisCode,valueAttr.toString()));
            }
        } catch (IOException e) {
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
        }
    }
}
