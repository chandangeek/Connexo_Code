/*
 * GenericWrite.java
 *
 * Created on 30 augustus 2004, 15:21
 */

package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class GenericWrite extends AbstractCosemObject {
    public final int DEBUG=0;

    int attr;

    /** Creates a new instance of GenericRead */
    public GenericWrite(ProtocolLink protocolLink, ObjectReference objectReference, int attr) {
        super(protocolLink,objectReference);
        this.attr=attr;
    }

    public void write(byte[] data) throws IOException {
        write(attr,data);
    }

    public int getAttr() {
        return attr;
    }

    protected int getClassId() {
        return getObjectReference().getClassId();
    }

}
