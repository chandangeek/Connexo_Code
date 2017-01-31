/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @author kvds
 */
public class DataFieldCoding {


    static public final int TYPE_BINARY=0;
    static public final int TYPE_BCD=1;
    static public final int TYPE_REAL=2;
    static public final int TYPE_NODATA=3;
    static public final int TYPE_VARIABLELENGTH=4;
    static public final int TYPE_SPECIALFUNCTIONS=5;


    static List list = new ArrayList();
    static {
        list.add(new DataFieldCoding(0,0, "NoData", TYPE_NODATA));
        list.add(new DataFieldCoding(1,1, "8 bit integer/binary", TYPE_BINARY));
        list.add(new DataFieldCoding(2,2, "16 bit integer/binary", TYPE_BINARY));
        list.add(new DataFieldCoding(3,3, "24 bit integer/binary", TYPE_BINARY));
        list.add(new DataFieldCoding(4,4, "32 bit integer/binary", TYPE_BINARY));
        list.add(new DataFieldCoding(5,4, "32 bit real", TYPE_REAL));
        list.add(new DataFieldCoding(6,6, "48 bit integer/binary", TYPE_BINARY));
        list.add(new DataFieldCoding(7,8, "64 bit integer/binary", TYPE_BINARY));
        list.add(new DataFieldCoding(8,0, "selection for readout", TYPE_NODATA));
        list.add(new DataFieldCoding(9,1, "2 digit BCD", TYPE_BCD));
        list.add(new DataFieldCoding(10,2, "4 digit BCD", TYPE_BCD));
        list.add(new DataFieldCoding(11,3, "6 digit BCD", TYPE_BCD));
        list.add(new DataFieldCoding(12,4, "8 digit BCD", TYPE_BCD));
        list.add(new DataFieldCoding(13,1, "variable length", TYPE_VARIABLELENGTH));
        list.add(new DataFieldCoding(14,6, "12 digit BCD", TYPE_BCD));
        list.add(new DataFieldCoding(15,8, "special functions", TYPE_SPECIALFUNCTIONS));
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

    public boolean isTYPE_NODATA() {
        return getType()==TYPE_NODATA;
    }

    public boolean isTYPE_SPECIALFUNCTIONS() {
        return getType()==TYPE_SPECIALFUNCTIONS;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DataFieldCoding:\n");
        strBuff.append("   description="+getDescription()+"\n");
        strBuff.append("   id="+getId()+"\n");
        strBuff.append("   lengthInBytes="+getLengthInBytes()+"\n");
        strBuff.append("   type="+getType()+"\n");
        return strBuff.toString();
    }

    static public DataFieldCoding findDataFieldCoding(int id) throws IOException {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            DataFieldCoding dfc = (DataFieldCoding)it.next();
            if (dfc.getId() == id)
                return dfc;
        }
        throw new IOException("DataFieldCoding, invalid id "+id);
    }

    public int getLengthInBytes() {
        return lengthInBytes;
    }

    public void setLengthInBytes(int lengthInBytes) {
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
