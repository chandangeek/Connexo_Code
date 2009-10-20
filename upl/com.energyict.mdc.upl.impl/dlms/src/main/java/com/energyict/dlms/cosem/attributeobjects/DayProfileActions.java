package com.energyict.dlms.cosem.attributeobjects;

import java.io.IOException;
import java.util.ArrayList;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;

public class DayProfileActions extends Structure {

	private OctetString startTime = null;
	private OctetString scriptLogicalName = null;
	private Unsigned16 scriptSelector = null;
	
	public DayProfileActions(){
		super();
	}
	
	/**
	 * @return the BER encoded structure.
	 * @throws IOException when parsing of the structure fails
	 * @throws IllegalArgumentException when not all necessary dayProfileAction fields are written
	 */
	protected byte[] doGetBEREncodedByteArray() throws IOException{ 
		if((getStartTime() == null) || (getScriptSelector() == null) || (getScriptLogicalName() == null)){
			throw new IllegalArgumentException("Some of the dayProfileAction fields are empty.");
		}
		this.dataTypes = new ArrayList();
		addDataType(getStartTime());
		addDataType(getScriptLogicalName());
		addDataType(getScriptSelector());
		return super.doGetBEREncodedByteArray();
	}
	
	
	/**
	 * @return the startTime
	 */
	public OctetString getStartTime() {
		return startTime;
	}

	/**
	 * @return the scriptLogicalName
	 */
	public OctetString getScriptLogicalName() {
		return scriptLogicalName;
	}

	/**
	 * @return the scriptSelector
	 */
	public Unsigned16 getScriptSelector() {
		return scriptSelector;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(OctetString startTime) {
		this.startTime = startTime;
	}

	/**
	 * @param scriptLogicalName the scriptLogicalName to set
	 */
	public void setScriptLogicalName(OctetString scriptLogicalName) {
		this.scriptLogicalName = scriptLogicalName;
	}

	/**
	 * @param scriptSelector the scriptSelector to set
	 */
	public void setScriptSelector(Unsigned16 scriptSelector) {
		this.scriptSelector = scriptSelector;
	}
	
	
}
