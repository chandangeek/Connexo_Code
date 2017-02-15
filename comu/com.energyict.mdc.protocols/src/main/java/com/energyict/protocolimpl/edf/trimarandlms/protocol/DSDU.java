/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DSDU.java
 *
 * Created on 13 februari 2007, 17:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.protocol;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DSDU {

    private byte[] data;
    private byte[] segmentData;
    private boolean priority;
    private boolean end;
    private int stsap;
    private int dtsap;

    /** Creates a new instance of DSDU */
    public DSDU() {
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DSDU:\n");
        if (getData() != null) {
            strBuff.append(ProtocolUtils.outputHexString(getData())+"\n");
        }
        strBuff.append("   dtsap="+getDtsap()+"\n");
        strBuff.append("   end="+isEnd()+"\n");
        strBuff.append("   priority="+isPriority()+"\n");
        if (getSegmentData() != null) {
            strBuff.append(ProtocolUtils.outputHexString(getSegmentData())+"\n");
        }
        strBuff.append("   stsap="+getStsap()+"\n");
        return strBuff.toString();
    }
    // check if the DT+ is 101
    public boolean checkSgt() {
        return  ((int)data[0]&0xE0)==0xA0;
    }
    public boolean lastSgt() {
        return isEnd();
    }
    public void init(boolean priority, boolean end, int stsap, int dtsap, byte[] data) throws IOException {
        setEnd(end);
        setStsap(stsap);
        setDtsap(dtsap);
        setPriority(priority);

        int temp = 0xA000;
        temp |= end?0x1000:0x0000;
        temp |= (stsap<<10);
        temp |= (dtsap);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write((temp>>8)&0xff);
        baos.write(temp&0xff);
        baos.write(data);
        setData(baos.toByteArray());
    }

    public void init(boolean priority) throws IOException {
        init(null, priority);
    }
    public void init(byte[] data) throws IOException {
        init(data,false);
    }
    public void init(byte[] data, boolean priority) throws IOException {
        setPriority(priority);
        setData(data);
        if (data != null) {
           setSegmentData(ProtocolUtils.getSubArray(data,2,data.length-3));
//System.out.println("segment ="+ProtocolUtils.outputHexString(getSegmentData()));
           int temp = ProtocolUtils.getInt(data,0, 2);
           setEnd((temp&0x1000) != 0);
           setStsap((temp&0xC00)>>10);
           setDtsap(temp&0x3FF);
        }
    }

    public int size() {
        return getData().length;
    }

    public byte[] getData() {
        return data;
    }

    private void setData(byte[] data) {
        this.data = data;
    }

    public boolean isPriority() {
        return priority;
    }

    private void setPriority(boolean priority) {
        this.priority = priority;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }

    public int getStsap() {
        return stsap;
    }

    public void setStsap(int stsap) {
        this.stsap = stsap;
    }

    public int getDtsap() {
        return dtsap;
    }

    public void setDtsap(int dtsap) {
        this.dtsap = dtsap;
    }

    public byte[] getSegmentData() {
        return segmentData;
    }

    public void setSegmentData(byte[] segmentData) {
        this.segmentData = segmentData;
    }


}
