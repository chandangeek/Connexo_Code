package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;

public class BatchObisCodeReader {

	List<ObjectInfo> objectInfos = new ArrayList<ObjectInfo>();
	AbstractDLMS abstractDLMS;
	TransparentObjectListRead transparentObjectListRead=null;
	
	public BatchObisCodeReader(AbstractDLMS abstractDLMS) {
		this.abstractDLMS=abstractDLMS;
	}
	
	public void add(int attribute, ObisCode obisCode) throws NoSuchRegisterException {
		objectInfos.add(new ObjectInfo(attribute,AbstractDLMS.findObjectByObiscode(obisCode).getClassId(),obisCode));
	}
	
	public void add(int attribute, String description) throws NoSuchRegisterException {
		Entry<ObisCode,ObjectEntry> entry = AbstractDLMS.findEntryByDescription(description);
		objectInfos.add(new ObjectInfo(attribute,entry.getValue().getClassId(),entry.getKey()));
	}

	public int intValue(ObisCode obisCode) {
		return transparentObjectListRead.getRegisterValues().get(obisCode).getQuantity().getAmount().intValue();
	}
	
	public int intValue(String description) throws NoSuchRegisterException {
		Entry<ObisCode,ObjectEntry> entry = AbstractDLMS.findEntryByDescription(description);
		return transparentObjectListRead.getRegisterValues().get(entry.getKey()).getQuantity().getAmount().intValue();
	}
	
	public void invoke() throws IOException {
		transparentObjectListRead = new TransparentObjectListRead(abstractDLMS,objectInfos);
		transparentObjectListRead.read();
		objectInfos.clear();
	}
	
	

}
