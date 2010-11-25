package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.ansi.c12.procedures.SetDateTime;

public class TransparantObjectAccessFactory {

	GenericHeader genericHeader;
	
	final GenericHeader getGenericHeader() {
		return genericHeader;
	}

	private final int ATTRIBUTE_VALUE=2;	
	
	private final AbstractDLMS abstractDLMS;

	TransparantObjectAccessFactory(AbstractDLMS abstractDLMS) {
		this.abstractDLMS = abstractDLMS;
	}

	private final void invoke(AbstractTransparentObjectAccess atoa) throws IOException {
		atoa.invoke();
		genericHeader = atoa.getGenericHeader();
	}
	
	final AbstractDataType readObjectValue(ObisCode obisCode) throws IOException {
    	ObjectEntry o = AbstractDLMS.findObjectByObiscode(obisCode);
    	TransparentGet tg = new TransparentGet(abstractDLMS, new ObjectInfo(ATTRIBUTE_VALUE, o.getClassId(),obisCode));
    	invoke(tg);
    	return tg.getDataType();
	}
	
	final AbstractDataType readObjectAttribute(ObisCode obisCode, int attribute) throws IOException {
    	ObjectEntry o = AbstractDLMS.findObjectByObiscode(obisCode);
    	TransparentGet tg = new TransparentGet(abstractDLMS, new ObjectInfo(attribute, o.getClassId(),obisCode));
    	invoke(tg);
    	return tg.getDataType();
	}

	final void writeObjectAttribute(ObisCode obisCode, int attribute, AbstractDataType dataType) throws IOException {
    	ObjectEntry o = AbstractDLMS.findObjectByObiscode(obisCode);
    	TransparentSet ts = new TransparentSet(abstractDLMS, new ObjectInfo(attribute, o.getClassId(),obisCode));
    	ts.setDataType(dataType);
    	invoke(ts);
	}
	
	
}
