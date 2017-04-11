package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;


import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.obis.ObisCode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * Will hold information about a GENERIC schedule item.
 * The buffer size can be either Unsigned32 or Unsigned16,
 * this causes problems on JSON de-serialization, because it cannot
 * instantiate back an AbstractDataType correctly.
 */
@JsonSerialize(using = ScheduleableItemDataSerializer.class)
@JsonDeserialize(using = SchedulableItemDeserializer.class)
public class SchedulableItem {
    /**
     * The obis code for this item
     */
    protected ObisCode obisCode;

    /**
     * Buffer size as AbstractDataType - this can be either Unsigned32 or Unsigned16
     */
    private AbstractDataType bufferSize;

    public SchedulableItem(ObisCode obisCode, AbstractDataType bufferSize) {
        this.obisCode = obisCode;
        this.bufferSize = bufferSize;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    public AbstractDataType getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(AbstractDataType bufferSize) {
        this.bufferSize = bufferSize;
    }


    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(new OctetString(obisCode.getLN()));

        if (bufferSize != null) {
            structure.addDataType(bufferSize);
        } else {
            structure.addDataType(new Unsigned32(1));
        }

        return structure;
    }

    public static SchedulableItem findObisCode(ObisCode obisCode, List<SchedulableItem> items) {
        if (obisCode == null || items == null) {
            return null;
        }
        for (SchedulableItem item : items) {
            if (obisCode.equals(item.getObisCode())) {
                return item;
            }
        }
        return null;
    }
}
