package com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.enums.G3NodeNodeModulationScheme;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.enums.G3NodePhaseInfo;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.enums.G3NodeState;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.topology.enums.G3NodeTxModulation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

/**
 * Represents a single entry in the node list
 */
public class G3Node {

    private final byte[] macAddress;
    private final byte[] parentMacAddress;
    private final int shortAddress;
    private final Date lastSeenDate;
    private final Date lastPathRequest;
    private final G3NodeState nodeState;
    private final long roundTrip;
    private final int linkCost;
    private final G3NodeNodeModulationScheme modulationScheme;
    private final G3NodeTxModulation txModulation;
    private final int lqi;
    private final G3NodePhaseInfo phaseInfo;

    public G3Node(final byte[] macAddress, final byte[] parentMacAddress, final int shortAddress, final Date lastSeenDate, final Date lastPathRequest) {
        this.macAddress = safeCopy(macAddress);
        this.parentMacAddress = safeCopy(parentMacAddress);
        this.shortAddress = shortAddress;
        this.lastSeenDate = lastSeenDate;
        this.lastPathRequest = lastPathRequest;
        this.nodeState = null;
        this.roundTrip = -1;
        this.linkCost = -1;
        this.modulationScheme = null;
        this.txModulation = null;
        this.lqi = -1;
        this.phaseInfo = null;
    }

    public G3Node(final byte[] macAddress, final byte[] parentMacAddress, final int shortAddress,
                  final Date lastSeenDate, final Date lastPathRequest, final G3NodeState nodeState,
                  final long roundTrip, final int linkCost,
                  G3NodeNodeModulationScheme modulationScheme, G3NodeTxModulation txModulation, int lqi, G3NodePhaseInfo phaseInfo) {
        this.macAddress = safeCopy(macAddress);
        this.parentMacAddress = safeCopy(parentMacAddress);
        this.shortAddress = shortAddress;
        this.lastSeenDate = lastSeenDate;
        this.lastPathRequest = lastPathRequest;
        this.nodeState = nodeState;
        this.roundTrip = roundTrip;
        this.linkCost = linkCost;
        this.modulationScheme = modulationScheme;
        this.txModulation = txModulation;
        this.lqi = lqi;
        this.phaseInfo = phaseInfo;
    }

    private static final byte[] safeCopy(final byte[] bytes) {
        return bytes == null ? new byte[0] : Arrays.copyOf(bytes, bytes.length);
    }

    /**
     * @param structure
     * @param timeZone
     * @return
     * @throws IOException
     */
    public static final G3Node fromStructure(final Structure structure, final TimeZone timeZone) throws IOException {
        if (structure == null) {
            return null;
        }

        final OctetString macAddressAttr = structure.getDataType(0, OctetString.class);
        final OctetString parentMacAddressAttr = structure.getDataType(1, OctetString.class);
        final Integer32 shortAddressAttr = structure.getDataType(2, Integer32.class);
        final OctetString lastUpdatedAttr = structure.getDataType(3, OctetString.class);
        final OctetString lastPathRequestAttr = structure.getDataType(4, OctetString.class);

        final byte[] macAddress = macAddressAttr == null ? new byte[0] : macAddressAttr.getOctetStr();
        final byte[] parentMacAddress = parentMacAddressAttr == null ? new byte[0] : parentMacAddressAttr.getOctetStr();
        final int shortAddress = shortAddressAttr == null ? -1 : shortAddressAttr.getValue();
        final Date lastSeenDate = getDateFromOctetString(timeZone, lastUpdatedAttr);
        final Date lastPathRequest = getDateFromOctetString(timeZone, lastPathRequestAttr);

        return new G3Node(
                macAddress,
                parentMacAddress,
                shortAddress,
                lastSeenDate,
                lastPathRequest
        );

    }

    /**
     * G3-PLC network management IC
     * <p>
     * g3_node ::= structure {
     * [0] node_address: octet-string, -- EUI-64
     * [1] reverse_parent_address: octet-string, -- EUI-64
     * [2] short_address: double-long,
     * [3] last_update: date-time,
     * [4] last_path_request: date-time,
     * [5] forward_parent_address: octet-string, -- EUI-64
     * [6] state ::= enum:
     * (0) UNKNOWN,
     * (1) NOT_ASSOCIATED,
     * (2) AVAILABLE,
     * (3) VANISHED,
     * (4) BLACKLISTED
     * [7] modulation_scheme ::= enum:
     * (0) DIFFERENTIAL,
     * (1) COHERENT
     * [8] tx_modulation ::= enum:
     * (0) ROBO,
     * (1) DBPSK,
     * (2) DQPSK,
     * (3) D8PSK,
     * (4) QAM16,
     * (5) SUPERROBO,      // not present in mac_neighbour_table (G3-PLC MAC setup IC)
     * (99) UNKNOWN        // not present in mac_neighbour_table (G3-PLC MAC setup IC)
     * [9] lqi: unsigned,
     * [10] phase_info ::= enum:
     * (0) INPHASE,
     * (1) DEGREE60,
     * (2) DEGREE120,
     * (3) DEGREE180,
     * (4) DEGREE240,
     * (5) DEGREE300,
     * (7) NOPHASEINFO     // not present in mac_neighbour_table (G3-PLC MAC setup IC)
     * [11] round_trip: double-long-unsigned,
     * [12] link_cost: unsigned
     * }
     *
     * @param structure
     * @param timeZone
     * @return
     * @throws IOException
     */
    public static G3Node fromStructureV2(final Structure structure, final TimeZone timeZone) throws IOException {
        if (structure == null) {
            return null;
        }

        final OctetString macAddressAttr = structure.getDataType(0, OctetString.class);
        final OctetString parentMacAddressAttr = structure.getDataType(1, OctetString.class);
        final Integer32 shortAddressAttr = structure.getDataType(2, Integer32.class);
        final OctetString lastUpdatedAttr = structure.getDataType(3, OctetString.class);
        final OctetString lastPathRequestAttr = structure.getDataType(4, OctetString.class);
        //5 = forward-parent
        final TypeEnum stateAttr = structure.getDataType(6, TypeEnum.class);
        final TypeEnum modulationScheme = structure.getDataType(7, TypeEnum.class);
        final TypeEnum txModulation = structure.getDataType(8, TypeEnum.class);
        final Unsigned8 lqiAttr = structure.getDataType(9, Unsigned8.class);
        final TypeEnum phaseInfo = structure.getDataType(10, TypeEnum.class);
        final Unsigned32 roundTripAttr = structure.getDataType(11, Unsigned32.class);
        final Unsigned8 linkCostAttr = structure.getDataType(12, Unsigned8.class);

        final byte[] macAddress = macAddressAttr == null ? new byte[0] : macAddressAttr.getOctetStr();
        final byte[] parentMacAddress = parentMacAddressAttr == null ? new byte[0] : parentMacAddressAttr.getOctetStr();
        final int shortAddress = shortAddressAttr == null ? -1 : shortAddressAttr.getValue();
        final Date lastSeenDate = getDateFromOctetString(timeZone, lastUpdatedAttr);
        final Date lastPathRequest = getDateFromOctetString(timeZone, lastPathRequestAttr);
        final G3NodeState nodeState = G3NodeState.fromValue(stateAttr.getValue());
        final G3NodeNodeModulationScheme g3NodeModulationScheme = G3NodeNodeModulationScheme.fromValue(modulationScheme.getValue());
        final G3NodeTxModulation g3NodeTxModulation = G3NodeTxModulation.fromValue(txModulation.getValue());
        final G3NodePhaseInfo g3NodePhaseInfo = G3NodePhaseInfo.fromValue(phaseInfo.getValue());
        final long roundTrip = roundTripAttr.getValue();
        final int linkCost = linkCostAttr.getValue();
        final int lqi = lqiAttr.getValue();

        return new G3Node(
                macAddress, parentMacAddress, shortAddress,
                lastSeenDate, lastPathRequest, nodeState,
                roundTrip, linkCost,
                g3NodeModulationScheme, g3NodeTxModulation, lqi, g3NodePhaseInfo
        );

    }

    /**
     * Parse a given axdrDateTime octetstring into a date, return null if the date is unspecified
     */
    private static Date getDateFromOctetString(TimeZone timeZone, OctetString dateTime) throws IOException {
        if (dateTime != null) {
            final byte[] berEncodedByteArray = dateTime.getBEREncodedByteArray();
            if (isUnspecifiedDate(berEncodedByteArray)) {
                return null;
            } else {
                final AXDRDateTime axdrDateTime = new AXDRDateTime(berEncodedByteArray, 0, timeZone);
                return axdrDateTime.getValue().getTime();
            }
        } else {
            return null;
        }
    }

    private static boolean isUnspecifiedDate(byte[] berEncodedByteArray) {
        return (berEncodedByteArray[2] & 0xFF) == 0xFF && (berEncodedByteArray[3] & 0xFF) == 0xFF && (berEncodedByteArray[4] & 0xFF) == 0xFF && (berEncodedByteArray[5] & 0xFF) == 0xFF;
    }

    public G3NodeNodeModulationScheme getModulationScheme() {
        return modulationScheme;
    }

    public G3NodeTxModulation getTxModulation() {
        return txModulation;
    }

    public int getLqi() {
        return lqi;
    }

    public G3NodePhaseInfo getPhaseInfo() {
        return phaseInfo;
    }

    public byte[] getMacAddress() {
        return macAddress;
    }

    public String getMacAddressString() {
        return macAddress == null ? "null" : ProtocolTools.getHexStringFromBytes(macAddress, "");
    }

    public byte[] getParentMacAddress() {
        return parentMacAddress;
    }

    public String getParentMacAddressString() {
        return parentMacAddress == null ? "null" : ProtocolTools.getHexStringFromBytes(parentMacAddress, "");
    }

    public int getShortAddress() {
        return shortAddress;
    }

    public Date getLastSeenDate() {
        return lastSeenDate;   //Null if the date is unspecified!
    }

    public Date getLastPathRequest() {
        return lastPathRequest;    //Null if the date is unspecified!
    }

    public G3NodeState getNodeState() {
        return nodeState;
    }

    public long getRoundTrip() {
        return roundTrip;
    }

    public int getLinkCost() {
        return linkCost;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("G3Node");
        sb.append("{ macAddress=").append(macAddress == null ? "null" : ProtocolTools.getHexStringFromBytes(macAddress, ""));
        sb.append(", parentMacAddress=").append(parentMacAddress == null ? "null" : ProtocolTools.getHexStringFromBytes(parentMacAddress, ""));
        sb.append(", shortAddress=").append(shortAddress);
        sb.append(", state=").append(nodeState==null?"null":nodeState.getDescription());
        sb.append(", modulationScheme=").append(modulationScheme == null ? "null" : modulationScheme.getDescription());
        sb.append(", txModulation=").append(txModulation == null ? "null" : txModulation.getDescription());
        sb.append(", LQI=").append(lqi);
        sb.append(", phaseInfo=").append(phaseInfo == null ? "null" : phaseInfo.getDescription());
        sb.append(", lastSeenDate=").append(lastSeenDate == null ? "Never" : lastSeenDate);
        sb.append(", lastPathRequest=").append(lastPathRequest == null ? "Never" : lastPathRequest);
        sb.append('}');
        return sb.toString();
    }
}
