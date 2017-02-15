/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Class8FirmwareConfiguration.java
 *
 * Created on 12 juli 2005, 13:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class Class8FirmwareConfiguration extends AbstractClass {

    ClassIdentification classIdentification = new ClassIdentification(8,64,false);

    static public final int PHENOMENON_UNDEFINED=0;
    static public final int PHENOMENON_ACTIVE=1;
    static public final int PHENOMENON_APPARENT=2;
    static public final int PHENOMENON_REACTIVE=3;


    final Unit[] XUOM_UNITS= {Unit.get(""),Unit.get("kWh"),Unit.get("kVAh"),Unit.get("kvarh")};

    //    A1D+:0 A1T+:1 A1K+:2 A1R+:3 AT,K,R:6 A1D:8 -A:9 -L:$A -AL:$B
    String[] meterTypes = {"A1D+","A1T+","A1K+","A1R+","","","AT,K,R","","A1D","-A","-L","-AL"};

    int MKTPROD;
    int SSPEC1;
    int GROUP1;
    int REVNO1;
    int PCODE;
    int PSERIES1;
    // RESERVED [3]
    // SSPEC2 [3]
    // GROUP2 [1]
    // REVNO2 [1]
    // PCODE2 [1]
    // PSERIES2 [1]
    // RESERVED [3]
    long SSPEC3; // referenced in class 29, bcd 10 digits
    // GROUP3 [1]
    // REVNO3 [1]
    int XUOM; // referenced in class 6, binary
    // SCRATCH [30]
    int LPLMEM;
    int INTERNAL_NUM;
    // SPARE [3]

    public String toString() {
        return "Class8FirmwareConfiguration: MKTPROD="+MKTPROD+", SSPEC1="+SSPEC1+", GROUP1=0x"+Integer.toHexString(GROUP1)+", REVNO1="+REVNO1+", PCODE=0x"+Integer.toHexString(PCODE)+", PSERIES1="+PSERIES1+" ("+meterTypes[PSERIES1]+"), SSPEC3="+SSPEC3+
               ", XUOM=0x"+Integer.toHexString(XUOM)+", LPLMEM=0x"+Integer.toHexString(LPLMEM)+", INTERNAL_NUM="+INTERNAL_NUM;
    }

    public String getFirmwareVersion() {
        return "MKTPROD="+MKTPROD+", SSPEC1="+SSPEC1+", GROUP1=0x"+Integer.toHexString(GROUP1)+", REVNO1="+REVNO1+", PCODE=0x"+Integer.toHexString(PCODE)+", PSERIES1="+PSERIES1+" ("+meterTypes[PSERIES1]+"), SSPEC3="+SSPEC3+
               ", XUOM=0x"+Integer.toHexString(XUOM)+", LPLMEM=0x"+Integer.toHexString(LPLMEM)+", INTERNAL_NUM="+INTERNAL_NUM;
    }

    /** Creates a new instance of Class8FirmwareConfiguration */
    public Class8FirmwareConfiguration(ClassFactory classFactory) {
        super(classFactory);
    }

    protected void parse(byte[] data) throws IOException {
        MKTPROD = ProtocolUtils.getBCD2Int(data,0, 1);
        SSPEC1 = ProtocolUtils.getBCD2Int(data,1, 3);
        GROUP1 = ProtocolUtils.getInt(data,4, 1);
        REVNO1 = ProtocolUtils.getInt(data,5, 1);
        PCODE =  ProtocolUtils.getInt(data,6, 1);
        PSERIES1 =  ProtocolUtils.getInt(data,7, 1);
        // pad 13 bytes
        SSPEC3 = ParseUtils.getBCD2Long(data, 21, 3);
        // pad = 2
        XUOM = ProtocolUtils.getInt(data,26, 2);
        // SCRATCH [30]
        LPLMEM = ProtocolUtils.getInt(data,58, 2);
        INTERNAL_NUM = ProtocolUtils.getInt(data,60, 1);

    }

    protected ClassIdentification getClassIdentification() {
        return classIdentification;
    }

    public int getMKTPROD() {
        return MKTPROD;
    }

    public int getSSPEC1() {
        return SSPEC1;
    }

    public int getGROUP1() {
        return GROUP1;
    }

    public int getREVNO1() {
        return REVNO1;
    }

    public int getPCODE() {
        return PCODE;
    }

    public int getPSERIES1() {
        return PSERIES1;
    }

    public long getSSPEC3() {
        return SSPEC3;
    }

    public int getXUOM() {
        return XUOM;
    }

    public int getLPLMEM() {
        return LPLMEM;
    }

    public String getMeterType() {
        return meterTypes[getPSERIES1()];
    }

    public boolean isRMeterType() {
        return getMeterType().indexOf("R") >= 0;
    }
    public boolean isKMeterType() {
        return getMeterType().indexOf("K") >= 0;
    }

    public Unit getPrimaryMeterFlowUnit() {
        return getPrimaryMeterUnit().getFlowUnit();
    }

    public Unit getPrimaryMeterUnit() {
       int index = getPrimaryPhenomenon();
       if (index > 3)
           return Unit.get("");
       else
          return XUOM_UNITS[index];
    }

    public Unit getAlternateMeterFlowUnit() {
        return getAlternateMeterUnit().getFlowUnit();
    }

    public Unit getAlternateMeterUnit() {
       int index = getAlternatePhenomenon();
       if (index > 3)
           return Unit.get("");
       else
          return XUOM_UNITS[index];
    }

    /*
     *  @return 0=undefined, 1=active, 2=apparent, 3=reactive
     */
    public int getPrimaryPhenomenon() {
        return  ((getXUOM() >> 12) & 0x000F);
    }

    public int getAlternatePhenomenon() {
        return  ((getXUOM() >> 8) & 0x000F);
    }

    /*
     *   We suppose that primary is block 0 and alternate is block 1
     */
    public int getBlockPhenomenon(int block) {
        if (block==0)
            return getPrimaryPhenomenon();
        else
            return getAlternatePhenomenon();
    }

    public Unit getBlockPhenomenonUnit(int block, boolean energy) {
        if (block==0)
            return energy?getPrimaryMeterUnit():getPrimaryMeterFlowUnit();
        else
            return energy?getAlternateMeterUnit():getAlternateMeterFlowUnit();
    }
}
