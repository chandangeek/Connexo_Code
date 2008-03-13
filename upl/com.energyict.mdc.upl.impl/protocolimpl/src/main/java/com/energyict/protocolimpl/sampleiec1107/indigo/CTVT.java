/*
 * CTVT.java
 *
 * Created on 7 juli 2004, 12:25
 */

package com.energyict.protocolimpl.sampleiec1107.indigo;

import java.util.*;
import java.io.*;

import com.energyict.protocol.ProtocolUtils;

/**
 *
 * @author  Koen
 */
public class CTVT extends AbstractLogicalAddress {
    
    // not yet implemented...
    // same as historic data for the current period?
    
    /** Creates a new instance of CTVT */
    public CTVT(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }
    
    public void parse(byte[] data, java.util.TimeZone timeZone) throws IOException {
        System.out.println("KV_DEBUG>");
        ProtocolUtils.printResponseData(data);
    }
    
}
