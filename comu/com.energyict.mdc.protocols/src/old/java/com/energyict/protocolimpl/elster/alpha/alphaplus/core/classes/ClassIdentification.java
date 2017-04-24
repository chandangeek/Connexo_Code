/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ClassIdentification.java
 *
 * Created on 11 juli 2005, 17:14
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

/**
 *
 * @author Koen
 */
public class ClassIdentification {
    
    public static final int CLASS_17_PROFILE_DATA=17;
    
    private int id;
    private int length;
    private boolean verify; 
    
    /** Creates a new instance of ClassIdentification */
    public ClassIdentification(int id,int length,boolean verify) {
        this.id=id;
        this.length=length;
        this.verify=verify;
    }

    public int getId() {
        return id;
    }

    public int getLength() {
        return length;
    }

    
    public boolean isVerify() {
        return verify;
    }
    
    public void setVerify(boolean verify) {
        this.verify=verify;
    }
    
    public boolean isMultipleClass() {
        
        // As long as packetsize > 1x64, means from 2x64 up to 16x64, it is OK and length is in 2 bytes!
        // Otherwise, the last length byte is 1 byte long!!
        // blijkt niet naar behoren te werken...
        if (getId() == CLASS_17_PROFILE_DATA)
            return true;
        else return false;
    }

    public void setLength(int length) {
        this.length = length;
    }
    
}
