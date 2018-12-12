/*
 * DataFieldCoding.java
 *
 * Created on 3 oktober 2007, 17:07
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import com.energyict.mdc.upl.ProtocolException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author kvds
 */
class DataFieldCoding {

    static final int TYPE_BINARY=0;
    static final int TYPE_BCD=1;
    static final int TYPE_REAL=2;
    private static final int TYPE_NODATA=3;
    static final int TYPE_VARIABLELENGTH=4;
    static final int TYPE_SPECIALFUNCTIONS=5;

    enum DataFieldCodingType {
        TYPE_BINARY(0),
        TYPE_BCD(1),
        TYPE_REAL(2),
        TYPE_NODATA(3),
        TYPE_VARIABLELENGTH(4),
        TYPE_SPECIALFUNCTIONS(5),
        UNKNOWN(-1);

        /** Id of the type */
        private final int id;

        /** Returns the id of the type */
        public int getId() {
            return id;
        }

        /**
         * Creates an instance of DataFieldCodingType
         * @param id    The idf of the type
         */
        DataFieldCodingType(final int id) {
            this.id = id;
        }

        /**
         * Returns the DataFieldCodingType for the given id
         * @param id    the lookup value
         * @return      the DataFieldCodingType for the given id
         */
        public static DataFieldCodingType fromId(final int id) {
            for (final DataFieldCodingType type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }

            return UNKNOWN;
        }
    }

    static final List<DataFieldCoding> INSTANCES = new ArrayList<>();
    static {
        INSTANCES.add(new DataFieldCoding(0,0, "NoData", TYPE_NODATA));
        INSTANCES.add(new DataFieldCoding(1,1, "8 bit integer/binary", TYPE_BINARY));
        INSTANCES.add(new DataFieldCoding(2,2, "16 bit integer/binary", TYPE_BINARY));
        INSTANCES.add(new DataFieldCoding(3,3, "24 bit integer/binary", TYPE_BINARY));
        INSTANCES.add(new DataFieldCoding(4,4, "32 bit integer/binary", TYPE_BINARY));
        INSTANCES.add(new DataFieldCoding(5,4, "32 bit real", TYPE_REAL));
        INSTANCES.add(new DataFieldCoding(6,6, "48 bit integer/binary", TYPE_BINARY));
        INSTANCES.add(new DataFieldCoding(7,8, "64 bit integer/binary", TYPE_BINARY));
        INSTANCES.add(new DataFieldCoding(8,0, "selection for readout", TYPE_NODATA));
        INSTANCES.add(new DataFieldCoding(9,1, "2 digit BCD", TYPE_BCD));
        INSTANCES.add(new DataFieldCoding(10,2, "4 digit BCD", TYPE_BCD));
        INSTANCES.add(new DataFieldCoding(11,3, "6 digit BCD", TYPE_BCD));
        INSTANCES.add(new DataFieldCoding(12,4, "8 digit BCD", TYPE_BCD));
        INSTANCES.add(new DataFieldCoding(13,1, "variable length", TYPE_VARIABLELENGTH));
        INSTANCES.add(new DataFieldCoding(14,6, "12 digit BCD", TYPE_BCD));
        INSTANCES.add(new DataFieldCoding(15,8, "special functions", TYPE_SPECIALFUNCTIONS));
    }

    private int lengthInBytes;
    private String description;
    private int type;
    private int id;

    /** Creates a new instance of DataFieldCoding */
    private DataFieldCoding(int id, int lengthInBytes, String description, int type) {
        this.setLengthInBytes(lengthInBytes);
        this.setDescription(description);
        this.setType(type);
        this.setId(id);
    }

    boolean isTYPE_NODATA() {
        return getType()==TYPE_NODATA;
    }

    boolean isTYPE_SPECIALFUNCTIONS() {
        return getType()==TYPE_SPECIALFUNCTIONS;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        return "DataFieldCoding:\n" +
                "   description=" + getDescription() + "\n" +
                "   id=" + getId() + "\n" +
                "   lengthInBytes=" + getLengthInBytes() + "\n" +
                "   type=" + getType() + "\n";
    }

    static DataFieldCoding findDataFieldCoding(int id) throws IOException {
        for (DataFieldCoding dfc : INSTANCES) {
            if (dfc.getId() == id) {
                return dfc;
            }
        }
        throw new ProtocolException("DataFieldCoding, invalid id "+id);
    }

    int getLengthInBytes() {
        return lengthInBytes;
    }

    private void setLengthInBytes(int lengthInBytes) {
        this.lengthInBytes = lengthInBytes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
