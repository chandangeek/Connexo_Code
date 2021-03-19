package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.attribute;

import com.energyict.dlms.cosem.Register;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.ActarisSl7000;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.IgnoreChannelMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.ObisCodeMatcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;

import java.io.IOException;

public class BillingCounterReader implements ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> {

    private static ObisCode OBIS_NUMBER_OF_AVAILABLE_HISTORICAL_SETS = ObisCode.fromString("0.0.0.1.1.255");

    private final CollectedRegisterBuilder collectedRegisterBuilder;
    private final ObisCodeMatcher matcher;

    public BillingCounterReader(CollectedRegisterBuilder collectedRegisterBuilder, ObisCodeMatcher matcher) {
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.matcher = matcher;
    }

    @Override
    public CollectedRegister read(ActarisSl7000 protocol, OfflineRegister offlineRegister) {
        try {
            Register register = protocol.getDlmsSession().getCosemObjectFactory().getRegister(OBIS_NUMBER_OF_AVAILABLE_HISTORICAL_SETS);
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, new RegisterValue(offlineRegister, register.getQuantityValue()));
        } catch (IOException e) {
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
        }
    }

    @Override
    public boolean isApplicable(ObisCode obisCode) {
        return matcher.matches(obisCode);
    }
}
