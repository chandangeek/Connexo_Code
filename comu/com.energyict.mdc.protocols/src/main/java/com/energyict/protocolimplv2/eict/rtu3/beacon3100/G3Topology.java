/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class G3Topology {

    private static final Logger logger = Logger.getLogger(G3Topology.class.getName());

    public static final List<G3Node> convertNodeList(final Array nodeList, final TimeZone timeZone) {
        final List<G3Node> nodes = new ArrayList<G3Node>();
        if (nodeList == null) {
            return nodes;
        }

        for (final AbstractDataType dataType : nodeList) {
            if (dataType != null && dataType instanceof Structure) {
                try {
                    final G3Node g3Node = G3Node.fromStructure((Structure) dataType, timeZone);
                    if (g3Node != null) {
                        nodes.add(g3Node);
                    }
                } catch (IOException e) {
                    if (logger.isLoggable(Level.WARNING)) {
                        logger.log(Level.WARNING, "Unable to parse G3Node info: " + e.getMessage() + " [" + dataType.toString() + "]", e);
                    }
                }
            }
        }

        return nodes;
    }

    /**
     * Represents a single entry in the node list, containing 4 items
     */
    public static class G3Node {

        private final byte[] macAddress;
        private final byte[] parentMacAddress;
        private final int shortAddress;
        private final Date lastSeenDate;
        private final Date lastPathRequest;

        public G3Node(final byte[] macAddress, final byte[] parentMacAddress, final int shortAddress, final Date lastSeenDate, final Date lastPathRequest) {
            this.macAddress = safeCopy(macAddress);
            this.parentMacAddress = safeCopy(parentMacAddress);
            this.shortAddress = shortAddress;
            this.lastSeenDate = lastSeenDate;
            this.lastPathRequest = lastPathRequest;
        }

        private static final byte[] safeCopy(final byte[] bytes) {
            return bytes == null ? new byte[0] : Arrays.copyOf(bytes, bytes.length);
        }

        /**
         * @param structure
         * @param timeZone
         * @return
         * @throws java.io.IOException
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

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("G3Node");
            sb.append("{macAddress=").append(macAddress == null ? "null" : ProtocolTools.getHexStringFromBytes(macAddress, ""));
            sb.append(", parentMacAddress=").append(parentMacAddress == null ? "null" : ProtocolTools.getHexStringFromBytes(parentMacAddress, ""));
            sb.append(", shortAddress=").append(shortAddress);
            sb.append(", lastSeenDate=").append(lastSeenDate == null ? "Never" : lastSeenDate);
            sb.append(", lastPathRequest=").append(lastPathRequest == null ? "Never" : lastPathRequest);
            sb.append('}');
            return sb.toString();
        }
    }
}
