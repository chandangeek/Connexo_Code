/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * InformationObject97.java
 *
 * Created on 12 april 2006, 16:23
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

/** @author fbo */

public class InformationObject97 extends InformationObject {
    
    int direction;
    
    /** Creates a new instance of InformationObject97 */
    public InformationObject97() {
    }
    
    boolean isImport( ){
        return direction == 1;
    }
    
    boolean isExport( ){
        return direction == 2;
    }
    
    void setDirection( int d ) {
        this.direction = d;
    }
    
    public String toString( ){
        return "InformationObject97[ direction=" + direction + "]"; 
    }
    
}
