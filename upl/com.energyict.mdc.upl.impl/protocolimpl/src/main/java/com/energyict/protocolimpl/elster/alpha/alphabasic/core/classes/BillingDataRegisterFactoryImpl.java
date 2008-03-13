/*
 * BillingDataQuantities.java
 *
 * Created on 19 juli 2005, 14:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes;

import java.io.*;
import java.util.*;
import java.math.*;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.elster.alpha.core.connection.*;
import com.energyict.protocolimpl.elster.alpha.core.classes.ClassParseUtils;
import com.energyict.protocolimpl.elster.alpha.core.classes.BillingDataRegisterFactory;

import com.energyict.protocolimpl.base.ParseUtils;  
import com.energyict.cbo.*; 
import com.energyict.obis.ObisCode;

/**
 *
 * @author koen
 */
public class BillingDataRegisterFactoryImpl implements BillingDataRegisterFactory {
    
    
    ClassFactory classFactory;
    
    static public final int CURRENT_BILLING_REGISTERS=0;
    static public final int PREVIOUS_MONTH_BILLING_REGISTERS=1;
    static public final int PREVIOUS_SEASON_BILLING_REGISTERS=2;
    
    
    // KV_TO_DO, max blocks is 6 TOU and 2 COIN
    
    List[] billingDataRegisters=null;
    
    /** Creates a new instance of BillingDataQuantities */
    public BillingDataRegisterFactoryImpl(ClassFactory classFactory) {
        billingDataRegisters = new List[3];
        this.classFactory=classFactory;
    }
    
    
    public void buildAll() throws IOException {
        
        
        // current, previous and previous season
        build(CURRENT_BILLING_REGISTERS);
        build(PREVIOUS_MONTH_BILLING_REGISTERS);
        build(PREVIOUS_SEASON_BILLING_REGISTERS);
    }
    
    private void build(int set) throws IOException {
        ClassBillingData cbd=null;
        int fieldF=255;
        int fieldEOffset=0;
        int maxRates;
        int maxBlocks=6;
        
        if (set == CURRENT_BILLING_REGISTERS) {
            billingDataRegisters[0] = new ArrayList();
            cbd = classFactory.getClass11BillingData();
            fieldF=255;
        }
        if (set == PREVIOUS_MONTH_BILLING_REGISTERS) {
            billingDataRegisters[1] = new ArrayList();
            cbd = classFactory.getClass12PreviousMonthBillingData();
            fieldF=0;
        }
        if (set == PREVIOUS_SEASON_BILLING_REGISTERS) {
            billingDataRegisters[2] = new ArrayList();
            cbd = classFactory.getClass13PreviousSeasonBillingData();
            fieldF=1;
        }
        
        if (classFactory.getClass2IdentificationAndDemandData().isSingleRate()) {
            maxRates=1;
        } else {
            maxRates=4;
        }
        
        for (int block=0;block<=5;block++) {
            if (classFactory.getClass2IdentificationAndDemandData().isSingleRate())
                 fieldEOffset=0; // 1 for total...
            else
                 fieldEOffset=1; // start with rate 1
            for (int fieldE=0;fieldE<maxRates;fieldE++) {
                if (classFactory.getClass14LoadProfileConfiguration().isTOUBlockEnabled(block))
                    buildBillingDataRegisters(cbd, fieldE, fieldF, fieldEOffset, block, set);
            } // for (fieldE=rateStart;fieldE<=maxRates,fieldE++) 
        } // for (int block=0;block<maxBlocks;block++)

        for (int block=6;block<=7;block++) {
            if (classFactory.getClass2IdentificationAndDemandData().isSingleRate())
                 fieldEOffset=0; // 1 for total...
            else
                 fieldEOffset=1; // start with rate 1
            for (int fieldE=0;fieldE<maxRates;fieldE++) {
                if (classFactory.getClass14LoadProfileConfiguration().isTOUBlockEnabled(block))
                    buildBillingDataCoincidentRegisters(cbd, fieldE, fieldF, fieldEOffset, block, set);
            } // for (fieldE=rateStart;fieldE<=maxRates,fieldE++) 
        } // for (int block=0;block<maxBlocks;block++)
        
    } // private void build(int billingIndex) throws IOException

    
    /*
     *  Build all registers for the 6 TOU blocks
     */
    private void buildBillingDataRegisters(ClassBillingData cbd, int fieldE, int fieldF, int fieldEOffset, int block, int set) throws IOException {
        Quantity quantity;
        ObisCode obisCode;
        RegisterValue registerValue;
        String description = classFactory.getClass14LoadProfileConfiguration().getTOUDescription(block);
        int fieldC = classFactory.getClass14LoadProfileConfiguration().getTOUObisCField(block);
        
        
        
        quantity = new Quantity(cbd.getKWH(block, fieldE),classFactory.getClass14LoadProfileConfiguration().getTOUUnit(block, true));
        obisCode = ObisCode.fromByteArray(new byte[]{1,1,(byte)fieldC,8,(byte)(fieldEOffset+fieldE),(byte)fieldF});
        registerValue = new RegisterValue(obisCode,quantity);
        billingDataRegisters[set].add(new BillingDataRegister(obisCode, description, registerValue));

        quantity = new Quantity(cbd.getKW(block, fieldE),classFactory.getClass14LoadProfileConfiguration().getTOUUnit(block, false));
        obisCode = ObisCode.fromByteArray(new byte[]{1,1,(byte)fieldC,6,(byte)(fieldEOffset+fieldE),(byte)fieldF});
        registerValue = new RegisterValue(obisCode,quantity,cbd.getTD(block, fieldE));
        billingDataRegisters[set].add(new BillingDataRegister(obisCode, description, registerValue));

        quantity = new Quantity(cbd.getKWCUM(block, fieldE),classFactory.getClass14LoadProfileConfiguration().getTOUUnit(block, false));
        obisCode = ObisCode.fromByteArray(new byte[]{1,1,(byte)fieldC,2,(byte)(fieldEOffset+fieldE),(byte)fieldF});
        registerValue = new RegisterValue(obisCode,quantity);
        billingDataRegisters[set].add(new BillingDataRegister(obisCode, description, registerValue));
        
        if ((fieldEOffset == 1) && (fieldE == 0)) {
            quantity = new Quantity(cbd.getKWHtotal(block),classFactory.getClass14LoadProfileConfiguration().getTOUUnit(block, true));
            obisCode = ObisCode.fromByteArray(new byte[]{1,1,(byte)fieldC,8,0,(byte)fieldF});
            registerValue = new RegisterValue(obisCode,quantity);
            billingDataRegisters[set].add(new BillingDataRegister(obisCode, description, registerValue));
        }
        

    } // buildBillingDataRegisters

    private void buildBillingDataCoincidentRegisters(ClassBillingData cbd, int fieldE, int fieldF, int fieldEOffset, int block, int set) throws IOException {
        
        Quantity quantity;
        ObisCode obisCode;
        RegisterValue registerValue;
        int touCfg = classFactory.getClass14LoadProfileConfiguration().getTBLKCF(block);
        int triggerTOUBlock = (touCfg & 0x07)-1;
        int captureTOUBlock = ((touCfg & 0x70) >> 4)-1;
        String triggerDescription = classFactory.getClass14LoadProfileConfiguration().getTOUDescription(triggerTOUBlock);
        String captureDescription = classFactory.getClass14LoadProfileConfiguration().getTOUDescription(captureTOUBlock);
        int captureFieldC = classFactory.getClass14LoadProfileConfiguration().getTOUObisCField(captureTOUBlock);
        
        // KV_TO_DO Coincident taken from COIN block...
        // Coïncident demand taken from the other block...
        // We can also use cbd.getKW(captureTOUBlock, fieldE) instead of cbd.getAK(block-6, fieldE) ?????
        quantity = new Quantity(cbd.getAK(block-6, fieldE),classFactory.getClass14LoadProfileConfiguration().getTOUUnit(captureTOUBlock, false));
        obisCode = ObisCode.fromByteArray(new byte[]{1,1,(byte)captureFieldC,(byte)128,(byte)(fieldEOffset+fieldE),(byte)fieldF});
        registerValue = new RegisterValue(obisCode,quantity,cbd.getTD(captureTOUBlock,fieldE));
        billingDataRegisters[set].add(new BillingDataRegister(obisCode,"coincident "+captureDescription+" on demand trigger "+triggerDescription , registerValue));
        
        // Power factor
        quantity = new Quantity(cbd.getPF(block-6, fieldE),Unit.get(""));
        obisCode = ObisCode.fromByteArray(new byte[]{1,1,13,(byte)128,(byte)(fieldEOffset+fieldE),(byte)fieldF});
        registerValue = new RegisterValue(obisCode,quantity);
        billingDataRegisters[set].add(new BillingDataRegister(obisCode,"coincident power factor on demand trigger "+triggerDescription , registerValue));
        
        if ((fieldEOffset == 1) && (fieldE == 0)) {
            // average Power factor for multiple rate TOU meter
            quantity = new Quantity(cbd.getPF(block-6, fieldE),Unit.get(""));
            obisCode = ObisCode.fromByteArray(new byte[]{1,1,13,(byte)128,0,(byte)fieldF});
            registerValue = new RegisterValue(obisCode,quantity);
            billingDataRegisters[set].add(new BillingDataRegister(obisCode,"coincident power factor on demand trigger "+triggerDescription , registerValue));
        }
    }

    
    
    public List getBillingDataRegisters(int set) throws IOException {
        if (billingDataRegisters[set] == null) 
            build(set);
        return billingDataRegisters[set];
   }

    
} // public class BillingDataQuantities
