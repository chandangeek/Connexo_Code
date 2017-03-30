/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.Array;

import java.io.IOException;
import java.util.List;

public class ObjectList extends Array {

    public ObjectList(byte[] berEncodedData, int offset) throws IOException {
        super(berEncodedData, offset, 0);
    }

    public ObjectList(List<ObjectListItem> objectListItems) throws IOException {
        for (ObjectListItem item : objectListItems) {
            addDataType(item);
        }
    }

    public int getNumberOfObjects() {
        return nrOfDataTypes();
    }

}
