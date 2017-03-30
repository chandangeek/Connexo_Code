/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SiemensSCTMFrame.java
 *
 * Created on 5 februari 2003, 14:41
 */

package com.energyict.protocolimpl.siemens7ED62;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;


/**
 *
 * @author  Koen
 */
public class SiemensSCTMFrameDecoder {

    byte[] data=null;
    int length=0;
    int status=0;
    int DBN=0,AKN=0;
    String ID=null;

    public SiemensSCTMFrameDecoder(SiemensSCTM siemensSCTM) {
      data=null;
      length=0;
      status=0;
      DBN=AKN=0;
      ID=null;

    }

    public SiemensSCTMFrameDecoder(SiemensSCTM siemensSCTM,byte[] frame) throws IOException {
        int i;
        if (frame.length < siemensSCTM.SCTM_DATABLOCK_OFFSET)
            throw new IOException("SiemensSCTMFrameDecoder, wrong framelength");

        status = frame[siemensSCTM.SCTM_HEADER_STATUS] &0x0F; //- 0x30;
        byte[] arr=new byte[siemensSCTM.SCTM_HEADER_ID_SIZE];
        for (i=0;i<+siemensSCTM.SCTM_HEADER_ID_SIZE;i++) arr[i] = frame[i+siemensSCTM.SCTM_HEADER_ID];
        ID = new String(arr);
        DBN = frame[siemensSCTM.SCTM_HEADER_DBN] - 0x30;
        AKN = frame[siemensSCTM.SCTM_HEADER_AKN] - 0x30;
        length = ProtocolUtils.parseIntFromStr(frame,siemensSCTM.SCTM_HEADER_LENGTH,siemensSCTM.SCTM_HEADER_LENGTH_SIZE);
        if (length >= 3) {
           data = new byte[length-3];
           for (i=0;i<(length-3);i++) data[i] = frame[i+siemensSCTM.SCTM_DATABLOCK_OFFSET];
        }
    }
    protected boolean isHeaderOnly() {
        return (data==null);
    }

    protected byte[] getData() {
        return data;
    }
    protected int getLength() {
        return length;
    }
    protected int getDBN() {
        return DBN;
    }
    protected int getAKN() {
        return AKN;
    }
    protected int getStatus() {
        return status;
    }
    protected String getID() {
        return ID;
    }
} // public class SiemensSCTMFrame
