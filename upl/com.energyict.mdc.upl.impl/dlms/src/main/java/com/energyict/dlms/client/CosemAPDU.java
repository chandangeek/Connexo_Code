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
        strBuff.append("CosemAPDU:");
        strBuff.append(" cosemAPDUService=0x"+Integer.toHexString(getCosemAPDUService()));
        strBuff.append(" "+getCosemAttributeDescriptor());
        strBuff.append(" invokeAndPriority=0x"+Integer.toHexString(getInvokeAndPriority()));
        strBuff.append(" dataType="+getDataType());
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
