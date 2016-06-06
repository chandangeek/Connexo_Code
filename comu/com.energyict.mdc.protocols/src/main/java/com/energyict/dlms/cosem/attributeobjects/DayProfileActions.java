package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;

import java.util.ArrayList;

/**
 * Object to describe a DayProfileAction in an ActivityCalendar
 */
public class DayProfileActions extends Structure {

    /** The dataType index of the {@link #startTime} */
    private static final int indexStartTime = 0;
    /** The dataType index of the {@link #scriptLogicalName} */
    private static final int indexScriptLogicalName = 1;
    /** The dataType index of the {@link #scriptSelector} */
    private static final int indexScriptSelector = 2;

    /** The startTime of the current {@link com.energyict.dlms.cosem.attributeobjects.DayProfileActions} */
	private OctetString startTime = null;
    /** The scriptLogicalName of the current {@link com.energyict.dlms.cosem.attributeobjects.DayProfileActions} */
	private OctetString scriptLogicalName = null;
    /** The scriptSelector of the current {@link com.energyict.dlms.cosem.attributeobjects.DayProfileActions} */
	private Unsigned16 scriptSelector = null;

	public DayProfileActions(){
		super();
        addDataType(startTime);
        addDataType(scriptLogicalName);
        addDataType(scriptSelector);
	}

    /**
	 * @return the BER encoded structure.
	 * @throws IllegalArgumentException when not all necessary dayProfileAction fields are written
	 */
	protected byte[] doGetBEREncodedByteArray() {
		if ((getStartTime() == null) || (getScriptSelector() == null) || (getScriptLogicalName() == null)) {
			throw new IllegalArgumentException("Some of the dayProfileAction fields are empty.");
		}
		this.dataTypes = new ArrayList<>();
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
        setDataType(indexStartTime, startTime);
	}

	/**
	 * @param scriptLogicalName the scriptLogicalName to set
	 */
	public void setScriptLogicalName(OctetString scriptLogicalName) {
		this.scriptLogicalName = scriptLogicalName;
        setDataType(indexScriptLogicalName, scriptLogicalName);
	}

	/**
	 * @param scriptSelector the scriptSelector to set
	 */
	public void setScriptSelector(Unsigned16 scriptSelector) {
		this.scriptSelector = scriptSelector;
        setDataType(indexScriptSelector, scriptSelector);
	}

}