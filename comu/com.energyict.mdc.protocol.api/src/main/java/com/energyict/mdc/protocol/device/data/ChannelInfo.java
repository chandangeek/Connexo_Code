package com.energyict.mdc.protocol.device.data;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;

import java.io.IOException;
import java.math.BigDecimal;


/**
 * <p/>
 * Contains information about a device logical channel
 *
 * @author Karel
 *         </p>
 */

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
     * Constructor
     *
     * @param id     logical channel id (0 based, contrary to previous documentation all protocols use zero base)
     * @param name   logical channel name (use OBIS code if device uses OBIS codes)
     * @param unit   the logical channel unit
     * @param scaler unit scale
     * @deprecated scaler is obsolete (contained in unit), use {@link #ChannelInfo(int, int, String, Unit)} instead
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
    public int getId() {
        return id;
    }

    /**
     * Getter for the name
     *
     * @return the name of the logical channel
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for the unit
     *
     * @return the logical channel's unit
     */
    public Unit getUnit() {
        return unit;
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
     * Getter for the channelId
     *
     * @return int
     */
    public int getChannelId() {
        return channelId;
    }

    /**
     * Setter for the id
     *
     * @param id int
     */
    public void setId(int id) {
        this.id = id;
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
     * Setter for the unit
     *
     * @param unit Unit
     */
    public void setUnit(Unit unit) {
        this.unit = unit;
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
     * Setter for channelId
     *
     * @param channelId int
     */
    public void setChannelId(int channelId) {
        this.channelId = channelId;
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
     * Getter for the cumulativeWrapValue.
     *
     * @return BigDecimal cumulativeWrapValue
     * @deprecated
     */
    public BigDecimal getCumulativeWrapValue() {
        return cumulativeWrapValue;
    }

    /**
     * returns true if cumulativeWrapValue != null, else false
     *
     * @return true or false
     */
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
    public ObisCode getChannelObisCode() throws IOException {
        try {
            return ObisCode.fromString(this.name);
        } catch (IllegalArgumentException e) {
            throw new IOException("The name of the channelInfo is not a valid Obiscode [" + this.name + "]");
        }
    }

    /**
     * Getter for the {@link #meterIdentifier}
     *
     * @return the meterIdentifier
     */
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
        return "ChannelInfo -> Id: " + this.id + " - Name: " + this.name + " - Unit: " + this.unit;
    }
} // end ChannelInfo
