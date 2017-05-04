/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.Structure;

import java.io.IOException;

public class ObjectListItem extends Structure {

    public ObjectListItem() {
    }

    public ObjectListItem(byte[] berEncodedByteArray, int offset) throws IOException {
        super(berEncodedByteArray, offset, 0);
    }
}
