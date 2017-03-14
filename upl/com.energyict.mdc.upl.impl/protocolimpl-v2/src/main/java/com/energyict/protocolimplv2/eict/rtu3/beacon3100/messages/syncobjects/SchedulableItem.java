package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;


import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class SchedulableItem {
    protected ObisCode obisCode;
    private AbstractDataType bufferSize;

    public SchedulableItem() {
    }

    public SchedulableItem(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    public SchedulableItem(ObisCode obisCode, AbstractDataType bufferSize) {
        this.obisCode = obisCode;
        this.bufferSize = bufferSize;
    }

    @XmlAttribute
    public ObisCode getObisCode() {
        return obisCode;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    @XmlAttribute
    public AbstractDataType getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(AbstractDataType bufferSize) {
        this.bufferSize = bufferSize;
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(new OctetString(obisCode.getLN()));
        structure.addDataType(bufferSize);
        return structure;
    }

    public static SchedulableItem findObisCode(ObisCode obisCode, List<SchedulableItem> items) {
        if (obisCode == null || items == null) {
            return null;
        }
        for(SchedulableItem item : items) {
            if (obisCode.equals(item.getObisCode())){
                return item;
            }
        }
        return null;
    }
}
