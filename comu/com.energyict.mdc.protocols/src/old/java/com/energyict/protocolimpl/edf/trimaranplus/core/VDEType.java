/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * VDEType.java
 *
 * Created on 21 februari 2007, 15:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus.core;

/**
 *
 * @author Koen
 */
public class VDEType {
    
    static private final int VDEBASE=0;
    static private final int VDEEJP=1;
    static private final int VDEMODULABLE=2;
    
    private int vDEType;
    
    /** Creates a new instance of VDEType */
    public VDEType() {
        this(VDEBASE);
    }
    
    public VDEType(int vDEType) {
        setVDEType(vDEType);
    }

    
    public boolean isVDEBASE() {
        return getVDEType() == VDEBASE;
    }

    public boolean isVDEEJP() {
        return getVDEType() == VDEEJP;
    }

    public boolean isVDEMODULABLE() {
        return getVDEType() == VDEMODULABLE;
    }

    public int getVDEType() {
        return vDEType;
    }

    public void setVDEType(int vDEType) {
        this.vDEType = vDEType;
    }

    static public int getVDEBASE() {
        return VDEBASE;
    }

    static public int getVDEEJP() {
        return VDEEJP;
    }

    static public int getVDEMODULABLE() {
        return VDEMODULABLE;
    }
    
}
