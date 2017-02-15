/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * BillingDataQuantities.java
 *
 * Created on 19 juli 2005, 14:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.elster.alpha.core.classes.BillingDataRegisterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author koen
 */
public class BillingDataRegisterFactoryImpl implements BillingDataRegisterFactory {


    ClassFactory classFactory;

    static public final int CURRENT_BILLING_REGISTERS=0;
    static public final int PREVIOUS_MONTH_BILLING_REGISTERS=1;
    static public final int PREVIOUS_SEASON_BILLING_REGISTERS=2;


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
        int maxBlocks;

        if (set == 0) {
            billingDataRegisters[0] = new ArrayList();
            cbd = classFactory.getClass11BillingData();
            fieldF=255;
        }
        if (set == 1) {
            billingDataRegisters[1] = new ArrayList();
            cbd = classFactory.getClass12PreviousMonthBillingData();
            fieldF=0;
        }
        if (set == 2) {
            billingDataRegisters[2] = new ArrayList();
            cbd = classFactory.getClass13PreviousSeasonBillingData();
            fieldF=1;
        }

        if (classFactory.getClass2IdentificationAndDemandData().isSingleRate()) {
            maxRates=1;
        } else {
            maxRates=4;
        }

        if (classFactory.getClass8FirmwareConfiguration().getPSERIES1() < 2) {
            maxBlocks=1;   // metertypes A1D+","A1T+ types only having kwh in primary meter type...
        } else {
            maxBlocks=2;   // all other types also having alternate metering engine
        }

        for (int block=0;block<maxBlocks;block++) {
            if (block == 1) {
                if (classFactory.getClass8FirmwareConfiguration().getPrimaryPhenomenon() ==
                    classFactory.getClass8FirmwareConfiguration().getAlternatePhenomenon()) {
                    if (classFactory.getClass2IdentificationAndDemandData().isSingleRate())
                         fieldEOffset=1; // 1 for total...
                    else
                         fieldEOffset=5; // 5 in case of TOU meters // rates 5..8 or rate 5 for total
                }
                else {
                    if (classFactory.getClass2IdentificationAndDemandData().isSingleRate())
                         fieldEOffset=0; // 0 for total...
                    else
                         fieldEOffset=1; // start with rate 1
                }
            }
            else if (block == 0) { // block == 0
                if (classFactory.getClass2IdentificationAndDemandData().isSingleRate())
                     fieldEOffset=0; // 1 for total...
                else
                     fieldEOffset=1; // start with rate 1
            }

            for (int fieldE=0;fieldE<maxRates;fieldE++) {

                switch (classFactory.getClass8FirmwareConfiguration().getBlockPhenomenon(block)) {

                    case Class8FirmwareConfiguration.PHENOMENON_UNDEFINED: {
                        //1.1.82.2.fieldE.fieldF // cumulmative maximum demand
                        //1.1.82.6.fieldE.fieldF // maximum demand
                        //1.1.82.8.fieldE.fieldF // time integral 1, energy
                        //1.1.82.128.fieldE.fieldF // coincident
                        buildBillingDataRegisters(cbd, 82, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                    } break; // Class8FirmwareConfiguration.PHENOMENON_UNDEFINED

                    case Class8FirmwareConfiguration.PHENOMENON_ACTIVE: {
                        if (classFactory.getClass2IdentificationAndDemandData().getDirection(block) == Class2IdentificationAndDemandData.IMPORT)
                            //1.1.1.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.1.6.fieldE.fieldF // maximum demand
                            //1.1.1.8.fieldE.fieldF // time integral 1, energy
                            //1.1.1.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 1, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else if (classFactory.getClass2IdentificationAndDemandData().getDirection(block) == Class2IdentificationAndDemandData.EXPORT)
                            //1.1.2.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.2.6.fieldE.fieldF // maximum demand
                            //1.1.2.8.fieldE.fieldF // time integral 1, energy
                            //1.1.2.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 2, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else throw new IOException("BillingDataQuantities, build(), wrong quadrant configuration, CLASS2 EBLKCF"+block+"="+classFactory.getClass2IdentificationAndDemandData().getEBLKCF(block));
                    } break; // Class8FirmwareConfiguration.PHENOMENON_ACTIVE

                    case Class8FirmwareConfiguration.PHENOMENON_APPARENT: {
                        if (classFactory.getClass2IdentificationAndDemandData().isVAImport(block) ||
                            classFactory.getClass2IdentificationAndDemandData().isVASum(block))
                            //1.1.9.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.9.6.fieldE.fieldF // maximum demand
                            //1.1.9.8.fieldE.fieldF // time integral 1, energy
                            //1.1.9.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 9, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else if (classFactory.getClass2IdentificationAndDemandData().isVAExport(block))
                            //1.1.10.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.10.6.fieldE.fieldF // maximum demand
                            //1.1.10.8.fieldE.fieldF // time integral 1, energy
                            //1.1.10.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 10, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else if (classFactory.getClass2IdentificationAndDemandData().isOnlyQ1(block))
                            //1.1.128.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.128.6.fieldE.fieldF // maximum demand
                            //1.1.128.8.fieldE.fieldF // time integral 1, energy
                            //1.1.128.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 128, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else if (classFactory.getClass2IdentificationAndDemandData().isOnlyQ2(block))
                            //1.1.129.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.129.6.fieldE.fieldF // maximum demand
                            //1.1.129.8.fieldE.fieldF // time integral 1, energy
                            //1.1.129.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 129, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else if (classFactory.getClass2IdentificationAndDemandData().isOnlyQ3(block))
                            //1.1.130.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.130.6.fieldE.fieldF // maximum demand
                            //1.1.130.8.fieldE.fieldF // time integral 1, energy
                            //1.1.130.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 130, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else if (classFactory.getClass2IdentificationAndDemandData().isOnlyQ4(block))
                            //1.1.131.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.131.6.fieldE.fieldF // maximum demand
                            //1.1.131.8.fieldE.fieldF // time integral 1, energy
                            //1.1.131.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 131, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else throw new IOException("BillingDataQuantities, build(), wrong quadrant configuration, CLASS2 EBLKCF"+block+"="+classFactory.getClass2IdentificationAndDemandData().getEBLKCF(block));
                    } break; // Class8FirmwareConfiguration.PHENOMENON_APPARENT

                    case Class8FirmwareConfiguration.PHENOMENON_REACTIVE: {
                        if (classFactory.getClass2IdentificationAndDemandData().isvarImport(block))
                            //1.1.3.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.3.6.fieldE.fieldF // maximum demand
                            //1.1.3.8.fieldE.fieldF // time integral 1, energy
                            //1.1.3.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 3, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else if (classFactory.getClass2IdentificationAndDemandData().isvarExport(block))
                            //1.1.4.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.4.6.fieldE.fieldF // maximum demand
                            //1.1.4.8.fieldE.fieldF // time integral 1, energy
                            //1.1.4.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 4, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else if (classFactory.getClass2IdentificationAndDemandData().isOnlyQ1(block))
                            //1.1.5.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.5.6.fieldE.fieldF // maximum demand
                            //1.1.5.8.fieldE.fieldF // time integral 1, energy
                            //1.1.5.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 5, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else if (classFactory.getClass2IdentificationAndDemandData().isOnlyQ2(block))
                            //1.1.6.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.6.6.fieldE.fieldF // maximum demand
                            //1.1.6.8.fieldE.fieldF // time integral 1, energy
                            //1.1.6.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 6, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else if (classFactory.getClass2IdentificationAndDemandData().isOnlyQ3(block))
                            //1.1.7.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.7.6.fieldE.fieldF // maximum demand
                            //1.1.7.8.fieldE.fieldF // time integral 1, energy
                            //1.1.7.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 7, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else if (classFactory.getClass2IdentificationAndDemandData().isOnlyQ4(block))
                            //1.1.8.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.8.6.fieldE.fieldF // maximum demand
                            //1.1.8.8.fieldE.fieldF // time integral 1, energy
                            //1.1.8.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 8, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
//                        else if (classFactory.getClass2IdentificationAndDemandData().isOnlyQ1Q4(block))
//                            //1.1.8.2.fieldE.fieldF // cumulmative maximum demand
//                            //1.1.8.6.fieldE.fieldF // maximum demand
//                            //1.1.8.8.fieldE.fieldF // time integral 1, energy
//                            //1.1.8.128.fieldE.fieldF // coincident
//                            buildBillingDataRegisters(cbd, 3, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else if (classFactory.getClass2IdentificationAndDemandData().isvarSum(block))
                            //1.1.132.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.132.6.fieldE.fieldF // maximum demand
                            //1.1.132.8.fieldE.fieldF // time integral 1, energy
                            //1.1.132.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 132, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else if (classFactory.getClass2IdentificationAndDemandData().isNoQuadrants(block))
                            //1.1.133.2.fieldE.fieldF // cumulmative maximum demand
                            //1.1.133.6.fieldE.fieldF // maximum demand
                            //1.1.133.8.fieldE.fieldF // time integral 1, energy
                            //1.1.133.128.fieldE.fieldF // coincident
                            buildBillingDataRegisters(cbd, 133, fieldE, fieldF, fieldEOffset, block, maxBlocks, set);
                        else throw new IOException("BillingDataQuantities, build(), wrong quadrant configuration, CLASS2 EBLKCF"+block+"="+classFactory.getClass2IdentificationAndDemandData().getEBLKCF(block));
                    } break; // Class8FirmwareConfiguration.PHENOMENON_REACTIVE

                    default: {
                        throw new IOException("BillingDataQuantities, build(), wrong phenomenon info "+classFactory.getClass8FirmwareConfiguration().getBlockPhenomenon(block));
                    }

                } // switch (classFactory.getClass8FirmwareConfiguration().getPrimaryPhenomenon())

            } // for (fieldE=rateStart;fieldE<=maxRates,fieldE++)

        } // for (int block=0;block<maxBlocks;block++)

        addTotalRegisters(set);

    } // private void build(int set) throws IOException

    // KV 19072006
    private void addTotalRegisters(int set) throws IOException {
        List extraBillingDataRegisters;
        if (!classFactory.getClass2IdentificationAndDemandData().isSingleRate()) {
             int totalRegisterRate = classFactory.getAlpha().getTotalRegisterRate();
             extraBillingDataRegisters = new ArrayList();
             Iterator it = billingDataRegisters[set].iterator();
             while(it.hasNext()) {
                 BillingDataRegister bdr = (BillingDataRegister)it.next();
                 if (bdr.getObisCode().getE() == totalRegisterRate) {
                    ObisCode obisCode = new ObisCode(bdr.getObisCode().getA(),bdr.getObisCode().getB(),bdr.getObisCode().getC(),bdr.getObisCode().getD(),0,bdr.getObisCode().getF());
                    RegisterValue registerValue = new RegisterValue(obisCode,bdr.getRegisterValue().getQuantity(),bdr.getRegisterValue().getEventTime(),bdr.getRegisterValue().getFromTime(),bdr.getRegisterValue().getToTime(),bdr.getRegisterValue().getReadTime(),bdr.getRegisterValue().getRegisterSpecId(), bdr.getRegisterValue().getText());
                    extraBillingDataRegisters.add(new BillingDataRegister(obisCode, bdr.getDescription(), registerValue));
                 }
             }
             billingDataRegisters[set].addAll(extraBillingDataRegisters);
        }
    }

    private void buildBillingDataRegisters(ClassBillingData cbd, int fieldC, int fieldE, int fieldF, int fieldEOffset, int block, int maxBlocks, int set) throws IOException {
        Quantity quantity;
        ObisCode obisCode;
        RegisterValue registerValue;
        String description=null;

        if (fieldC==128)
           description= "apparent Q1 Quadrant";
        else if (fieldC==129)
           description= "apparent Q2 Quadrant";
        else if (fieldC==130)
           description= "apparent Q3 Quadrant";
        else if (fieldC==131)
           description= "apparent Q4 Quadrant";
        else if (fieldC==132)
           description= "reactive sum Q1+Q2+Q3+Q4 Quadrant";
        else if (fieldC==133)
           description= "reactive no Quadrants specified";

        quantity = new Quantity(cbd.getKWH(block, fieldE),classFactory.getClass8FirmwareConfiguration().getBlockPhenomenonUnit(block, true));
        obisCode = ObisCode.fromByteArray(new byte[]{1,1,(byte)fieldC,8,(byte)(fieldEOffset+fieldE),(byte)fieldF});
        registerValue = new RegisterValue(obisCode,quantity);
        billingDataRegisters[set].add(new BillingDataRegister(obisCode, description, registerValue));

        quantity = new Quantity(cbd.getKW(block, fieldE),classFactory.getClass8FirmwareConfiguration().getBlockPhenomenonUnit(block, false));
        obisCode = ObisCode.fromByteArray(new byte[]{1,1,(byte)fieldC,6,(byte)(fieldEOffset+fieldE),(byte)fieldF});
        registerValue = new RegisterValue(obisCode,quantity,cbd.getTD(block, fieldE));
        billingDataRegisters[set].add(new BillingDataRegister(obisCode, description, registerValue));

        quantity = new Quantity(cbd.getKWCUM(block, fieldE),classFactory.getClass8FirmwareConfiguration().getBlockPhenomenonUnit(block, false));
        obisCode = ObisCode.fromByteArray(new byte[]{1,1,(byte)fieldC,2,(byte)(fieldEOffset+fieldE),(byte)fieldF});
        registerValue = new RegisterValue(obisCode,quantity);
        billingDataRegisters[set].add(new BillingDataRegister(obisCode, description, registerValue));

        if (maxBlocks == 2) {
            // coincident demand taken from the other block...
            quantity = new Quantity(cbd.getKWC(block==1?0:1, fieldE),classFactory.getClass8FirmwareConfiguration().getBlockPhenomenonUnit(block==1?0:1, false));
            obisCode = ObisCode.fromByteArray(new byte[]{1,1,(byte)fieldC,(byte)128,(byte)(fieldEOffset+fieldE),(byte)fieldF});
            registerValue = new RegisterValue(obisCode,quantity,cbd.getTD(block==1?0:1, fieldE));
            billingDataRegisters[set].add(new BillingDataRegister(obisCode, (block==0?"Primary":"Alternate")+"metering block coincident demand value", registerValue));
        } // if (maxBlocks == 2)
    } // buildBillingDataRegisters



    public List getBillingDataRegisters(int set) throws IOException {
        if (billingDataRegisters[set] == null)
            build(set);
        return billingDataRegisters[set];
   }


} // public class BillingDataQuantities
