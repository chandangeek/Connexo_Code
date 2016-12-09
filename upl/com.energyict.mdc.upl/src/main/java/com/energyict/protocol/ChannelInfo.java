/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocol;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;


/**
 * <p>
 * Contains information about a device logical channel
 *
 * @author Karel
 *         </p>
 */
@XmlRootElement
public class ChannelInfo implements java.io.Serializable {

    private int id;

    /**
     * Logical name of the channel. Use OBIS code from the registerMapping is used.
     */
    private String name;

    /**
     * The <CODE>Unit</CODE> for this channel
     */
    private Unit unit;

    private int channelId;

    /**
     * Indicates the overFlow value for the cumulative channel
     */
    private BigDecimal cumulativeWrapValue = null;

    /**
     * Represents the multiplier for this channel
     */
    private BigDecimal multiplier = BigDecimal.ONE;

    /**
     * Indication whether this channel contains cumulative values
     */
    private boolean cumulative = false;

    /**
     * Identification (serialNumber) of the meter for which this object is created.
     */
    private String meterIdentifier = "";

    /**
     * The MRID of the ReadingType of the <i>channel</i> which will store the collected Data
     */
    private String readingTypeMRID;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private ChannelInfo() {
    }

    /**
     * Constructor
     *
     * @param id   logical channel id (0 based, contrary to previous documentation all protocols use zero base)
     * @param name logical channel name (use OBIS code if device uses OBIS codes)
     * @param unit the logical channel unit
     */
    public ChannelInfo(int id, String name, Unit unit) {
        this.id = id;
        this.channelId = id;
        this.name = name;
        this.unit = unit;
    }

    /**
     * Constructor for <CODE>LoadProfiles</CODE> which contain channels from different meters. The {@link #name} field is used to identify the <CODE>ObisCode</CODE> but
     * some <CODE>ProfileData</CODE may contain multiple channels with the same <CODE>ObisCode</CODE> but from a different Slave meter. The distinction between those channels is
     * made with the {@link #meterIdentifier}
     *
     * @param id                  logical channel id (0 based)
     * @param name                logical channel name (use OBIS code if device uses OBIS codes, 0 based)
     * @param unit                the logical channel unit
     * @param meterIdentification identifier (SerialNumber) of the meter which will provide data for this channel
     * @param readingTypeMRID     the MRID of the ReadingType of the MeasurementType
     */
    public ChannelInfo(int id, String name, Unit unit, String meterIdentification, String readingTypeMRID) {
        this(id, name, unit);
        this.meterIdentifier = meterIdentification;
        this.readingTypeMRID = readingTypeMRID;
    }

    /**
     * Constructor for <CODE>LoadProfiles</CODE> which contain channels from different meters. The {@link #name} field is used to identify the <CODE>ObisCode</CODE> but
     * some <CODE>ProfileData</CODE may contain multiple channels with the same <CODE>ObisCode</CODE> but from a different Slave meter. The distinction between those channels is
     * made with the {@link #meterIdentifier}
     *
     * @param id                  logical channel id (0 based)
     * @param name                logical channel name (use OBIS code if device uses OBIS codes, 0 based)
     * @param unit                the logical channel unit
     * @param meterIdentification identifier (SerialNumber) of the meter which will provide data for this channel
     * @param cumulative          indicates whether the channel is cumulative
     * @param readingTypeMRID     the ReadingType of the MeasurementType
     */
    public ChannelInfo(int id, String name, Unit unit, String meterIdentification, boolean cumulative, String readingTypeMRID) {
        this(id, name, unit);
        this.meterIdentifier = meterIdentification;
        this.cumulative = cumulative;
        this.readingTypeMRID = readingTypeMRID;
    }

    /**
     * Constructor
     *
     * @param id     logical channel id (0 based, contrary to previous documentation all protocols use zero base)
     * @param name   logical channel name (use OBIS code if device uses OBIS codes)
     * @param unit   the logical channel unit
     * @param scaler unit scale
     * @deprecated scaler is obsolete (contained in unit), use {@link #ChannelInfo(int, int, String, com.energyict.cbo.Unit)} instead
     */
    public ChannelInfo(int id, String name, Unit unit, int scaler) {
        this(id, name, unit);
    }

    /**
     * Constructor
     *
     * @param channelId logical channel id (0 based, contrary to previous documentation all protocols use zero base)
     * @param id        logical channel id (0 based, contrary to previous documentation all protocols use zero base)
     * @param name      logical channel name (use OBIS code if device uses OBIS codes)
     * @param unit      the logical channel unit
     * @param scaler    unit scale
     * @deprecated scaler is obsolete (contained in unit)
     */
    public ChannelInfo(int id, String name, Unit unit, int scaler, int channelId) {
        this(id, name, unit, scaler);
        this.channelId = channelId;
    }

    // KV added 6/4/2006

    /**
     * Constructor
     *
     * @param id         0-based channel id
     * @param name       logical channel name (use OBIS code if device uses OBIS codes)
     * @param unit       the logical channel unit
     * @param scaler     unit scale
     * @param channelId  logical channel id (0 based, contrary to previous documentation all protocols use zero base)
     * @param multiplier BigDecimal multiplier to calculate engineering values from basic pulse values
     */
    public ChannelInfo(int id, String name, Unit unit, int scaler, int channelId, BigDecimal multiplier) {
        this(id, name, unit, scaler);
        this.channelId = channelId;
        this.multiplier = multiplier;
    }

    /**
     * <p></p>
     *
     * @param channelId logical channel id (0 based, contrary to previous documentation all protocols use zero base)
     * @param id        logical channel id (0 based)
     * @param name      logical channel name (use OBIS code if device uses OBIS codes, 0 based)
     * @param unit      the logical channel unit
     */
    public ChannelInfo(int id, int channelId, String name, Unit unit) {
        this(id, name, unit);
        this.channelId = channelId;
    }

    /**
     * Constructor for <CODE>LoadProfiles</CODE> which contain channels from different meters. The {@link #name} field is used to identify the <CODE>ObisCode</CODE> but
     * some <CODE>ProfileData</CODE may contain multiple channels with the same <CODE>ObisCode</CODE> but from a different Slave meter. The distiction between those channels is
     * made with the {@link #meterIdentifier}
     *
     * @param id                  logical channel id (0 based)
     * @param name                logical channel name (use OBIS code if device uses OBIS codes, 0 based)
     * @param unit                the logical channel unit
     * @param meterIdentification identifier (SerialNumber) of the meter which will provide data for this channel
     */
    public ChannelInfo(int id, String name, Unit unit, String meterIdentification) {
        this(id, name, unit);
        this.meterIdentifier = meterIdentification;
    }

    /**
     * Constructor for <CODE>LoadProfiles</CODE> which contain channels from different meters. The {@link #name} field is used to identify the <CODE>ObisCode</CODE> but
     * some <CODE>ProfileData</CODE may contain multiple channels with the same <CODE>ObisCode</CODE> but from a different Slave meter. The distiction between those channels is
     * made with the {@link #meterIdentifier}
     *
     * @param id                  logical channel id (0 based)
     * @param name                logical channel name (use OBIS code if device uses OBIS codes, 0 based)
     * @param unit                the logical channel unit
     * @param cumulative          indicates whether the channel is cumulative
     * @param meterIdentification identifier (SerialNumber) of the meter which will provide data for this channel
     */
    public ChannelInfo(int id, String name, Unit unit, String meterIdentification, boolean cumulative) {
        this(id, name, unit);
        this.meterIdentifier = meterIdentification;
        this.cumulative = cumulative;
    }

    /**
     * Getter for the id
     *
     * @return the id of the logical channel (1 based)
     */
    @XmlAttribute
    public int getId() {
        return id;
    }

    /**
     * Setter for the id
     *
     * @param id int
     */
    public void setId(int id) {
        this.id = id;
    }

    public String getReadingTypeMRID() {
        return readingTypeMRID;
    }

    @XmlAttribute
    public void setReadingTypeMRID(String readingTypeMRID) {
        this.readingTypeMRID = readingTypeMRID;
    }

    /**
     * Getter for the name
     *
     * @return the name of the logical channel
     */
    @XmlAttribute
    public String getName() {
        return name;
    }

    /**
     * Setter for the name
     *
     * @param name String
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for the unit
     *
     * @return the logical channel's unit
     */
    @XmlAttribute
    public Unit getUnit() {
        return unit;
    }

    /**
     * Setter for the unit
     *
     * @param unit Unit
     */
    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    /**
     * Getter for the scaler
     *
     * @return scale to apply to this channel's unit
     * @deprecated scaler is obsolete
     */
    public int getScaler() {
        return 0;
    }

    /**
     * Setter for the scaler
     *
     * @param scaler int
     * @deprecated scaler is obsolete
     */
    public void setScaler(int scaler) {
    }

    /**
     * Getter for the channelId
     *
     * @return int
     */
    @XmlAttribute
    public int getChannelId() {
        return channelId;
    }

    /**
     * Setter for channelId
     *
     * @param channelId int
     */
    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    /**
     * Getter for the cumulativeWrapValue.
     *
     * @return BigDecimal cumulativeWrapValue
     * @deprecated
     */
    @XmlAttribute
    public BigDecimal getCumulativeWrapValue() {
        return cumulativeWrapValue;
    }

    /**
     * Setter for the cumulativeWrapValue. when this is set, the BusinessLogic will suppose that the values in the IntervalData are indexe rather then advances
     *
     * @param cumulativeWrapValue BigDecimal
     * @deprecated use setCumulative()
     */
    public void setCumulativeWrapValue(BigDecimal cumulativeWrapValue) {
        this.cumulativeWrapValue = cumulativeWrapValue;
    }

    /**
     * returns true if cumulativeWrapValue != null, else false
     *
     * @return true or false
     */
    @XmlAttribute
    public boolean isCumulative() {
        return ((cumulative) || (cumulativeWrapValue != null));
    }

    public void setCumulative() {
        this.cumulative = true;
    }

    /**
     * Getter for the multiplier
     *
     * @return BigDecimal multiplier
     */
    @XmlAttribute
    public BigDecimal getMultiplier() {
        return multiplier;
    }

    /**
     * Set the multiplier to calculate engineering values from basic pulse values
     *
     * @param multiplier BigDecimal multiplier
     */
    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * Getter for the <CODE>ObisCode</CODE> of this <CODE>Channel</CODE>.
     * (Normally this is the converted {@link #name})
     *
     * @return the <CODE>ObisCode</CODE> of this channel
     */
    public ObisCode getChannelObisCode() throws IllegalArgumentException {
        return ObisCode.fromString(this.name);
    }

    /**
     * Getter for the {@link #meterIdentifier}
     *
     * @return the meterIdentifier
     */
    @XmlAttribute
    public String getMeterIdentifier() {
        return meterIdentifier;
    }

    /**
     * Setter for the {@link #meterIdentifier}
     *
     * @param meterIdentifier the new meterIdentifier to set
     */
    public void setMeterIdentifier(String meterIdentifier) {
        this.meterIdentifier = meterIdentifier;
    }

    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
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
        return "ChannelInfo -> Id: " + this.id + " - Name: " + this.name + " - Unit: " + this.unit;
    }


    /**
     * 2 channel infos are considered equal if they have the same ObisCode (or text name), BaseUnit and serial number.
     * - Scale of the unit is ignored, only BaseUnit is compared
     * - Wild cards at the B-field of the ObisCode is equal to any b-field value
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChannelInfo that = (ChannelInfo) o;

        if (name != null && that.name == null) {
            return false;   //Not equal if one of the names is null
        }
        if (name == null && that.name != null) {
            return false;   //Not equal if one of the names is null
        }

        if (name != null && that.name != null) {
            try {
                ObisCode thisObisCode = ObisCode.fromString(name);
                ObisCode thatObisCode = ObisCode.fromString(that.name);
                if (thisObisCode.anyChannel() || thatObisCode.anyChannel()) {
                    if (!thisObisCode.equalsIgnoreBChannel(thatObisCode)) {
                        return false;
                    }
                } else if (!thisObisCode.equals(thatObisCode)) {
                    return false;
                }
            } catch (IllegalArgumentException e) {  //Name field is not an obis code
                if (!name.equals(that.name)) {
                    return false;
                }
            }
        }

        if (!meterIdentifier.equals(that.getMeterIdentifier())) {
            return false;       //Also check the serial number, because multiple channels can have the same obiscode (0.x.24.2.1.255)
        }

        if (unit != null && unit.isUndefined()) {
            return true;
        }
        if (that.unit != null && that.unit.isUndefined()) {
            return true;
        }

        if (this.getReadingTypeMRID() == null && that.getReadingTypeMRID() != null) {
            return false;
        }
        if (this.getReadingTypeMRID() != null && that.getReadingTypeMRID() == null) {
            return false;
        }
        if (this.getReadingTypeMRID() != null && that.getReadingTypeMRID() != null && !this.getReadingTypeMRID().equals(that.getReadingTypeMRID())) {
            return false;
        }

        //Units are considered equal if they are both null, or if they both have the same BaseUnit
        return (unit == null) ? (that.unit == null) : ((that.unit != null) && unit.equalBaseUnit(that.unit));
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        result = 31 * result + (meterIdentifier != null ? meterIdentifier.hashCode() : 0);
        result = 31 * result + (readingTypeMRID != null ? readingTypeMRID.hashCode() : 0);
        return result;
    }

    public static class ChannelInfoBuilder {

        private String name;
        private ObisCode obisCode;
        private Unit unit;
        private String meterIdentifier;
        private String readingTypeMRID;

        private int id;
        private int channelId;
        private BigDecimal cumulativeWrapValue;
        private boolean cumulative = false;
        private BigDecimal multiplier = BigDecimal.ONE;

        private ChannelInfoBuilder(ObisCode obisCode) {
            this.obisCode = obisCode;
        }

        public static ChannelInfoBuilder fromObisCode(ObisCode obisCode) {
            ChannelInfoBuilder channelInfoBuilder = new ChannelInfoBuilder(obisCode);
            channelInfoBuilder.name = obisCode.toString();
            return channelInfoBuilder;
        }

        public ChannelInfoBuilder meterIdentifier(String meterIdentifier) {
            this.meterIdentifier = meterIdentifier;
            return this;
        }

        public ChannelInfoBuilder readingTypeMRID(String readingTypeMRID) {
            this.readingTypeMRID = readingTypeMRID;
            return this;
        }

        public ChannelInfoBuilder unit(Unit unit) {
            this.unit = unit;
            return this;
        }

        public ChannelInfoBuilder multiplier(BigDecimal multiplier) {
            this.multiplier = multiplier;
            return this;
        }

        public ChannelInfoBuilder id(int id) {
            this.id = id;
            return this;
        }

        public ChannelInfoBuilder channelId(int channelId) {
            this.channelId = channelId;
            return this;
        }

        public ChannelInfoBuilder cumulativeWrapValue(BigDecimal cumulativeWrapValue) {
            this.cumulativeWrapValue = cumulativeWrapValue;
            return this;
        }

        public ChannelInfoBuilder cumulative(boolean cumulative) {
            this.cumulative = cumulative;
            return this;
        }

        public ChannelInfo build() {
            ChannelInfo channelInfo = new ChannelInfo();
            channelInfo.id = this.id;
            channelInfo.name = this.name;
            channelInfo.unit = this.unit;
            channelInfo.meterIdentifier = this.meterIdentifier;
            channelInfo.cumulative = this.cumulative;
            channelInfo.readingTypeMRID = this.readingTypeMRID;
            channelInfo.cumulativeWrapValue = this.cumulativeWrapValue;
            channelInfo.channelId = this.channelId;
            channelInfo.multiplier = this.multiplier;
            return channelInfo;
        }
    }
}