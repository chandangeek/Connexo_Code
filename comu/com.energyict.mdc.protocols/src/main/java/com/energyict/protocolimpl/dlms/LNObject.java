package com.energyict.protocolimpl.dlms;

/**
 * @deprecated As of 11022009, replaced by
 * {@link com.energyict.dlms.cosem.CapturedObject}
 */
public class LNObject {

    int classId;
    int attribute;
    byte[] logicalName;
    byte type;
    int channelId;

    public LNObject(int classId, byte[] logicalName, int attribute, byte type,int channelId)
    {
       this.classId = classId;
       this.attribute = attribute;
       this.type = type;
       this.logicalName = (byte[])logicalName.clone();
       this.channelId = channelId;
    }

	public int getClassId() {
		return classId;
	}

	public int getAttribute() {
		return attribute;
	}

	public byte[] getLogicalName() {
		return logicalName;
	}

	public byte getType() {
		return type;
	}

	public int getChannelId() {
		return channelId;
	}

}
