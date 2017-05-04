package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class BatchObisCodeReader {

	List<ObjectInfo> objectInfos = new ArrayList<ObjectInfo>();
	AbstractDLMS abstractDLMS;
	TransparentObjectListRead transparentObjectListRead=null;

	public BatchObisCodeReader(AbstractDLMS abstractDLMS) {
		this.abstractDLMS=abstractDLMS;
	}

	public void add(int attribute, ObisCode obisCode) throws NoSuchRegisterException {
		objectInfos.add(new ObjectInfo(attribute,abstractDLMS.findObjectByObiscode(obisCode).getClassId(),obisCode));
	}

	public void add(int attribute, String description) throws NoSuchRegisterException {
		Entry<ObisCode,ObjectEntry> entry = abstractDLMS.findEntryByDescription(description);
		objectInfos.add(new ObjectInfo(attribute,entry.getValue().getClassId(),entry.getKey()));
	}

	public int intValue(ObisCode obisCode) {
		return transparentObjectListRead.getRegisterValues().get(obisCode).getQuantity().getAmount().intValue();
	}

	public int intValue(String description) throws NoSuchRegisterException {
		Entry<ObisCode,ObjectEntry> entry = abstractDLMS.findEntryByDescription(description);
		return transparentObjectListRead.getRegisterValues().get(entry.getKey()).getQuantity().getAmount().intValue();
	}

	public void invoke() throws IOException {
		if (objectInfos.size()>0) {
			transparentObjectListRead = new TransparentObjectListRead(abstractDLMS,objectInfos);
			transparentObjectListRead.read();
			objectInfos.clear();
		}
	}



}
