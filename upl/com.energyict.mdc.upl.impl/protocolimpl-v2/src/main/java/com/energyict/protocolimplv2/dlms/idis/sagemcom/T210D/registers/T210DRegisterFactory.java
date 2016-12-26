package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.registers;

import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.idis.am130.AM130;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;

/**
 * Created by cisac on 8/2/2016.
 */
public class T210DRegisterFactory extends AM130RegisterFactory{

    private static final String ALARM_REGISTER3 = "0.0.97.98.2.255";

    public T210DRegisterFactory(AM130 am130) {
        super(am130, collectedDataFactory, issueFactory);
    }

    @Override
    protected RegisterValue getRegisterValueForAlarms(OfflineRegister offlineRegister, AbstractDataType dataValue) {
        RegisterValue registerValue;

        if (offlineRegister.getObisCode().equals(ObisCode.fromString(ALARM_REGISTER3))) {
            AlarmBitsRegister3 alarmBitsRegister3 = new AlarmBitsRegister3(offlineRegister.getObisCode(), dataValue.longValue());
            registerValue = alarmBitsRegister3.getRegisterValue();
        } else {
            registerValue = super.getRegisterValueForAlarms(offlineRegister, dataValue);
        }
        return registerValue;
    }
}
