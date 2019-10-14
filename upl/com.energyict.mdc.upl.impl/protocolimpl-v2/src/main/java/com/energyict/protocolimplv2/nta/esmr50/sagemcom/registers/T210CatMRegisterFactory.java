package com.energyict.protocolimplv2.nta.esmr50.sagemcom.registers;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.ESMR50RegisterFactory;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.GSMDiagnosticPSStatusVersion2;
import com.energyict.protocolimplv2.nta.esmr50.common.registers.enums.LTEMonitoringWrapperVersion1;

import java.io.IOException;
import java.util.Date;

public class T210CatMRegisterFactory extends ESMR50RegisterFactory {

    public T210CatMRegisterFactory(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    @Override
    protected RegisterValue convertCustomAbstractObjectsToRegisterValues(final OfflineRegister register, AbstractDataType abstractDataType) throws UnsupportedException {
        try {
            ObisCode rObisCode = getCorrectedRegisterObisCode(register);

            if (rObisCode.equals(LTE_MONITORING_BASE)) {
                LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()) {
                    return new RegisterValue(register, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3402)) {
                LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()) {
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getT3402(), Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3412)) {
                LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()) {
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getT3412(), Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3412ext2)) {
                LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()) {
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getT3412ext2(), Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_T3324)) {
                LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()) {
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getT3324(), Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_TeDRX)) {
                LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()) {
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getTeDrx(), Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_TPTW)) {
                LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()) {
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getTptw(), Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_RSRQ)) {
                LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()) {
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getRsrq(), Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_RSRQ)) {
                LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()) {
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getRsrq(), Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_RSRP)) {
                LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()) {
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getRsrp(), Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_Q_RXLEV_MIN)) {
                LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()) {
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getqRxlevMin(), Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_Q_RXLEV_MIN_CE_r13)) {
                LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()) {
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getqRxlevMinCEr13(), Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equals(LTE_MONITORING_LTE_QUALITY_OF_SERVICE_Q_RXLEV_MIN_CE1_r13)) {
                LTEMonitoringWrapperVersion1 lteMonitoringWrapper = new LTEMonitoringWrapperVersion1(abstractDataType);
                if (lteMonitoringWrapper.isDecoded()) {
                    return new RegisterValue(register, new Quantity(lteMonitoringWrapper.getqRxlevMinCE1r13(), Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, lteMonitoringWrapper.toString());
                } else {
                    return new RegisterValue(register, abstractDataType.toString());
                }
            } else if (rObisCode.equalsIgnoreBChannel(GSM_DIAGNOSTIC_PS_STATUS)) {
                int id = abstractDataType.getTypeEnum().intValue();
                String desc = GSMDiagnosticPSStatusVersion2.getDescriptionForId(id);
                return new RegisterValue(register, new Quantity(id, Unit.get(BaseUnit.UNITLESS)), null, null, null, new Date(), 0, desc);
            }

            protocol.getLogger().finest(" > register " + register.getObisCode() + " for " + register.getSerialNumber() + " was translated as " + rObisCode.toString() + " and could not be handled by ESMR5 register factory. Asking ancestors.");
        } catch (Exception ex) {
            protocol.getLogger().warning(" Error while interpreting value for " + register.getObisCode() + ": " + ex.getMessage());
        }

        return super.convertCustomAbstractObjectsToRegisterValues(register, abstractDataType);
    }
}
