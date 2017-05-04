/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms;

import com.energyict.mdc.protocol.api.DeviceProtocolCache;

import com.energyict.xml.DeviceProtocolCacheXmlMarshallAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;

/**
 * @author Koen
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
public class DLMSCache implements DeviceProtocolCache, Serializable {
    UniversalObject[] objectList;
    int confProgChange;
    boolean changed;

    /**
     * Creates a new instance of DLMSCache
     */
    public DLMSCache() {
        this(null, -1);
    }

    // constructor for the
    public DLMSCache(UniversalObject[] objectList, int confProgChange, boolean changed) {
        this.objectList = objectList;
        this.confProgChange = confProgChange;
        this.changed = changed;
    }

    public DLMSCache(UniversalObject[] objectList, int confProgChange) {
        this.objectList = objectList;
        this.confProgChange = confProgChange;
        setChanged(false);
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public void saveObjectList(UniversalObject[] objectList) {
        this.objectList = objectList;
        setChanged(true);
    }

    public UniversalObject[] getObjectList() {
        return objectList;
    }

    public void setConfProgChange(int confProgChange) {
        this.confProgChange = confProgChange;
        setChanged(true);
    }

    public int getConfProgChange() {
        return confProgChange;
    }


    @Override
    public boolean isDirty() {
        return changed;
    }

    @Override
    public void markClean() {
        this.changed = false;

    }

    @Override
    public void markDirty() {
        this.changed = true;

    }
}
