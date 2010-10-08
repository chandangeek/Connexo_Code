/**
 *
 */
package com.energyict.dlms.cosem.attributeobjects;

import java.io.IOException;
import java.util.ArrayList;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;

/**
 * A seasonProfile is defined by their starting date and a specific weekProfile to be executed
 */
public class SeasonProfiles extends Structure{

    /** The dataType index of the {@link #seasonProfileName} */
    private static final int indexSeasonProfileName = 0;
    /** The dataType index of the {@link #seasonStart} */
    private static final int indexSeasonStart = 1;
    /** The dataType index of the {@link #weekName} */
    private static final int indexWeekName = 2;

    /** The seasonProfileName of the current {@link com.energyict.dlms.cosem.attributeobjects.SeasonProfiles} */
	private OctetString seasonProfileName = null;
    /** The seasonStart time of the current {@link com.energyict.dlms.cosem.attributeobjects.SeasonProfiles} */
	private OctetString seasonStart = null;
    /** The weekname for the current {@link com.energyict.dlms.cosem.attributeobjects.SeasonProfiles} */
	private OctetString weekName = null;

	public SeasonProfiles(){
		super();
        addDataType(seasonProfileName);
        addDataType(seasonStart);
        addDataType(weekName);
	}

    public SeasonProfiles(byte[] berEncodedData, int offset, int level) throws IOException {
        super(berEncodedData, offset, level);
        this.seasonProfileName = (OctetString) getDataType(indexSeasonProfileName);
        this.seasonStart = (OctetString) getDataType(indexSeasonStart);
        this.weekName = (OctetString) getDataType(indexWeekName);
    }

    /**
	 * @return the BER encoded structure.
	 * @throws IllegalArgumentException when not all necessary seasonProfile fields are written
	 */
	protected byte[] doGetBEREncodedByteArray() {
        
		if ((getSeasonProfileName() == null) || (getSeasonStart() == null) || (getWeekName() == null)) {
			throw new IllegalArgumentException("Some of the seasonProfile fields are empty.");
		}
//		dataTypes = new ArrayList();
//		addDataType(getSeasonProfileName());
//		addDataType(getSeasonStart());
//		addDataType(getWeekName());
		return super.doGetBEREncodedByteArray();
	}

	/**
	 * @return the seasonProfileName
	 */
	public OctetString getSeasonProfileName() {
		return seasonProfileName;
	}

	public int getSeasonId(){
		return Integer.parseInt(new String(getSeasonProfileName().getOctetStr()));
	}

	/**
	 * @return the seasonStart
	 */
	public OctetString getSeasonStart() {
		return seasonStart;
	}

	/**
	 * @return the weekName
	 */
	public OctetString getWeekName() {
		return weekName;
	}

	/**
	 * @param seasonProfileName the seasonProfileName to set
	 */
	public void setSeasonProfileName(OctetString seasonProfileName) {
		this.seasonProfileName = seasonProfileName;
        setDataType(indexSeasonProfileName, seasonProfileName);
	}

	/**
	 * @param seasonStart the seasonStart to set
	 */
	public void setSeasonStart(OctetString seasonStart) {
		this.seasonStart = seasonStart;
        setDataType(indexSeasonStart, seasonStart);
	}

	/**
	 * @param weekName the weekName to set
	 */
	public void setWeekName(OctetString weekName) {
		this.weekName = weekName;
        setDataType(indexWeekName, weekName);
	}



}