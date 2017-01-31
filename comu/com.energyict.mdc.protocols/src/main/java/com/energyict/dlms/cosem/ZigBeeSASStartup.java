/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.attributes.ZigBeeSASStartupAttribute;

import java.io.IOException;

public class ZigBeeSASStartup extends AbstractCosemObject {

    public static final ObisCode LN = ObisCode.fromString("0.0.35.1.0.255");

    /**
     * Creates a new instance of ZigBeeSASStartup
     *
     * @param protocolLink
     */
    public ZigBeeSASStartup(ProtocolLink protocolLink) {
        super(protocolLink, new ObjectReference(LN.getLN()));
    }

    /**
     * Creates a new instance of ZigBeeSASStartup
     *
     * @param protocolLink
     * @param objectReference
     */
    public ZigBeeSASStartup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.ZIGBEE_SAS_STARTUP.getClassId();
    }

    public OctetString readLogicalName() throws IOException {
        return new OctetString(getResponseData(ZigBeeSASStartupAttribute.LOGICAL_NAME), 0);
    }

    public void writeLogicalName(OctetString logicalName) throws IOException {
        write(ZigBeeSASStartupAttribute.LOGICAL_NAME, logicalName.getBEREncodedByteArray());
    }

    public Unsigned16 readShortAddress() throws IOException {
        return new Unsigned16(getResponseData(ZigBeeSASStartupAttribute.SHORT_ADDRESS), 0);
    }

    public void writeShortAddress(Unsigned16 shortAddress) throws IOException {
        write(ZigBeeSASStartupAttribute.SHORT_ADDRESS, shortAddress.getBEREncodedByteArray());
    }

    public OctetString readExtendedPanId() throws IOException {
        return new OctetString(getResponseData(ZigBeeSASStartupAttribute.EXTENDED_PAN_ID), 0);
    }

    public void writeExtendedPanId(OctetString extendedPanID) throws IOException {
        write(ZigBeeSASStartupAttribute.EXTENDED_PAN_ID, extendedPanID.getBEREncodedByteArray());
    }

    public Unsigned16 readPanId() throws IOException {
        return new Unsigned16(getResponseData(ZigBeeSASStartupAttribute.PAN_ID), 0);
    }

    public void writePanId(Unsigned16 panId) throws IOException {
        write(ZigBeeSASStartupAttribute.PAN_ID, panId.getBEREncodedByteArray());
    }

    public Unsigned32 readChannelMask() throws IOException {
        return new Unsigned32(getResponseData(ZigBeeSASStartupAttribute.CHANNEL_MASK), 0);
    }

    public void writeChannelMask(Unsigned32 channelMask) throws IOException {
        write(ZigBeeSASStartupAttribute.CHANNEL_MASK, channelMask.getBEREncodedByteArray());
    }

    public Unsigned8 readProtocolVersion() throws IOException {
        return new Unsigned8(getResponseData(ZigBeeSASStartupAttribute.PROTOCOL_VERSION), 0);
    }

    public void writeProtocolVersion(Unsigned8 protocolVersion) throws IOException {
        write(ZigBeeSASStartupAttribute.PROTOCOL_VERSION, protocolVersion.getBEREncodedByteArray());
    }

    public Unsigned8 readStackProfile() throws IOException {
        return new Unsigned8(getResponseData(ZigBeeSASStartupAttribute.STACK_PROFILE), 0);
    }

    public void writeStackProfile(Unsigned8 stackProfile) throws IOException {
        write(ZigBeeSASStartupAttribute.STACK_PROFILE, stackProfile.getBEREncodedByteArray());
    }

    public Unsigned8 readStartUpControl() throws IOException {
        return new Unsigned8(getResponseData(ZigBeeSASStartupAttribute.START_UP_CONTROL), 0);
    }

    public void writeStartUpControl(Unsigned8 startUpControl) throws IOException {
        write(ZigBeeSASStartupAttribute.START_UP_CONTROL, startUpControl.getBEREncodedByteArray());
    }

    public OctetString readTrustCentreAddress() throws IOException {
        return new OctetString(getResponseData(ZigBeeSASStartupAttribute.TRUST_CENTER_ADDRESS), 0);
    }

    public void writeTrustCentreAddress(OctetString trustCentreAddress) throws IOException {
        write(ZigBeeSASStartupAttribute.TRUST_CENTER_ADDRESS, trustCentreAddress.getBEREncodedByteArray());
    }

    public OctetString readLinkKey() throws IOException {
        return new OctetString(getResponseData(ZigBeeSASStartupAttribute.LINK_KEY), 0);
    }

    public void writeLinkKey(OctetString linkKey) throws IOException {
        write(ZigBeeSASStartupAttribute.LINK_KEY, linkKey.getBEREncodedByteArray());
    }

    public OctetString readNetworkKey() throws IOException {
        return new OctetString(getResponseData(ZigBeeSASStartupAttribute.NETWORK_KEY), 0);
    }

    public void writeNetworkKey(OctetString networkKey) throws IOException {
        write(ZigBeeSASStartupAttribute.NETWORK_KEY, networkKey.getBEREncodedByteArray());
    }

    public BooleanObject readUseInsecureJoin() throws IOException {
        return new BooleanObject(getResponseData(ZigBeeSASStartupAttribute.USE_INSECURE_JOIN), 0);
    }

    public void writeUseInsecureJoin(BooleanObject useInsecureJoin) throws IOException {
        write(ZigBeeSASStartupAttribute.USE_INSECURE_JOIN, useInsecureJoin.getBEREncodedByteArray());
    }


}
