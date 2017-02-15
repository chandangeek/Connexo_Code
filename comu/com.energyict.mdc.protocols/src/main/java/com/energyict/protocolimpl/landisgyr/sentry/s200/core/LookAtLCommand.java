/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LookAtLCommand.java
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
public class LookAtLCommand extends AbstractCommand {

    private int inputType; // 00 = FORM A 2 wire, 01 = FORM C 3 wire
    // ns
    private int nrOfInputs; // high order nibble of
    private int segmentDataSize; // low order nibble of
                                 // 0=8bit, 1=12bit, 2=16bit, 3=16bit signed
    private int[] pulseDivisors = new int[4]; // inputs 5..8

    /** Creates a new instance of ForceStatusCommand */
    public LookAtLCommand(CommandFactory cm) {
        super(cm);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LookAtLCommand:\n");
        strBuff.append("   inputType="+getInputType()+"\n");
        strBuff.append("   nrOfInputs="+getNrOfInputs()+"\n");
        for (int i=0;i<getPulseDivisors().length;i++) {
            strBuff.append("       pulseDivisors["+i+"]="+getPulseDivisors()[i]+"\n");
        }
        strBuff.append("   segmentDataSize="+getSegmentDataSize()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setInputType((int)data[offset++]&0xFF);
        setNrOfInputs(((((int)data[offset]&0xFF)>>4) & 0xF)+1);
        setSegmentDataSize(((int)data[offset++]&0xFF) & 0xF);
        for(int i=0;i<getPulseDivisors().length;i++) {
            getPulseDivisors()[i] = (int)data[offset++]&0xFF;
        }
    }

    protected CommandDescriptor getCommandDescriptor() {
        return new CommandDescriptor('L');
    }

//    protected byte[] prepareData() throws IOException {
//        return null;
//    }

    public int getInputType() {
        return inputType;
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
    }

    public int getNrOfInputs() {
        return nrOfInputs;
    }

    public void setNrOfInputs(int nrOfInputs) {
        this.nrOfInputs = nrOfInputs;
    }

    public int getSegmentDataSize() {
        return segmentDataSize;
    }

    public void setSegmentDataSize(int segmentDataSize) {
        this.segmentDataSize = segmentDataSize;
    }

    public int[] getPulseDivisors() {
        return pulseDivisors;
    }

    public void setPulseDivisors(int[] pulseDivisors) {
        this.pulseDivisors = pulseDivisors;
    }

}
