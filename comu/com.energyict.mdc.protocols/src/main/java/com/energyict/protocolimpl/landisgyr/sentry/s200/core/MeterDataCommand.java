/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeterDataCommand.java
 *
 * Created on 26 juli 2006, 17:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;


import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.math.BigDecimal;
/**
 *
 * @author Koen
 */
public class MeterDataCommand extends AbstractCommand {

    private int meterInput;

    private long cumulativePulseCount;
    private int status;

    private BigDecimal bigDecimalCumulativePulseCount;

    /** Creates a new instance of ForceStatusCommand */
    public MeterDataCommand(CommandFactory cm) {
        super(cm);
    }

    // =0x"+Integer.toHexString(
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MeterDataCommand:\n");
        strBuff.append("   meterInput="+getMeterInput()+"\n");
        strBuff.append("   cumulativePulseCount="+getCumulativePulseCount()+"\n");
        strBuff.append("   status=0x"+Integer.toHexString(getStatus())+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;

        LookAtCommand lac = getCommandFactory().getLookAtCommand();
        if (lac.isCumulativePulseCount()) {
            offset=0;
            setCumulativePulseCount(ParseUtils.getBCD2Long(data,offset,5));
            offset+=5;
        }
        else {
            if (getCommandFactory().getVerifyCommand().getSoftwareVersion() != 4) {
                offset=2;
                if (lac.isPSIEncoder() || lac.isSangamoEncoder()) {
                    setCumulativePulseCount(ParseUtils.getBCD2Long(data,offset,3));
                }
                else if (lac.isJEM1MeterReadings() || lac.isJEM2MeterReadings()) {
                    setCumulativePulseCount(ParseUtils.getBCD2Long(data,offset,3));
                }
                else throw new IOException("MeterDataCommand, parse, invalid encoder type "+lac.getEncoderType());
                offset+=3;
                setStatus(ProtocolUtils.getInt(data,offset,1));
            }
            else throw new IOException("MeterDataCommand, parse, encoders not supported for softwareversion 4");
        }

        setBigDecimalCumulativePulseCount(BigDecimal.valueOf(getCumulativePulseCount()));

        // page 3-25 states that "this command not supported by n4nn versions... Does this means that the M command is not supported or only the encoder reading?
        // mail answer from George Sandler on 27/7/2006
    }

    protected CommandDescriptor getCommandDescriptor() {
        return new CommandDescriptor('M');
    }

    protected byte[] prepareData() throws IOException {
        return new byte[]{(byte)getMeterInput(),0,0,0,0,0};

    }

    public int getMeterInput() {
        return meterInput;
    }

    public void setMeterInput(int meterInput) {
        this.meterInput = meterInput;
    }

    public long getCumulativePulseCount() {
        return cumulativePulseCount;
    }

    public void setCumulativePulseCount(long cumulativePulseCount) {
        this.cumulativePulseCount = cumulativePulseCount;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public BigDecimal getBigDecimalCumulativePulseCount() {
        return bigDecimalCumulativePulseCount;
    }

    public void setBigDecimalCumulativePulseCount(BigDecimal bigDecimalCumulativePulseCount) {
        this.bigDecimalCumulativePulseCount = bigDecimalCumulativePulseCount;
    }

}
