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
    KEEP_ALIVE_ENABLED(10, 72),
    KEEP_ALIVE_SCHEDULE_INTERVAL(11, 80),
    KEEP_ALIVE_BUCKET_SIZE(12, 88),
    KEEP_ALIVE_MIN_INACTIVE_METER_TIME(13, 96),
    KEEP_ALIVE_MAX_INACTIVE_METER_TIME(14, 104),
    KEEP_ALIVE_RETRIES(15, 112),
    KEEP_ALIVE_TIMEOUT(16, 120),
    IS_G3_INTERFACE_ENABLED(17, 128),
    JOINING_NODES(18, 136);

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
