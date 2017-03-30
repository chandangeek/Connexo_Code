/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

import java.io.UnsupportedEncodingException;

public class IdentificationProcessIdentify extends AbstractField<IdentificationProcessIdentify> {

    private String manufacturerIdentifier;
    private String deviceIdentifier;
    private String serialNumber;
    private static final int LENGTH = 18;

    public IdentificationProcessIdentify(byte[] identify) {
        int ptr = 0;
        this.manufacturerIdentifier = ProtocolTools.getAsciiFromBytes(ProtocolTools.getSubArray(identify, ptr, ptr = +3));
        this.deviceIdentifier = ProtocolTools.getAsciiFromBytes(ProtocolTools.getSubArray(identify, ptr, ptr = +3));
        this.serialNumber = ProtocolTools.getAsciiFromBytes(ProtocolTools.getSubArray(identify, ptr, ptr = +10));
    }

    public IdentificationProcessIdentify(String manufacturer, String device, String serialNumber) {
        this.manufacturerIdentifier = manufacturer;
        this.deviceIdentifier = device;
        this.serialNumber = serialNumber;
    }

    public IdentificationProcessIdentify() {
    }

    public String getManufacturerIdentifier() {
        return manufacturerIdentifier;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public byte[] getBytes() {
        try {
            byte[] bytes = (manufacturerIdentifier + deviceIdentifier + serialNumber).getBytes("ASCII");
            byte[] closure = new byte[]{0x0, 0x0};
            return ProtocolTools.concatByteArrays(bytes, closure);
        } catch (UnsupportedEncodingException e) {
            return new byte[0];
        }
    }

    public IdentificationProcessIdentify parse(byte[] rawData, int offset) throws CTRParsingException {
        this.manufacturerIdentifier = ProtocolTools.getAsciiFromBytes(ProtocolTools.getSubArray(rawData, offset, offset += 3));
        this.deviceIdentifier = ProtocolTools.getAsciiFromBytes(ProtocolTools.getSubArray(rawData, offset, offset += 3));
        this.serialNumber = ProtocolTools.getAsciiFromBytes(ProtocolTools.getSubArray(rawData, offset, offset += 10));
        return this;
    }

    public int getLength() {
        return LENGTH;
    }

    @Override
    public String toString() {
        return manufacturerIdentifier + deviceIdentifier + serialNumber;
    }
}
