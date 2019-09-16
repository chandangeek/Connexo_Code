package com.energyict.dlms.cosem.attributes;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * Topology setup attributes.
 *
 * @author alex
 */
public enum G3NetworkManagementAttributes implements DLMSClassAttributes {

    LOGICAL_NAME(1, 0),
    NODE_LIST(2, 8),
    AUTOMATIC_ROUTE_MANAGEMENT_ENABLED(3, 16),
    USE_FIXED_GMK(4, 24),
    FIXED_GMK(5, 35),
    SNR_ENABLED(6, 40),
    SNR_INTERVAL(7, 48),
    SNR_QUIET_TIME(8, 56),
    SNR_PAYLOAD(9, 64),
    KEEP_ALIVE_ENABLED(10, 72),                 // Indicates if the background G3-PLC keepalive service is enabled.
    KEEP_ALIVE_SCHEDULE_INTERVAL(11, 80),       // Keep-alive service scan interval (in seconds).
    @Deprecated
    KEEP_ALIVE_BUCKET_SIZE(12, 88),             // TODO check with Beacon-FW what is this, is not in documentation anymore
    KEEP_ALIVE_MIN_INACTIVE_METER_TIME(13, 96), // Minimum time of no communication before a meter becomes eligible for the keepalive task (in seconds).
    KEEP_ALIVE_MAX_INACTIVE_METER_TIME(14, 104),// Maximum time of no communication before no longer trying to contact the meter (in seconds).
    KEEP_ALIVE_FAIL_COUNT(15, 112),             // Maximum amount of failed communication attempts before a meter is considered vanished.
    KEEP_ALIVE_DELAY_BETWEEN_PINGS(16, 120),    // Minimum delay between two meter communication attempts.
    IS_G3_INTERFACE_ENABLED(17, 128),
    JOINING_NODES(18, 136),
    BLACKLISTED_NODES(19, 144),
    AVAILABLE_NODES(20, 152),
    AVERAGE_ROUNDTRIP_TIME(21, 160),
    AVERAGE_HOPS(22, 168),
    DLMS_METER_PORT(23, 176),
    DLMS_METER_PUSH_NOTIFICATION_PORT(24, 182),
    METER_ASSOCIATION_TIMEOUT(25, 190),
    DLMS_METER_BIND_PORT(26, 198);

    /**
     * Attribute ID.
     */
    private final int attributeId;

    /**
     * The short name of the attribute (offset from base address).
     */
    private final int shortName;

    private G3NetworkManagementAttributes(final int attributeId, final int shortName) {
        this.attributeId = attributeId;
        this.shortName = shortName;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSClassId getDlmsClassId() {
        return DLMSClassId.FIREWALL_SETUP;
    }

    /**
     * {@inheritDoc}
     */
    public final int getShortName() {
        return this.shortName;
    }

    /**
     * {@inheritDoc}
     */
    public final int getAttributeNumber() {
        return this.attributeId;
    }

    /**
     * {@inheritDoc}
     */
    public final DLMSAttribute getDLMSAttribute(ObisCode obisCode) {
        return new DLMSAttribute(obisCode, this);
    }
}
