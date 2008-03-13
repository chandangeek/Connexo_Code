package com.energyict.genericprotocolimpl.lgadvantis;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.genericprotocolimpl.lgadvantis.collector.Collector;
import com.energyict.genericprotocolimpl.lgadvantis.encoder.Encoder;
import com.energyict.genericprotocolimpl.lgadvantis.parser.Parser;
import com.energyict.genericprotocolimpl.lgadvantis.parser.ProfileParser;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.List;

public abstract class AbstractCosem implements Cosem {

	private ObisCode obisCode;
	private int shortName;

	private Parser parser;
	private Collector collector;
	private Encoder encoder;

	public abstract List addMeTo(Task task, boolean write);
	public abstract void addMeTo(CosemObject cosemObject);

	public Collector getCollector() {
		return collector;
	}

	public Encoder getEncoder() {
		return encoder;
	}

	public ObisCode getObisCode() {
		return obisCode;
	}

	Cosem setObisCode( ObisCode obisCode ) {
		this.obisCode = obisCode;
		return this;
	}

	Cosem setObisCode( String obisCode ) {
		ObisCode oc = ( obisCode != null ) ? ObisCode.fromString(obisCode) : null;
		return this.setObisCode( oc );
	}

	Cosem setShortName( int shortName ) {
		this.shortName = shortName;
		return this;
	}

	public int getShortName() {
		return shortName;
	}

	public Cosem setCollector(Collector collector) {
		this.collector = collector;
		return this;
	}

	public Cosem setEncoder(Encoder encoder) {
		this.encoder = encoder;
		return this;
	}

	Parser getParser() {
		return parser;
	}

	Cosem setParser(Parser parser) {
		this.parser = parser;
		return this;
	}

	/** Interprete my data */
	public void parse(AbstractDataType dataType, Task task) throws IOException {
		getParser().parse(dataType, task);  
	}

	public void parse(byte[] binaryData, Task task) throws IOException {
		Parser parser= getParser();
		if (parser instanceof ProfileParser){
			ProfileParser pParser = (ProfileParser) parser;
			pParser.parse(binaryData, task);
		} else {
			throw new IOException("Cannot parse binary data");
		}
	}

}
