/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;


import com.energyict.mdc.common.ObisCode;

/**
 * Created by cisac on 8/4/2016.
 */
public class ObjectDefinition {

    private int classId;
    private ObisCode obisCode;
    private int attributeIndex;
    private int dataIndex;

    public ObjectDefinition(int classId, ObisCode obisCode, int attributeIndex, int dataIndex) {
        this.classId = classId;
        this.obisCode = obisCode;
        this.attributeIndex = attributeIndex;
        this.dataIndex = dataIndex;
    }

    public int getClassId() {
        return classId;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public int getAttributeIndex() {
        return attributeIndex;
    }

    public int getDataIndex() {
        return dataIndex;
    }

}
