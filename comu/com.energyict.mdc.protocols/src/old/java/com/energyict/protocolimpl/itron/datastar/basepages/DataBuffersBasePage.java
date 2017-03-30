/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DataBuffersBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.datastar.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class DataBuffersBasePage extends AbstractBasePage {

    private int[] inputDataBuffers = new int[4];
    private int[] inputTotalBuffers = new int[4];
    private int[] temporaryDataBuffers = new int[4];
    private int[] temporaryTotalBuffers = new int[4];


    /** Creates a new instance of RealTimeBasePage */
    public DataBuffersBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DataBuffersBasePage:\n");
        for (int i=0;i<getInputDataBuffers().length;i++) {
            strBuff.append("       inputDataBuffers["+i+"]="+getInputDataBuffers()[i]+"\n");
        }
        for (int i=0;i<getInputTotalBuffers().length;i++) {
            strBuff.append("       inputTotalBuffers["+i+"]="+getInputTotalBuffers()[i]+"\n");
        }
        for (int i=0;i<getTemporaryDataBuffers().length;i++) {
            strBuff.append("       temporaryDataBuffers["+i+"]="+getTemporaryDataBuffers()[i]+"\n");
        }
        for (int i=0;i<getTemporaryTotalBuffers().length;i++) {
            strBuff.append("       temporaryTotalBuffers["+i+"]="+getTemporaryTotalBuffers()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0xBB,40);
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        for (int i=0;i<getInputDataBuffers().length;i++) {
            getInputDataBuffers()[i] = ProtocolUtils.getInt(data,offset,2);
            offset+=2;
        }
        for (int i=0;i<getInputDataBuffers().length;i++) {
            getInputDataBuffers()[i] = ProtocolUtils.getInt(data,offset,2);
            offset+=2;
        }
        for (int i=0;i<getTemporaryDataBuffers().length;i++) {
            getTemporaryDataBuffers()[i] = ProtocolUtils.getInt(data,offset,3);
            offset+=3;
        }
        for (int i=0;i<getTemporaryTotalBuffers().length;i++) {
            getTemporaryTotalBuffers()[i] = ProtocolUtils.getInt(data,offset,2);
            offset+=3;
        }
    }

    public int[] getInputDataBuffers() {
        return inputDataBuffers;
    }

    public void setInputDataBuffers(int[] inputDataBuffers) {
        this.inputDataBuffers = inputDataBuffers;
    }

    public int[] getInputTotalBuffers() {
        return inputTotalBuffers;
    }

    public void setInputTotalBuffers(int[] inputTotalBuffers) {
        this.inputTotalBuffers = inputTotalBuffers;
    }

    public int[] getTemporaryDataBuffers() {
        return temporaryDataBuffers;
    }

    public void setTemporaryDataBuffers(int[] temporaryDataBuffers) {
        this.temporaryDataBuffers = temporaryDataBuffers;
    }

    public int[] getTemporaryTotalBuffers() {
        return temporaryTotalBuffers;
    }

    public void setTemporaryTotalBuffers(int[] temporaryTotalBuffers) {
        this.temporaryTotalBuffers = temporaryTotalBuffers;
    }


} // public class RealTimeBasePage extends AbstractBasePage
