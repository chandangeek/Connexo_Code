/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.frame;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.CrcMismatchException;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.frame.field.Crc;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.frame.field.Function;
import com.energyict.protocolimplv2.abnt.common.frame.field.ReaderSerialNumber;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 14:05
 */
public class RequestFrame implements Frame<RequestFrame> {

    public static final int REQUEST_DATA_LENGTH = 60;
    public static final int REQUEST_FRAME_LENGTH = 66;

    private Function function;
    private ReaderSerialNumber readerSerialNumber;
    private Data data;
    private Crc crc;

    private final TimeZone timeZone;

    public RequestFrame(TimeZone timeZone) {
        this.timeZone = timeZone;
        this.function = new Function();
        this.readerSerialNumber = new ReaderSerialNumber();
        this.data = new Data(REQUEST_DATA_LENGTH, timeZone);
        this.crc = new Crc();
    }

    @Override
    public byte[] getBytes() {
        if (Function.isRegularFunction(function)) {
            return ProtocolTools.concatByteArrays(
                    function.getBytes(),
                    readerSerialNumber.getBytes(),
                    data.getBytes(),
                    crc.getBytes()
            );
        } else {
            return function.getBytes();
        }
    }

    @Override
    public RequestFrame parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        function.parse(rawData, ptr);
        ptr += function.getLength();

        if(Function.isRegularFunction(function)) {
            readerSerialNumber.parse(rawData, ptr);
            ptr += readerSerialNumber.getLength();

            data.parse(rawData, ptr);
            ptr += data.getLength();

            crc.parse(rawData, ptr);
        }
        return this;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public ReaderSerialNumber getReaderSerialNumber() {
        return readerSerialNumber;
    }

    public void setReaderSerialNumber(ReaderSerialNumber readerSerialNumber) {
        this.readerSerialNumber = readerSerialNumber;
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
        if (Function.isRegularFunction(function)) {
            crc = generateCrc();
        }
    }

    private Crc generateCrc() {
        byte[] partOfFrameToGenerateCrcFor = ProtocolTools.getSubArray(getBytes(), 0, getLength() - Crc.LENGTH);
        return new Crc().generateAndSetCrc(partOfFrameToGenerateCrcFor);
    }

    @Override
    public void validateCRC() throws CrcMismatchException {
        if (generateCrc().getCrc() != crc.getCrc()) {
            throw new CrcMismatchException("Failed to validate the CRC of the frame");
        }
    }

    @Override
    public int getLength() {
        return Function.isRegularFunction(getFunction())
                ? REQUEST_FRAME_LENGTH
                : getFunction().getLength();
    }
}