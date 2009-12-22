/*
 * GenericWrite.java
 *
 * Created on 30 augustus 2004, 15:21
 */

package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
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

    protected int getClassId() {
        return getObjectReference().getClassId();
    }

}
