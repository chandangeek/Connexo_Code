/*
 * CapturedObject.java
 *
 * Created on 18 augustus 2004, 9:17
 */

package com.energyict.dlms.cosem;


import com.energyict.obis.ObisCode;

/**
 * @author Koen
 */
public class CapturedObject {

    private int classId;
    private LogicalName logicalName;
    private int attributeIndex;
    private int dataIndex;

    /**
     * Creates a new instance of CapturedObject
     */
    public CapturedObject(int classId, LogicalName logicalName, int attributeIndex, int dataIndex) {
        this.classId = classId;
        this.logicalName = logicalName;
        this.attributeIndex = attributeIndex;
        this.dataIndex = dataIndex;
    }

    public ObisCode getObisCode() {
        return logicalName.getObisCode();
    }

    public String toString() {
        return "classId=" + getClassId() + ", logicalName=" + getLogicalName() + ", attributeIndex=" + getAttributeIndex() + ", dataIndex=" + getDataIndex();
    }

    /**
     * Getter for property classId.
     *
     * @return Value of property classId.
     */
    public int getClassId() {
        return classId;
    }

    /**
     * Getter for property logicalName.
     *
     * @return Value of property logicalName.
     */
    public LogicalName getLogicalName() {
        return logicalName;
    }

    /**
     * Getter for property attributeIndex.
     *
     * @return Value of property attributeIndex.
     */
    public int getAttributeIndex() {
        return attributeIndex;
    }

    /**
     * Getter for property dataIndex.
     *
     * @return Value of property dataIndex.
     */
    public int getDataIndex() {
        return dataIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CapturedObject that = (CapturedObject) o;

        if (classId != that.classId) return false;
        if (attributeIndex != that.attributeIndex) return false;
        if (dataIndex != that.dataIndex) return false;
        return !(logicalName != null ? !logicalName.equals(that.logicalName) : that.logicalName != null);

}

    @Override
    public int hashCode() {
        int result = classId;
        result = 31 * result + (logicalName != null ? logicalName.hashCode() : 0);
        result = 31 * result + attributeIndex;
        result = 31 * result + dataIndex;
        return result;
    }
}