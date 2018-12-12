/*
 * DataParse.java
 *
 * Created on 17 januari 2003, 14:13
 *      
 */

package com.energyict.protocolimpl.actarissevc;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.iec1107.abba1140.Calculate;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.math.BigDecimal;

/**
 *
 * @author  Koen
 */
abstract public class SEVCDataParse {
    
    abstract protected int getOffset();
    abstract protected int getFormat();
    abstract protected Unit getUnit();
    abstract protected int getLength();
    
    public final static int SEVC_VOLUME=0;
    public final static int SEVC_FLOATING_POINT=1;
    public final static int SEVC_UNSIGNED_INTEGER=2;
    public final static int SEVC_HOUR=3;
    public final static int SEVC_TIME=4;
    public final static int SEVC_BINARY_MASK=5;
    public final static int SEVC_INTEGER_BINARY=6;
    public final static int SEVC_BYTE=7;
    public final static int SEVC_SHORT=8;
    
    
    /** Creates a new instance of DataParse */
    public SEVCDataParse() {
    }
    
    protected Number getValue(byte[] data) {
        switch(getFormat()) {
            case SEVC_VOLUME:
                return getVolume(data);
            case SEVC_FLOATING_POINT:
                return getfp(data);
            case SEVC_UNSIGNED_INTEGER:
                // in de veronderstelling dat die unsigned integers 4 bytes zijn
                return BigDecimal.valueOf(ProtocolUtils.getIntLE(data,(getOffset()/2)));
            case SEVC_BINARY_MASK:
                // no little endian??
                return BigDecimal.valueOf(ProtocolUtils.getShort(data,(getOffset()/2)));
            case SEVC_INTEGER_BINARY: // KV 25082003
                return BigDecimal.valueOf((long)data[(getOffset()/2)]);
            case SEVC_SHORT:
                return BigDecimal.valueOf(ProtocolUtils.getShortLE(data,(getOffset()/2)));
            case SEVC_BYTE:
                return BigDecimal.valueOf((long)data[(getOffset()/2)]&0xFFL);
                
        }
        return null;
    } // protected Number getValue(byte[] data)
    
    private Number getfp(byte[] data) {
        return (Calculate.convertIEEE32fp2NumberLE(data, getOffset()/2));
    }
    
    private Number getVolume(byte[] data) {
        long gtzero = ProtocolUtils.getIntLE(data,(getOffset()/2));
        long ltzero = ProtocolUtils.getIntLE(data,(getOffset()/2)+4);
        String str = String.valueOf(gtzero)+"."+String.valueOf(ltzero);
        return new BigDecimal(str);
    }
    
} // abstract public class SEVCDataParse
