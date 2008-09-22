package com.energyict.dlms.client;

import java.io.IOException;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;

public class CosemAttributeDescriptor {
	
	private int classId;
	private ObisCode obis;
	private int attributeId;

	
	public CosemAttributeDescriptor(int classId, ObisCode obis, int attributeId) {
		this.classId = classId;
		this.obis = obis;
		this.attributeId = attributeId;
	}
	
	public CosemAttributeDescriptor(byte[] data, int offset) throws IOException {
		classId = ProtocolUtils.getInt(data, offset, 2);
		offset+=2;
		obis = new ObisCode(ProtocolUtils.getInt(data, offset++,1),ProtocolUtils.getInt(data, offset++,1),ProtocolUtils.getInt(data, offset++,1),ProtocolUtils.getInt(data, offset++,1),ProtocolUtils.getInt(data, offset++,1),ProtocolUtils.getInt(data, offset++,1));
		attributeId = ProtocolUtils.getInt(data, offset++,1);
	}
	
	static public int size() {
		return 9;
	}
//    public static void main(String[] args) {
//        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new CosemAttributeDescriptor()));
//    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CosemAttributeDescriptor:\n");
        strBuff.append("   attributeId="+getAttributeId()+"\n");
        strBuff.append("   classId="+getClassId()+"\n");
        strBuff.append("   obis="+getObis()+"\n");
        return strBuff.toString();
    }	
	public int getClassId() {
		return classId;
	}
	public ObisCode getObis() {
		return obis;
	}
	public int getAttributeId() {
		return attributeId;
	}
	
}
