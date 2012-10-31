/*
 * DLMSCache.java
 *
 * Created on 22 augustus 2003, 14:21
 */

package com.energyict.dlms;

import com.energyict.mdc.protocol.DeviceProtocolCache;

import java.io.Serializable;

/**
 *
 * @author  Koen
 */
public class DLMSCache implements DeviceProtocolCache, Serializable {
    UniversalObject[] objectList;
    int confProgChange;
    boolean changed;
    /** Creates a new instance of DLMSCache */
    public DLMSCache() {
        this(null,-1);
    }
    
    // constructor for the
    public DLMSCache(UniversalObject[] objectList,int confProgChange,boolean changed) {
        this.objectList=objectList;
        this.confProgChange=confProgChange;
        this.changed=changed;
    }
    
    public DLMSCache(UniversalObject[] objectList,int confProgChange) {
        this.objectList=objectList;
        this.confProgChange=confProgChange;
        setChanged(false);
    }
    public void setChanged(boolean changed) {
        this.changed = changed;
    }
    public void saveObjectList(UniversalObject[] objectList) {
        this.objectList=objectList;
        setChanged(true);
    }
    public UniversalObject[] getObjectList () {
        return objectList;   
    }
    public void setConfProgChange(int confProgChange) {
        this.confProgChange=confProgChange;
        setChanged(true);
    }
    public int getConfProgChange() {
        return confProgChange;   
    }
    @Override
    public boolean contentChanged() {
        return changed;
    }
}
