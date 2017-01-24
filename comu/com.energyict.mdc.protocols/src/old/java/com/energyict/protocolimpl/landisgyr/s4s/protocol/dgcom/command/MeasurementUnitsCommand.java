/*
 * TemplateCommand.java
 *
 * Created on 22 mei 2006, 15:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.command;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Unit;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class MeasurementUnitsCommand extends AbstractCommand {

    /*
        00h kWh (for billing metric only)
        02h kVARh rms (not valid after 2.11)
        03h kVAh rms
        04h kQh (not valid in 3.00 or greater)
        06h kVARh time delay
        11h kVAh time delay (valid with 2.12 or greater)
     **/
    private int selectableMetric;
    private int billingMetric;
    private int thirdMetric;

    /** Creates a new instance of TemplateCommand */
    public MeasurementUnitsCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    private Unit getUnit(int val) {
        switch(val) {
            case 0x0:
                return Unit.get("kWh");
            case 0x2:
                return Unit.get("kvarh");
            case 0x3:
                return Unit.get("kVAh");
            case 0x4:
                return Unit.get("kvarh");
            case 0x6:
                return Unit.get("kvarh");
            case 0x11:
                return Unit.get("kVAh");
        }

        return Unit.get("");
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MeasurementUnitsCommand:\n");
        strBuff.append("   selectableMetric="+getSelectableMetric()+"\n");
        strBuff.append("   billingMetric="+getBillingMetric()+"\n");
        strBuff.append("   thirdMetric="+getThirdMetric()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        if ((getCommandFactory().getFirmwareVersionCommand().isRX())) {
            if (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00)
                return new byte[]{(byte)0xC4,0,0,0,0,0,0,0,0};
            else
                return new byte[]{(byte)0xA4,0,0,0,0,0,0,0,0};
        }
        else throw new IOException("MeasurementUnitsCommand, only for RX meters!");

    }

    protected void parse(byte[] data) throws IOException {
        setSelectableMetric(ProtocolUtils.getInt(data,0, 1));
        setBillingMetric(ProtocolUtils.getInt(data,1, 1));
        if (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00)
            setThirdMetric(ProtocolUtils.getInt(data,2, 1));

    }

    public int getSelectableMetric() {
        return selectableMetric;
    }

    private void setSelectableMetric(int selectableMetric) {
        this.selectableMetric = selectableMetric;
    }

    public int getBillingMetric() {
        return billingMetric;
    }

    private void setBillingMetric(int billingMetric) {
        this.billingMetric = billingMetric;
    }

    public int getThirdMetric() {
        return thirdMetric;
    }

    private void setThirdMetric(int thirdMetric) {
        this.thirdMetric = thirdMetric;
    }

    public Unit getSelectableMetricUnit(boolean energy) {
        return energy?getUnit(selectableMetric):getUnit(selectableMetric).getFlowUnit();
    }
    public Unit getBillingMetricUnit(boolean energy) {
        return energy?getUnit(billingMetric):getUnit(billingMetric).getFlowUnit();
    }
    public Unit getThirdMetricUnit(boolean energy) {
        return energy?getUnit(thirdMetric):getUnit(thirdMetric).getFlowUnit();
    }
}
