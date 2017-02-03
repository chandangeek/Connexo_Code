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

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class PresentDemandCommand extends AbstractCommand {
    
    private long presentDemandInPulses;
    private long presentDemandForSelectableMetricInPulses; // for RX only
    private long presentReactiveDemandInPulses; // for RX only and FW >= 3.00
    private long presentDemandForThirdMetric;
    
    /** Creates a new instance of TemplateCommand */
    public PresentDemandCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PresentDemandCommand:\n");
        strBuff.append("   presentDemandForSelectableMetricInPulses="+getPresentDemandForSelectableMetricInPulses()+"\n");
        strBuff.append("   presentDemandInPulses="+getPresentDemandInPulses()+"\n");
        strBuff.append("   presentReactiveDemandInPulses="+getPresentReactiveDemandInPulses()+"\n");
        strBuff.append("   presentDemandForThirdMetric="+getPresentDemandForThirdMetric()+"\n");
        return strBuff.toString();
    }
    
    protected byte[] prepareBuild() throws IOException {
        if ((getCommandFactory().getFirmwareVersionCommand().isRX()) && (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00))
            return new byte[]{(byte)0xC5,0,0,0,0,0,0,0,0};
        else    
            return new byte[]{(byte)0x82,0,0,0,0,0,0,0,0};
    }
    
    protected void parse(byte[] data) throws IOException {
        
        setPresentDemandInPulses(ProtocolUtils.getIntLE(data,0,2));
        
        if ((getCommandFactory().getFirmwareVersionCommand().isRX()) && (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()<3.00))
            setPresentDemandForSelectableMetricInPulses(ProtocolUtils.getIntLE(data,2,2));
            
        if ((getCommandFactory().getFirmwareVersionCommand().isRX()) && (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00)) {
            setPresentReactiveDemandInPulses(ProtocolUtils.getIntLE(data,4,2));
            setPresentDemandForThirdMetric(ProtocolUtils.getIntLE(data,6,2));
        }
    }

    public long getPresentDemandInPulses() {
        return presentDemandInPulses;
    }

    private void setPresentDemandInPulses(long presentDemandInPulses) {
        this.presentDemandInPulses = presentDemandInPulses;
    }

    public long getPresentDemandForSelectableMetricInPulses() {
        return presentDemandForSelectableMetricInPulses;
    }

    private void setPresentDemandForSelectableMetricInPulses(long presentDemandForSelectableMetricInPulses) {
        this.presentDemandForSelectableMetricInPulses = presentDemandForSelectableMetricInPulses;
    }

    public long getPresentReactiveDemandInPulses() {
        return presentReactiveDemandInPulses;
    }

    private void setPresentReactiveDemandInPulses(long presentReactiveDemandInPulses) {
        this.presentReactiveDemandInPulses = presentReactiveDemandInPulses;
    }

    public long getPresentDemandForThirdMetric() {
        return presentDemandForThirdMetric;
    }

    public void setPresentDemandForThirdMetric(long presentDemandForThirdMetric) {
        this.presentDemandForThirdMetric = presentDemandForThirdMetric;
    }


}
