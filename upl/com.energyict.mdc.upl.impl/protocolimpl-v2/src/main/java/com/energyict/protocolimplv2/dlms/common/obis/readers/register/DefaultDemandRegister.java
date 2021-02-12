package com.energyict.protocolimplv2.dlms.common.obis.readers.register;

import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.AbstractObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.Matcher;

import java.io.IOException;

public class DefaultDemandRegister<T, K extends AbstractDlmsProtocol> extends AbstractObisReader<CollectedRegister, OfflineRegister, T, K> {

    private final CollectedRegisterBuilder collectedRegisterBuilder;
    private final boolean readCaptureTime;

    public DefaultDemandRegister(Matcher<T> matcher, CollectedRegisterBuilder collectedRegisterBuilder, boolean readCaptureTime) {
        super(matcher);
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.readCaptureTime = readCaptureTime;
    }

    @Override
    public CollectedRegister read(K protocol, OfflineRegister offlineRegister) {
        try {
            ObisCode cxoObisCode = offlineRegister.getObisCode();
            DemandRegister demandRegister = protocol.getDlmsSession().getCosemObjectFactory().getDemandRegister(super.map(cxoObisCode));
            if (readCaptureTime) {
                return collectedRegisterBuilder.createCollectedRegister(offlineRegister, new RegisterValue(offlineRegister, demandRegister.getQuantityValue(), demandRegister.getCaptureTime()));
            }
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, new RegisterValue(offlineRegister, demandRegister.getQuantityValue()));
        } catch (IOException e) {
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
        }
    }
}
