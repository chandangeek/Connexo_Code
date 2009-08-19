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
		int off = offset;
		this.classId = ProtocolUtils.getInt(data, off, 2);
		off+=2;
		this.obis = new ObisCode(ProtocolUtils.getInt(data, off++,1),ProtocolUtils.getInt(data, off++,1),ProtocolUtils.getInt(data, off++,1),ProtocolUtils.getInt(data, off++,1),ProtocolUtils.getInt(data, off++,1),ProtocolUtils.getInt(data, off++,1));
		this.attributeId = ProtocolUtils.getInt(data, off++,1);
	}

	static public int size() {
		return 9;
	}

	@Override
	public String toString() {
		// Generated code by ToStringBuilder
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("CosemAttributeDescriptor: ");
		strBuff.append(" attributeId=");
		strBuff.append(getAttributeId());
		strBuff.append(" classId=");
		strBuff.append(getClassId());
		strBuff.append(" obis=");
		strBuff.append(getObis());
		return strBuff.toString();
	}

	public int getClassId() {
		return this.classId;
	}

	public ObisCode getObis() {
		return this.obis;
	}

	public int getAttributeId() {
		return this.attributeId;
	}

}
