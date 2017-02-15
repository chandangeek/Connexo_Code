/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributes.PLCOFDMType2MACSetupAttribute;

import java.io.IOException;

public class PLCOFDMType2MACSetup extends AbstractCosemObject {

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public PLCOFDMType2MACSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static ObisCode getDefaultObisCode() {
        return ObisCode.fromString("0.0.29.1.0.255");
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.PLC_OFDM_TYPE2_MAC_SETUP.getClassId();
    }

    /**
     * The 16-bit address that the device uses to communicate in the PAN.
     * <pre>
     * - 0x0000-0xFFFD Gives the short MAC address allocated by a coordinator
     *   during the registration of the device to the PAN coordinator.
     * - 0xFFFE Indicates that the device has found a PAN coordinator but
     *   a valid short MAC address has not yet been allocated
     * - 0xFFFF Indicates that the device has not found a PAN coordinator.
     * </pre>
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType readShortAddress() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_SHORT_ADDRESS);
    }

    public AbstractDataType readRCCoord() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_RC_COORD);
    }

    /**
     * The 16-bit identifier of the PAN on which the device is operating. A value equal
     * to 0xFFFF indicates that the device is not associated
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType readPanId() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_PAN_ID);
    }

    public AbstractDataType readToneMask() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_TONE_MASK);
    }

    public AbstractDataType readTMRTTL() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_TMR_TTL);
    }

    public AbstractDataType readMaxFrameRetries() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_MAX_FRAME_RETRIES);
    }

    public AbstractDataType readNeighbourTableEntryTTL() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_NEIGHBOUR_TABLE_ENTRY_TTL);
    }

    /**
     * The neighbour table contains information about all the devices within the POS of
     * the device. One element of the table represents one PLC direct neighbour of the device.
     *
     * @return
     * @throws IOException
     */
    public AbstractDataType readNeighbourTable() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_NEIGHBOUR_TABLE);
    }

    public AbstractDataType readHighPriorityWindowSize() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_HIGH_PRIORITY_WINDOW_SIZE);
    }

    public AbstractDataType readCSMAFairnessLimit() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_CSMA_FAIRNESS_LIMIT);
    }

    public AbstractDataType readBeaconRandomizationWindowLength() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_BEACON_RANDOMIZATION_WINDOW_LENGTH);
    }

    public AbstractDataType readMacA() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_A);
}

    public AbstractDataType readMacK() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_K);
    }

    public AbstractDataType readMinCWAttempts() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_MIN_CW_ATTEMPTS);
    }

    public AbstractDataType readCenelecLegacyMode() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_CENELEC_LEGACY_MODE);
    }

    public AbstractDataType readMaxBE() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_MAX_BE);
    }

    public AbstractDataType readMaxCSMABackoff() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_MAX_CSMA_BACKOFF);
    }

    public AbstractDataType readMinBE() throws IOException {
        return readDataType(PLCOFDMType2MACSetupAttribute.MAC_MIN_BE);
    }

    public void writePANID(int panID) throws IOException {
        write(PLCOFDMType2MACSetupAttribute.MAC_PAN_ID, new Unsigned16(panID).getBEREncodedByteArray());
    }

    public void writeToneMask(boolean[] toneMask) throws IOException {
        final BitString value = new BitString(toneMask);
        write(PLCOFDMType2MACSetupAttribute.MAC_TONE_MASK, value.getBEREncodedByteArray());
    }

    public void writeTMRTTL(int tmrTTL) throws IOException {
        final Unsigned8 value = new Unsigned8(tmrTTL);
        write(PLCOFDMType2MACSetupAttribute.MAC_TMR_TTL, value.getBEREncodedByteArray());
    }

    public void writeMaxFrameRetries(int maxFrameRetries) throws IOException {
        final Unsigned8 value = new Unsigned8(maxFrameRetries);
        write(PLCOFDMType2MACSetupAttribute.MAC_MAX_FRAME_RETRIES, value.getBEREncodedByteArray());
    }

    public void writeNeighbourTableEntryTTL(int ttl) throws IOException {
        final Unsigned8 value = new Unsigned8(ttl);
        write(PLCOFDMType2MACSetupAttribute.MAC_NEIGHBOUR_TABLE_ENTRY_TTL, value.getBEREncodedByteArray());
    }

    public void writeHighPriorityWindowSize(int windowSize) throws IOException {
        final Unsigned8 value = new Unsigned8(windowSize);
        write(PLCOFDMType2MACSetupAttribute.MAC_HIGH_PRIORITY_WINDOW_SIZE, value.getBEREncodedByteArray());
    }

    public void writeCSMAFairnessLimit(int fairnessLimit) throws IOException {
        final Unsigned8 value = new Unsigned8(fairnessLimit);
        write(PLCOFDMType2MACSetupAttribute.MAC_CSMA_FAIRNESS_LIMIT, value.getBEREncodedByteArray());
    }

    public void writeBeaconRandomizationWindowLength(int windowLength) throws IOException {
        final Unsigned8 value = new Unsigned8(windowLength);
        write(PLCOFDMType2MACSetupAttribute.MAC_BEACON_RANDOMIZATION_WINDOW_LENGTH, value.getBEREncodedByteArray());
    }

    public void writeMacA(int macA) throws IOException {
        final Unsigned8 value = new Unsigned8(macA);
        write(PLCOFDMType2MACSetupAttribute.MAC_A, value.getBEREncodedByteArray());
    }

    public void writeMacK(int macK) throws IOException {
        final Unsigned8 value = new Unsigned8(macK);
        write(PLCOFDMType2MACSetupAttribute.MAC_K, value.getBEREncodedByteArray());
    }

    public void writeMinCWAttempts(int minCWAttempts) throws IOException {
        final Unsigned8 value = new Unsigned8(minCWAttempts);
        write(PLCOFDMType2MACSetupAttribute.MAC_MIN_CW_ATTEMPTS, value.getBEREncodedByteArray());
    }

    public void writeMaxBE(int maxBE) throws IOException {
        final Unsigned8 value = new Unsigned8(maxBE);
        write(PLCOFDMType2MACSetupAttribute.MAC_MAX_BE, value.getBEREncodedByteArray());
    }

    public void writeMaxCSMABackOff(int maxCSMABackOff) throws IOException {
        final Unsigned8 value = new Unsigned8(maxCSMABackOff);
        write(PLCOFDMType2MACSetupAttribute.MAC_MAX_CSMA_BACKOFF, value.getBEREncodedByteArray());
    }

    public void writeMinBE(int minBE) throws IOException {
        final Unsigned8 value = new Unsigned8(minBE);
        write(PLCOFDMType2MACSetupAttribute.MAC_MIN_BE, value.getBEREncodedByteArray());
    }
}