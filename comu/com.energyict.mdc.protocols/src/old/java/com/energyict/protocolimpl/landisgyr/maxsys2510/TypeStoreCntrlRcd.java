/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.maxsys2510;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * DATA_SIZE                
 * The leftmost byte contains 8, 12, 15, or 
 * 16 for the number of bits per register.  15 is 16-bit signed data;  
 * MSB contains the sign.  See note below. 
 * 
 * INTVL_IN_MINS
 * The number of minutes in a storage interval;  must be a binary integral 
 * divisor of 60.
 *
 * NO_OF_CHNLS
 * The number of channels of data to store.  Must be binary from 1 to 
 * MAX_STORAGE_CHNNLS.

 * @author fbo
 *
 */

class TypeStoreCntrlRcd {

    int dataSize;
    int intvlInMins;
    int noOfChnls;
    List chnlCntrl;
    
    static TypeStoreCntrlRcd parse( MaxSys maxSys, Assembly assembly ) throws IOException {
        TypeStoreCntrlRcd rcd = new TypeStoreCntrlRcd();
        rcd.dataSize = assembly.intValue();
        rcd.intvlInMins = assembly.intValue();
        rcd.noOfChnls = assembly.intValue();
        
        rcd.chnlCntrl = new ArrayList();
        /*  fetch the number of channels */
        int ms = maxSys.getTable0().getTypeMaximumValues().getMaxStorageChnnls();
        for( int i = 0; i < ms; i ++ ){
            rcd.chnlCntrl.add( TypeStoreChnlCntrlRcd.parse( assembly ) );
        }
        
        return rcd;
    }

    /** 8, 12, 15, or 16 bit data, binary value */
    int getDataSize() {
        return dataSize;
    }
    
    /** binary integral divisor of 60 */
    int getIntvlInMins() {
        return intvlInMins;
    }

    /** Binary, 1 to MAX_STORAGE_CHNNLS */
    int getNoOfChnls() {
        return noOfChnls;
    }

    public TypeStoreChnlCntrlRcd getChnlCntrl(int i) {
    	return (TypeStoreChnlCntrlRcd) chnlCntrl.get(i);
    }

    public String toString() {
        StringBuffer rslt = new StringBuffer();
        rslt.append("TypeStoreCntrlRcd [ \n");
        rslt.append("    dataSize=" + this.dataSize + "\n");
        rslt.append("    intvlInMins=" + this.intvlInMins + "\n");
        rslt.append("    noOfChnls=" + this.noOfChnls + "\n");
        
        Iterator i = chnlCntrl.iterator();
        while( i.hasNext() ){
            rslt.append("    " + i.next() + "\n");
        }
        
        rslt.append("]");
        return rslt.toString();
    }

}
