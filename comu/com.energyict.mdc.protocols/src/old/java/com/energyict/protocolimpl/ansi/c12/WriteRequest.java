/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * WriteRequest.java
 *
 * Created on 19 oktober 2005, 16:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class WriteRequest extends AbstractRequest {

    RequestData requestData=new RequestData();

    /** Creates a new instance of WriteRequest */
    public WriteRequest(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }

    protected void parse(ResponseData responseData) throws IOException {
          response = new WriteResponse(getPSEMServiceFactory());
          response.build(responseData);
    }

    protected RequestData getRequestData() {
        return requestData;
    }

    private byte[] assembleTableData(byte[] tableData) {
        byte[] assembledTableData = new byte[tableData.length+3];
        assembledTableData[0] = (byte)(tableData.length>>8);
        assembledTableData[1] = (byte)tableData.length;
        System.arraycopy(tableData,0,assembledTableData,2,tableData.length);
        int check = TableData.calcCheckSum(tableData);
        assembledTableData[assembledTableData.length-1]=(byte)check;
        return assembledTableData;
    }

    // full write
    public void fullWrite(int tableId, byte[] tableData) throws IOException {
        requestData.setCode(FULL_WRITE);
        byte[] assembledTableData = assembleTableData(tableData);
        byte[] data = new byte[assembledTableData.length+2];
        System.arraycopy(assembledTableData,0,data,2,assembledTableData.length);
        data[0] = (byte)(tableId>>8);
        data[1] = (byte)(tableId);
        requestData.setData(data);
    }

    // partial write index
    public void partialWriteIndex(int tableId, int index, byte[] tableData) throws IOException {
        requestData.setCode(PARTIAL_WRITE_INDEX_1+index);
        byte[] assembledTableData = assembleTableData(tableData);
        byte[] data = new byte[assembledTableData.length+4];
        System.arraycopy(assembledTableData,0,data,4,assembledTableData.length);
        data[0] = (byte)(tableId>>8);
        data[1] = (byte)(tableId);
        data[2] = (byte)(index>>8);
        data[3] = (byte)(index);
        requestData.setData(data);
    }
    // partial write offset
    public void partialWriteOffset(int tableId, int offset, byte[] tableData) throws IOException {
        requestData.setCode(PARTIAL_WRITE_OFFSET);
        byte[] assembledTableData = assembleTableData(tableData);
        byte[] data = new byte[assembledTableData.length+5];
        System.arraycopy(assembledTableData,0,data,5,assembledTableData.length);
        data[0] = (byte)(tableId>>8);
        data[1] = (byte)(tableId);
        data[2] = (byte)(offset>>16);
        data[3] = (byte)(offset>>8);
        data[4] = (byte)(offset);
        requestData.setData(data);
    }

}
