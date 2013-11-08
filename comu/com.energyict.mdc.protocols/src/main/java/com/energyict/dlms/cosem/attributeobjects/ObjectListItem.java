package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.Structure;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 18/08/11
 * Time: 13:38
 */
public class ObjectListItem extends Structure {

    public ObjectListItem() {
    }

    public ObjectListItem(byte[] berEncodedByteArray, int offset) throws IOException {
        super(berEncodedByteArray, offset, 0);
    }
}
