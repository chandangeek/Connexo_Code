package com.energyict.mdc.protocol.inbound.idis;

import com.energyict.obis.ObisCode;

/**
 * Created by cisac on 7/14/2016.
 */
public class T210DPushObjectListEntry {

    long classId;
    ObisCode obisCode;
    int attributeNr;
    long element3;

    public T210DPushObjectListEntry(long classId, ObisCode obisCode, int attributeNr, long element3) {
        this.classId = classId;
        this.obisCode = obisCode;
        this.attributeNr = attributeNr;
        this.element3 = element3;
    }

    public long getClassId() {
        return classId;
    }

    public void setClassId(long classId) {
        this.classId = classId;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    public int getAttributeNr() {
        return attributeNr;
    }

    public void setAttributeNr(int attributeNr) {
        this.attributeNr = attributeNr;
    }

    public long getElement3() {
        return element3;
    }

    public void setElement3(long element3) {
        this.element3 = element3;
    }


}
