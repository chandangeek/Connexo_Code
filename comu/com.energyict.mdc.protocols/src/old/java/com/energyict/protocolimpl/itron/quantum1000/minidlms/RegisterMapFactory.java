/*
 * RegisterMapFactory.java
 *
 * Created on 9 januari 2007, 16:07
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.ObisCodeExtensions;
import com.energyict.protocolimpl.itron.quantum1000.Quantum1000;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class RegisterMapFactory {

    Quantum1000 quantum1000;

    List registerMaps;

    private final int MAX_TOU_RATES=8;

    private final int OBISCODE_D_PROJECTED_DEMAND=200;

    // 5 max demand peak values
    private final int OBISCODE_E_MULTIPLE_PEAK_VALUE1=128;
    private final int OBISCODE_E_MULTIPLE_PEAK_VALUE2=129;
    private final int OBISCODE_E_MULTIPLE_PEAK_VALUE3=130;
    private final int OBISCODE_E_MULTIPLE_PEAK_VALUE4=131;
    private final int OBISCODE_E_MULTIPLE_PEAK_VALUE5=132;

    // 5 minimum demand peak values
    private final int OBISCODE_E_MULTIPLE_MIN_VALUE1=133;
    private final int OBISCODE_E_MULTIPLE_MIN_VALUE2=134;
    private final int OBISCODE_E_MULTIPLE_MIN_VALUE3=135;
    private final int OBISCODE_E_MULTIPLE_MIN_VALUE4=136;
    private final int OBISCODE_E_MULTIPLE_MIN_VALUE5=137;

    /**
     * Creates a new instance of RegisterMapFactory
     */
    public RegisterMapFactory(Quantum1000 quantum1000) {
        this.quantum1000=quantum1000;
    }

    public void init() throws IOException {
        buildRegisterList();
    }

    public String getRegisterInfo() {
        StringBuffer strBuff = new StringBuffer();
        Iterator it = registerMaps.iterator();
        while(it.hasNext()) {
            RegisterMap rm = (RegisterMap)it.next();
            strBuff.append(rm);
        }
        return strBuff.toString();

    }

    public RegisterMap findRegisterMap(ObisCode obisCode) throws IOException {

        StringBuffer strBuff = new StringBuffer();
        Iterator it = registerMaps.iterator();
        while(it.hasNext()) {
            RegisterMap rm = (RegisterMap)it.next();
            if (rm.getObisCode().equals(obisCode))
                return rm;
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");

    }

    private void buildRegisterList() throws IOException {

        registerMaps = new ArrayList();
        int maxRates = quantum1000.getDataDefinitionFactory().isTOUMeter()?MAX_TOU_RATES:0;

        EnergyRegisterConfiguration energyRegisterConfiguration = quantum1000.getDataDefinitionFactory().getEnergyRegisterConfiguration();
        EnergyRegister[] registerConfigs = energyRegisterConfiguration.getRegisterConfig();
        for (int register=0;register<registerConfigs.length;register++) {
            if (registerConfigs[register].getQuantityId().isValid()) {
                for (int rate=0;rate<=maxRates;rate++) {
                   registerMaps.add(new RegisterMap(ObisCode.fromString("1."+registerConfigs[register].getQuantityId().getObisBField()+"."+registerConfigs[register].getQuantityId().getObisCField()+".8."+rate+".255"),
                                    registerConfigs[register].getQuantityId().getDescription(),
                                    registerConfigs[register].getQuantityId().getUnit().getVolumeUnit(),
                                    register,
                                    RegisterMap.getREGISTER(),
                                    registerConfigs[register].getMultiplier()));
                } // for (int rate=1;rate<=MAX_TOU_RATES;rate++)
            } // if (registerConfigs[register].getQuantityId().isValid())
        } // for (int register=0;register<registerConfigs.length;register++)



        DemandRegisterConfiguration demandRegisterConfiguration = quantum1000.getDataDefinitionFactory().getDemandRegisterConfiguration();
        DemandRegister[] demandRegisters = demandRegisterConfiguration.getDemandRegisters();
        int multiplePeakSet=0;
        for (int register=0;register<demandRegisters.length;register++) {
            for (int rate=0;rate<=maxRates;rate++) {
                if (demandRegisters[register].getQuantityId().isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getQuantityId().getUnit(),register, rate, ObisCode.CODE_D_RISING_DEMAND); // presentDemand
                if (demandRegisters[register].getQuantityId().isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getQuantityId().getUnit(),register, rate, ObisCode.CODE_D_LAST_AVERAGE); // previousDemand
                if (demandRegisters[register].getQuantityId().isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getQuantityId().getUnit(),register, rate, getOBISCODE_D_PROJECTED_DEMAND(), "projectedDemand"); // projectedDemand
                if (demandRegisters[register].getQuantityId().isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getQuantityId().getUnit(),register, rate, ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND); // cumulativeDemand
                if (demandRegisters[register].getQuantityId().isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getQuantityId().getUnit(),register, rate, ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND,"contCumulativeDemand"); // contCumulativeDemand Current average
                if (demandRegisters[register].getQuantityId().isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getQuantityId().getUnit(),register, rate, ObisCode.CODE_D_MAXIMUM_DEMAND); // peakValue Current average
                if (demandRegisters[register].getCoincidentQuantities()[0].isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getCoincidentQuantities()[0].getUnit(),register, rate, ObisCodeExtensions.OBISCODE_D_COINCIDENT,"peakCoincidentReg1Value"); // peakCoincidentReg1Value
                if (demandRegisters[register].getCoincidentQuantities()[1].isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getCoincidentQuantities()[1].getUnit(),register, rate, ObisCodeExtensions.OBISCODE_D_COINCIDENT+1,"peakCoincidentReg2Value"); // peakCoincidentReg2Value
                if (demandRegisters[register].getCoincidentQuantities()[2].isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getCoincidentQuantities()[2].getUnit(),register, rate, ObisCodeExtensions.OBISCODE_D_COINCIDENT+2,"peakCoincidentReg3Value"); // peakCoincidentReg3Value
                if (demandRegisters[register].getQuantityId().isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getQuantityId().getUnit(),register, rate, ObisCode.CODE_D_MINIMUM); // valleyValue
                if (demandRegisters[register].getCoincidentQuantities()[0].isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getCoincidentQuantities()[0].getUnit(),register, rate, ObisCodeExtensions.OBISCODE_D_COINCIDENT+3,"valleyCoincidentReg1Value"); // valleyCoincidentReg1Value
                if (demandRegisters[register].getCoincidentQuantities()[1].isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getCoincidentQuantities()[1].getUnit(),register, rate, ObisCodeExtensions.OBISCODE_D_COINCIDENT+4,"valleyCoincidentReg2Value"); // valleyCoincidentReg2Value
                if (demandRegisters[register].getCoincidentQuantities()[2].isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getCoincidentQuantities()[2].getUnit(),register, rate, ObisCodeExtensions.OBISCODE_D_COINCIDENT+5,"valleyCoincidentReg3Value"); // valleyCoincidentReg3Value

            } // for (int rate=0;rate<=(quantum1000.getDataDefinitionFactory().isTOUMeter()?MAX_TOU_RATES:0);rate++)

            if (demandRegisters[register].isMultipleMinimumRequired()) {
                for (int peak=0;peak<5;peak++) {
                    if (demandRegisters[register].getQuantityId().isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getQuantityId().getUnit(),register, getOBISCODE_E_MULTIPLE_MIN_VALUE1()+peak, ObisCode.CODE_D_MAXIMUM_DEMAND, "minimum "+peak,multiplePeakSet); // peakValue Current average
                    if (demandRegisters[register].getCoincidentQuantities()[0].isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getCoincidentQuantities()[0].getUnit(),register, getOBISCODE_E_MULTIPLE_MIN_VALUE1()+peak, ObisCodeExtensions.OBISCODE_D_COINCIDENT,"coin1Value minimum "+peak,multiplePeakSet); // coin1Value
                    if (demandRegisters[register].getCoincidentQuantities()[1].isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getCoincidentQuantities()[1].getUnit(),register, getOBISCODE_E_MULTIPLE_MIN_VALUE1()+peak, ObisCodeExtensions.OBISCODE_D_COINCIDENT+1,"coin2Value minimum "+peak,multiplePeakSet); // coin1Value
                    if (demandRegisters[register].getCoincidentQuantities()[2].isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getCoincidentQuantities()[2].getUnit(),register, getOBISCODE_E_MULTIPLE_MIN_VALUE1()+peak, ObisCodeExtensions.OBISCODE_D_COINCIDENT+2,"coin3Value minimum "+peak,multiplePeakSet); // coin1Value
                }
                multiplePeakSet++;
            }

            if (demandRegisters[register].isMultiplePeaksRequired()) {
                for (int peak=0;peak<5;peak++) {
                    if (demandRegisters[register].getQuantityId().isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getQuantityId().getUnit(),register, getOBISCODE_E_MULTIPLE_PEAK_VALUE1()+peak, ObisCode.CODE_D_MAXIMUM_DEMAND, "peak "+peak,multiplePeakSet); // peakValue Current average
                    if (demandRegisters[register].getCoincidentQuantities()[0].isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getCoincidentQuantities()[0].getUnit(),register, getOBISCODE_E_MULTIPLE_PEAK_VALUE1()+peak, ObisCodeExtensions.OBISCODE_D_COINCIDENT,"coin1Value peak "+peak,multiplePeakSet); // coin1Value
                    if (demandRegisters[register].getCoincidentQuantities()[1].isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getCoincidentQuantities()[1].getUnit(),register, getOBISCODE_E_MULTIPLE_PEAK_VALUE1()+peak, ObisCodeExtensions.OBISCODE_D_COINCIDENT+1,"coin2Value peak "+peak,multiplePeakSet); // coin1Value
                    if (demandRegisters[register].getCoincidentQuantities()[2].isValid()) buildDemandRegisterMap(demandRegisters, demandRegisters[register].getCoincidentQuantities()[2].getUnit(),register, getOBISCODE_E_MULTIPLE_PEAK_VALUE1()+peak, ObisCodeExtensions.OBISCODE_D_COINCIDENT+2,"coin3Value peak "+peak,multiplePeakSet); // coin1Value
                }
                multiplePeakSet++;
            }
        } // for (int register=0;register<demandRegisters.length;register++)


        SelfReadGeneralInformation selfReadRegisterInformation = quantum1000.getDataDefinitionFactory().getSelfReadGeneralInformation();
        if (selfReadRegisterInformation.getNumberOfSelfRead() > 0) {
            SelfReadRegisterConfiguration selfReadRegisterConfiguration = quantum1000.getDataDefinitionFactory().getSelfReadRegisterConfiguration();
            SelfReadRegisterConfigurationType[] selfReadRegisterConfigurationTypes = selfReadRegisterConfiguration.getSelfReadRegisterConfigurationTypes();
            int selfReadRegisterId=0;
            for (int register=0;register<selfReadRegisterConfigurationTypes.length;register++) {
                if (selfReadRegisterConfigurationTypes[register].getQuantityId().isValid()) {
                    if (selfReadRegisterConfigurationTypes[register].isSelfReadEnergyRegisterType()) {
                        for (int selfRead=0;selfRead<selfReadRegisterInformation.numberOfSelfReadSets();selfRead++) {
                           registerMaps.add(new RegisterMap(ObisCode.fromString("1."+selfReadRegisterConfigurationTypes[register].getQuantityId().getObisBField()+"."+selfReadRegisterConfigurationTypes[register].getQuantityId().getObisCField()+".8."+selfReadRegisterConfigurationTypes[register].getId2()+"."+selfRead),
                                                            selfReadRegisterConfigurationTypes[register].getQuantityId().getDescription(),
                                                            selfReadRegisterConfigurationTypes[register].getQuantityId().getUnit().getVolumeUnit(),
                                                            selfReadRegisterId,
                                                            RegisterMap.getSELFREAD(),
                                                            energyRegisterConfiguration.findEnergyRegister(selfReadRegisterConfigurationTypes[register].getQuantityId()).getMultiplier()));

                           registerMaps.add(new RegisterMap(ObisCode.fromString("1."+selfReadRegisterConfigurationTypes[register].getQuantityId().getObisBField()+"."+selfReadRegisterConfigurationTypes[register].getQuantityId().getObisCField()+".9."+selfReadRegisterConfigurationTypes[register].getId2()+"."+selfRead),
                                                            selfReadRegisterConfigurationTypes[register].getQuantityId().getDescription(),
                                                            selfReadRegisterConfigurationTypes[register].getQuantityId().getUnit().getVolumeUnit(),
                                                            selfReadRegisterId,
                                                            RegisterMap.getSELFREAD(),
                                                            energyRegisterConfiguration.findEnergyRegister(selfReadRegisterConfigurationTypes[register].getQuantityId()).getMultiplier()));
                        }
                        selfReadRegisterId++;
                    }
                    else if (selfReadRegisterConfigurationTypes[register].isSelfReadDemandRegisterType()) {
                        for (int selfRead=0;selfRead<selfReadRegisterInformation.numberOfSelfReadSets();selfRead++) {
                            DemandRegister demandRegister = demandRegisterConfiguration.findDemandRegister(selfReadRegisterConfigurationTypes[register].getQuantityId(), selfReadRegisterConfigurationTypes[register].getRegisterType());
                            if (selfReadRegisterConfigurationTypes[register].getQuantityId().isValid()) buildSelfReadDemandRegisterMap(selfReadRegisterConfigurationTypes, selfReadRegisterConfigurationTypes[register].getQuantityId().getUnit(), register, ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND, selfRead, demandRegisterConfiguration,selfReadRegisterId); // cumulativeDemand
                            if (selfReadRegisterConfigurationTypes[register].getQuantityId().isValid()) buildSelfReadDemandRegisterMap(selfReadRegisterConfigurationTypes, selfReadRegisterConfigurationTypes[register].getQuantityId().getUnit(), register, ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND, selfRead, demandRegisterConfiguration, "contCumulativeDemand",selfReadRegisterId); // contCumulativeDemand
                            if (selfReadRegisterConfigurationTypes[register].getQuantityId().isValid()) buildSelfReadDemandRegisterMap(selfReadRegisterConfigurationTypes, selfReadRegisterConfigurationTypes[register].getQuantityId().getUnit(), register, ObisCode.CODE_D_MAXIMUM_DEMAND, selfRead, demandRegisterConfiguration,selfReadRegisterId); // peakValue
                            if (demandRegisters[register].getCoincidentQuantities()[0].isValid()) buildSelfReadDemandRegisterMap(selfReadRegisterConfigurationTypes, demandRegister.getCoincidentQuantities()[0].getUnit(), register, ObisCodeExtensions.OBISCODE_D_COINCIDENT, selfRead, demandRegisterConfiguration, "peakCoincidentReg1Value",selfReadRegisterId); // peakCoincidentReg1Value
                            if (demandRegisters[register].getCoincidentQuantities()[1].isValid()) buildSelfReadDemandRegisterMap(selfReadRegisterConfigurationTypes, demandRegister.getCoincidentQuantities()[1].getUnit(), register, ObisCodeExtensions.OBISCODE_D_COINCIDENT+1, selfRead, demandRegisterConfiguration, "peakCoincidentReg2Value",selfReadRegisterId); // peakCoincidentReg2Value
                            if (demandRegisters[register].getCoincidentQuantities()[2].isValid()) buildSelfReadDemandRegisterMap(selfReadRegisterConfigurationTypes, demandRegister.getCoincidentQuantities()[2].getUnit(), register, ObisCodeExtensions.OBISCODE_D_COINCIDENT+2, selfRead, demandRegisterConfiguration, "peakCoincidentReg3Value",selfReadRegisterId); // peakCoincidentReg3Value
                            if (selfReadRegisterConfigurationTypes[register].getQuantityId().isValid()) buildSelfReadDemandRegisterMap(selfReadRegisterConfigurationTypes, selfReadRegisterConfigurationTypes[register].getQuantityId().getUnit(), register, ObisCode.CODE_D_MINIMUM, selfRead, demandRegisterConfiguration,selfReadRegisterId); // valleyValue
                            if (demandRegisters[register].getCoincidentQuantities()[0].isValid()) buildSelfReadDemandRegisterMap(selfReadRegisterConfigurationTypes, demandRegister.getCoincidentQuantities()[0].getUnit(), register, ObisCodeExtensions.OBISCODE_D_COINCIDENT+3, selfRead, demandRegisterConfiguration, "valleyCoincidentReg1Value",selfReadRegisterId); // valleyCoincidentReg1Value
                            if (demandRegisters[register].getCoincidentQuantities()[1].isValid()) buildSelfReadDemandRegisterMap(selfReadRegisterConfigurationTypes, demandRegister.getCoincidentQuantities()[1].getUnit(), register, ObisCodeExtensions.OBISCODE_D_COINCIDENT+4, selfRead, demandRegisterConfiguration, "valleyCoincidentReg2Value",selfReadRegisterId); // valleyCoincidentReg2Value
                            if (demandRegisters[register].getCoincidentQuantities()[2].isValid()) buildSelfReadDemandRegisterMap(selfReadRegisterConfigurationTypes, demandRegister.getCoincidentQuantities()[2].getUnit(), register, ObisCodeExtensions.OBISCODE_D_COINCIDENT+5, selfRead, demandRegisterConfiguration, "valleyCoincidentReg3Value",selfReadRegisterId); // valleyCoincidentReg3Value

                        }
                        selfReadRegisterId++;
                    }
                }
            } // for (int register=0;register<registerConfigs.length;register++)
        } // if (selfReadRegisterInformation.getNumberOfSelfRead() > 0)
    }

    private void buildSelfReadDemandRegisterMap(SelfReadRegisterConfigurationType[] selfReadRegisterConfigurationTypes,Unit unit,int register,int dField, int selfRead,DemandRegisterConfiguration demandRegisterConfiguration, int id) throws IOException {
        buildSelfReadDemandRegisterMap(selfReadRegisterConfigurationTypes,unit,register,dField,selfRead,demandRegisterConfiguration, "", id);
    }
    private void buildSelfReadDemandRegisterMap(SelfReadRegisterConfigurationType[] selfReadRegisterConfigurationTypes,Unit unit,int register,int dField, int selfRead,DemandRegisterConfiguration demandRegisterConfiguration, String description, int id) throws IOException {
       registerMaps.add(new RegisterMap(ObisCode.fromString("1."+selfReadRegisterConfigurationTypes[register].getQuantityId().getObisBField()+"."+selfReadRegisterConfigurationTypes[register].getQuantityId().getObisCField()+".6."+selfReadRegisterConfigurationTypes[register].getId2()+"."+selfRead),
                                        selfReadRegisterConfigurationTypes[register].getQuantityId().getDescription()+", "+description,
                                        unit,
                                        id,
                                        RegisterMap.getSELFREAD(),
                                        demandRegisterConfiguration.findDemandRegister(selfReadRegisterConfigurationTypes[register].getQuantityId(), selfReadRegisterConfigurationTypes[register].getRegisterType()).getMultiplier()));
    }

    private void buildDemandRegisterMap(DemandRegister[] demandRegisters,Unit unit,int register, int rate,int dField) {
        buildDemandRegisterMap(demandRegisters, unit, register, rate, dField,"");
    }
    private void buildDemandRegisterMap(DemandRegister[] demandRegisters,Unit unit,int register, int rate,int dField, int id) {
        buildDemandRegisterMap(demandRegisters, unit, register, rate, dField,"",id);
    }
    private void buildDemandRegisterMap(DemandRegister[] demandRegisters,Unit unit,int register, int rate,int dField,String description) {
        buildDemandRegisterMap(demandRegisters, unit, register, rate, dField,"",register);
    }
    private void buildDemandRegisterMap(DemandRegister[] demandRegisters,Unit unit,int register, int rate,int dField,String description, int id) {
        registerMaps.add(new RegisterMap(ObisCode.fromString("1."+demandRegisters[register].getQuantityId().getObisBField()+"."+demandRegisters[register].getQuantityId().getObisCField()+"."+dField+"."+rate+".255"),
                         demandRegisters[register].getQuantityId().getDescription()+", "+description,
                         unit,
                         id,
                         RegisterMap.getDEMAND(),
                         demandRegisters[register].getMultiplier()));

    }

    public int getOBISCODE_D_PROJECTED_DEMAND() {
        return OBISCODE_D_PROJECTED_DEMAND;
    }

    public int getOBISCODE_E_MULTIPLE_PEAK_VALUE1() {
        return OBISCODE_E_MULTIPLE_PEAK_VALUE1;
    }

    public int getOBISCODE_E_MULTIPLE_MIN_VALUE1() {
        return OBISCODE_E_MULTIPLE_MIN_VALUE1;
    }

    public int getOBISCODE_E_MULTIPLE_PEAK_VALUE5() {
        return OBISCODE_E_MULTIPLE_PEAK_VALUE5;
    }

    public int getOBISCODE_E_MULTIPLE_MIN_VALUE5() {
        return OBISCODE_E_MULTIPLE_MIN_VALUE5;
    }

}
