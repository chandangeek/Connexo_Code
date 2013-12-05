/*
 * ReadRequest.java
 *
 * Created on 18 oktober 2005, 9:10
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
public class ReadRequest extends AbstractRequest {

    RequestData requestData=new RequestData();


    /** Creates a new instance of ReadRequest */
    public ReadRequest(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }

    protected void parse(ResponseData responseData) throws IOException {
          response = new ReadResponse(getPSEMServiceFactory());
          response.build(responseData);
    }

    protected RequestData getRequestData() {
        return requestData;
    }

    // default read
    public void partialReadDefault() throws IOException {
        requestData.setCode(PARTIAL_READ_DEFAULT);
    }
    // full read
    public void fullRead(int tableId) throws IOException {
        requestData.setCode(FULL_READ);
        byte[] data = new byte[2];
        data[0] = (byte)(tableId>>8);
        data[1] = (byte)(tableId);
        requestData.setData(data);
    }
    // partial read index
    public void partialReadIndex(int tableId, int index, int count) throws IOException {
        requestData.setCode(PARTIAL_READ_INDEX_1+index);
        byte[] data = new byte[6];
        data[0] = (byte)(tableId>>8);
        data[1] = (byte)(tableId);
        data[2] = (byte)(index>>8);
        data[3] = (byte)(index);
        data[4] = (byte)(count>>8);
        data[5] = (byte)(count);
        requestData.setData(data);
    }
    // partial read offset
    public void partialReadOffset(int tableId, int offset, int count) throws IOException {
        requestData.setCode(PARTIAL_READ_OFFSET);
        byte[] data = null;
        if (count > 0) {
            data = new byte[7];
            data[0] = (byte)(tableId>>8);
            data[1] = (byte)(tableId);
            data[2] = (byte)(offset>>16);
            data[3] = (byte)(offset>>8);
            data[4] = (byte)(offset);
            data[5] = (byte)(count>>8);
            data[6] = (byte)(count);
        }
        else {
            data = new byte[5];
            data[0] = (byte)(tableId>>8);
            data[1] = (byte)(tableId);
            data[2] = (byte)(offset>>16);
            data[3] = (byte)(offset>>8);
            data[4] = (byte)(offset);
        }

        requestData.setData(data);
    }


}
