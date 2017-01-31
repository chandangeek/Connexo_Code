/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributes.SixLowPanAdaptationLayerSetupAttribute;

import java.io.IOException;

public class SixLowPanAdaptationLayerSetup extends AbstractCosemObject {

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public SixLowPanAdaptationLayerSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static ObisCode getDefaultObisCode() {
        return ObisCode.fromString("0.0.29.2.0.255");
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.SIX_LOW_PAN_ADAPTATION_LAYER_SETUP.getClassId();
    }

    /**
     * Defines the maximum number of hops to be used by the routing
     * algorithm.
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType readAdpMaxHops() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_MAX_HOPS);
    }

    /**
     * Defines the threshold below which a direct neighbour is not taken into
     * account during the commissioning procedure (based on LQI
     * measurement done by the physical layer).
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType readAdpWeakLQIValue() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_WEAK_LQI_VALUE);
    }

    public AbstractDataType readSecurityLevel() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_SECURITY_LEVEL);
    }

    public AbstractDataType readPrefixTable() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_PREFIX_TABLE);
    }

    /**
     * The routing configuration entity specifies all parameters linked to the
     * routing mechanism.
     *
     * @return
     * @throws IOException
     */
    public Array readAdpRoutingConfiguration() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_ROUTING_CONFIGURATION, Array.class);
    }

    /**
     * Defines the number of seconds below which an entry in the Broadcast
     * Table remains active in the table. On timer expiration, the entry is
     * automatically removed.
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType readAdpBroadcastLogTableEntryTTL() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_BROADCAST_LOG_TABLE_ENTRY_TTL);
    }

    /**
     * The routing table contains information about the different routes in
     * which the device is implicated.
     *
     * @return
     * @throws IOException
     */
    public Array readAdpRoutingTable() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_ROUTING_TABLE, Array.class);
    }


    public AbstractDataType readContextInformationTable() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_CONTEXT_INFORMATION_TABLE);
    }

    public AbstractDataType readBlacklistTable() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_BLACKLIST_TABLE);
    }

    public AbstractDataType readBroadcastLogTable() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_BROADCAST_LOG_TABLE);
    }

    public AbstractDataType readGroupTable() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_GROUP_TABLE);
    }

    public AbstractDataType readMaxJoinWaitTime() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_MAX_JOIN_WAIT_TIME);
    }

    public AbstractDataType readPathDiscoveryTime() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_PATH_DISCOVERY_TIME);
    }

    public AbstractDataType readActiveKeyIndex() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_ACTIVE_KEY_INDEX);
    }

    public AbstractDataType readMetricType() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_METRIC_TYPE);
    }

    public AbstractDataType readCoordShortAddress() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_COORD_SHORT_ADDRESS);
    }

    public AbstractDataType readDisableDefaultRouting() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_DISABLE_DEFAULT_ROUTING);
    }

    public AbstractDataType readDeviceType() throws IOException {
        return readDataType(SixLowPanAdaptationLayerSetupAttribute.ADP_DEVICE_TYPE);
    }

    /**
     * @param maxHops
     * @throws IOException
     */
    public void writeMaxHops(int maxHops) throws IOException {
        final Unsigned8 value = new Unsigned8(maxHops);
        final byte[] rawValue = value.getBEREncodedByteArray();
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_MAX_HOPS, rawValue);
    }

    /**
     * @param weakLqiValue
     * @throws IOException
     */
    public void writeWeakLqiValue(int weakLqiValue) throws IOException {
        final Unsigned8 value = new Unsigned8(weakLqiValue);
        final byte[] rawValue = value.getBEREncodedByteArray();
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_WEAK_LQI_VALUE, rawValue);
    }

    public void writeSecurityLevel(int securityLevel) throws IOException {
        final Unsigned8 value = new Unsigned8(securityLevel);
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_SECURITY_LEVEL, value.getBEREncodedByteArray());
    }

    public void writeRoutingConfiguration(int adp_net_traversal_time,
                                          int adp_routing_table_entry_TTL,
                                          int adp_Kr,
                                          int adp_Km,
                                          int adp_Kc,
                                          int adp_Kq,
                                          int adp_Kh,
                                          int adp_Krt,
                                          int adp_RREQ_retries,
                                          int adp_RREQ_RERR_wait,
                                          int adp_Blacklist_table_entry_TTL,
                                          boolean adp_unicast_RREQ_gen_enable,
                                          boolean adp_RLC_enabled,
                                          int adp_add_rev_link_cost) throws IOException {

        Structure routingConfiguration = new Structure();
        routingConfiguration.addDataType(new Unsigned8(adp_net_traversal_time));
        routingConfiguration.addDataType(new Unsigned16(adp_routing_table_entry_TTL));
        routingConfiguration.addDataType(new Unsigned8(adp_Kr));
        routingConfiguration.addDataType(new Unsigned8(adp_Km));
        routingConfiguration.addDataType(new Unsigned8(adp_Kc));
        routingConfiguration.addDataType(new Unsigned8(adp_Kq));
        routingConfiguration.addDataType(new Unsigned8(adp_Kh));
        routingConfiguration.addDataType(new Unsigned8(adp_Krt));
        routingConfiguration.addDataType(new Unsigned8(adp_RREQ_retries));
        routingConfiguration.addDataType(new Unsigned8(adp_RREQ_RERR_wait));
        routingConfiguration.addDataType(new Unsigned16(adp_Blacklist_table_entry_TTL));
        routingConfiguration.addDataType(new BooleanObject(adp_unicast_RREQ_gen_enable));
        routingConfiguration.addDataType(new BooleanObject(adp_RLC_enabled));
        routingConfiguration.addDataType(new Unsigned8(adp_add_rev_link_cost));

        write(SixLowPanAdaptationLayerSetupAttribute.ADP_ROUTING_CONFIGURATION, routingConfiguration.getBEREncodedByteArray());
    }

    /**
     * @param broadcastLogTableTTL
     * @throws IOException
     */
    public void writeBroadcastLogTableTTL(int broadcastLogTableTTL) throws IOException {
        final Unsigned16 value = new Unsigned16(broadcastLogTableTTL);
        final byte[] rawValue = value.getBEREncodedByteArray();
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_BROADCAST_LOG_TABLE_ENTRY_TTL, rawValue);
    }

    public void writeMaxJoinWaitTime(int waitTime) throws IOException {
        final Unsigned16 value = new Unsigned16(waitTime);
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_MAX_JOIN_WAIT_TIME, value.getBEREncodedByteArray());
    }

    public void writePathDiscoveryTime(int pathDiscoveryTime) throws IOException {
        final Unsigned8 value = new Unsigned8(pathDiscoveryTime);
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_PATH_DISCOVERY_TIME, value.getBEREncodedByteArray());
}

    public void writeMetricType(int metricType) throws IOException {
        final Unsigned8 value = new Unsigned8(metricType);
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_METRIC_TYPE, value.getBEREncodedByteArray());
    }

    public void writeCoordShortAddress(int coordShortAddress) throws IOException {
        final Unsigned16 value = new Unsigned16(coordShortAddress);
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_COORD_SHORT_ADDRESS, value.getBEREncodedByteArray());
    }

    public void writeDisableDefaultRouting(boolean disableDefaultRouting) throws IOException {
        final BooleanObject value = new BooleanObject(disableDefaultRouting);
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_DISABLE_DEFAULT_ROUTING, value.getBEREncodedByteArray());
    }

    public void writeDeviceType(int deviceType) throws IOException {
        final Unsigned8 value = new Unsigned8(deviceType);
        write(SixLowPanAdaptationLayerSetupAttribute.ADP_DEVICE_TYPE, value.getBEREncodedByteArray());
    }
}