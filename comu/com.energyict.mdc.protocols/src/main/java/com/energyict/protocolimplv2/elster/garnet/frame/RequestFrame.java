/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.frame;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.exception.CrcMismatchException;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Address;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Crc;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.ExtendedFunction;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Function;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Part;

/**
 * @author sva
 * @since 23/05/2014 - 14:05
 */
public class RequestFrame implements Frame<RequestFrame> {

    private Address destinationAddress;
    private Function function;
    private ExtendedFunction extendedFunction;
    private Part part;
    private Data data;
    private Crc crc;

    public RequestFrame() {
        this.destinationAddress = new Address();
        this.function = new Function();
        this.extendedFunction = new ExtendedFunction();
        this.part = new Part();
        this.data = new Data();
        this.crc = new Crc();
    }

    @Override
    public byte[] getBytes() {
        if (function.getFunctionCode().usesExtendedFrameFormat()) {
            return ProtocolTools.concatByteArrays(
                    destinationAddress.getBytes(),
                    function.getBytes(),
                    extendedFunction.getBytes(),
                    part.getBytes(),
                    data.getBytes(),
                    crc.getBytes()
            );
        } else {
            return ProtocolTools.concatByteArrays(
                    destinationAddress.getBytes(),
                    function.getBytes(),
                    data.getBytes(),
                    crc.getBytes()
            );
        }
    }

    @Override
    public RequestFrame parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        destinationAddress.parse(rawData, ptr);
        ptr += destinationAddress.getLength();

        function.parse(rawData, ptr);
        ptr += function.getLength();

        if(function.getFunctionCode().usesExtendedFrameFormat()) {
            extendedFunction.parse(rawData, ptr);
            ptr += extendedFunction.getLength();

            part = new Part().parse(rawData, ptr);
            ptr += part.getLength();
        }

        data = new Data(function.getFunctionCode()).parse(rawData, ptr);
        ptr += data.getLength();

        crc.parse(rawData, ptr);
        return this;
    }

    public Address getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(Address destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
        if (function.getFunctionCode().usesExtendedFrameFormat()) {
            this.extendedFunction = new ExtendedFunction();
            this.part = new Part(1);
        }
    }

    public ExtendedFunction getExtendedFunction() {
        return extendedFunction;
    }

    public void setExtendedFunction(ExtendedFunction extendedFunction) {
        this.extendedFunction = extendedFunction;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Crc getCrc() {
        return crc;
    }

    @Override
    public void generateAndSetCRC() {
        crc = generateCrc();
    }

    private Crc generateCrc() {
        return new Crc().generateAndSetCrc(getBytes(), getBytes().length - Crc.LENGTH);
    }

    @Override
    public void validateCRC() throws CrcMismatchException {
        if (generateCrc().getCrc() != crc.getCrc()) {
            throw new CrcMismatchException("Failed to validate the CRC of the frame");
        }
    }

    @Override
    public int getLength() {
        int length = destinationAddress.getLength();
        length += function.getLength();
        if (function.getFunctionCode().usesExtendedFrameFormat()) {
            length += extendedFunction.getLength();
            length += part.getLength();
        }
        length += data.getLength();
        length += crc.getLength();
        return length;
    }

    public RequestFrame doClone() throws ParsingException {
        try {
            return new RequestFrame().parse(this.getBytes().clone(), 0);
        } catch (ParsingException e) {
            throw new ParsingException("Failed to build a proper request frame", e);
        }
    }
}