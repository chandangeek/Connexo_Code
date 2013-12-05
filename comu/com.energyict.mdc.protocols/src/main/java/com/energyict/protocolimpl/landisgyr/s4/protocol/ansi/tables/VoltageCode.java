/*
 * VoltageCode.java
 *
 * Created on 11 juli 2006, 9:52
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Koen
 */
public class VoltageCode {

    private int id;
    private int volt;
    private BigDecimal multiplier;


    static List voltageCodes = new ArrayList();
    static {
        voltageCodes.add(new VoltageCode(1, 120, new BigDecimal("0.00833")));
        voltageCodes.add(new VoltageCode(2, 240, new BigDecimal("0.01666")));
        voltageCodes.add(new VoltageCode(4, 277, new BigDecimal("0.01923")));
        voltageCodes.add(new VoltageCode(8, 480, new BigDecimal("0.03333")));
    }

    static public VoltageCode findVoltageCode(int id) throws IOException {
        for (int i=0;i<voltageCodes.size();i++) {
            VoltageCode vc = (VoltageCode)voltageCodes.get(i);
            if (vc.getId()==id)
                return vc;
        }
        throw new IOException("VoltageCode, unknown voltagecode "+id);
    }

    /** Creates a new instance of VoltageCode */
    private VoltageCode(int id, int volt, BigDecimal multiplier) {
        this.setId(id);
        this.setVolt(volt);
        this.setMultiplier(multiplier);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVolt() {
        return volt;
    }

    public void setVolt(int volt) {
        this.volt = volt;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

}
