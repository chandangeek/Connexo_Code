/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * GenericRead.java
 *
 * Created on 30 augustus 2004, 14:27
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.OctetString;
import com.energyict.dlms.ProtocolLink;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class GenericRead extends AbstractCosemObject {
    public final int DEBUG=0;

    int attr;

    /** Creates a new instance of GenericRead */
    public GenericRead(ProtocolLink protocolLink, ObjectReference objectReference, int attr) {
        super(protocolLink,objectReference);
        this.attr=attr;
    }

    public String toString() {
        try {
           return "value="+getDataContainer().toString();
        }
        catch(IOException e) {
           return "DataGeneric retrieving error!";
        }
    }

    public long getValue() throws IOException {
        DataContainer dataContainer=getDataContainer();
        if (dataContainer.getRoot().isInteger(0)) {
           return (long)((Integer)dataContainer.getRoot().getElement(0)).intValue();
        }
        throw new ProtocolException("DataGeneric, getValue(), invalid data value type...");
    }

    public String getString() throws IOException {
        DataContainer dataContainer=getDataContainer();
        if (dataContainer.getRoot().isOctetString(0)) {
           return ((OctetString)dataContainer.getRoot().getElement(0)).toString().trim();
        }
        else if (dataContainer.getRoot().isString(0)) {
           return ((String)dataContainer.getRoot().getElement(0)).trim();
        }
        throw new ProtocolException("DataGeneric, getString(), invalid data value type...");
    }

    public DataContainer getDataContainer() throws IOException {
        DataContainer dataContainer = new DataContainer();
        dataContainer.parseObjectList(getResponseData(attr),protocolLink.getLogger());
        if (DEBUG >= 1) {
			dataContainer.printDataContainer();
		}
        return dataContainer;
    }

    public byte[] getResponseData() throws IOException {
        return getResponseData(attr);
    }

    protected int getClassId() {
        return getObjectReference().getClassId();
    }

}
