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

import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.itron.quantum1000.minidlms.*;
import java.io.*;
import java.math.*;
import java.util.*;
import java.math.BigDecimal;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.cbo.*;
import com.energyict.protocolimpl.base.ObisUtils;
import com.energyict.protocolimpl.ansi.c12.*;
import com.energyict.protocolimpl.ansi.c12.tables.*;

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
        return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(err.getTotalReg()).multiply(registerMap.getMultiplier()),registerMap.getUnit()));
    } // private RegisterValue readEnergyRegister(RegisterMap registerMap)
    
    private RegisterValue readDemandRegister(RegisterMap registerMap) throws IOException {
        // also peaks
        DemandRegisterReadings drr = quantum1000.getDataDefinitionFactory().getDemandRegisterReadings(registerMap.getId(), registerMap.getObisCode().getE());
        DemandRegisterReadingsType demand = drr.getDemandRegisterReadingsTypes()[0];
        
        if (registerMap.getObisCode().getE() < quantum1000.getRegisterMapFactory().getOBISCODE_E_MULTIPLE_PEAK_VALUE1()) {
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_RISING_DEMAND)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getPresentDemand()).multiply(registerMap.getMultiplier()),registerMap.getUnit()));
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_LAST_AVERAGE)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getPreviousDemand()).multiply(registerMap.getMultiplier()),registerMap.getUnit()));
            if (registerMap.getObisCode().getD() == quantum1000.getRegisterMapFactory().getOBISCODE_D_PROJECTED_DEMAND())
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getProjectedDemand()).multiply(registerMap.getMultiplier()),registerMap.getUnit()));
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getCumulativeDemand()).multiply(registerMap.getMultiplier()),registerMap.getUnit()));
            if (registerMap.getObisCode().getD() == ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getContCumulativeDemand()).multiply(registerMap.getMultiplier()),registerMap.getUnit()));
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_MAXIMUM_DEMAND)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getPeakValue()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getPeakTime());
            if (registerMap.getObisCode().getD() == ObisCodeExtensions.OBISCODE_D_COINCIDENT)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getPeakCoincidentReg1Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getPeakTime());
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+1))
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getPeakCoincidentReg2Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getPeakTime());
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+2))
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getPeakCoincidentReg3Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getPeakTime());
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_MINIMUM)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getValleyValue()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getValleyTime());
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+3))
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getValleyCoincidentReg1Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getValleyTime());
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+4))
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getValleyCoincidentReg2Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getValleyTime());
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+5))
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getValleyCoincidentReg3Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getValleyTime());
        }
        else if ((registerMap.getObisCode().getE() >= quantum1000.getRegisterMapFactory().getOBISCODE_E_MULTIPLE_PEAK_VALUE1()) &&
                 (registerMap.getObisCode().getE() <= quantum1000.getRegisterMapFactory().getOBISCODE_E_MULTIPLE_PEAK_VALUE5())) {
            MultiplePeaksType multiplePeaksType = quantum1000.getDataDefinitionFactory().getMultiplePeaksOrMinimums().getMultiplePeaksTypes()[registerMap.getId()];
            PeakValue peakValue = multiplePeaksType.getPeakValues()[registerMap.getObisCode().getE() - quantum1000.getRegisterMapFactory().getOBISCODE_E_MULTIPLE_PEAK_VALUE1()];
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_MAXIMUM_DEMAND)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+peakValue.getValue()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),peakValue.getTimeOfOccurrence());
            if (registerMap.getObisCode().getD() == ObisCodeExtensions.OBISCODE_D_COINCIDENT)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+peakValue.getCoin1Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),peakValue.getTimeOfOccurrence());
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+1))
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+peakValue.getCoin2Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),peakValue.getTimeOfOccurrence());
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+2))
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+peakValue.getCoin3Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),peakValue.getTimeOfOccurrence());
        }
        else if ((registerMap.getObisCode().getE() >= quantum1000.getRegisterMapFactory().getOBISCODE_E_MULTIPLE_MIN_VALUE1()) &&
                 (registerMap.getObisCode().getE() <= quantum1000.getRegisterMapFactory().getOBISCODE_E_MULTIPLE_MIN_VALUE5())) {
            MultiplePeaksType multiplePeaksType = quantum1000.getDataDefinitionFactory().getMultiplePeaksOrMinimums().getMultiplePeaksTypes()[registerMap.getId()];
            PeakValue peakValue = multiplePeaksType.getPeakValues()[registerMap.getObisCode().getE() - quantum1000.getRegisterMapFactory().getOBISCODE_E_MULTIPLE_MIN_VALUE1()];
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_MAXIMUM_DEMAND)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+peakValue.getValue()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),peakValue.getTimeOfOccurrence());
            if (registerMap.getObisCode().getD() == ObisCodeExtensions.OBISCODE_D_COINCIDENT)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+peakValue.getCoin1Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),peakValue.getTimeOfOccurrence());
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+1))
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+peakValue.getCoin2Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),peakValue.getTimeOfOccurrence());
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+2))
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+peakValue.getCoin3Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),peakValue.getTimeOfOccurrence());
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
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getCumulativeDemand()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),null,selfReadDataRecord.getDate());
            if (registerMap.getObisCode().getD() == ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getContCumulativeDemand()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),null,selfReadDataRecord.getDate());
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_MAXIMUM_DEMAND)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getPeakValue()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getPeakTime(),selfReadDataRecord.getDate());
            if (registerMap.getObisCode().getD() == ObisCodeExtensions.OBISCODE_D_COINCIDENT)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getPeakCoincidentReg1Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getPeakTime(),selfReadDataRecord.getDate());
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+1))
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getPeakCoincidentReg2Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getPeakTime(),selfReadDataRecord.getDate());
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+2))
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getPeakCoincidentReg3Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getPeakTime(),selfReadDataRecord.getDate());
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_MINIMUM)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getValleyValue()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getValleyTime(),selfReadDataRecord.getDate());
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+3))
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getValleyCoincidentReg1Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getValleyTime(),selfReadDataRecord.getDate());
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+4))
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getValleyCoincidentReg2Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getValleyTime(),selfReadDataRecord.getDate());
            if (registerMap.getObisCode().getD() == (ObisCodeExtensions.OBISCODE_D_COINCIDENT+5))
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+demand.getValleyCoincidentReg3Value()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),demand.getValleyTime(),selfReadDataRecord.getDate());
                    
        } 
        else if (srfr instanceof SelfReadEnergyRegister) {
            SelfReadEnergyRegister energy = (SelfReadEnergyRegister)srfr;
            if (registerMap.getObisCode().getD() == ObisCode.CODE_D_TIME_INTEGRAL1)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+energy.getIntegralValue()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),null,selfReadDataRecord.getDate());
            if (registerMap.getObisCode().getD() == ObisCodeExtensions.CODE_D_TIME_INTEGRAL2)
                return new RegisterValue(registerMap.getObisCode(),new Quantity(new BigDecimal(""+energy.getFractionalValue()).multiply(registerMap.getMultiplier()),registerMap.getUnit()),null,selfReadDataRecord.getDate());
        }
        throw new NoSuchRegisterException("ObisCode "+registerMap.getObisCode().toString()+" is not supported!");
        
    } // private RegisterValue readSelfReadRegister(RegisterMap registerMap)
    
} // public class ObisCodeMapper
