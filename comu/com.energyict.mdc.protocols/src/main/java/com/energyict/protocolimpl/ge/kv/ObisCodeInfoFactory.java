/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeInfoFactory.java
 *
 * Created on 16 november 2005, 16:14
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.ansi.c12.tables.ActualRegisterTable;
import com.energyict.protocolimpl.ansi.c12.tables.DataBlock;
import com.energyict.protocolimpl.ansi.c12.tables.RegisterData;
import com.energyict.protocolimpl.ansi.c12.tables.RegisterInf;
import com.energyict.protocolimpl.ge.kv.tables.SourceInfo;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class ObisCodeInfoFactory {

    List obisCodeInfos;
    GEKV gekv;

    /** Creates a new instance of ObisCodeInfoFactory */
    public ObisCodeInfoFactory(GEKV gekv) throws IOException {
        this.gekv=gekv;
        buildObisCodeInfos();
    }

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
        ActualRegisterTable art = gekv.getStandardTableFactory().getActualRegisterTable();
        // current registers
        buildRegisterObisCodeInfos(CURRENT);
        // previous season registers
        buildRegisterObisCodeInfos(PREVIOUS_SEASON);
        // previous demand reset registers
        buildRegisterObisCodeInfos(PREVIOUS_DEMAND_RESET);
        // self read registers
        for(int index=0;index<art.getNrOfSelfReads();index++) {
            buildRegisterObisCodeInfos(SELF_READ_OFFSET-index);
        }
    }

    public static final int CONT_CUMULATIVE_DEMAND=128;
    public static final int COIN_DEMAND=129;


    private void buildRegisterObisCodeInfos(int fField) throws IOException {
        SourceInfo si = new SourceInfo(gekv);
        ActualRegisterTable art = gekv.getStandardTableFactory().getActualRegisterTable();
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
                int c = si.getSummationCField(index);
                int d = ObisCode.CODE_D_TIME_INTEGRAL;

                obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,1,c,d,tier,fField),registerSetInfo+"summation register index "+index+", unit ",si.getSummationUnit(index),index));
            }

            for(int index=0;index<art.getNrOfDemands();index++) {
                int c = si.getDemandCField(index);
                int d = ObisCode.CODE_D_MAXIMUM_DEMAND;
                for(int occurance=0;occurance<art.getNrOfOccur();occurance++) {
                    obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,occurance+1,c,d,tier,fField),registerSetInfo+"max/min demand register index "+index+", unit ",si.getDemandUnit(index),index));
                }
                if (art.isCumulativeDemandFlag()) {
                    d = ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND;
                    obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,1,c,d,tier,fField),registerSetInfo+"cumulative demand register index "+index+", unit ",si.getDemandUnit(index),index));
                }
                if (art.isContinueCumulativeDemandFlag()) {
                    d = CONT_CUMULATIVE_DEMAND;
                    obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,1,c,d,tier,fField),registerSetInfo+"continue cumulative demand register index "+index+", unit ",si.getDemandUnit(index),index));
                }
            }

            for(int index=0;index<art.getNrOfCoinValues();index++) {
                int c = si.getCoincidentCField(index);
                int d = COIN_DEMAND;
                for(int occurance=0;occurance<art.getNrOfOccur();occurance++) {
                    obisCodeInfos.add(new ObisCodeInfo(new ObisCode(1,occurance+1,c,d,tier,fField),registerSetInfo+"coincident demand register index "+index+", unit ",si.getCoincidentUnit(index),index));
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
            RegisterData registerData = gekv.getStandardTableFactory().getCurrentRegisterDataTable().getRegisterData();
            if (obi.getTierIndex() == -1)  // E FIELD
                registerValue = doGetRegister(obi, registerData.getTotDatablock());
            else
                registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()]);
        }
        else if (obi.isPreviousSeason()) {
            RegisterData registerData = gekv.getStandardTableFactory().getPreviousSeasonDataTable().getPreviousSeasonRegisterData();
            RegisterInf registerInf = gekv.getStandardTableFactory().getPreviousSeasonDataTable().getRegisterInfo();
            if (obi.getTierIndex() == -1)  // E FIELD
                registerValue = doGetRegister(obi, registerData.getTotDatablock(), registerInf.getEndDateTime());
            else
                registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()], registerInf.getEndDateTime());
        }
        else if (obi.isPreviousDemandReset()) {
            RegisterData registerData = gekv.getStandardTableFactory().getPreviousDemandResetDataTable().getPreviousDemandResetData();
            RegisterInf registerInf = gekv.getStandardTableFactory().getPreviousDemandResetDataTable().getRegisterInfo();
            if (obi.getTierIndex() == -1)  // E FIELD
                registerValue = doGetRegister(obi, registerData.getTotDatablock(), registerInf.getEndDateTime());
            else
                registerValue = doGetRegister(obi, registerData.getTierDataBlocks()[obi.getTierIndex()], registerInf.getEndDateTime());
        }
        else if (obi.isSelfRead()) {
            int index = obi.getSelfReadIndex();
            RegisterData registerData = gekv.getStandardTableFactory().getSelfReadDataTable().getSelfReadList().getSelfReadEntries()[index].getSelfReadRegisterData();
            RegisterInf registerInf = gekv.getStandardTableFactory().getSelfReadDataTable().getSelfReadList().getSelfReadEntries()[index].getRegisterInfo();
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
            int registerIndex = obi.getRegisterIndex();// C
            value = dataBlock.getSummations()[registerIndex];
            energy=true;
        }
        else if (obi.isMaximumDemand()) {
            int registerIndex = obi.getRegisterIndex();// C
            value = dataBlock.getDemands()[registerIndex].getDemands()[obi.getOccurance()];
            if (dataBlock.getDemands()[registerIndex].getEventTimes() != null)
               date = dataBlock.getDemands()[registerIndex].getEventTimes()[obi.getOccurance()];
        }
        else if (obi.isCumulativeMaximumDemand()) {
            int registerIndex = obi.getRegisterIndex();// C
            value = dataBlock.getDemands()[registerIndex].getCumDemand();
        }
        else if (obi.isContCumulativeMaximumDemand()) {
            int registerIndex = obi.getRegisterIndex();// C
            value = dataBlock.getDemands()[registerIndex].getContinueCumDemand();
        }
        else if (obi.isCoinMaximumDemandDemand()) {
            int registerIndex = obi.getRegisterIndex();// C
            value = dataBlock.getCoincidents()[registerIndex].getCoincidentValues()[obi.getOccurance()];
        }

        return new RegisterValue(obi.getObisCode(),new Quantity(getEngineeringValue((BigDecimal)value,energy), obi.getUnit()),date,toTime);
    }

    private BigDecimal getEngineeringValue(BigDecimal bd, boolean energy) throws IOException {
        // calculate engineering units
        long scaleFactor = energy?(long)gekv.getManufacturerTableFactory().getScaleFactorTable().getEnergyScaleFactorVA():(long)gekv.getManufacturerTableFactory().getScaleFactorTable().getDemandScaleFactorVA();
        bd = bd.multiply(BigDecimal.valueOf(scaleFactor));
        bd = energy?bd.movePointLeft(6+3):bd.movePointLeft(4+3); // see kv doc page 97 for energy, 100 for demand
        return bd;
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
