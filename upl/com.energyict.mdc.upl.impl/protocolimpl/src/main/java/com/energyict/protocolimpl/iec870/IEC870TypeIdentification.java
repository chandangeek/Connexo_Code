/*
 * TypeIdentification.java
 *
 * Created on 18 juni 2003, 16:11
 */

package com.energyict.protocolimpl.iec870;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class IEC870TypeIdentification {
    public static final List typeids = new ArrayList();
    static {
        typeids.add(new IEC870TypeIdentification(1,"Single-point information","M_SP_NA_1"));
        typeids.add(new IEC870TypeIdentification(2,"Single-point information with time tag","M_SP_TA_1"));
        typeids.add(new IEC870TypeIdentification(3,"Double-point information","M_DP_NA_1"));
        typeids.add(new IEC870TypeIdentification(4,"Double-point information with time tag","M_DP_TA_1"));
        typeids.add(new IEC870TypeIdentification(5,"Step position information","M_ST_NA_1"));
        typeids.add(new IEC870TypeIdentification(6,"Step position information with time tag","M_ST_TA_1"));
        typeids.add(new IEC870TypeIdentification(7,"Bitstring of 32 bit","M_BO_NA_1"));
        typeids.add(new IEC870TypeIdentification(8,"Bitstring of 32 bit with time tag","M_BO_TA_1"));
        typeids.add(new IEC870TypeIdentification(9,"Measured value, normalized value","M_ME_NA_1"));
        typeids.add(new IEC870TypeIdentification(10,"Measured value, normalized value with time tag","M_ME_TA_1"));
        typeids.add(new IEC870TypeIdentification(11,"Measured value, scaled value","M_ME_NB_1"));
        typeids.add(new IEC870TypeIdentification(12,"Measured value, scaled value with time tag","M_ME_TB_1"));
        typeids.add(new IEC870TypeIdentification(13,"Measured value, short floating point value","M_ME_NC_1"));
        typeids.add(new IEC870TypeIdentification(14,"Measured value, short floating point value with time tag","M_ME_TC_1"));
        typeids.add(new IEC870TypeIdentification(15,"Integrated totals","M_IT_NA_1"));
        typeids.add(new IEC870TypeIdentification(16,"Integrated totals with time tag","M_IT_TA_1"));
        typeids.add(new IEC870TypeIdentification(17,"Event of protection equipment with time tag","M_EP_TA_1"));
        typeids.add(new IEC870TypeIdentification(18,"Packed start events of protection equipment with time tag","M_EP_TB_1"));
        typeids.add(new IEC870TypeIdentification(19,"Packed output circuit information of protection equipment with time tag","M_EP_TC_1"));
        typeids.add(new IEC870TypeIdentification(20,"Packed single-point information with status change detection","M PS NA 1"));
        typeids.add(new IEC870TypeIdentification(21,"Measured value, normalized value without quality descriptor","M_ME_ND_1"));
        typeids.add(new IEC870TypeIdentification(30,"Single-point information with time tag CP56Time2a","M_SP_TB_1"));
        typeids.add(new IEC870TypeIdentification(31,"Double-point information with time tag CP56Time2a","M DP TB 1"));
        typeids.add(new IEC870TypeIdentification(32,"Step position information with time tag CP56Time2a","M_ST_TB_1"));
        typeids.add(new IEC870TypeIdentification(33,"Bitstring of 32 bit with time tag CP56Time2a","M_BO_TB_1"));
        typeids.add(new IEC870TypeIdentification(34,"Measured value, normalized value with time tag CP56Time2a","M_ME_TD_1"));
        typeids.add(new IEC870TypeIdentification(35,"Measured value, scaled value with time tag CP56Time2a","M_ME_TE_1"));
        typeids.add(new IEC870TypeIdentification(36,"Measured value, short floating point value with time tag CP56Time2a","M ME TF 1"));
        typeids.add(new IEC870TypeIdentification(37,"Integrated totals with time tag CP56Time2a","M_IT_TB_1"));
        typeids.add(new IEC870TypeIdentification(38,"Event of protection equipment with time tag CP56Time2a","M_EP_TD_1"));
        typeids.add(new IEC870TypeIdentification(39,"Packed start events of protection equipment with time tag CP56Time2a","M_EP_TE_1"));
        typeids.add(new IEC870TypeIdentification(40,"Packed output circuit information of protection equipment with time tag CP56Time2a","M_EP_TF_1"));
        typeids.add(new IEC870TypeIdentification(45,"Single command","C_SC_NA_1"));
        typeids.add(new IEC870TypeIdentification(46,"Double command","C_DC_NA_1"));
        typeids.add(new IEC870TypeIdentification(47,"Regulating step command","C_RC_NA_1"));
        typeids.add(new IEC870TypeIdentification(48,"Set point command, normalized value","C_SE_NA_1"));
        typeids.add(new IEC870TypeIdentification(49,"Set point command, scaled value","C_SE_NB_1"));
        typeids.add(new IEC870TypeIdentification(50,"Set point command, short floating point value","C_SE_NC_1"));
        typeids.add(new IEC870TypeIdentification(51,"Bitstring of 32 bit","C_BO_NA_1"));
        typeids.add(new IEC870TypeIdentification(70,"End of initialization","M_EI_NA_1"));
        typeids.add(new IEC870TypeIdentification(100,"Interrogation command","C_IC_NA_1"));
        typeids.add(new IEC870TypeIdentification(101,"Counter interrogation command","C_CI_NA_1"));
        typeids.add(new IEC870TypeIdentification(102,"Read command","C_RD_NA_1"));
        typeids.add(new IEC870TypeIdentification(103,"Clock synchronization command","C_CS_NA_1"));
        typeids.add(new IEC870TypeIdentification(104,"Test command","C_TS_NA_1"));
        typeids.add(new IEC870TypeIdentification(105,"Reset process command","C_RP_NA_1"));
        typeids.add(new IEC870TypeIdentification(106,"Delay acquisition command","C_CD_NA_1"));
        typeids.add(new IEC870TypeIdentification(110,"Parameter of measured value, normalized value","P_ME_NA_1"));
        typeids.add(new IEC870TypeIdentification(111,"Parameter of measured value, scaled value","P_ME_NB_1"));
        typeids.add(new IEC870TypeIdentification(112,"Parameter of measured value, short floating point value","P_ME_NC_1"));
        typeids.add(new IEC870TypeIdentification(113,"Parameter activation","P_AC_NA_1"));
        typeids.add(new IEC870TypeIdentification(120,"File ready","F_FR_NA_1"));
        typeids.add(new IEC870TypeIdentification(121,"Section ready","F_SR_NA_1"));
        typeids.add(new IEC870TypeIdentification(122,"Call directory, select file, call file, call section","F_SC_NA_1"));
        typeids.add(new IEC870TypeIdentification(123,"Last section, last segment","F_LS_NA_1"));
        typeids.add(new IEC870TypeIdentification(124,"Ack file, ack section","F_AF_NA_1"));
        typeids.add(new IEC870TypeIdentification(125,"Segment","F_SG_NA_1"));
        typeids.add(new IEC870TypeIdentification(126,"Directory {blank or X, only available in monitor (standard) direction}","F_DR_TA_1"));
        
        // reserved type identification ranges
        typeids.add(new IEC870TypeIdentification(127,"Reserved for further compatible definitions",""));
        typeids.add(new IEC870TypeIdentification(114,"(114..119)Reserved for further compatible definitions",""));
        typeids.add(new IEC870TypeIdentification(107,"(107..109)Reserved for further compatible definitions",""));
        typeids.add(new IEC870TypeIdentification(71,"(71..99)Reserved for further compatible definitions",""));
        typeids.add(new IEC870TypeIdentification(52,"(52..69)Reserved for further compatible definitions",""));
        typeids.add(new IEC870TypeIdentification(41,"(41..44)Reserved for further compatible definitions",""));
        typeids.add(new IEC870TypeIdentification(22,"(22..29)Reserved for further compatible definitions",""));
        
        // Datawatt specific
        typeids.add(new IEC870TypeIdentification(136,"Hang up dialled line","C_HU_NA_P"));
        typeids.add(new IEC870TypeIdentification(137,"Login data","C_LD_NA_P"));
        typeids.add(new IEC870TypeIdentification(138,"Hist. single point with spec.","M_SP_TB_P"));
        typeids.add(new IEC870TypeIdentification(139,"Hist. normalized value with spec. time tag.","M_ME_TD_P"));
        typeids.add(new IEC870TypeIdentification(140,"Hist. scaled value with spec. time tag","M_ME_TE_P"));
        typeids.add(new IEC870TypeIdentification(141,"Hist. integrated total with spec. time tag","C_RP_TB_P"));
        typeids.add(new IEC870TypeIdentification(142,"Interrogation of historical data","C_IH_NA_P"));
        typeids.add(new IEC870TypeIdentification(143,"Settings for daylight saving time","C_SU_NA_P"));
        typeids.add(new IEC870TypeIdentification(200,"DSAP message","X_DS_NA_P"));
        typeids.add(new IEC870TypeIdentification(201,"Master Poll request (for radio)","X_MP_NA_P"));
    }
    
    int id;
    String description;
    String shortdescr;
    /** Creates a new instance of TypeIdentification */
    public IEC870TypeIdentification(int id, String description, String shortdescr) {
        this.id=id;
        this.description=description;
        this.shortdescr=shortdescr;
    }
    public String getDescription() {
        return description;
    }
    public String getShortdescr() {
        return shortdescr;
    }
    public int getId() {
        return id;
    }
    public static IEC870TypeIdentification getTypeIdentification(int id) {
        
        // // reserved type identification ranges
        if ((id>=114)&&(id<=119)) id=114;
        if ((id>=107)&&(id<=109)) id=107;
        if ((id>=71)&&(id<=99)) id=71;
        if ((id>=52)&&(id<=69)) id=52;
        if ((id>=41)&&(id<=44)) id=41;
        if ((id>=22)&&(id<=29)) id=22;
        
        Iterator it = typeids.iterator();
        while(it.hasNext()) {
            IEC870TypeIdentification tid = (IEC870TypeIdentification)it.next();
            if (tid.getId() == id) return tid;
        }
        throw new IllegalArgumentException("IEC870TypeIdentification, id "+id+" not found");
    }
    
    public static IEC870TypeIdentification getTypeIdentification(String shortdescr) {
        Iterator it = typeids.iterator();
        while(it.hasNext()) {
            IEC870TypeIdentification tid = (IEC870TypeIdentification)it.next();
            if (tid.getShortdescr().compareTo(shortdescr) == 0) return tid;
        }
        throw new IllegalArgumentException("IEC870TypeIdentification, "+shortdescr+" not found");
    }
    public static int getId(String shortdescr) {
        Iterator it = typeids.iterator();
        while(it.hasNext()) {
            IEC870TypeIdentification tid = (IEC870TypeIdentification)it.next();
            if (tid.getShortdescr().compareTo(shortdescr) == 0) return tid.getId();
        }
        throw new IllegalArgumentException("IEC870TypeIdentification, "+shortdescr+" not found");
    }
}
