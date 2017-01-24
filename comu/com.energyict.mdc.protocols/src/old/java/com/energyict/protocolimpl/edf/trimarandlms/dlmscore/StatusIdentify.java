/*
 * StatusIdentify.java
 *
 * Created on 23 februari 2007, 13:07
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.dlmscore;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class StatusIdentify {

    private String resources;
    private String vendorName;
    private String model;
    private int versionNr;
    int size;

    /** Creates a new instance of StatusIdentify */
    public StatusIdentify(byte[] data, int offset) throws IOException {
        size = offset;
        int length = ProtocolUtils.getInt(data,offset++,1);
        setResources(new String(ProtocolUtils.getSubArray2(data, offset, length)));
        offset+=length;
        length = ProtocolUtils.getInt(data,offset++,1);
        setVendorName(new String(ProtocolUtils.getSubArray2(data, offset, length)));
        offset+=length;
        length = ProtocolUtils.getInt(data,offset++,1);
        setModel(new String(ProtocolUtils.getSubArray2(data, offset, length)));
        offset+=length;
        setVersionNr(ProtocolUtils.getInt(data,offset++,1));
        offset++;
        size = offset-size;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("model="+getModel()+", ");
        strBuff.append("resources="+getResources()+", ");
        strBuff.append("vendorName="+getVendorName()+", ");
        strBuff.append("versionNr="+getVersionNr()+"\n");
        return strBuff.toString();
    }

    public int getSize() {
        return size;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getVersionNr() {
        return versionNr;
    }

    public void setVersionNr(int versionNr) {
        this.versionNr = versionNr;
    }
}
