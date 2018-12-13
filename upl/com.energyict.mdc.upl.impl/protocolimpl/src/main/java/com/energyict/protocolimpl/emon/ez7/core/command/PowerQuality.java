/*
 * PowerQuality.java
 *
 * Created on 17 mei 2005, 17:19
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class PowerQuality extends AbstractCommand {

    private static final int DEBUG=0;
    private static final String COMMAND="RL";
    private static final int NR_OF_PHASES=4; // 0..2 3 phases, 3 = total

    Quantity[] frequency = new Quantity[NR_OF_PHASES];
    Quantity[] voltage = new Quantity[NR_OF_PHASES];
    Quantity[] phaseAngle = new Quantity[NR_OF_PHASES];
    Quantity[] powerFactor = new Quantity[NR_OF_PHASES];
    Quantity[] amperage = new Quantity[NR_OF_PHASES];
    Quantity[] kwLoad = new Quantity[NR_OF_PHASES];


    /** Creates a new instance of PowerQuality */
    public PowerQuality(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PowerQuality:\n");
        for (int phase = 0; phase < NR_OF_PHASES; phase++) {
           strBuff.append("phase "+phase+": ");
           strBuff.append("frequency="+getFrequency(phase)+", ");
           strBuff.append("voltage="+getVoltage(phase)+", ");
           strBuff.append("phaseAngle="+getPhaseAngle(phase)+", ");
           strBuff.append("powerFactor="+getPowerFactor(phase)+", ");
           strBuff.append("amperage="+getAmperage(phase)+", ");
           strBuff.append("kwLoad="+getKwLoad(phase)+"\n ");
           strBuff.append("\n");
        }
        return strBuff.toString();    }

    public void build() throws IOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
    }

    private void parse(byte[] data) throws NoSuchRegisterException {
        if (DEBUG>=1)
           System.out.println(new String(data));


        if (new String(data).indexOf("R?")>=0)
            throw new NoSuchRegisterException("PowerQuality, parse, not supported!");
        CommandParser cp = new CommandParser(data);
        List values = cp.getValues("TOTAL");
        fillValues(0,values);
        for (int phase=1;phase<NR_OF_PHASES;phase++) {
            values = cp.getValues("PHASE-"+phase);
            fillValues(phase,values);
        }
    }

    private void fillValues(int phase, List values) {
        frequency[phase] = new Quantity(BigDecimal.valueOf((long) getSignedShort((String) values.get(0)), 2).multiply(getEz7CommandFactory().getEz7().getAdjustRegisterMultiplier()), Unit.get(BaseUnit.HERTZ));
        voltage[phase] = new Quantity(BigDecimal.valueOf((long) getSignedShort((String) values.get(1)), 2).multiply(getEz7CommandFactory().getEz7().getAdjustRegisterMultiplier()), Unit.get(BaseUnit.VOLT));
        phaseAngle[phase] = new Quantity(BigDecimal.valueOf((long) getSignedShort((String) values.get(2)), 2).multiply(getEz7CommandFactory().getEz7().getAdjustRegisterMultiplier()), Unit.get(BaseUnit.DEGREE));
        powerFactor[phase] = new Quantity(BigDecimal.valueOf((long) getSignedShort((String) values.get(3)), 2).multiply(getEz7CommandFactory().getEz7().getAdjustRegisterMultiplier()), Unit.get(""));
        amperage[phase] = new Quantity(BigDecimal.valueOf((long) getSignedShort((String) values.get(4)), 2).multiply(getEz7CommandFactory().getEz7().getAdjustRegisterMultiplier()), Unit.get(BaseUnit.AMPERE));
        kwLoad[phase] = new Quantity(BigDecimal.valueOf((long) getSignedShort((String) values.get(5)), 2).multiply(getEz7CommandFactory().getEz7().getAdjustRegisterMultiplier()), Unit.get("kW"));
    }

    /**
     * Extract the signed short value, out of the given ASCII-HEX string.
     * We should use this method, cause the signed short can be negative!
     *
     * @param hexString
     * @return
     */
    private short getSignedShort(String hexString) {
        return ProtocolUtils.getShort(ProtocolTools.getBytesFromHexString(hexString, ""), 0);
    }

    /**
     * Getter for property frequency.
     * @return Value of property frequency.
     */
    public Quantity getFrequency(int phase) {
        return this.frequency[phase];
    }

    /**
     * Getter for property voltage.
     * @return Value of property voltage.
     */
    public Quantity getVoltage(int phase) {
        return this.voltage[phase];
    }

    /**
     * Getter for property phaseAngle.
     * @return Value of property phaseAngle.
     */
    public Quantity getPhaseAngle(int phase) {
        return this.phaseAngle[phase];
    }

    /**
     * Getter for property powerFactor.
     * @return Value of property powerFactor.
     */
    public Quantity getPowerFactor(int phase) {
        return this.powerFactor[phase];
    }

    /**
     * Getter for property amperage.
     * @return Value of property amperage.
     */
    public Quantity getAmperage(int phase) {
        return this.amperage[phase];
    }

    /**
     * Getter for property kwLoad.
     * @return Value of property kwLoad.
     */
    public Quantity getKwLoad(int phase) {
        return this.kwLoad[phase];
    }



}
