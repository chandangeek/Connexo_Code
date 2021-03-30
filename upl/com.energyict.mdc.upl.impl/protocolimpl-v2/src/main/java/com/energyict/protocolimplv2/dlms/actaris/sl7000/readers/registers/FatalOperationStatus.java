package com.energyict.protocolimplv2.dlms.actaris.sl7000.readers.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.actaris.sl7000.ActarisSl7000;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.Matcher;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.CollectedRegisterBuilder;
import com.energyict.protocolimplv2.dlms.common.obis.readers.register.DefaultRegister;

import java.util.Date;

public class FatalOperationStatus extends DefaultRegister<ObisCode, ActarisSl7000> {

    public FatalOperationStatus(Matcher<ObisCode> matcher, CollectedRegisterBuilder collectedRegisterBuilder) {
        super(matcher, collectedRegisterBuilder);
    }

    @Override
    protected RegisterValue map(AbstractDataType attributeValue, Unit unit, Date captureTime, OfflineRegister offlineRegister)  {
        BitString bitString = attributeValue.getBitString();
        return new RegisterValue(offlineRegister, "0x" + ProtocolTools.getHexStringFromBytes(ProtocolTools.getBytesFromLong(bitString.longValue(), 1), " "));
    }
}
