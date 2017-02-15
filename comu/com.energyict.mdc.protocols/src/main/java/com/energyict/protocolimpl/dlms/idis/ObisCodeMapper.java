/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.idis;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.Register;
import com.energyict.protocolimpl.base.DLMSAttributeMapper;
import com.energyict.protocolimpl.dlms.idis.registers.AlarmBitsRegister;
import com.energyict.protocolimpl.dlms.idis.registers.SFSKActiveInitiatorMapper;
import com.energyict.protocolimpl.dlms.idis.registers.SFSKIec61334LLCSetupMapper;
import com.energyict.protocolimpl.dlms.idis.registers.SFSKMacCountersMapper;
import com.energyict.protocolimpl.dlms.idis.registers.SFSKPhyMacSetupMapper;
import com.energyict.protocolimpl.dlms.idis.registers.SFSKReportingSystemListMapper;
import com.energyict.protocolimpl.dlms.idis.registers.SFSKSyncTimeoutsMapper;

import java.io.IOException;
import java.math.BigDecimal;

public class ObisCodeMapper {

    private IDIS idis;
    private static final String ALARM_REGISTER = "0.0.97.98.0.255";
    private final DLMSAttributeMapper[] attributeMappers;
    public static final ObisCode SFSK_PHY_MAC_SETUP = ObisCode.fromString("0.0.26.0.0.255");
    public static final ObisCode SFSK_ACTIVE_INITIATOR = ObisCode.fromString("0.0.26.1.0.255");
    public static final ObisCode SFSK_SYNC_TIMEOUTS = ObisCode.fromString("0.0.26.2.0.255");
    public static final ObisCode SFSK_MAC_COUNTERS = ObisCode.fromString("0.0.26.3.0.255");
    public static final ObisCode SFSK_IEC_LLC_SETIP = ObisCode.fromString("0.0.26.5.0.255");
    public static final ObisCode SFSK_REPORTING_SYSTEM_LIST = ObisCode.fromString("0.0.26.6.0.255");

    public ObisCodeMapper(IDIS idis) {
        this.idis = idis;
        this.attributeMappers = new DLMSAttributeMapper[]{
                new SFSKPhyMacSetupMapper(SFSK_PHY_MAC_SETUP, getCosemObjectFactory()),
                new SFSKActiveInitiatorMapper(SFSK_ACTIVE_INITIATOR, getCosemObjectFactory()),
                new SFSKSyncTimeoutsMapper(SFSK_SYNC_TIMEOUTS, getCosemObjectFactory()),
                new SFSKMacCountersMapper(SFSK_MAC_COUNTERS, getCosemObjectFactory()),
                new SFSKIec61334LLCSetupMapper(SFSK_IEC_LLC_SETIP, getCosemObjectFactory()),
                new SFSKReportingSystemListMapper(SFSK_REPORTING_SYSTEM_LIST, getCosemObjectFactory()),
        };
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {

        //Read out plc object attributes, where the obiscode F-field is the attribute number
        for (DLMSAttributeMapper attributeMapper : attributeMappers) {
            if (attributeMapper.isObisCodeMapped(obisCode)) {
                RegisterValue registerValue = attributeMapper.getRegisterValue(obisCode);
                try {
                    String textValue = registerValue.getText();
                    BigDecimal value = new BigDecimal(textValue);
                    registerValue.setQuantity(new Quantity(value, Unit.get("")));
                } catch (NumberFormatException e) {
                    //Text register, quantity is not used
                }
                return registerValue;
            }
        }

        if (obisCode.getF() != 255) {
            HistoricalValue historicalValue = idis.getStoredValues().getHistoricalValue(obisCode);
            return new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate());
        }

        final UniversalObject uo = idis.getMeterConfig().findObject(obisCode);
        if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
            final Register register = getCosemObjectFactory().getRegister(obisCode);
            return new RegisterValue(obisCode, register.getQuantityValue());
        } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            final DemandRegister register = getCosemObjectFactory().getDemandRegister(obisCode);
            return new RegisterValue(obisCode, register.getQuantityValue());
        } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            final ExtendedRegister register = getCosemObjectFactory().getExtendedRegister(obisCode);
            return new RegisterValue(obisCode, register.getQuantityValue());
        } else if (uo.getClassID() == DLMSClassId.DISCONNECT_CONTROL.getClassId()) {
            final Disconnector register = getCosemObjectFactory().getDisconnector(obisCode);
            return new RegisterValue(obisCode, "" + register.getState());
        } else if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
            final Data register = getCosemObjectFactory().getData(obisCode);
            OctetString octetString = register.getValueAttr().getOctetString();
            if (octetString != null && octetString.stringValue() != null) {
                return new RegisterValue(obisCode, octetString.stringValue());
            }
            Unsigned32 value = register.getValueAttr().getUnsigned32();
            if (value != null) {
                if (obisCode.equals(ObisCode.fromString(ALARM_REGISTER))) {
                    AlarmBitsRegister alarmBitsRegister = new AlarmBitsRegister(obisCode, value);
                    return alarmBitsRegister.getRegisterValue();
                }
                return new RegisterValue(obisCode, new Quantity(value.getValue(), Unit.get("")));
            }
            throw new NoSuchRegisterException();
        } else {
            throw new NoSuchRegisterException();
        }
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return idis.getCosemObjectFactory();
    }
}