/*
 * ResponseData.java
 *
 * Created on 19 september 2005, 16:42
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.connection;

import com.energyict.protocolimpl.utils.ProtocolTools;


/**
 *
 * @author Koen
 */
public class ResponseData {
    
    private byte[] data;
    private int address;
    private int functionCode;
    boolean exception;
    
    public static final int ILLEGAL_FUNCTION=1;
    public static final int ILLEGAL_DATA_ADDRESS=2;
    public static final int ILLEGAL_DATA_VALUE=3;
    public static final int SLAVE_DEVICE_FAILURE=4;
    public static final int ACKNOWLEDGE=5;
    public static final int SLAVE_DEVICE_BUSY=6;
    public static final int MEMORY_PARITY_ERROR=8;
    public static final int GATEWAY_PATH_UNAVAILABLE=10;
    public static final int GATEWAY_TARGET_DEVICE_FAILED_TO_RESPOND=11;
            
    public static final String[] exceptionStrings = new String[]{"",
                                                                 "ILLEGAL_FUNCTION",
                                                                 "ILLEGAL_DATA_ADDRESS",
                                                                 "ILLEGAL_DATA_VALUE",
                                                                 "SLAVE_DEVICE_FAILURE",
                                                                 "ACKNOWLEDGE",   
                                                                 "SLAVE_DEVICE_BUSY", 
                                                                 "", 
                                                                 "MEMORY_PARITY_ERROR",
                                                                 "",
                                                                 "GATEWAY_PATH_UNAVAILABLE",
                                                                 "GATEWAY_TARGET_DEVICE_FAILED_TO_RESPOND"};

    
    /** Creates a new instance of ResponseData */
    public ResponseData() {
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
/*
        sb.append("ResponseData: address="+address+", functionCode="+functionCode+"\n");
        for (int i=0;i<data.length;i++) {
            sb.append("0x"+Integer.toHexString((int)data[i]&0xff)+" ");
        }
        sb.append("\n");
        sb.append(new String(data)+"\n");
*/

        sb.append("ResponseData=[");
        sb.append("functionCode=").append(getFunctionCode()).append(", ");
        sb.append("address=").append(getAddress()).append(", ");
        sb.append("data=").append(getData() != null ? ProtocolTools.getHexStringFromBytes(getData()) : "null").append(", ");
        sb.append("]");

        return sb.toString();
    }
    
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getFunctionCode() {
        return functionCode;
    }
    
    
    public void setFunctionCode(int functionCode) {
        this.functionCode = functionCode;
        if ((functionCode&0x80) == 0x80) {
            exception=true;
        }
    }

    public String getExceptionString() {
        if (isException()) {
            return exceptionStrings[getExceptionCode()];
        }    
        else return "NO EXCEPTION!";
    }
    
    public boolean isException() {
        return exception;
    }

    public int getExceptionCode() {
        return (int)getData()[0]&0xFF;
    }

}
