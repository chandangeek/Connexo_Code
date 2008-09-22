package com.energyict.dlms.client;

import java.util.List;

import com.energyict.dlms.axrdencoding.AbstractDataType;

public class CosemAPDU {

	int cosemAPDUService;
	int invokeAndPriority;
	CosemAttributeDescriptor cosemAttributeDescriptor;
	AbstractDataType dataType;
	
	public CosemAPDU(int cosemAPDUService, int invokeAndPriority,
			CosemAttributeDescriptor cosemAttributeDescriptor,
			AbstractDataType dataType) {
		this.cosemAPDUService = cosemAPDUService;
		this.invokeAndPriority = invokeAndPriority;
		this.cosemAttributeDescriptor = cosemAttributeDescriptor;
		this.dataType = dataType;
	}

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CosemAPDU:\n");
        strBuff.append("   cosemAPDUService=0x"+Integer.toHexString(getCosemAPDUService())+"\n");
        strBuff.append("   cosemAttributeDescriptor="+getCosemAttributeDescriptor()+"\n");
        strBuff.append("   dataType="+getDataType()+"\n");
        strBuff.append("   invokeAndPriority=0x"+Integer.toHexString(getInvokeAndPriority())+"\n");
        return strBuff.toString();
    }
	
	public int getCosemAPDUService() {
		return cosemAPDUService;
	}

	public int getInvokeAndPriority() {
		return invokeAndPriority;
	}

	public CosemAttributeDescriptor getCosemAttributeDescriptor() {
		return cosemAttributeDescriptor;
	}

	public AbstractDataType getDataType() {
		return dataType;
	}
	

	
	
	
}
