/*
 * DLMSCache.java
 *
 * Created on 22 augustus 2003, 14:21
 */

package com.energyict.dlms;

import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.cache.DeviceProtocolCacheXmlMarshallAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;

/**
 * @author Koen
 */
@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
public class DLMSCache implements DeviceProtocolCache, Serializable {
    private UniversalObject[] objectList;
    private int confProgChange;
    private boolean changed;

    public DLMSCache() {
        this(null, -1);
    }

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
    public boolean contentChanged() {
        return changed;
    }

    @Override
    public void setContentChanged(boolean contentChanged) {
        this.changed = contentChanged;
    }
}