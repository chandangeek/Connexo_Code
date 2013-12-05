package com.energyict.mdc.protocol.api;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * Straightforward ValueObject representing a meters's LoadProfile to read.
 * </p>
 * <p/>
 * The 'Date' strategy:<br>
 * <li>If the startDate is null, then we will read data from up to a month ago (current Date - 1 month)
 * <li>If the endDate is null, then we will use the current Date
 */
public class LoadProfileReader {

    private static final long SECONDS_IN_DAY = 86400L;
    private static final long MILLISECONDS_IN_SECOND = 1000L;
    private static final long DAYS_IN_MONTH = 30L;

    /**
     * Holds the <CODE>ObisCode</CODE> from the <CODE>LoadProfile</CODE> to read
     */
    private final ObisCode profileObisCode;
    /**
     * Holds the serialNumber of the meter for this LoadProfile
     */
    private final String meterSerialNumber;

    /**
     * Holds the Date from where to start fetching data from the <CODE>LoadProfile</CODE>
     */
    private final Date startReadingTime;

    /**
     * Holds the Date from the <i>LAST</i> interval to fetch in the <CODE>LoadProfile</CODE>
     */
    private final Date endReadingTime;

    /**
     * Represents the database ID of the LoadProfile to read.
     * We will need this to set in the ProfileData object.
     */
    private final int loadProfileId;

    /**
     * Contains a <CODE>List</CODE> of <b>necessary</b> channels to read from the meter.
     */
    private final List<ChannelInfo> channelInfos;


    /**
     * Default constructor
     *
     * @param profileObisCode   the <CODE>ObisCode</CODE> from the <CODE>LoadProfile</CODE>
     * @param startReadingTime  the readTime to start reading the <CODE>LoadProfile</CODE>
     * @param endReadingTime    the endTime of the last <CODE>LoadProfile</CODE> interval
     * @param meterSerialNumber the serialNumber of the meter for which this <CODE>ObisCode</CODE> and <CODE>LoadProfile</CODE> is mapped
     * @param channelInfos      the <CODE>List</CODE> of <CODE>ChannelInfo</CODE> representing the channels to read from the profile in the meter
     */
    public LoadProfileReader(ObisCode profileObisCode, Date startReadingTime, Date endReadingTime, int loadProfileId, String meterSerialNumber, List<ChannelInfo> channelInfos) {
        this.profileObisCode = profileObisCode;
        if (endReadingTime == null) {
            this.endReadingTime = new Date();
        } else {
            this.endReadingTime = endReadingTime;
        }
        if (startReadingTime == null) {
            this.startReadingTime = new Date(this.endReadingTime.getTime() - (DAYS_IN_MONTH * SECONDS_IN_DAY * MILLISECONDS_IN_SECOND));    //endTime - 1 month
        } else {
            this.startReadingTime = startReadingTime;
        }
        this.loadProfileId = loadProfileId;
        this.meterSerialNumber = meterSerialNumber;
        this.channelInfos = channelInfos;
    }

    /**
     * Getter for the {@link #startReadingTime}
     *
     * @return the {@link #startReadingTime}
     */
    public Date getStartReadingTime() {
        return startReadingTime;
    }

    /**
     * Getter for the {@link #profileObisCode}
     *
     * @return the {@link #profileObisCode}
     */
    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    /**
     * Getter for the {@link #meterSerialNumber}
     *
     * @return the {@link #meterSerialNumber}
     */
    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    /**
     * Getter for the {@link #endReadingTime}
     *
     * @return the {@link #endReadingTime}
     */
    public Date getEndReadingTime() {
        return endReadingTime;
    }

    /**
     * Getter for the {@link #loadProfileId}
     *
     * @return the {@link #loadProfileId}
     */
    public int getLoadProfileId() {
        return loadProfileId;
    }

    /**
     * Getter for the {@link #channelInfos}
     *
     * @return the {@link #channelInfos}
     */
    public List<ChannelInfo> getChannelInfos() {
        return channelInfos;
    }

    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p/>
     * The <code>toString</code> method for class <code>Object</code>
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `<code>@</code>', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ObisCode : ");
        builder.append(getProfileObisCode());
        builder.append(" for meter : ");
        builder.append(getMeterSerialNumber());
        return builder.toString();
    }
}
