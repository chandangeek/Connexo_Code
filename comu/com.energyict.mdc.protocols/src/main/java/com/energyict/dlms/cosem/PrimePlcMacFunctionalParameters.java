/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Integer16;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributes.PrimePlcMacFunctionalParametersAttributes;

import java.io.IOException;

public class PrimePlcMacFunctionalParameters extends AbstractCosemObject {

    public static final ObisCode DEFAULT_OBIS_CODE = ObisCode.fromString("0.0.28.3.0.255");

    /**
     * Creates a new instance of {@link PrimePlcMacFunctionalParameters}
     *
     * @param protocolLink
     * @param objectReference
     */
    public PrimePlcMacFunctionalParameters(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.PRIME_PLC_MAC_FUNCTIONAL_PARAMETERS.getClassId();
    }

    public static ObisCode getDefaultObisCode() {
        return DEFAULT_OBIS_CODE;
    }

    /**
     * Holds the PIB variable 0x20 specified in PRIME-R1.3E:
     * LNID allocated to this node at time of its registration.
     *
     * @return
     * @throws java.io.IOException
     */
    public Integer16 getLNID() throws IOException {
        return readDataType(PrimePlcMacFunctionalParametersAttributes.LNID, Integer16.class);
    }

    /**
     * Holds the PIB variable 0x21 specified in PRIME-R1.3E:
     * LSID allocated to this node at the time of its promotion. This attribute is not
     * maintained if the node is in a Terminal state.
     *
     * @return
     * @throws java.io.IOException
     */
    public Unsigned8 getLSID() throws IOException {
        return readDataType(PrimePlcMacFunctionalParametersAttributes.LSID, Unsigned8.class);
    }

    /**
     * Holds the PIB variable 0x22 specified in PRIME-R1.3E:
     * SID of the switch node through which this node is connected to the sub
     * network. This attribute is not maintained in a base node.
     *
     * @return
     * @throws java.io.IOException
     */
    public Unsigned8 getSID() throws IOException {
        return readDataType(PrimePlcMacFunctionalParametersAttributes.SID, Unsigned8.class);
    }

    /**
     * Holds the PIB variable 0x23 specified in PRIME-R1.3E:
     * Subnetwork address to which this node is registered.
     * The base node returns the SNA it is using.
     *
     * @return
     * @throws java.io.IOException
     */
    public OctetString getSNA() throws IOException {
        return readDataType(PrimePlcMacFunctionalParametersAttributes.SNA, OctetString.class);
    }

    /**
     * Holds the PIB variable 0x24 specified in PRIME-R1.3E:
     * Present functional state of the node.
     * enum [Disconnected, Terminal, Switch, Base]
     *
     * @return
     * @throws java.io.IOException
     */
    public TypeEnum getState() throws IOException {
        return readDataType(PrimePlcMacFunctionalParametersAttributes.STATE, TypeEnum.class);
    }

    /**
     * Holds the PIB variable 0x25 specified in PRIME-R1.3E:
     * The SCP length, in symbols, in present frame.
     *
     * @return
     * @throws java.io.IOException
     */
    public Integer16 getScpLength() throws IOException {
        return readDataType(PrimePlcMacFunctionalParametersAttributes.SCP_LENGTH, Integer16.class);
    }

    /**
     * Holds the PIB variable 0x26 specified in PRIME-R1.3E:
     * Level of this node in subnetwork hierarchy.
     *
     * @return
     * @throws java.io.IOException
     */
    public Unsigned8 getNodeHierarchyLevel() throws IOException {
        return readDataType(PrimePlcMacFunctionalParametersAttributes.NODE_HIERARCHY_LEVEL, Unsigned8.class);
    }

    /**
     * Holds the PIB variable 0x27 specified in PRIME-R1.3E:
     * Number of beacon slots provisioned in present frame structure.
     *
     * @return
     * @throws java.io.IOException
     */
    public Unsigned8 getBeaconSlotCount() throws IOException {
        return readDataType(PrimePlcMacFunctionalParametersAttributes.BEACON_SLOT_COUNT, Unsigned8.class);
    }

    /**
     * Holds the PIB variable 0x28 specified in PRIME-R1.3E:
     * Beacon slot in which this device’s switch node transmits its beacon.
     * This attribute is not maintained in a base node.
     *
     * @return
     * @throws java.io.IOException
     */
    public Unsigned8 getBeaconRxSlot() throws IOException {
        return readDataType(PrimePlcMacFunctionalParametersAttributes.BEACON_RX_SLOT, Unsigned8.class);
    }

    /**
     * Holds the PIB variable 0x29 specified in PRIME-R1.3E:
     * Beacon slot in which this device transmits its beacon. This attribute is
     * not maintained in service nodes that are in a Terminal state.
     *
     * @return
     * @throws java.io.IOException
     */
    public Unsigned8 getBeaconTxSlot() throws IOException {
        return readDataType(PrimePlcMacFunctionalParametersAttributes.BEACON_TX_SLOT, Unsigned8.class);
    }

    /**
     * Holds the PIB variable 0x2A specified in PRIME-R1.3E:
     * Number of frames between receptions of two successive beacons. A value
     * of 0x0 indicates beacons are received in every frame. This attribute is not
     * maintained in a base node.
     *
     * @return
     * @throws java.io.IOException
     */
    public Unsigned8 getBeaconRxFrequency() throws IOException {
        return readDataType(PrimePlcMacFunctionalParametersAttributes.BEACON_RX_FREQUENCY, Unsigned8.class);
    }

    /**
     * Holds the PIB variable 0x2B specified in PRIME-R1.3E:
     * Number of frames between transmissions of two successive beacons. A
     * value of 0x0 indicates beacons are transmitted in every frame. This
     * attribute is not maintained in service nodes that are in a Terminal
     * state.
     *
     * @return
     * @throws java.io.IOException
     */
    public Unsigned8 getBeaconTxFrequency() throws IOException {
        return readDataType(PrimePlcMacFunctionalParametersAttributes.BEACON_TX_FREQUENCY, Unsigned8.class);
    }

}
