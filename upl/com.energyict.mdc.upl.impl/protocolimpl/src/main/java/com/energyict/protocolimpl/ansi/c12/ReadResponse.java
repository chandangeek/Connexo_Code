/*
 * ReadResponse.java
 *
 * Created on 18 oktober 2005, 9:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class ReadResponse extends AbstractResponse {

    private int count;
    private byte[] tableData;
    private int checkSum;
    
    /** Creates a new instance of ReadResponse */
    public ReadResponse(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }
    
    public String toString() {
        return ProtocolUtils.outputHexString(tableData);
    }
    
    protected void parse(ResponseData responseData) throws IOException {
        // in case of <ok>
        byte[] data = responseData.getData();
        if (data.length >1) {
            count = ProtocolUtils.getInt(data,1, 2);
            
           
//System.out.println("KV_DEBUG> count="+count);

            tableData = ProtocolUtils.getSubArray2(data, 3, count);
            checkSum = ProtocolUtils.getInt(data,3+count, 1);
            // calculate checksum...
            int check = TableData.calcCheckSum(tableData);
            if (check != checkSum) {
                    throw new IOException("ReadResponse, parse, checksum failure in table data!");
            }
        }
        else throw new ResponseIOException("ReadResponse, parse, data length = 0!", AbstractResponse.SNAPSHOT_ERROR);
    }

    public byte[] getTableData() {
        return tableData;
    }

}
