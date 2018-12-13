/**
 *
 */
package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;

import java.io.IOException;
import java.util.Date;

/**
 * A seasonProfile is defined by their starting date and a specific weekProfile to be executed.
 */
public class SeasonProfiles extends Structure implements Comparable {

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

	public String getSeasonId(){
		return new String(getSeasonProfileName().getOctetStr());
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

    public Date getSeasonStartDate() {
        try {
            AXDRDateTime axdrDateTime = new AXDRDateTime(this.getSeasonStart(), AXDRDateTimeDeviationType.Negative);
            return axdrDateTime.getValue().getTime();
        } catch (IOException e) {
            return new Date(0);
        }
    }

    /**
     * Implement comparable to be able to sort the season profile entries
     */
    public int compareTo(Object o) {
        if (o instanceof SeasonProfiles) {
            SeasonProfiles otherSeasonProfile = ((SeasonProfiles) o);
            if (this.getSeasonStartDate().before(otherSeasonProfile.getSeasonStartDate())) {
                return -1;
            } else if (this.getSeasonStartDate().after(otherSeasonProfile.getSeasonStartDate())) {
                return 1;
            }
        }
        return 0;
    }
}