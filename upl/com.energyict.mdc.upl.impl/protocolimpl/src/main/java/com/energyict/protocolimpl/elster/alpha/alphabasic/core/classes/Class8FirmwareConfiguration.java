/*
 * Class8FirmwareConfiguration.java
 *
 * Created on 12 juli 2005, 13:53
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
import com.energyict.protocolimpl.elster.alpha.core.connection.*;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.cbo.*;

/**
 *
 * @author Koen
 */
public class Class8FirmwareConfiguration extends AbstractClass {
    
    ClassIdentification classIdentification = new ClassIdentification(8,8,false);

    
    
    int SSPEC;
    int GROUP;
    int REVNO;
    private int PCODE;
    
// pprim primary metering constants flag (pcode:15)
//       1 = primary metering (use class00 and class02 constants) 
//       0 = use predefined meter constants in class07.
// ptou tou enable flag (pcode:14)
//       1 = perform tou (checks class04 and 05 and power outage timekeeping)
//       0 = disable tou checks and power fail (demand metering).
// pbat battery test flag (pcode:13)
//       1 = conduct battery test
//       0 = do not conduct battery test.
// spare unused (pcode:12); don't care.
// pfut future configuration switch selector (pcode:11)
//       1 = future switch enabled (checks class19-23)
//       0 = future switch disabled.
// palt alternate quantity definition flag (pcode:10) 
//       1 = alternate input is VAh, 0 = alternate input is VARh.
// ptoue tou energy definition flag (pcode:9) 
//       1 = tou energy is driven by the alternate input
//       0 =tou energy is driven by the kWh input.
// ptoud tou demand definition flag (pcode:8) 
//       1 = tou demand is driven by the alternate input
//       0 = tou demand is driven by the kWh input.

    static private final int PALT=0x04;
// pseries device family code (pcode:0-7)
    /*
0 = EMF-2400,
1 = EMF-2500,
2 = EMF-2600,
3 = EMF-2160,
4 = EMF-2460,
5 = EMF-3410,
6 = A1T, A1R, A1K Alpha,
7 = EMF-3110,
8 = A1D Alpha,
9 = "L" Alpha: A1T with Load profile (single channel) option board,
A = "A" Alpha: A1R, A1K with Advanced (multi-quadrant) option board, 18 Alpha Meter Abridged Data Dictionary
B = "AL" Alpha: A1R, A1K with Advanced (multi-quadrant), Load profile (multi-channel) option board.    
      */
    static private final int MAX_METERTYPES=11;
    static private final String[] METERTYPES={"EMF-2400","EMF-2500","EMF-2600","EMF-2160","EMF-2460","EMF-3410","A1T, A1R, A1K Alpha","EMF-3110","A1D Alpha","A1T-L","A1R-A, A1K-A","A1R-AL, A1K-AL"};
    
    static public final int D_TYPE=0; // A1D
    static public final int K_TYPE=1; // A1K
    static public final int R_TYPE=2; // A1R
    static public final int OTHER_TYPE=3;
    
    
    
    
    int XUOMHI; // bit 15 .. 8 VAh import, VAh export, Qh import, Qh export, kvarh import, kvarh export, kWh import, kWhexport
    
    public String toString() {
        return "Class8FirmwareConfiguration: SSPEC="+SSPEC+", GROUP=0x"+Integer.toHexString(GROUP)+", REVNO="+REVNO+", XUOMHI=0x"+Integer.toHexString(XUOMHI)+", PCODE="+Integer.toHexString(getPCODE());
    }
    
    public String getFirmwareVersion() {
        return "SSPEC="+SSPEC+", GROUP=0x"+Integer.toHexString(GROUP)+", REVNO="+REVNO+", XUOMHI=0x"+Integer.toHexString(XUOMHI);
    }
    
    /** Creates a new instance of Class8FirmwareConfiguration */
    public Class8FirmwareConfiguration(ClassFactory classFactory) {
        super(classFactory);
    }
    
    protected void parse(byte[] data) throws IOException {
        SSPEC = ProtocolUtils.getBCD2Int(data,0, 3);
        GROUP = ProtocolUtils.getBCD2Int(data,3, 1);
        REVNO = ProtocolUtils.getInt(data,4, 1);
        setPCODE(ProtocolUtils.getInt(data,5, 2));
        XUOMHI = ProtocolUtils.getInt(data,7, 1);
    }
    
    protected ClassIdentification getClassIdentification() {
        return classIdentification; 
    }


    public int getSSPEC() {
        return SSPEC;
    }

    public int getGROUP() {
        return GROUP;
    }

    public int getREVNO() {
        return REVNO;
    }

    public int getXUOMHI() {
        return XUOMHI;
    }

    public String getRegisterFirmware() {
        return ProtocolUtils.buildStringDecimal(getSSPEC(), 6)+" "+ProtocolUtils.buildStringDecimal(getGROUP(),2);
    }
    public String getMeterTypeString() {
        return METERTYPES[getPSERIES()];
    }

    public int getMeterType() {
       if (isDType()) return D_TYPE;  
       if (isRType()) return R_TYPE;
       if (isKType()) return K_TYPE;
       else return OTHER_TYPE;
    }
    
    public boolean isDType() {
        return getMeterTypeString().indexOf("D")>=0;
    }
    public boolean isKType() {
        return getMeterTypeString().indexOf("K")>=0;
    }
    public boolean isRType() {
        return getMeterTypeString().indexOf("R")>=0;
    }
    
    public int getPSERIES() {
        return getPCODE()&0xFF;
    }
    
    public int getPCODEFlags() {
        return getPCODE() >> 8;
    }
    
    public int getPCODE() {
        return PCODE;
    }

    /*
     *   Starting bit = 0 .. 7
     */
    public boolean isPCODEFlag(int bit) {
        return (getPCODEFlags()&(0x01<<bit)) == (0x01<<bit);
    }
    
    public boolean isVAAlternateInput() {
        return isPCODEFlag(PALT);
    }
    public boolean isVarAlternateInput() {
        return !isPCODEFlag(PALT);
    }
    
    public void setPCODE(int PCODE) {
        this.PCODE = PCODE;
    }
    
    
//    public int getPrimaryPhenomenon() {
//        return  0; //((getXUOM() >> 12) & 0x000F);   // KV_TO_DO
//    }
//    
//    public int getAlternatePhenomenon() {
//        return  0; // ((getXUOM() >> 8) & 0x000F);   // KV_TO_DO  
//    }
//    
//    /*
//     *   We suppose that primary is block 0 and alternate is block 1
//     */
//    public int getBlockPhenomenon(int block) {
//        if (block==0) 
//            return getPrimaryPhenomenon();
//        else
//            return getAlternatePhenomenon();
//    }
//            
//    public Unit getBlockPhenomenonUnit(int block, boolean energy) {
//        if (block==0) 
//            return energy?getPrimaryMeterUnit():getPrimaryMeterFlowUnit();
//        else
//            return energy?getAlternateMeterUnit():getAlternateMeterFlowUnit();
//    }
//    
//    
//    public Unit getPrimaryMeterFlowUnit() {
//        return getPrimaryMeterUnit().getFlowUnit();
//    }
//    
//    public Unit getPrimaryMeterUnit() {
//       return Unit.get(""); // KV_TO_DO
////       int index = getPrimaryPhenomenon(); 
////       if (index > 3)
////           return Unit.get("");
////       else
////          return XUOM_UNITS[index]; 
//    }
//    
//    public Unit getAlternateMeterFlowUnit() {
//        return getAlternateMeterUnit().getFlowUnit();
//    }
//    
//    public Unit getAlternateMeterUnit() {
//       return Unit.get(""); // KV_TO_DO
////       int index = getAlternatePhenomenon(); 
////       if (index > 3)
////           return Unit.get("");
////       else
////          return XUOM_UNITS[index]; 
//    }
    
}
