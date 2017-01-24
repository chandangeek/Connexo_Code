/*
 * DeviceObject.java
 *
 * Created on 21 september 2005, 11:19
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.functioncode;

/**
 *
 * @author Koen
 */
public class DeviceObject {
    private int id;
    private int length;
    private byte[] data;
    private String str;
    /** Creates a new instance of DeviceObject */
    public DeviceObject(int id, int length, byte[] data) {
        this.setId(id);
        this.setLength(length);
        this.setData(data);
        this.setStr(new String(getData()));
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DeviceObject:\n");
        strBuff.append("id="+id+", length="+length+", data=");
        for (int i=0;i<data.length;i++) {
            strBuff.append("0x"+Integer.toHexString((int)data[i]&0xFF)+" ");
        }
        strBuff.append("\n");
        return strBuff.toString();
    }   
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }
    
}
