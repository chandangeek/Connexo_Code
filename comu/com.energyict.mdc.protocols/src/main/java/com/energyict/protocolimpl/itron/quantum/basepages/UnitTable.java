/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * UnitTable.java
 *
 * Created on 27 september 2006, 14:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum.basepages;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ObisCodeExtensions;
import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Koen
 */
public class UnitTable {

    static List units=new ArrayList();
    static {

        units.add(new UnitTable(1,1, ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get("Wh"),"delivered watthour"));
        units.add(new UnitTable(2,2,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get("Wh"),"received wathour"));
        units.add(new UnitTable(3,3,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get("varh"),"delivered varhour"));
        units.add(new UnitTable(4,4,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get("varh"),"received varhour"));
        units.add(new UnitTable(5,5,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get("varh"),"q1 varhour"));
        units.add(new UnitTable(6,6,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get("varh"),"q2 varhour"));
        units.add(new UnitTable(7,7,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get("varh"),"q3 varhour"));
        units.add(new UnitTable(8,8,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get("varh"),"q4 varhour"));
        units.add(new UnitTable(9,5,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get("varh"),"delivered qhour"));
        units.add(new UnitTable(10,8,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get("varh"),"received qhour"));
        units.add(new UnitTable(11,9,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get("VAh"),"delivered VAhour"));
        units.add(new UnitTable(12,10,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get("VAh"),"received VAhour"));
        units.add(new UnitTable(13,9,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get("VAh"),"delivered + received VAhour"));
        units.add(new UnitTable(14,ObisCodeExtensions.OBISCODE_C_VOLTSQUARE,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get(BaseUnit.VOLTSQUAREHOUR),"volts square hour"));
        units.add(new UnitTable(15,ObisCodeExtensions.OBISCODE_C_AMPSQUARE,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get(BaseUnit.AMPERESQUAREHOUR),"amps square hour"));
        units.add(new UnitTable(16,1,ObisCode.CODE_D_INSTANTANEOUS, Unit.get("W"),"instantaneous watts"));
        units.add(new UnitTable(17,3,ObisCode.CODE_D_INSTANTANEOUS, Unit.get("var"),"instantaneous vars"));
        units.add(new UnitTable(18,9,ObisCode.CODE_D_INSTANTANEOUS, Unit.get("VA"),"instantaneous VA"));
        units.add(new UnitTable(19,13,ObisCode.CODE_D_INSTANTANEOUS, Unit.get(""),"instantaneous power factor"));
        units.add(new UnitTable(20,ObisCodeExtensions.OBISCODE_C_VOLTSQUARE,ObisCode.CODE_D_INSTANTANEOUS, Unit.get(BaseUnit.VOLTSQUARE),"instantaneous volt square"));
        units.add(new UnitTable(21,ObisCodeExtensions.OBISCODE_C_AMPSQUARE,ObisCode.CODE_D_INSTANTANEOUS, Unit.get(BaseUnit.AMPERESQUARE),"instantaneous amp square"));
        units.add(new UnitTable(22,1,ObisCode.CODE_D_MAXIMUM_DEMAND, Unit.get("W"),"delivered maximum watt"));
        units.add(new UnitTable(23,2,ObisCode.CODE_D_MAXIMUM_DEMAND, Unit.get("W"),"received maximum watt"));
        units.add(new UnitTable(24,3,ObisCode.CODE_D_MAXIMUM_DEMAND, Unit.get("var"),"delivered maximum var"));
        units.add(new UnitTable(25,4,ObisCode.CODE_D_MAXIMUM_DEMAND, Unit.get("var"),"received maximum var"));
        units.add(new UnitTable(26,5,ObisCode.CODE_D_MAXIMUM_DEMAND, Unit.get("var"),"maximum q1 var"));
        units.add(new UnitTable(27,6,ObisCode.CODE_D_MAXIMUM_DEMAND, Unit.get("var"),"maximum q2 var"));
        units.add(new UnitTable(28,7,ObisCode.CODE_D_MAXIMUM_DEMAND, Unit.get("var"),"maximum q3 var"));
        units.add(new UnitTable(29,8,ObisCode.CODE_D_MAXIMUM_DEMAND, Unit.get("var"),"maximum q4 var"));
        units.add(new UnitTable(30,9,ObisCode.CODE_D_MAXIMUM_DEMAND, Unit.get("VA"),"delivered maximum VA"));
        units.add(new UnitTable(31,10,ObisCode.CODE_D_MAXIMUM_DEMAND, Unit.get("VA"),"received maximum VA"));
        units.add(new UnitTable(32,9,ObisCode.CODE_D_MAXIMUM_DEMAND, Unit.get("VA"),"delivered + received maximum VA"));
        units.add(new UnitTable(33,1,ObisCode.CODE_D_CURRENT_AVERAGE5, Unit.get("W"),"delivered present watt"));
        units.add(new UnitTable(34,2,ObisCode.CODE_D_CURRENT_AVERAGE5, Unit.get("W"),"received present watt"));
        units.add(new UnitTable(35,3,ObisCode.CODE_D_CURRENT_AVERAGE5, Unit.get("var"),"delivered present var"));
        units.add(new UnitTable(36,4,ObisCode.CODE_D_CURRENT_AVERAGE5, Unit.get("var"),"received present var"));
        units.add(new UnitTable(37,5,ObisCode.CODE_D_CURRENT_AVERAGE5, Unit.get("var"),"present q1 var"));
        units.add(new UnitTable(38,6,ObisCode.CODE_D_CURRENT_AVERAGE5, Unit.get("var"),"present q2 var"));
        units.add(new UnitTable(39,7,ObisCode.CODE_D_CURRENT_AVERAGE5, Unit.get("var"),"present q3 var"));
        units.add(new UnitTable(40,8,ObisCode.CODE_D_CURRENT_AVERAGE5, Unit.get("var"),"present q4 var"));
        units.add(new UnitTable(41,9,ObisCode.CODE_D_CURRENT_AVERAGE5, Unit.get("VA"),"delivered present VA"));
        units.add(new UnitTable(42,10,ObisCode.CODE_D_CURRENT_AVERAGE5, Unit.get("VA"),"received present VA"));
        units.add(new UnitTable(43,9,ObisCode.CODE_D_CURRENT_AVERAGE5, Unit.get("VA"),"delivered + received present VA"));
        units.add(new UnitTable(44,1,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND, Unit.get("W"),"delivered cumulative maximum watt"));
        units.add(new UnitTable(45,2,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND, Unit.get("W"),"received cumulative maximum watt"));
        units.add(new UnitTable(46,3,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND, Unit.get("var"),"delivered cumulative maximum var"));
        units.add(new UnitTable(47,4,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND, Unit.get("var"),"received cumulative maximum var"));
        units.add(new UnitTable(48,5,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND, Unit.get("var"),"cumulative maximum q1 vars"));
        units.add(new UnitTable(49,6,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND, Unit.get("var"),"cumulative maximum q2 vars"));
        units.add(new UnitTable(50,7,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND, Unit.get("var"),"cumulative maximum q3 vars"));
        units.add(new UnitTable(51,8,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND, Unit.get("var"),"cumulative maximum q4 vars"));
        units.add(new UnitTable(52,9,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND, Unit.get("VA"),"delivered cumulative maximum VA"));
        units.add(new UnitTable(53,10,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND, Unit.get("VA"),"received cumulative maximum VA"));
        units.add(new UnitTable(54,9,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND, Unit.get("VA"),"delivered + received cumulative maximum VA"));
        units.add(new UnitTable(55,1,ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND, Unit.get("W"),"delivered continuous cumulative maximum watt"));
        units.add(new UnitTable(56,2,ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND, Unit.get("W"),"received continuous cumulative maximum watt"));
        units.add(new UnitTable(57,3,ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND, Unit.get("var"),"delivered continuous cumulative maximum var"));
        units.add(new UnitTable(58,4,ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND, Unit.get("var"),"received continuous cumulative maximum var"));
        units.add(new UnitTable(59,5,ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND, Unit.get("var"),"continuous cumulative maximum q1 vars"));
        units.add(new UnitTable(60,6,ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND, Unit.get("var"),"continuous cumulative maximum q2 vars"));
        units.add(new UnitTable(61,7,ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND, Unit.get("var"),"continuous cumulative maximum q3 vars"));
        units.add(new UnitTable(62,8,ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND, Unit.get("var"),"continuous cumulative maximum q4 vars"));
        units.add(new UnitTable(63,9,ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND, Unit.get("VA"),"delivered continuous cumulative maximum VA"));
        units.add(new UnitTable(64,10,ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND, Unit.get("VA"),"received continuous cumulative maximum VA"));
        units.add(new UnitTable(65,9,ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND, Unit.get("VA"),"delivered + received continuous cumulative maximum VA"));
        units.add(new UnitTable(66,1,ObisCodeExtensions.OBISCODE_D_COINCIDENT, Unit.get(""),"power factor at maximum delivered watts"));
        units.add(new UnitTable(67,2,ObisCodeExtensions.OBISCODE_D_COINCIDENT, Unit.get(""),"power factor at maximum received watts"));
        units.add(new UnitTable(68,9,ObisCodeExtensions.OBISCODE_D_COINCIDENT, Unit.get(""),"power factor at maximum delivered VA"));
        units.add(new UnitTable(69,10,ObisCodeExtensions.OBISCODE_D_COINCIDENT, Unit.get(""),"power factor at maximum received VA"));
        units.add(new UnitTable(70,9,ObisCodeExtensions.OBISCODE_D_COINCIDENT, Unit.get(""),"power factor at maximum delivered + received VA"));
        units.add(new UnitTable(71,12,ObisCode.CODE_D_INSTANTANEOUS, Unit.get("V"),"instantaneous volts"));
        units.add(new UnitTable(72,11,ObisCode.CODE_D_INSTANTANEOUS, Unit.get("A"),"instantaneous amps"));
        units.add(new UnitTable(73,11,ObisCode.CODE_D_MAXIMUM_DEMAND, Unit.get("A"),"maximum amps"));
        units.add(new UnitTable(74,11,ObisCode.CODE_D_CURRENT_AVERAGE5, Unit.get("A"),"present amps"));
        units.add(new UnitTable(75,11,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND, Unit.get("A"),"cumulative amps"));
        units.add(new UnitTable(76,11,ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND, Unit.get("A"),"continuous cumulative amps"));
        units.add(new UnitTable(77,1,ObisCodeExtensions.OBISCODE_D_PEAK_INSTANTANEOUS, Unit.get("W"),"peak instantaneous watts"));
        units.add(new UnitTable(78,3,ObisCodeExtensions.OBISCODE_D_PEAK_INSTANTANEOUS, Unit.get("var"),"peak instantaneous vars"));
        units.add(new UnitTable(79,9,ObisCodeExtensions.OBISCODE_D_PEAK_INSTANTANEOUS, Unit.get("VA"),"peak instantaneous VAs"));
        units.add(new UnitTable(80,13,ObisCodeExtensions.OBISCODE_D_PEAK_INSTANTANEOUS, Unit.get(""),"peak instantaneous power factor"));
        units.add(new UnitTable(81,ObisCodeExtensions.OBISCODE_C_VOLTSQUARE,ObisCodeExtensions.OBISCODE_D_PEAK_INSTANTANEOUS, Unit.get(BaseUnit.VOLTSQUARE),"peak instantaneous volt square"));
        units.add(new UnitTable(82,ObisCodeExtensions.OBISCODE_C_AMPSQUARE,ObisCodeExtensions.OBISCODE_D_PEAK_INSTANTANEOUS, Unit.get(BaseUnit.AMPERESQUARE),"peak instantaneous amp square"));
        units.add(new UnitTable(83,12,ObisCodeExtensions.OBISCODE_D_PEAK_INSTANTANEOUS, Unit.get(BaseUnit.VOLT),"peak instantaneous volt"));
        units.add(new UnitTable(84,11,ObisCodeExtensions.OBISCODE_D_PEAK_INSTANTANEOUS, Unit.get(BaseUnit.AMPERE),"peak instantaneous amp"));
        units.add(new UnitTable(85,ObisCodeExtensions.OBISCODE_C_TIMEOFOCCURANCE,ObisCodeExtensions.OBISCODE_D_TIMEOFOCCURANCE, Unit.get(""),"time of occurance"));
        units.add(new UnitTable(86,91,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get(BaseUnit.AMPEREHOUR),"neutral amphour"));
        units.add(new UnitTable(87,91,ObisCode.CODE_D_CURRENT_AVERAGE5, Unit.get(BaseUnit.AMPERE),"present neutral amps"));
        units.add(new UnitTable(88,91,ObisCode.CODE_D_MAXIMUM_DEMAND, Unit.get(BaseUnit.AMPERE),"maximum neutral amps"));
        units.add(new UnitTable(89,91,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND, Unit.get(BaseUnit.AMPERE),"cumulative neutral amps"));
        units.add(new UnitTable(90,91,ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND, Unit.get(BaseUnit.AMPERE),"continuous cumulative neutral amps"));
        units.add(new UnitTable(91,91,ObisCode.CODE_D_INSTANTANEOUS, Unit.get(BaseUnit.AMPERE),"instantaneous neutral amps"));
        units.add(new UnitTable(92,91,ObisCodeExtensions.OBISCODE_D_PEAK_INSTANTANEOUS, Unit.get(BaseUnit.AMPERE),"peak instantaneous neutral amps"));
        units.add(new UnitTable(93,11,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get(BaseUnit.AMPEREHOUR),"amphour"));
        units.add(new UnitTable(94,12,ObisCode.CODE_D_TIME_INTEGRAL1, Unit.get(BaseUnit.VOLTHOUR),"volthour"));
        units.add(new UnitTable(95,82,ObisCodeExtensions.OBISCODE_D_COINCIDENT, Unit.get(""),"coincident"));
        units.add(new UnitTable(96,1,ObisCode.CODE_D_MINIMUM, Unit.get("W"),"minimum delivered watts"));
        units.add(new UnitTable(97,2,ObisCode.CODE_D_MINIMUM, Unit.get("W"),"minimum received watts"));
        units.add(new UnitTable(98,3,ObisCode.CODE_D_MINIMUM, Unit.get("var"),"minimum delivered vars"));
        units.add(new UnitTable(99,4,ObisCode.CODE_D_MINIMUM, Unit.get("var"),"minimum received vars"));
        units.add(new UnitTable(100,5,ObisCode.CODE_D_MINIMUM, Unit.get("var"),"minimum q1 vars"));
        units.add(new UnitTable(101,6,ObisCode.CODE_D_MINIMUM, Unit.get("var"),"minimum q2 vars"));
        units.add(new UnitTable(102,7,ObisCode.CODE_D_MINIMUM, Unit.get("var"),"minimum q3 vars"));
        units.add(new UnitTable(103,8,ObisCode.CODE_D_MINIMUM, Unit.get("var"),"minimum q4 vars"));
        units.add(new UnitTable(104,9,ObisCode.CODE_D_MINIMUM, Unit.get("VA"),"minimum delivered VAs"));
        units.add(new UnitTable(105,10,ObisCode.CODE_D_MINIMUM, Unit.get("VA"),"minimum received VAs"));
        units.add(new UnitTable(106,9,ObisCode.CODE_D_MINIMUM, Unit.get("VA"),"minimum delivered + received VAs"));
        units.add(new UnitTable(107,11,ObisCode.CODE_D_MINIMUM, Unit.get(BaseUnit.AMPERE),"minimum amps"));
        units.add(new UnitTable(108,91,ObisCode.CODE_D_MINIMUM, Unit.get(BaseUnit.AMPERE),"minimum neutral amps"));
        units.add(new UnitTable(109,82,ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK, Unit.get(""),"high five"));
        units.add(new UnitTable(110,13,ObisCode.CODE_D_CURRENT_AVERAGE5, Unit.get(""),"present power factor"));

    }

    private int registerNr;
    private int obisCField;
    private int obisDField;
    private Unit unit;
    private String description;

    public boolean isVoltSquare() {
        return getObisCField()==ObisCodeExtensions.OBISCODE_C_VOLTSQUARE;
    }
    public boolean isAmpSquare() {
        return getObisCField()==ObisCodeExtensions.OBISCODE_C_AMPSQUARE;
    }

    public BigDecimal getRegisterValue(byte[] data, int offset, int firmwareRevision, int scale) throws IOException {
        BigDecimal bd=null;

        switch(obisDField) {

            // instantaneous
            case ObisCodeExtensions.OBISCODE_D_PEAK_INSTANTANEOUS:
            case ObisCode.CODE_D_INSTANTANEOUS: {
                if (firmwareRevision < 10) {
                    if (obisCField == ObisCodeExtensions.OBISCODE_C_VOLTSQUARE)
                        bd = ParseUtils.convertNormSignedFP2Number(data,offset,6,24);
                    else
                        bd = ParseUtils.convertNormSignedFP2Number(data,offset,6,32);
                }
                else if (firmwareRevision >= 10) {
                    if (obisCField == ObisCodeExtensions.OBISCODE_C_VOLTSQUARE) {
                        bd = ParseUtils.convertNormSignedFP2Number(data,offset,6,8);
                    }
                    else if (obisCField == ObisCode.CODE_C_POWERFACTOR) {
                        bd = ParseUtils.convertNormSignedFP2Number(data,offset,6,32);
                    }
                    else {
                        bd = ParseUtils.convertNormSignedFP2Number(data,offset,6,16);
                    }
                }

                //System.out.println("KV_DEBUG> "+bd);
                bd = bd.movePointLeft(scale); // only for the instantaneous values... ?? KV_TO_DO
                //System.out.println("KV_DEBUG> "+bd);
            } break; // ObisCode.CODE_D_INSTANTANEOUS

            case ObisCodeExtensions.OBISCODE_D_TIMEOFOCCURANCE: {
                bd = BigDecimal.valueOf(ProtocolUtils.getLong(data,offset, 6));
            } break; // ObisCodeExtensions.OBISCODE_D_TIMEOFOCCURANCE

            // energy & demand
            case ObisCode.CODE_D_MINIMUM:
            case ObisCode.CODE_D_TIME_INTEGRAL1:
            case ObisCode.CODE_D_MAXIMUM_DEMAND:
            case ObisCode.CODE_D_CURRENT_AVERAGE5:
            case ObisCodeExtensions.OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND:
            case ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND: {

                if (firmwareRevision < 13) {
                    bd = ParseUtils.convertBCDFixedPoint(data,offset,6,12);
                }
                else if (firmwareRevision >= 13) {
                    bd = ParseUtils.convertBCDFixedPoint(data,offset,6,16);
                }

            } break;

            // powerfactor
            case ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK:
            case ObisCodeExtensions.OBISCODE_D_COINCIDENT: {
                bd = ParseUtils.convertNormSignedFP2Number(data,offset,6,32);
            } break;

        } // switch(obisDField)

        return bd;
    } // public BigDecimal getRegisterValue(byte[] data, int offset, int firmwareRevision)


    public String toString() {
       return getUnit()+", "+getDescription();
    }

    /** Creates a new instance of UnitTable */
    private UnitTable(int registerNr, int obisCField, int obisDField, Unit unit, String description) {
        this.setRegisterNr(registerNr);
        this.setObisCField(obisCField);
        this.setObisDField(obisDField);
        this.setUnit(unit);
        this.description=description;
    }

    public int getObisCField() {
        return obisCField;
    }

    public void setObisCField(int obisCField) {
        this.obisCField = obisCField;
    }

    public int getObisDField() {
        return obisDField;
    }

    public void setObisDField(int obisDField) {
        this.obisDField = obisDField;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public int getRegisterNr() {
        return registerNr;
    }

    public void setRegisterNr(int registerNr) {
        this.registerNr = registerNr;
    }

    static public UnitTable findUnitTable(int registerNr) throws IOException {
        Iterator it = units.iterator();
        while(it.hasNext()) {
            UnitTable u = (UnitTable)it.next();
            if (u.getRegisterNr() == registerNr)
                return u;
        }

        throw new IOException("UnitTable, findUnitTable, invalid registerNr "+registerNr);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
