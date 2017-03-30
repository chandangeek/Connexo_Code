/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LookAtlCommand2.java
 *
 * Created on 26 juli 2006, 17:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class LookAtlCommand2 extends AbstractCommand {

    // enc t/n
    private int encoderType; // high order nibble
                     // 0 = PSI encoder
                     // 1 = Sangamo encoder
                     // 2 = JEM2 meter readings
                     // 3-F = reserved for future use
    private int nrOfEncoders; // low order nibble

    private int encoderFlags; // bit1 = MID
                      // bit0 = M

    private int[] pulseDivisors = new int[4]; // inputs 5..8

    //enc

    /** Creates a new instance of ForceStatusCommand */
    public LookAtlCommand2(CommandFactory cm) {
        super(cm);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LookAtlCommand2:\n");
        strBuff.append("   encoderFlags=0x"+Integer.toHexString(getEncoderFlags())+"\n");
        strBuff.append("   encoderType="+getEncoderType()+"\n");
        strBuff.append("   nrOfEncoders="+getNrOfEncoders()+"\n");
        for (int i=0;i<getPulseDivisors().length;i++) {
            strBuff.append("       pulseDivisors["+i+"]="+getPulseDivisors()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setEncoderType((((int)data[offset]&0xFF)>>4) & 0xF);
        setNrOfEncoders(((int)data[offset++]&0xFF) & 0xF);
        setEncoderFlags((int)data[offset++]&0xFF);
        for(int i=0;i<getPulseDivisors().length;i++) {
            getPulseDivisors()[i] = (int)data[offset++]&0xFF;
        }
    }

    protected CommandDescriptor getCommandDescriptor() {
        return new CommandDescriptor('l');
    }

//    protected byte[] prepareData() throws IOException {
//        return null;
//    }

    public int getEncoderType() {
        return encoderType;
    }

    public void setEncoderType(int encoderType) {
        this.encoderType = encoderType;
    }

    public int getNrOfEncoders() {
        return nrOfEncoders;
    }

    public void setNrOfEncoders(int nrOfEncoders) {
        this.nrOfEncoders = nrOfEncoders;
    }

    public int getEncoderFlags() {
        return encoderFlags;
    }

    public void setEncoderFlags(int encoderFlags) {
        this.encoderFlags = encoderFlags;
    }

    public int[] getPulseDivisors() {
        return pulseDivisors;
    }

    public void setPulseDivisors(int[] pulseDivisors) {
        this.pulseDivisors = pulseDivisors;
    }

}
