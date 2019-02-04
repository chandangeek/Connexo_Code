package com.energyict.protocolimplv2.nta.dsmr23.Iskra;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.registers.Dsmr23RegisterFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class Mx382RegisterFactory extends Dsmr23RegisterFactory {
    public static final ObisCode DAILY_MIN_VOTAGE_L1 = ObisCode.fromString("0.0.128.8.13.255");
    public static final ObisCode DAILY_MIN_VOTAGE_L2 = ObisCode.fromString("0.0.128.8.23.255");
    public static final ObisCode DAILY_MIN_VOTAGE_L3 = ObisCode.fromString("0.0.128.8.33.255");
    public static final ObisCode DAILY_PEAK_VOTAGE_L1 = ObisCode.fromString("0.0.128.8.11.255");
    public static final ObisCode DAILY_PEAK_VOTAGE_L2 = ObisCode.fromString("0.0.128.8.21.255");
    public static final ObisCode DAILY_PEAK_VOTAGE_L3 = ObisCode.fromString("0.0.128.8.31.255");

    public Mx382RegisterFactory(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected RegisterValue convertCustomAbstractObjectsToRegisterValues(OfflineRegister register, AbstractDataType abstractDataType) throws
            UnsupportedException {
        ObisCode rObisCode = getCorrectedRegisterObisCode(register);
        TimeZone timeZone = protocol.getTimeZone();
        if (rObisCode.equalsIgnoreBChannel(DAILY_MIN_VOTAGE_L1)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(BaseUnit.VOLT)), null, truncateFromTime(timeZone), truncateToTime(timeZone), new Date(), 0, new String("DAILY_MIN_VOTAGE_L1: " + Long
                    .toString(abstractDataType.longValue())));
        }else if (rObisCode.equalsIgnoreBChannel(DAILY_MIN_VOTAGE_L2)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(BaseUnit.VOLT)), null, truncateFromTime(timeZone), truncateToTime(timeZone), new Date(), 0, new String("DAILY_MIN_VOTAGE_L2: " + Long
                    .toString(abstractDataType.longValue())));
        } else if (rObisCode.equalsIgnoreBChannel(DAILY_MIN_VOTAGE_L3)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(BaseUnit.VOLT)), null, truncateFromTime(timeZone), truncateToTime(timeZone), new Date(), 0, new String("DAILY_MIN_VOTAGE_L3: " + Long
                    .toString(abstractDataType.longValue())));
        } else if (rObisCode.equalsIgnoreBChannel(DAILY_PEAK_VOTAGE_L1)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(BaseUnit.VOLT)), null, truncateFromTime(timeZone), truncateToTime(timeZone), new Date(), 0, new String("DAILY_PEAK_VOTAGE_L1: " + Long
                    .toString(abstractDataType.longValue())));
        } else if (rObisCode.equalsIgnoreBChannel(DAILY_PEAK_VOTAGE_L2)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(BaseUnit.VOLT)), null, truncateFromTime(timeZone), truncateToTime(timeZone), new Date(), 0, new String("DAILY_PEAK_VOTAGE_L2: " + Long
                    .toString(abstractDataType.longValue())));
        } else if (rObisCode.equalsIgnoreBChannel(DAILY_PEAK_VOTAGE_L3)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(BaseUnit.VOLT)), null, truncateFromTime(timeZone), truncateToTime(timeZone), new Date(), 0, new String("DAILY_PEAK_VOTAGE_L3: " + Long
                    .toString(abstractDataType.longValue())));
        }
        return super.convertCustomAbstractObjectsToRegisterValues(register, abstractDataType);
    }

    public static Date truncateToTime(TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime();
    }

    public static Date truncateFromTime(TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime();
    }

    /**
     * Construct a ComposedCosemObject from a list of <CODE>Registers</CODE>.
     * If the {@link com.energyict.protocol.Register} is a DLMS {@link com.energyict.dlms.cosem.Register} or {@link com.energyict.dlms.cosem.ExtendedRegister},
     * and the ObisCode is listed in the ObjectList(see {@link com.energyict.dlms.DLMSMeterConfig#getInstance(String)}, then we define a ComposedRegister and add
     * it to the {@link #composedRegisterMap}. Otherwise if it is not a DLMS <CODE>Register</CODE> or <CODE>ExtendedRegister</CODE>, but the ObisCode exists in the
     * ObjectList, then we just add it to the {@link #registerMap}. The handling of the <CODE>registerMap</CODE> should be done by the {@link #readRegisters(java.util.List)}
     * method for each <CODE>ObisCode</CODE> in specific.
     *
     * @param registers           the Registers to convert
     * @param supportsBulkRequest indicates whether a DLMS Bulk reques(getWithList) is desired
     * @return a ComposedCosemObject or null if the list was empty
     */
    @Override
    protected ComposedCosemObject constructComposedObjectFromRegisterList(List<OfflineRegister> registers, boolean supportsBulkRequest) {
        if (registers != null) {
            List<DLMSAttribute> dlmsAttributes = new ArrayList<>();
            for (OfflineRegister register : registers) {
                ObisCode rObisCode = getCorrectedRegisterObisCode(register);
                if (rObisCode.equalsIgnoreBChannel(DAILY_MIN_VOTAGE_L1)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.REGISTER.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                }else if (rObisCode.equalsIgnoreBChannel(DAILY_MIN_VOTAGE_L2)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.REGISTER.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(DAILY_MIN_VOTAGE_L3)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.REGISTER.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(DAILY_PEAK_VOTAGE_L1)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.REGISTER.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                }  else if (rObisCode.equalsIgnoreBChannel(DAILY_PEAK_VOTAGE_L2)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.REGISTER.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(DAILY_PEAK_VOTAGE_L3)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), DLMSClassId.REGISTER.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                }
            }
            ComposedCosemObject sRegisterList = super.constructComposedObjectFromRegisterList(registers, supportsBulkRequest);
            if (sRegisterList != null) {
                dlmsAttributes.addAll(Arrays.asList(sRegisterList.getDlmsAttributesList()));
            }
            return new ComposedCosemObject(protocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }
        return null;
    }
}
