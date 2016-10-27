/*
 * ObisCodeMapper.java
 *
 * Created on 16 november 2005, 13:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ObisCodeExtensions;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.DemandRegisterReadings;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.DemandRegisterReadingsType;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.EnergyRegisterValue;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.EnergyRegistersReading;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.MultiplePeaksType;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.PeakValue;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.RegisterMap;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.SelfReadDataRecord;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.SelfReadDataUpload;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.SelfReadDemandRegister;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.SelfReadEnergyRegister;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

    Quantum1000 quantum1000;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(Quantum1000 quantum1000) {
        this.quantum1000=quantum1000;
    }

    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode, true);
    }

    private Object doGetRegister(ObisCode obc, boolean read) throws IOException {
        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));
        if (read) {
            RegisterMap rm = quantum1000.getRegisterMapFactory().findRegisterMap(obisCode);

            if (rm.isREGISTER()) {
                return readEnergyRegister(rm);
            }
            else if (rm.isDEMAND()) {
                return readDemandRegister(rm);
            }
            else if (rm.isSELFREAD()) {
                return readSelfReadRegister(rm);
            }
        }
        else {
            return quantum1000.getRegisterMapFactory().findRegisterMap(obisCode).toString();
        }

        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    } // private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException


    private RegisterValue readEnergyRegister(RegisterMap registerMap) throws IOException {
        EnergyRegistersReading energyRegisterReading = quantum1000.getDataDefinitionFactory().getEnergyRegistersReading(registerMap.getObisCode().getE());
        EnergyRegisterValue err = energyRegisterReading.getEnergyRegisterValues()[registerMap.getId()];

        BigDecimal val = new BigDecimal(err.getTotalReg());
        if (quantum1000.isApplyEnergyRegisterMultiplier()) {
            val = val.multiply(registerMap.getMultiplier());
        }
        return new RegisterValue(registerMap.getObisCode(),new Quantity(val,registerMap.getUnit()));
    } // private RegisterValue readEnergyRegister(RegisterMap registerMap)

    private RegisterValue readDemandRegister(RegisterMap registerMap) throws IOException {
        // also peaks
        DemandRegisterReadings drr = quantum1000.getDataDefinitionFactory().getDemandRegisterReadings(registerMap.getId(), registerMap.getObisCode().getE());
        DemandRegisterReadingsType demand = drr.getDemandRegisterReadingsTypes()[0];

        if (registerMap.getObisCode().getE() < quantum1000.getRegisterMapFactory().getOBISCODE_E_MULTIPLE_PEAK_VALUE1()) {
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_RISING_DEMAND) {
                BigDecimal val = new BigDecimal(""+demand.getPresentDemand());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(),new Quantity(val,registerMap.getUnit()));
            }
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_LAST_AVERAGE) {
                BigDecimal val = new BigDecimal(""+demand.getPreviousDemand());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(),new Quantity(val,registerMap.getUnit()));
            }

            if (registerMap.getObisCode().getD() == quantum1000.getRegisterMapFactory().getOBISCODE_D_PROJECTED_DEMAND()) {
                BigDecimal val = new BigDecimal(""+demand.getProjectedDemand());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(),new Quantity(val,registerMap.getUnit()));
            }
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND) {
                BigDecimal val = new BigDecimal(""+demand.getCumulativeDemand());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()));
            }
            if (registerMap.getObisCode().getD() == ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND) {
                BigDecimal val = new BigDecimal(""+demand.getContCumulativeDemand());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()));
            }
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_MAXIMUM_DEMAND) {
                BigDecimal val = new BigDecimal(""+demand.getPeakValue());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getPeakTime());
            }
            if (registerMap.getObisCode().getD() == ObisCodeExtensions.OBISCODE_D_COINCIDENT) {
                BigDecimal val = new BigDecimal(""+demand.getPeakCoincidentReg1Value());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getPeakTime());
            }
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+1)) {
                BigDecimal val = new BigDecimal(""+demand.getPeakCoincidentReg2Value());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getPeakTime());
            }
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+2)) {
                BigDecimal val = new BigDecimal(""+demand.getPeakCoincidentReg3Value());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getPeakTime());
            }
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_MINIMUM) {
                BigDecimal val = new BigDecimal(""+demand.getValleyValue());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getValleyTime());
            }
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+3)) {
                BigDecimal val = new BigDecimal(""+demand.getValleyCoincidentReg1Value());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getValleyTime());
            }
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+4)) {
                BigDecimal val = new BigDecimal(""+demand.getValleyCoincidentReg2Value());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getValleyTime());
            }
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+5)) {
                BigDecimal val = new BigDecimal(""+demand.getValleyCoincidentReg3Value());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getValleyTime());
            }
        }
        else if ((registerMap.getObisCode().getE() >= quantum1000.getRegisterMapFactory().getOBISCODE_E_MULTIPLE_PEAK_VALUE1()) &&
                (registerMap.getObisCode().getE() <= quantum1000.getRegisterMapFactory().getOBISCODE_E_MULTIPLE_PEAK_VALUE5())) {
            MultiplePeaksType multiplePeaksType = quantum1000.getDataDefinitionFactory().getMultiplePeaksOrMinimums().getMultiplePeaksTypes()[registerMap.getId()];
            PeakValue peakValue = multiplePeaksType.getPeakValues()[registerMap.getObisCode().getE() - quantum1000.getRegisterMapFactory().getOBISCODE_E_MULTIPLE_PEAK_VALUE1()];
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_MAXIMUM_DEMAND) {
                BigDecimal val = new BigDecimal(""+peakValue.getValue());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), peakValue.getTimeOfOccurrence());
            }
            if (registerMap.getObisCode().getD() == ObisCodeExtensions.OBISCODE_D_COINCIDENT) {
                BigDecimal val = new BigDecimal(""+peakValue.getCoin1Value());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), peakValue.getTimeOfOccurrence());
            }
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+1)) {
                BigDecimal val = new BigDecimal(""+peakValue.getCoin2Value());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), peakValue.getTimeOfOccurrence());
            }
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+2)) {
                BigDecimal val = new BigDecimal(""+peakValue.getCoin3Value());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), peakValue.getTimeOfOccurrence());
            }
        }
        else if ((registerMap.getObisCode().getE() >= quantum1000.getRegisterMapFactory().getOBISCODE_E_MULTIPLE_MIN_VALUE1()) &&
                (registerMap.getObisCode().getE() <= quantum1000.getRegisterMapFactory().getOBISCODE_E_MULTIPLE_MIN_VALUE5())) {
            MultiplePeaksType multiplePeaksType = quantum1000.getDataDefinitionFactory().getMultiplePeaksOrMinimums().getMultiplePeaksTypes()[registerMap.getId()];
            PeakValue peakValue = multiplePeaksType.getPeakValues()[registerMap.getObisCode().getE() - quantum1000.getRegisterMapFactory().getOBISCODE_E_MULTIPLE_MIN_VALUE1()];
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_MAXIMUM_DEMAND) {
                BigDecimal val = new BigDecimal(""+peakValue.getValue());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), peakValue.getTimeOfOccurrence());
            }
            if (registerMap.getObisCode().getD() == ObisCodeExtensions.OBISCODE_D_COINCIDENT) {
                BigDecimal val = new BigDecimal(""+peakValue.getCoin1Value());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), peakValue.getTimeOfOccurrence());
            }
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+1)) {
                BigDecimal val = new BigDecimal(""+peakValue.getCoin2Value());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), peakValue.getTimeOfOccurrence());
            }
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+2)) {
                BigDecimal val = new BigDecimal(""+peakValue.getCoin3Value());
                if (quantum1000.isApplyDemandRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), peakValue.getTimeOfOccurrence());
            }
        }
        throw new NoSuchRegisterException("ObisCode "+registerMap.getObisCode().toString()+" is not supported!");
    } // private RegisterValue readDemandRegister(RegisterMap registerMap)

    private RegisterValue readSelfReadRegister(RegisterMap registerMap) throws IOException {

        SelfReadDataUpload srdu = quantum1000.getDataDefinitionFactory().getSelfReadDataUpload();
        List selfReadSets = srdu.getSelfReadDataRecords(); // list all billing sets
        SelfReadDataRecord selfReadDataRecord = (SelfReadDataRecord)selfReadSets.get(registerMap.getObisCode().getF()); // specific billing set
        List selfReadFreezedRegisters = selfReadDataRecord.getFreezeRegisterDatas(); // list all freezed register from billingset

        Object srfr = selfReadFreezedRegisters.get(registerMap.getId());

        if (srfr instanceof SelfReadDemandRegister) {
            SelfReadDemandRegister demand = (SelfReadDemandRegister)srfr;
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND) {
                BigDecimal val = new BigDecimal(""+demand.getCumulativeDemand());
                if (quantum1000.isApplySelfReadRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), null, selfReadDataRecord.getDate());
            }
            if (registerMap.getObisCode().getD() == ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND) {
                BigDecimal val = new BigDecimal(""+demand.getCumulativeDemand());
                if (quantum1000.isApplySelfReadRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), null, selfReadDataRecord.getDate());
            }
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_MAXIMUM_DEMAND) {
                BigDecimal val = new BigDecimal(""+demand.getPeakValue());
                if (quantum1000.isApplySelfReadRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getPeakTime(), selfReadDataRecord.getDate());
            }
            if (registerMap.getObisCode().getD() == ObisCodeExtensions.OBISCODE_D_COINCIDENT) {
                BigDecimal val = new BigDecimal(""+demand.getPeakCoincidentReg1Value());
                if (quantum1000.isApplySelfReadRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getPeakTime(), selfReadDataRecord.getDate());
            }
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+1)) {
                BigDecimal val = new BigDecimal(""+demand.getPeakCoincidentReg2Value());
                if (quantum1000.isApplySelfReadRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getPeakTime(), selfReadDataRecord.getDate());
            }
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+2)) {
                BigDecimal val = new BigDecimal(""+demand.getPeakCoincidentReg3Value());
                if (quantum1000.isApplySelfReadRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getPeakTime(), selfReadDataRecord.getDate());
            }
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_MINIMUM) {
                BigDecimal val = new BigDecimal(""+demand.getValleyValue());
                if (quantum1000.isApplySelfReadRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getValleyTime(), selfReadDataRecord.getDate());
            }
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+3)) {
                BigDecimal val = new BigDecimal(""+demand.getValleyCoincidentReg1Value());
                if (quantum1000.isApplySelfReadRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getValleyTime(), selfReadDataRecord.getDate());
            }
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+4)) {
                BigDecimal val = new BigDecimal(""+demand.getValleyCoincidentReg2Value());
                if (quantum1000.isApplySelfReadRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getValleyTime(), selfReadDataRecord.getDate());
            }
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+5)) {
                BigDecimal val = new BigDecimal(""+demand.getValleyCoincidentReg3Value());
                if (quantum1000.isApplySelfReadRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), demand.getValleyTime(), selfReadDataRecord.getDate());
            }

        }
        else if (srfr instanceof SelfReadEnergyRegister) {
            SelfReadEnergyRegister energy = (SelfReadEnergyRegister)srfr;
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_TIME_INTEGRAL1) {
                BigDecimal val = new BigDecimal(""+energy.getIntegralValue());
                if (quantum1000.isApplySelfReadRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), null, selfReadDataRecord.getDate());
            }
            if (registerMap.getObisCode().getD() == ObisCodeExtensions.CODE_D_TIME_INTEGRAL2) {
                BigDecimal val = new BigDecimal(""+energy.getFractionalValue());
                if (quantum1000.isApplySelfReadRegisterMultiplier()) {
                    val = val.multiply(registerMap.getMultiplier());
                }
                return new RegisterValue(registerMap.getObisCode(), new Quantity(val, registerMap.getUnit()), null, selfReadDataRecord.getDate());
            }
        }
        throw new NoSuchRegisterException("ObisCode "+registerMap.getObisCode().toString()+" is not supported!");

    } // private RegisterValue readSelfReadRegister(RegisterMap registerMap)

} // public class ObisCodeMapper
