package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.NetworkManagementAttributes;
import com.energyict.dlms.cosem.methods.NetworkManagementMethods;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 9/27/12
 * Time: 10:51 AM
 */
public class NetworkManagement extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.128.0.5.255");

    private Unsigned32 discoverDuration;
    private Unsigned8 discoverInterval;
    private Unsigned32 repeaterCallInterval;
    private Unsigned16 repeaterCallThreshold;
    private Unsigned8 repeaterCallTimeslots;

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public NetworkManagement(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.NETWORK_MANAGEMENT.getClassId();
    }

    /**
     * Read the discover duration from the device
     * @return the discovery process duration in minutes
     * @throws java.io.IOException
     */
	public Unsigned32 readDiscoverDuration() throws IOException {
    	this.discoverDuration = new Unsigned32(getResponseData(NetworkManagementAttributes.DISCVOER_DURATION), 0);
		return this.discoverDuration;
	}

    /**
     * Getter for the discover duration
     * @return the discovery process duration in minutes
     * @throws java.io.IOException
     */
    public Unsigned32 getDiscoverDuration() throws IOException {
        if (this.discoverDuration == null) {
            readDiscoverDuration();
        }
        return this.discoverDuration;
    }

    /**
     * Setter for the discover duration
     * @param discoverDuration the discovery process duration in minutes
     * @throws java.io.IOException
     */
	public void writeDiscoverDuration(Unsigned32 discoverDuration) throws IOException {
		write(NetworkManagementAttributes.DISCVOER_DURATION, discoverDuration.getBEREncodedByteArray());
		this.discoverDuration = discoverDuration;
	}

    /**
     * Read the discover interval from the device
     *
     * @return the discovery process interval in hours
     * @throws java.io.IOException
     */
    public Unsigned8 readDiscoverInterval() throws IOException {
        this.discoverInterval = new Unsigned8(getResponseData(NetworkManagementAttributes.DISCOVER_INTERVAL), 0);
        return this.discoverInterval;
    }

    /**
     * Getter for the discover interval
     *
     * @return the discovery process interval in hours
     * @throws java.io.IOException
     */
    public Unsigned8 getDiscoverInterval() throws IOException {
        if (this.discoverInterval == null) {
            readDiscoverInterval();
        }
        return this.discoverInterval;
    }

    /**
     * Setter for the discover interval
     *
     * @param discoverInterval the discovery process interval in hours
     * @throws java.io.IOException
     */
    public void writeDiscoverInterval(Unsigned8 discoverInterval) throws IOException {
        write(NetworkManagementAttributes.DISCOVER_INTERVAL, discoverInterval.getBEREncodedByteArray());
        this.discoverInterval = discoverInterval;
    }

    /**
     * Read the repeater call interval from the device
     *
     * @return the repeater call interval in minutes
     * @throws java.io.IOException
     */
    public Unsigned32 readRepeaterCallInterval() throws IOException {
        this.repeaterCallInterval = new Unsigned32(getResponseData(NetworkManagementAttributes.REPEATER_CALL_INTERVAL), 0);
        return this.repeaterCallInterval;
    }

    /**
     * Getter for the repeater call interval
     *
     * @return the repeater call interval in minutes
     * @throws java.io.IOException
     */
    public Unsigned32 getRepeaterCallInterval() throws IOException {
        if (this.repeaterCallInterval == null) {
            readRepeaterCallInterval();
        }
        return this.repeaterCallInterval;
    }

    /**
     * Setter for the repeater call interval
     *
     * @param repeaterCallInterval the repeater call interval in minutes
     * @throws java.io.IOException
     */
    public void writeRepeaterCallInterval(Unsigned32 repeaterCallInterval) throws IOException {
        write(NetworkManagementAttributes.REPEATER_CALL_INTERVAL, repeaterCallInterval.getBEREncodedByteArray());
        this.repeaterCallInterval = repeaterCallInterval;
    }

    /**
     * Read the repeater call threshold from the device
     *
     * @return the repeater call threshold in dBV
     * @throws java.io.IOException
     */
    public Unsigned16 readRepeaterCallThreshold() throws IOException {
        this.repeaterCallThreshold = new Unsigned16(getResponseData(NetworkManagementAttributes.REPEATER_CALL_THRESHOLD), 0);
        return this.repeaterCallThreshold;
    }

    /**
     * Getter for the repeater call threshold
     *
     * @return the repeater call threshold in dBV
     * @throws java.io.IOException
     */
    public Unsigned16 getRepeaterCallThreshold() throws IOException {
        if (this.repeaterCallThreshold == null) {
            readRepeaterCallThreshold();
        }
        return this.repeaterCallThreshold;
    }

    /**
     * Setter for the repeater call threshold
     *
     * @param repeaterCallThreshold the repeater call threshold in dBV
     * @throws java.io.IOException
     */
    public void writeRepeaterCallThreshold(Unsigned16 repeaterCallThreshold) throws IOException {
        write(NetworkManagementAttributes.REPEATER_CALL_THRESHOLD, repeaterCallThreshold.getBEREncodedByteArray());
        this.repeaterCallThreshold = repeaterCallThreshold;
    }

    /**
     * Read the repeater call timeslots from the device
     *
     * @return the the number of time slots reserved for new meters during the repeater call
     * @throws java.io.IOException
     */
    public Unsigned8 readRepeaterCallTimeslots() throws IOException {
        this.repeaterCallTimeslots = new Unsigned8(getResponseData(NetworkManagementAttributes.REPEATER_CALL_TIMESLOTS), 0);
        return this.repeaterCallTimeslots;
    }

    /**
     * Getter for the repeater call timeslots
     *
     * @return the the number of time slots reserved for new meters during the repeater call
     * @throws java.io.IOException
     */
    public Unsigned8 getRepeaterCallTimeslots() throws IOException {
        if (this.repeaterCallTimeslots == null) {
            readRepeaterCallTimeslots();
        }
        return this.repeaterCallTimeslots;
    }

    /**
     * Setter for the repeater call timeslots
     *
     * @param repeaterCallTimeslots the number of time slots reserved for new meters during the repeater call
     * @throws java.io.IOException
     */
    public void writeRepeaterCallTimeslots(Unsigned8 repeaterCallTimeslots) throws IOException {
        write(NetworkManagementAttributes.REPEATER_CALL_TIMESLOTS, repeaterCallTimeslots.getBEREncodedByteArray());
        this.repeaterCallTimeslots = repeaterCallTimeslots;
    }

    /**
     * Run new meter discovery on demand
     *
     * @throws java.io.IOException
     */
    public final void runMeterDiscovery() throws IOException {
        methodInvoke(NetworkManagementMethods.RUN_METER_DISCOVERY);
    }

    /**
     * Run alarm meter discovery on demand
     *
     * @throws java.io.IOException
     */
    public final void runAlarmMeterDiscovery() throws IOException {
        methodInvoke(NetworkManagementMethods.RUN_ALARM_METER_DISCOVERY);
    }

    /**
     * Run repeater call  on demand
     *
     * @throws java.io.IOException
     */
    public final void runRepeaterCall() throws IOException {
        methodInvoke(NetworkManagementMethods.RUN_REPEATER_CALL);
    }
}
