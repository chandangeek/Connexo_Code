package com.energyict.genericprotocolimpl.lgadvantis;

import java.io.IOException;
import java.util.List;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.genericprotocolimpl.lgadvantis.collector.Collector;
import com.energyict.genericprotocolimpl.lgadvantis.encoder.Encoder;
import com.energyict.obis.ObisCode;

/** Basic interface implemented by all CosemObjects */

public interface Cosem {

	int getShortName();

	List getPrimitiveShortNameList();

	ObisCode getObisCode();

	/* DDispatch */
	List addMeTo(Task task, boolean write);

	/* DDispatch */
	void addMeTo(CosemObject cosemObject);

	Collector getCollector();

	Cosem setCollector(Collector collector);

	Encoder getEncoder();

	Cosem setEncoder(Encoder convertor);

	void parse(AbstractDataType dataType, Task task) throws IOException;

	void parse(byte[] binaryData, Task task) throws IOException;
}
