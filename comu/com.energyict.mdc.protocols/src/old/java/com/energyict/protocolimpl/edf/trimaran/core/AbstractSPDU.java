/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AabstractSPDU.java
 *
 * Created on 19 juni 2006, 16:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran.core;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractSPDU {

    abstract protected byte[] prepareBuild() throws IOException;
    abstract protected void parse(byte[] data) throws IOException;

    private int length;

    private SPDUFactory sPDUFactory;

    public static final int SPDU_XID=0x0F;
    public static final int SPDU_EOS=0x01;
    public static final int SPDU_ENQ=0x09;
    public static final int SPDU_REC=0x06;
    public static final int SPDU_DAT=0x0C;
    public static final int SPDU_EOD=0x03;
    public static final int SPDU_WTM=0x0A;


    /** Creates a new instance of AabstractSPDU */
    public AbstractSPDU(SPDUFactory sPDUFactory) {
        this.sPDUFactory=sPDUFactory;
        setLength(0);
    }

    public void invoke() throws IOException {
        byte[] data = prepareBuild();
        byte[] rd = getSPDUFactory().getTrimeran().getTrimeranConnection().sendCommand(data, getLength());
        if (rd!=null)
            parse(rd);
    }

    public SPDUFactory getSPDUFactory() {
        return sPDUFactory;
    }

    private void setSPDUFactory(SPDUFactory sPDUFactory) {
        this.sPDUFactory = sPDUFactory;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }


}
