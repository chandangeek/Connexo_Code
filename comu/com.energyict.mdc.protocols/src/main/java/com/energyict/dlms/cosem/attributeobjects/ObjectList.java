package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.Array;

import java.io.IOException;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 18/08/11
 * Time: 13:19
 */
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
