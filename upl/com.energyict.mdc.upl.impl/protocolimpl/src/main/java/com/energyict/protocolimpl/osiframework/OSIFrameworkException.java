/*
 * OSIFrameworkException.java
 *
 * Created on 18 juli 2006, 15:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.osiframework;

import java.io.*;

/**
 *
 * @author Koen
 */
public class OSIFrameworkException extends IOException {
    
    static public final int UNKNOWN_REASON=0;
    static public final int TIMEOUT=1;
    static public final int PROTOCOL=2;
    static public final int CRC=3;
    
    int reason;
    

    
    /** Creates a new instance of OSIFrameworkException */
    public OSIFrameworkException(int reason) {
        this.reason = reason;
    }
    
    public OSIFrameworkException(String s) {
        super(s);
    }    
    
    public OSIFrameworkException(String s,int reason) {
        super(s);
        this.reason=reason;
    }    
    
    
    
}
