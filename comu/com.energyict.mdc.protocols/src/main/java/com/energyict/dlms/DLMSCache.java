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
    private UniversalObject[] objectList;
    private int confProgChange;
    private boolean dirty;

    public DLMSCache() {
        this(null, -1);
    }

    public DLMSCache(UniversalObject[] objectList, int confProgChange, boolean dirty) {
        this.objectList = objectList;
        this.confProgChange = confProgChange;
        this.dirty = dirty;
    }

    public DLMSCache(UniversalObject[] objectList, int confProgChange) {
        this.objectList = objectList;
        this.confProgChange = confProgChange;
        markClean();
    }

    public void saveObjectList(UniversalObject[] objectList) {
        this.objectList = objectList;
        markDirty();
    }

    public UniversalObject[] getObjectList() {
        return objectList;
    }

    public void setConfProgChange(int confProgChange) {
        this.confProgChange = confProgChange;
        markDirty();
    }

    public int getConfProgChange() {
        return confProgChange;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void markClean() {
        this.dirty = false;
    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

}