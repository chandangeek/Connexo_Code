package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.obis.ObisCode;

/**
 * Event log Item from Device type manager IC (class_id = 20015, version = 1, logical_name = 0.187.96.171.0.255)
 */
public class EventLogItem extends Item {
    public final static Unsigned32 DEFAULT_BUFFER_SIZE = new Unsigned32(864000L); //10 days in seconds
    Unsigned32 bufferSize;

    public EventLogItem(ObisCode obisCode) {
        this.obisCode = obisCode;
        this.bufferSize = DEFAULT_BUFFER_SIZE;
    }

    public EventLogItem(ObisCode obisCode, Unsigned32 bufferSize) {
        this.obisCode = obisCode;
        this.bufferSize = bufferSize;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    public Unsigned32 getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(Unsigned32 bufferSize) {
        this.bufferSize = bufferSize;
    }

    public Structure toStructure(){
        final Structure structure = new Structure();
        structure.addDataType(new OctetString(obisCode.getLN()));
        structure.addDataType(bufferSize);
        return structure;
    }
}
