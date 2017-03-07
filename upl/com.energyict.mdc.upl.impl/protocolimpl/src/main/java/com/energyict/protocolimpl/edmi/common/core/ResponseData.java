package com.energyict.protocolimpl.edmi.common.core;

import com.energyict.dialer.connection.Connection;
import com.energyict.protocol.ProtocolUtils;

/**
 *
 * @author koen
 */
public class ResponseData {
    
    static public final int CANNOT_WRITE=1; // e.g. serial number already set
    static public final int UNIMPLEMENTED_OPERATION=2;
    static public final int REGISTER_NOT_FOUND=3;
    static public final int ACCESS_DENIED=4; // Security reasons
    static public final int WRONG_LENGTH=5; // Number of byte in request was incorrect
    static public final int BAD_TYPE_CODE=6; // Internal error
    static public final int DATA_NOT_READY_YET=7; // Still processing. Try again later.
    static public final int OUT_OF_RANGE=8; // Written value was out of defined ranges.
    static public final int NOT_LOGGED_IN=9; // Not logged in.    
    
    private byte[] data;
    
    /** Creates a new instance of ResponseData */
    public ResponseData(byte[] data) {
        this.setData(data);
    }
   
    public String toString() {
        return "ResponseData binary: "+ProtocolUtils.outputHexString(getData())+"\n"+"ResponseData ascii: "+new String(getData());
    }
    
    public boolean isControl(byte controlKar) {
        return getData()[0] == controlKar;
    }

    public boolean isACK() {
        return getData()[0] == Connection.ACK;
    }
    
    public boolean isCAN() {
        return getData()[0] == Connection.CAN;
    }
    
    public int getCANCode() {
        if (getData().length == 2) {
			return getData()[1];
		} else {
			return -1;
		}
    }
    
    public byte[] getData() {
        return data;
    }

    private void setData(byte[] data) {
        this.data = data;
    }
}
