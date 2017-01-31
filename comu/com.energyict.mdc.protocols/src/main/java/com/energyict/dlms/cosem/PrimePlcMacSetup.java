/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributes.PrimePlcMacSetupAttributes;

import java.io.IOException;

public class PrimePlcMacSetup extends AbstractCosemObject {

    public static final ObisCode DEFAULT_OBIS_CODE = ObisCode.fromString("0.0.28.2.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public PrimePlcMacSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.PRIME_PLC_MAC_SETUP.getClassId();
    }

    /**
     * The default {@link ObisCode} of this {@link PrimePlcMacSetup}
     *
     * @return The obis code
     */
    public static ObisCode getDefaultObisCode() {
        return DEFAULT_OBIS_CODE;
    }

    /**
     * Holds the PIB variable 0x10 specified in PRIME-R1.3E:
     * Minimum time for which a service node in Disconnected status should scan the
     * channel for beacons before it can broadcast PNPDU.
     * This attribute is not maintained in base nodes.
     * The unit of this attribute is seconds.
     *
     * @return The value as {@link com.energyict.dlms.axrdencoding.Unsigned8}
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final Unsigned8 getMinSwitchSearchTime() throws IOException {
        return readDataType(PrimePlcMacSetupAttributes.MIN_SWITCH_SEARCH_TIME, Unsigned8.class);
    }

    /**
     * Holds the PIB variable 0x11 specified in PRIME-R1.3E:
     * Maximum number of PNPDUs that may be transmitted by a service node in a
     * period of mac_promotion_pdu_tx_period seconds
     * This attribute is not maintained in base nodes.
     *
     * @return The value as {@link com.energyict.dlms.axrdencoding.Unsigned8}
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final Unsigned8 getMaxPromotionPdu() throws IOException {
        return readDataType(PrimePlcMacSetupAttributes.MAX_PROMOTION_PDU, Unsigned8.class);
    }

    /**
     * Holds the PIB variable 0x12 specified in PRIME-R1.3E:
     * Time quantum for limiting the number of PNDPUs transmitted from a service
     * node. No more than mac_max_promotion_pdu may be transmitted in a period
     * of mac_promotion_pdu_tx_period.
     * The unit of this attribute is seconds.
     *
     * @return The value as {@link com.energyict.dlms.axrdencoding.Unsigned8}
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final Unsigned8 getPromotionPduTxPeriod() throws IOException {
        return readDataType(PrimePlcMacSetupAttributes.PROMOTION_PDU_TX_PERIOD, Unsigned8.class);
    }

    /**
     * Holds the PIB variable 0x13 specified in PRIME-R1.3E:
     * Maximum number of beacon slot that may be provisioned in a frame.
     * This attribute is maintained in the base node.
     *
     * @return The value as {@link com.energyict.dlms.axrdencoding.Unsigned8}
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final Unsigned8 getBeaconsPerFrame() throws IOException {
        return readDataType(PrimePlcMacSetupAttributes.BEACONS_PER_FRAME, Unsigned8.class);
    }

    /**
     * Holds the PIB variable 0x14 specified in PRIME-R1.3E:
     * Number of times the CSMA algorithm would attempt to transmit requested
     * data when a previous attempt was withheld due to PHY indicating channel
     * busy.
     *
     * @return The value as {@link com.energyict.dlms.axrdencoding.Unsigned8}
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final Unsigned8 getScpMaxTxAttempts() throws IOException {
        return readDataType(PrimePlcMacSetupAttributes.SCP_MAX_TX_ATTEMPTS, Unsigned8.class);
    }

    /**
     * Holds the PIB variable 0x15 specified in PRIME-R1.3E:
     * Number of seconds for which a MAC entity waits for acknowledgement of
     * receipt of MAC control packet from its peer entity. On expiry of this time, the
     * MAC entity may retransmit the MAC control packet.
     * The unit of this attribute is seconds.
     *
     * @return The value as {@link com.energyict.dlms.axrdencoding.Unsigned8}
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final Unsigned8 getCtlReTxTimer() throws IOException {
        return readDataType(PrimePlcMacSetupAttributes.CTL_RE_TX_TIMER, Unsigned8.class);
    }

    /**
     * Holds the PIB variable 0x18 specified in PRIME-R1.3E:
     * Maximum number of times a MAC entity will try to retransmit an
     * unacknowledged MAC control packet. If the retransmit count reaches this
     * maximum, the MAC entity shall abort further attempts to transmit the MAC
     * control packet.
     *
     * @return The value as {@link com.energyict.dlms.axrdencoding.Unsigned8}
     * @throws java.io.IOException If there occurred an error while reading the value
     */
    public final Unsigned8 getMaxCtlReTx() throws IOException {
        return readDataType(PrimePlcMacSetupAttributes.MAX_CTL_RE_TX, Unsigned8.class);
    }

}
