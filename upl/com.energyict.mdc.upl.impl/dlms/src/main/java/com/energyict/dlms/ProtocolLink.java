/*
 * ProtocolLink.java
 *
 * Created on 18 augustus 2004, 12:01
 */

package com.energyict.dlms;

import java.util.*;
import java.util.logging.*;

import com.energyict.dlms.cosem.StoredValues;
/**
 *
 * @author  Koen
 */
public interface ProtocolLink {
    
    static final int SN_REFERENCE=1;
    static final int LN_REFERENCE=0;
    
    DLMSConnection getDLMSConnection();
    public DLMSMeterConfig getMeterConfig();
    public TimeZone getTimeZone();
    public boolean isRequestTimeZone();
    public int getRoundTripCorrection();
    public Logger getLogger();
    public int getReference();
    public StoredValues getStoredValues();
} 
