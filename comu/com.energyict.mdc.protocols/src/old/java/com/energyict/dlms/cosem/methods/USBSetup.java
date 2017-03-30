/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.methods;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ObjectReference;

import java.io.IOException;

/**
 * Created by astor on 27.09.2016.
 */
public class USBSetup extends AbstractCosemObject {

    /**
     * Attributes
     */
    private BooleanObject usbState = null;    // Defines the accessPoint name of the network
    private BooleanObject usbActivity = null;    // Holds the personal identification number
    private OctetString lastActivityTimestamp = null;

    private static final int USB_STATE_ATTRIB = 2;
    private static final int USB_ACTIVITY_ATTRIB = 3;
    private static final int USB_LAST_ACTIVITY_TIMESTAMP_ATTRIB = 4;

    private static final ObisCode DEFAULT_OBIS_CODE = ObisCode.fromString("0.0.128.0.28.255");

    /**
     * @param protocolLink
     * @param objectReference
     */
    public USBSetup(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public final static ObisCode getDefaultObisCode() {
        return DEFAULT_OBIS_CODE;
    }

    protected int getClassId() {
        return DLMSClassId.USB_SETUP.getClassId();
    }

    /**
     * Read USB State from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public BooleanObject readUSBState() throws IOException {
        this.usbState = new BooleanObject(getResponseData(USB_STATE_ATTRIB), 0);
        return this.usbState;
    }

    /**
     * Read USB Acgtivity from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public BooleanObject readUSBActivity() throws IOException {
        this.usbActivity = new BooleanObject(getResponseData(USB_ACTIVITY_ATTRIB), 0);
        return this.usbActivity;
    }

    /**
     * Read Last Activity Timestamp from the device
     *
     * @return
     * @throws java.io.IOException
     */
    public OctetString readLastActivityTimeStamp() throws IOException {
        this.lastActivityTimestamp = new OctetString(getResponseData(USB_LAST_ACTIVITY_TIMESTAMP_ATTRIB), 0);
        return this.lastActivityTimestamp;
    }
}

