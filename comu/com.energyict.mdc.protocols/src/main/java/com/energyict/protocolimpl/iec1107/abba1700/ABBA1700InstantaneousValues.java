/*
 * ABBA1700InstantaneousValues.java
 *
 * Created on 22 december 2004, 15:24
 */

package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class ABBA1700InstantaneousValues {

    private static final int DEBUG=0;

    public static final int PHASE_TOTAL=0x00;
    public static final int PHASE_A=0x10;
    public static final int PHASE_B=0x20;
    public static final int PHASE_C=0x40;

    public static final int RMS_CURRENT=0x01;
    public static final int RMS_VOLTAGE=0x02;
    public static final int POWER_FACTOR=0x03;
    public static final int ACTIVE_POWER=0x04;
    public static final int REACTIVE_POWER=0x05;
    public static final int APPARENT_POWER=0x06;
    public static final int PHASE_ROTATION=0x07;
    public static final int FREQUENCY=0x08;
    public static final int PHASE_ANGLE=0x09;
    public static final int RMS_CURRENT_SCALE=0x0A;
    public static final int RMS_VOLTAGE_SCALE=0x0B;
    public static final int ACTIVE_POWER_SCALED=0x0C;
    public static final int REACTIVE_POWER_SCALED=0x0D;
    public static final int APPARENT_POWER_SCALED=0x0E;

    ABBA1700RegisterFactory abba1700RegisterFactory=null;

    /** Creates a new instance of ABBA1700InstantaneousValues */
    public ABBA1700InstantaneousValues(ABBA1700RegisterFactory abba1700RegisterFactory) {
        this.abba1700RegisterFactory = abba1700RegisterFactory;
    }

    public Quantity getInstantaneousValue(String val2retrieve) throws IOException {
        return doGetInstantaneousValue(val2retrieve, Unit.get(""));
    }


    public String getInstantaneousValueDescription(int phaseIndex, int quantityIndex) throws IOException {
        StringBuffer strBuff = new StringBuffer();

        strBuff.append("Instantaneous value, ");

        if (phaseIndex==PHASE_A) strBuff.append("Phase L1, ");
        else if (phaseIndex==PHASE_B) strBuff.append("Phase L2, ");
        else if (phaseIndex==PHASE_C) strBuff.append("Phase L3, ");
        else if (phaseIndex==PHASE_TOTAL) strBuff.append("Phase L1+L2+L3, ");

        if (quantityIndex == RMS_CURRENT) strBuff.append("RMS current");
        else if (quantityIndex == RMS_VOLTAGE) strBuff.append("RMS voltage");
        else if (quantityIndex == POWER_FACTOR) strBuff.append("power factor");
        else if (quantityIndex == ACTIVE_POWER) strBuff.append("active power");
        else if (quantityIndex == REACTIVE_POWER) strBuff.append("reactive power");
        else if (quantityIndex == APPARENT_POWER) strBuff.append("apparent power");
        else if (quantityIndex == PHASE_ROTATION) strBuff.append("phase rotation");
        else if (quantityIndex == FREQUENCY) strBuff.append("frequency");
        else if (quantityIndex == PHASE_ANGLE) strBuff.append("phase angle");
        else if (quantityIndex == RMS_CURRENT_SCALE) strBuff.append("RMS current scaled");
        else if (quantityIndex == RMS_VOLTAGE_SCALE) strBuff.append("RMS voltage scaled");
        else if (quantityIndex == ACTIVE_POWER_SCALED) strBuff.append("active power scaled");
        else if (quantityIndex == REACTIVE_POWER_SCALED) strBuff.append("reactive power scaled");
        else if (quantityIndex == APPARENT_POWER_SCALED) strBuff.append("apparent power scaled");

        return strBuff.toString();
    }

    public Quantity getInstantaneousValue(int phaseIndex, int quantityIndex) throws IOException {
        int instval = phaseIndex|quantityIndex;

        Unit unit=null;

        if (quantityIndex == RMS_CURRENT) unit = Unit.get(BaseUnit.AMPERE);
        else if (quantityIndex == RMS_VOLTAGE) unit = Unit.get(BaseUnit.VOLT);
        else if (quantityIndex == POWER_FACTOR) unit = Unit.get("");
        else if (quantityIndex == ACTIVE_POWER) unit = Unit.get("kW");
        else if (quantityIndex == REACTIVE_POWER) unit = Unit.get("kvar");
        else if (quantityIndex == APPARENT_POWER) unit = Unit.get("kVA");
        else if (quantityIndex == PHASE_ROTATION) unit = Unit.get("");
        else if (quantityIndex == FREQUENCY) unit = Unit.get(BaseUnit.HERTZ);
        else if (quantityIndex == PHASE_ANGLE) unit = Unit.get(BaseUnit.DEGREE);
        else if (quantityIndex == RMS_CURRENT_SCALE) unit = Unit.get(BaseUnit.AMPERE);
        else if (quantityIndex == RMS_VOLTAGE_SCALE) unit = Unit.get(BaseUnit.VOLT);
        else if (quantityIndex == ACTIVE_POWER_SCALED) unit = Unit.get("kW");
        else if (quantityIndex == REACTIVE_POWER_SCALED) unit = Unit.get("kvar");
        else if (quantityIndex == APPARENT_POWER_SCALED) unit = Unit.get("kVA");

        return doGetInstantaneousValue(ProtocolUtils.buildStringHex(instval,2),unit);
    }

    private Quantity doGetInstantaneousValue(String val2retrieve,Unit unit) throws IOException {
        int instval = 0;
        abba1700RegisterFactory.setRegister("InstantaneousValuesRequest",val2retrieve);
        long time = System.currentTimeMillis();
        do {
           instval = (int)((Long)abba1700RegisterFactory.getRegister("InstantaneousValuesRequest")).longValue();
           if ((System.currentTimeMillis() - time) > 60000)
               throw new IOException("getInstantaneousValue(), timeout retrieving instantaneous value. Polling did not return a value after 60 sec.");
        } while((instval&0x80) ==0);
        InstantaneousValue iv = (InstantaneousValue)abba1700RegisterFactory.getRegister("InstantaneousValues");
        return new Quantity(iv.getQuantity().getAmount(),unit);
    }

}
