package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.attribute;

import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.ActarisSl7000;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.obis.matchers.ChannelValueNotMatching;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;

import java.io.IOException;

public class BillingReader implements ObisReader<CollectedRegister, OfflineRegister, ObisCode, ActarisSl7000> {


    private final CollectedRegisterBuilder collectedRegisterBuilder;
    private final ChannelValueNotMatching matcher;

    public BillingReader(CollectedRegisterBuilder collectedRegisterBuilder, ChannelValueNotMatching matcher) {
        this.collectedRegisterBuilder = collectedRegisterBuilder;
        this.matcher = matcher;
    }

    @Override
    public CollectedRegister read(ActarisSl7000 protocol, OfflineRegister offlineRegister) {
        try {
            ObisCode obisCode = offlineRegister.getObisCode();
            int billingPoint = matcher.getIgnoredValue(obisCode);
            RegisterValue register;
            String obisCodeString = obisCode.getValue();
            StoredValues storedValues = protocol.getStoredValues();
            if ((obisCodeString.contains("1.1.0.1.2.")) || (obisCodeString.contains("1.0.0.1.2."))) {
                register = new RegisterValue(offlineRegister, storedValues.getBillingPointTimeDate(billingPoint));
            } else { // billing register
                HistoricalValue historicalValue = storedValues.getHistoricalValue(obisCode);
                register = new RegisterValue(offlineRegister, historicalValue.getQuantityValue(), historicalValue.getCaptureTime(), historicalValue.getBillingDate());
            }
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, register);
        } catch (IOException e) {
            return collectedRegisterBuilder.createCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
        }
    }

    @Override
    public boolean isApplicable(ObisCode obisCode) {
        return matcher.matches(obisCode);
    }


}
