/*
 * LookAtCommand.java
 *
 * Created on 27 juli 2006, 15:05
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
public class LookAtCommand {

    CommandFactory commandFactory;
    private LookAtLCommand lookAtL;
    private LookAtlCommand2 lookAtl;

    /** Creates a new instance of LookAtCommand */
    public LookAtCommand(CommandFactory commandFactory) {
        this.commandFactory=commandFactory;
    }

    public void init() throws IOException {
        setLookAtL(new LookAtLCommand(commandFactory));
        getLookAtL().build();
        setLookAtl(new LookAtlCommand2(commandFactory));
        getLookAtl().build();
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LookAtCommand:\n");
        strBuff.append("   lookAtL="+getLookAtL()+"\n");
        strBuff.append("   lookAtl="+getLookAtl()+"\n");
        return strBuff.toString();
    }

    // L command
    public int getInputType() {
        return getLookAtL().getInputType();
    }

    public int getNrOfInputs() {
        return getLookAtL().getNrOfInputs();
    }

    public int getSegmentDataSize() {
        return getLookAtL().getSegmentDataSize();
    }

    public int getPulseDivisors(int input) {
        if (input<=3)
           return getLookAtL().getPulseDivisors()[input];
        else
           return getLookAtl().getPulseDivisors()[input];
    }

    // l command
    public int getEncoderType() {
        return getLookAtl().getEncoderType();
    }

    public int getNrOfEncoders() {
        return getLookAtl().getNrOfEncoders();
    }

    public int getEncoderFlags() {
        return getLookAtl().getEncoderFlags();
    }

    public boolean isCumulativePulseCount() {
        return (getEncoderFlags() & 0x01) == 0x01;
    }

    public boolean isPSIEncoder() {
        return getEncoderType() == 0;
    }
    public boolean isSangamoEncoder() {
        return getEncoderType() == 1;
    }
    public boolean isJEM2MeterReadings() {
        return getEncoderType() == 2;
    }
    public boolean isJEM1MeterReadings() {
        return getEncoderType() == 3;
    }

    public LookAtLCommand getLookAtL() {
        return lookAtL;
    }

    public void setLookAtL(LookAtLCommand lookAtL) {
        this.lookAtL = lookAtL;
    }

    public LookAtlCommand2 getLookAtl() {
        return lookAtl;
    }

    public void setLookAtl(LookAtlCommand2 lookAtl) {
        this.lookAtl = lookAtl;
    }
}
