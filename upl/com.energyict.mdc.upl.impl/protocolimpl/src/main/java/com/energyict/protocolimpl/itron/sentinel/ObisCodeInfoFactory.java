/*
 * ObisCodeInfoFactory.java
 *
 * Created on July 2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel;

import com.energyict.protocolimpl.base.ObisCodeExtensions;
import java.util.*;
import java.io.*;
import java.math.*;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.ansi.c12.*;
import com.energyict.protocolimpl.ansi.c12.tables.*;
import com.energyict.protocolimpl.itron.sentinel.tables.*;
import com.energyict.obis.ObisCode;
import com.energyict.cbo.*;
/**
 *
 * @author Koen
 */
public class ObisCodeInfoFactory {

    List obisCodeInfos;
    Sentinel sentinel;
    boolean convertRegisterReadsToKiloUnits;

    /** Creates a new instance of ObisCodeInfoFactory */
    public ObisCodeInfoFactory(Sentinel sentinel, boolean convertRegisterReadsToKiloUnits) throws IOException {
        this.sentinel=sentinel;
        this.convertRegisterReadsToKiloUnits = convertRegisterReadsToKiloUnits;
        buildObisCodeInfos();
    }

//    public String registerInfo() throws IOException {
//        
//        StringBuffer strBuff = new StringBuffer();
//        
//        // current energy
//        // current demand
//        // current cumulative demand
//        if (sentinel.getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
//            // read current demand TOU
//        }
//
//        // last billing point energy
//        // last billing point demand
//        // last billing point cumulative demand
//        if (sentinel.getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
//            // read last billing point demand TOU
//        }
//        
//        if (sentinel.getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates() > 0) {
//            // last season point energy
//            // last season point demand
//            // last season point cumulative demand
//            if (sentinel.getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
//                // read last season point demand TOU
//            }
//        }
//
//        if (sentinel.getDataReadFactory().getCapabilitiesDataRead().isMeterHasValidSelfReadData()) {
//            // last selfread point energy
//            // last selfread point demand
//            // last selfread point cumulative demand
//            if (sentinel.getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
//                // read last selfread point demand TOU
//            }
//        }    
//        
//        return strBuff.toString();
//    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        Iterator it = obisCodeInfos.iterator();
        while(it.hasNext()) {
            strBuff.append(it.next()+"\n");
        }
        return strBuff.toString();
    }

    public static final int CURRENT=255;
    public static final int PREVIOUS_SEASON=0;
    public static final int PREVIOUS_DEMAND_RESET=1;
    public static final int SELF_READ_OFFSET=254;

    public void buildObisCodeInfos() throws IOException {
        obisCodeInfos=new ArrayList();
        ActualRegisterTable art = sentinel.getStandardTableFactory().getActualRegisterTable();

        // current registers
        if (sentinel.getStandardTableFactory().getConfigurationTable().isStdTableUsed(StandardTableFactory.CURRENT_REGISTER_DATA_TABLE))
            buildRegisterObisCodeInfos(CURRENT);
        // previous season registers
        if (sentinel.getStandardTableFactory().getConfigurationTable().isStdTableUsed(StandardTableFactory.PREVIOUS_SEASON_DATA_TABLE))
            buildRegisterObisCodeInfos(PREVIOUS_SEASON);
        // previous demand reset registers
        if (sentinel.getStandardTableFactory().getConfigurationTable().isStdTableUsed(StandardTableFactory.PREVIOUS_DEMAND_RESET_DATA_TABLE))
            buildRegisterObisCodeInfos(PREVIOUS_DEMAND_RESET);
        // self read registers
        if (sentinel.getStandardTableFactory().getConfigurationTable().isStdTableUsed(StandardTableFactory.SELF_READ_DATA_TABLE))
            for(int index=0;index<art.getNrOfSelfReads();index++) {
                buildRegisterObisCodeInfos(SELF_READ_OFFSET-index);
            }
    }

    private void buildRegisterObisCodeInfos(int fField) throws IOException {
        SourceInfo si = new SourceInfo(sentinel);
        ActualRegisterTable art = sentinel.getStandardTableFactory().getActualRegisterTable();
        String registerSetInfo;

        switch(fField) {
            case CURRENT: {
                registerSetInfo = "current, ";
            } break;
            case PREVIOUS_SEASON: {
                registerSetInfo = "previous season, ";
            } break;
            case PREVIOUS_DEMAND_RESET: {
                registerSetInfo = "previous demand reset, ";
            } break;
            default: { // SELF_READ_OFFSET
                registerSetInfo = "self read, ";
            } break;
        }

        for(int tier=0;tier<=art.getNrOfTiers();tier++) {
            for(int index=0;index<art.getNrOfSummations();index++) {
                int dataControlEntryIndex = sentinel.getStandardTableFactory().getDataSelectionTable().getSummationSelects()[index];
                if (dataControlEntryIndex != 255) {
                    ObisCodeDescriptor obisCodeDescriptor = si.getObisCodeDescriptor(dataControlEntryIndex);
                    if (obisCodeDescriptor != null) {
                        obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,obisCodeDescriptor.getObisCode().getB(),obisCodeDescriptor.getObisCode().getC(),ObisCode.CODE_D_TIME_INTEGRAL,tier,fField),registerSetInfo+"summation register index "+index+", "+obisCodeDescriptor.getDescription(),si.getUnit(dataControlEntryIndex).getVolumeUnit(),index,dataControlEntryIndex));
                    }
                }
            }

            for(int index=0;index<art.getNrOfDemands();index++) {
                int dataControlEntryIndex = sentinel.getStandardTableFactory().getDataSelectionTable().getDemandSelects()[index];
                if (dataControlEntryIndex != 255) {
                    ObisCodeDescriptor obisCodeDescriptor = si.getObisCodeDescriptor(dataControlEntryIndex);
                    if (obisCodeDescriptor != null) {
                        obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,obisCodeDescriptor.getObisCode().getB(),obisCodeDescriptor.getObisCode().getC(),ObisCode.CODE_D_MAXIMUM_DEMAND,tier,fField),registerSetInfo+"max/min demand register index "+index+", "+obisCodeDescriptor.getDescription(),si.getUnit(dataControlEntryIndex),index,dataControlEntryIndex));
                        if (art.isCumulativeDemandFlag()) {
                            obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,obisCodeDescriptor.getObisCode().getB(),obisCodeDescriptor.getObisCode().getC(),ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,tier,fField),registerSetInfo+"cumulative demand register index "+index+", "+obisCodeDescriptor.getDescription(),si.getUnit(dataControlEntryIndex),index,dataControlEntryIndex));
                        }
                        if (art.isContinueCumulativeDemandFlag()) {
                            obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,obisCodeDescriptor.getObisCode().getB(),obisCodeDescriptor.getObisCode().getC(),ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND,tier,fField),registerSetInfo+"continue cumulative demand register index "+index+", "+obisCodeDescriptor.getDescription(),si.getUnit(dataControlEntryIndex),index,dataControlEntryIndex));
                        }
                    }
                }
            }

            for(int index=0;index<art.getNrOfCoinValues();index++) {

                int dataControlEntryIndex = sentinel.getStandardTableFactory().getDataSelectionTable().getCoincidentSelects()[index];
                if (dataControlEntryIndex != 255) {
                    ObisCodeDescriptor obisCodeDescriptor = si.getObisCodeDescriptor(dataControlEntryIndex);
                    if (obisCodeDescriptor != null) {
                        obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,obisCodeDescriptor.getObisCode().getB(),obisCodeDescriptor.getObisCode().getC(),ObisCodeExtensions.OBISCODE_D_COINCIDENT+index,tier,fField),registerSetInfo+"coincident demand register index "+index+", "+obisCodeDescriptor.getDescription(),si.getUnit(dataControlEntryIndex),index,dataControlEntryIndex));
                    }
                }
            }
        }
    }

    public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeInfo obi = findObisCodeInfo(obisCode);
        return new RegisterInfo(obi.getDescription());
    }

    public RegisterValue getRegister(ObisCode obisCode) throws IOException {

        ObisCodeInfo obi = findObisCodeInfo(obisCode);
        RegisterValue registerValue=null;

        if (obi.isCurrent()) { // F FIELD
            System.out.println("Getting register for current");
            RegisterData registerData = sentinel.getStandardTableFactory().getCurrentRegisterDataTable().getRegisterData();
            if (obi.getTierIndex() == -1)  // E FIELD
                registerValue = doGetRegister(obi, registerData.getTotDatablock());
            else
                registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()]);
        }
        else if (obi.isPreviousSeason()) {
            System.out.println("Getting register for previous season");
            RegisterData registerData = sentinel.getStandardTableFactory().getPreviousSeasonDataTable().getPreviousSeasonRegisterData();
            RegisterInf registerInf = sentinel.getStandardTableFactory().getPreviousSeasonDataTable().getRegisterInfo();
            if (obi.getTierIndex() == -1)  // E FIELD
                registerValue = doGetRegister(obi, registerData.getTotDatablock(), registerInf.getEndDateTime());
            else
                registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()], registerInf.getEndDateTime());
        }
        else if (obi.isPreviousDemandReset()) {
            System.out.println("Getting register for previous demand reset");
            RegisterData registerData = sentinel.getStandardTableFactory().getPreviousDemandResetDataTable().getPreviousDemandResetData();
            RegisterInf registerInf = sentinel.getStandardTableFactory().getPreviousDemandResetDataTable().getRegisterInfo();
            if (obi.getTierIndex() == -1)  // E FIELD
                registerValue = doGetRegister(obi, registerData.getTotDatablock(), registerInf.getEndDateTime());
            else
                registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()], registerInf.getEndDateTime());
        }
        else if (obi.isSelfRead()) {
            System.out.println("Getting register for self read");
            int index = obi.getSelfReadIndex();
            RegisterData registerData = sentinel.getStandardTableFactory().getSelfReadDataTable().getSelfReadList().getSelfReadEntries()[index].getSelfReadRegisterData();
            RegisterInf registerInf = sentinel.getStandardTableFactory().getSelfReadDataTable().getSelfReadList().getSelfReadEntries()[index].getRegisterInfo();
            if (obi.getTierIndex() == -1)  // E FIELD
                registerValue = doGetRegister(obi, registerData.getTotDatablock(), registerInf.getEndDateTime());
            else
                registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()], registerInf.getEndDateTime());
        }

        return registerValue;
    }

    private RegisterValue doGetRegister(ObisCodeInfo obi,DataBlock dataBlock) throws IOException {
        return doGetRegister(obi,dataBlock,null);
    }

    private RegisterValue doGetRegister(ObisCodeInfo obi,DataBlock dataBlock,Date toTime) throws IOException {
        Number value=null;
        Date date=null;
        boolean energy=false;

        if (obi.isTimeIntegral()) { // D FIELD
            System.out.println("doGetRegister: time integral");
            int registerIndex = obi.getRegisterIndex();// C
            value = dataBlock.getSummations()[registerIndex];
            energy=true;
        }
        else if (obi.isMaximumDemand()) {
            System.out.println("doGetRegister: maximum demand");
            int registerIndex = obi.getRegisterIndex();// C
            value = dataBlock.getDemands()[registerIndex].getDemands()[obi.getOccurance()];
            if (dataBlock.getDemands()[registerIndex].getEventTimes() != null)
                date = dataBlock.getDemands()[registerIndex].getEventTimes()[obi.getOccurance()];
        }
        else if (obi.isCumulativeMaximumDemand()) {
            System.out.println("doGetRegister: cum max demand");
            int registerIndex = obi.getRegisterIndex();// C 
            value = dataBlock.getDemands()[registerIndex].getCumDemand();
        }
        else if (obi.isContCumulativeMaximumDemand()) {
            System.out.println("doGetRegister: cont cum max demand");
            int registerIndex = obi.getRegisterIndex();// C 
            value = dataBlock.getDemands()[registerIndex].getContinueCumDemand();
        }
        else if (obi.isCoinMaximumDemandDemand()) {
            System.out.println("doGetRegister: coin max demand");
            int registerIndex = obi.getRegisterIndex();// C 
            value = dataBlock.getCoincidents()[registerIndex].getCoincidentValues()[obi.getOccurance()];
        }

        System.out.println("doGetRegister: unit is " + obi.getUnit());
        System.out.println("doGetRegister: value is " + value);

        Unit unit = obi.getUnit();

        System.out.println("doGetRegister: unit scale is " + unit.getScale());

        if (unit.getScale() == 0 && convertRegisterReadsToKiloUnits) {
            System.out.println("doGetRegister: unit scale is 0, setting to 3");
            unit = Unit.get(unit.getDlmsCode(), 3);
            value = ((BigDecimal)value).divide(new BigDecimal(1000));
        }

        return new RegisterValue(obi.getObisCode(),new Quantity(getEngineeringValue((BigDecimal)value,energy, obi), unit),date,toTime);
    }

    private BigDecimal getEngineeringValue(BigDecimal bd, boolean energy, ObisCodeInfo obi) throws IOException {
        SourceInfo si = new SourceInfo(sentinel);

        return si.basic2engineering(bd,obi.getDatacontrolEntryIndex(),energy);

        // calculate engineering units
//        long scaleFactor = energy?(long)sentinel.getManufacturerTableFactory().getScaleFactorTable().getEnergyScaleFactorVA():(long)sentinel.getManufacturerTableFactory().getScaleFactorTable().getDemandScaleFactorVA();
//        bd = bd.multiply(BigDecimal.valueOf(scaleFactor));
//        bd = bd.movePointLeft(6+3); // see kv2 doc
//        return bd;
    }

    private ObisCodeInfo findObisCodeInfo(ObisCode obisCode) throws IOException {
        Iterator it = obisCodeInfos.iterator();
        while(it.hasNext()) {
            ObisCodeInfo obi = (ObisCodeInfo)it.next();
            if (obi.getObisCode().equals(obisCode)) return obi;
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }
}
