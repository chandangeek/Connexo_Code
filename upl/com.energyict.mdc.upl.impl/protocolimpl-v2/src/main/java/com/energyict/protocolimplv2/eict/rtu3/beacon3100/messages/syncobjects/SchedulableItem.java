package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;


import com.energyict.dlms.axrdencoding.*;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Will hold information about a GENERIC schedule item.
 * The buffer size can be either Unsigned32 or Unsigned16,
 * this causes problems on JSON de-serialization, because it cannot
 * instantiate back an AbstractDataType correctly.
 */
@XmlRootElement
public class SchedulableItem {
    /**
     * The obis code for this item
     */
    protected ObisCode obisCode;

    /**
     * Buffer size is stored and serialized as long, and re-converted back to U16 or U32
     */
    private long bufferSize;

    /**
     * Flag to know if we have to convert the bufferSize value after de-serialization to Unsigned32 or Unsigned16
     */
    private boolean u16 = false;

    public SchedulableItem() {
    }

    public SchedulableItem(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    public SchedulableItem(ObisCode obisCode, Unsigned32 bufferSize) {
        this.obisCode = obisCode;
        this.bufferSize = bufferSize.getValue();
    }

    public SchedulableItem(ObisCode obisCode, Unsigned16 bufferSize) {
        this.obisCode = obisCode;
        this.bufferSize = bufferSize.getValue();
        u16 = true;
    }

    @XmlAttribute
    public ObisCode getObisCode() {
        return obisCode;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    @XmlAttribute
    public boolean getU16(){
        return u16;
    }

    public void setU16(boolean u16){
        this.u16 = u16;
    }

    @XmlAttribute
    public long getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(long bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setBufferSizeFromAbstract(AbstractDataType bufferSize) {
        this.bufferSize = bufferSize.longValue();

        if (bufferSize.isUnsigned16()){
            u16 = true;
        }
    }


    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(new OctetString(obisCode.getLN()));
        if (u16) {
            structure.addDataType(new Unsigned16((int) bufferSize));
        } else{
            structure.addDataType(new Unsigned32(bufferSize));
        }
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
