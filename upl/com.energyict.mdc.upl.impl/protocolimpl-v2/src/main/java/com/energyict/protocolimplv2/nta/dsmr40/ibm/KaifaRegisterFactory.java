package com.energyict.protocolimplv2.nta.dsmr40.ibm;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.dlms.cosem.attributes.AssociationLNAttributes;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.dlms.cosem.attributes.DefinableLoadProfileAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr40.registers.Dsmr40RegisterFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.customdlms.cosem.attributes.DSMR4_MbusClientAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class KaifaRegisterFactory extends Dsmr40RegisterFactory {
    //Threshold for voltage registers
    public static final ObisCode PV_VOLTAGE_SAG = ObisCode.fromString("1.2.12.31.0.255");
    public static final ObisCode PV_TIME_SAG = ObisCode.fromString("1.2.12.43.0.255");
    public static final ObisCode PV_THRESHOLD_VOLTAGE_SWELL = ObisCode.fromString("1.2.12.35.0.255");
    public static final ObisCode PV_TIME_THRESHOLD_SWELL = ObisCode.fromString("1.2.12.44.0.255");

    public KaifaRegisterFactory(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected RegisterValue convertCustomAbstractObjectsToRegisterValues(OfflineRegister register, AbstractDataType abstractDataType) throws UnsupportedException {
        ObisCode rObisCode = getCorrectedRegisterObisCode(register);
        if (rObisCode.equalsIgnoreBChannel(PV_VOLTAGE_SAG)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(BaseUnit.VOLT)),
                    null, null, null, new Date(), 0, new String("PV_VOLTAGE_SAG value: " + Long.toString(abstractDataType.longValue())));
        } else if (rObisCode.equalsIgnoreBChannel(PV_TIME_SAG)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(BaseUnit.SECOND)),
                    null, null, null, new Date(), 0, new String("PV_TIME_SAG value: " + Long.toString(abstractDataType.longValue())));
        } else if (rObisCode.equalsIgnoreBChannel(PV_TIME_THRESHOLD_SWELL)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(BaseUnit.SECOND)),
                    null, null, null, new Date(), 0, new String("PV_TIME_THRESHOLD_SWELL value: " + Long.toString(abstractDataType.longValue())));
        } else if (rObisCode.equalsIgnoreBChannel(PV_THRESHOLD_VOLTAGE_SWELL)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.get(BaseUnit.VOLT)),
                    null, null, null, new Date(), 0, new String("PV_THRESHOLD_VOLTAGE_SWELL value: " + Long.toString(abstractDataType.longValue())));
        }
        return super.convertCustomAbstractObjectsToRegisterValues(register, abstractDataType);
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

                if (rObisCode.equalsIgnoreBChannel(PV_VOLTAGE_SAG)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.REGISTER.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(PV_TIME_SAG)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.REGISTER.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(PV_TIME_THRESHOLD_SWELL)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.REGISTER.getClassId()));
                    dlmsAttributes.add(this.registerMap.get(register));
                } else if (rObisCode.equalsIgnoreBChannel(PV_THRESHOLD_VOLTAGE_SWELL)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.REGISTER.getClassId()));
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
