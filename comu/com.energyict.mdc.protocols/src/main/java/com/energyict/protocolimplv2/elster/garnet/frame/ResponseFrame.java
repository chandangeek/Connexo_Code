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
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Part;
import com.energyict.protocolimplv2.elster.garnet.structure.ConcentratorStatusResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.ConcentratorVersionResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.ContactorResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.DiscoverMetersResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.DiscoverRepeatersResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.LogBookEventResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.NotExecutedErrorResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.OpenSessionResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.PoolingResponseWithLogsStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.PoolingResponseWithoutLogsStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.RadioParametersResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.ReadingResponseStructure;

import java.time.Clock;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 14:05
 */
public class ResponseFrame implements Frame<ResponseFrame> {

    private Address sourceAddress;
    private Address destinationAddress;
    private Function function;
    private ExtendedFunction extendedFunction;
    private Part part;
    private Data data;
    private Crc crc;

    private final TimeZone timeZone;

    public ResponseFrame(TimeZone timeZone) {
        this.sourceAddress = new Address();
        this.destinationAddress = new Address();
        this.function = new Function();
        this.extendedFunction = new ExtendedFunction();
        this.part = new Part();
        this.data = new Data();
        this.timeZone = timeZone;
        this.crc = new Crc();
    }

    @Override
    public byte[] getBytes() {
        switch (function.getFunctionCode().getFrameFormat()) {
            case REGULAR_FRAME_FORMAT:
                return ProtocolTools.concatByteArrays(
                        destinationAddress.getBytes(),
                        function.getBytes(),
                        sourceAddress.getBytes(),
                        data.getBytes(),
                        crc.getBytes()
                );
            case EXTENDED_FRAME_FORMAT:
                return ProtocolTools.concatByteArrays(
                        destinationAddress.getBytes(),
                        function.getBytes(),
                        sourceAddress.getBytes(),
                        extendedFunction.getBytes(),
                        part.getBytes(),
                        data.getBytes(),
                        crc.getBytes()
                );
            case SHORT_FRAME_FORMAT:
                return ProtocolTools.concatByteArrays(
                        destinationAddress.getBytes(),
                        function.getBytes(),
                        data.getBytes(),
                        crc.getBytes()
                );
        }
        return new byte[0];
    }

    public byte[] getEncryptedBytes() {
        return ProtocolTools.concatByteArrays(
                data.getBytes(),
                crc.getBytes()
        );
    }

    @Override
    public ResponseFrame parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        destinationAddress.parse(rawData, ptr);
        ptr += destinationAddress.getLength();

        function.parse(rawData, ptr);
        ptr += function.getLength();

        if (!function.getFunctionCode().usesShortFrameFormat()) {
            sourceAddress.parse(rawData, ptr);
            ptr += sourceAddress.getLength();
        }

        if (function.getFunctionCode().usesExtendedFrameFormat()) {
            extendedFunction.parse(rawData, ptr);
            ptr += extendedFunction.getLength();

            part.parse(rawData, ptr);
            ptr += part.getLength();
        }

        data = new Data(function.getFunctionCode()).parse(rawData, ptr);
        ptr += data.getLength();

        crc.parse(rawData, ptr);
        return this;
    }

    /**
     * <p>This method will check which kind of data is in the data field and parse it.<br></br>
     * If data is of unknown type or could not be parsed, then a ParsingException is thrown.
     * </p>
     * <b>Warning:</b> This method may only be used when data is already decrypted. So make sure to decrypt the frame first!
     *
     * @throws ParsingException in case the data could not be parsed correct
     * @param clock The Clock
     */
    public void doParseData(Clock clock) throws ParsingException {
        if (function.getFunctionCode().equals(FunctionCode.LOGBOOK_EVENT_RESPONSE)) {
            data = new LogBookEventResponseStructure(clock, getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(FunctionCode.CONCENTRATOR_VERSION_RESPONSE)) {
            data = new ConcentratorVersionResponseStructure(clock, getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(FunctionCode.OPEN_SESSION_RESPONSE)) {
            data = new OpenSessionResponseStructure(clock, getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(FunctionCode.POOLING_RESPONSE_WITHOUT_LOGS)) {
            data = new PoolingResponseWithoutLogsStructure(clock, getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(FunctionCode.POOLING_RESPONSE_WITH_LOGS)) {
            data = new PoolingResponseWithLogsStructure(clock, getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(FunctionCode.DISCOVER_METERS_RESPONSE)) {
            data = new DiscoverMetersResponseStructure(clock, getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(FunctionCode.CHECKPOINT_READING_RESPONSE) ||
                function.getFunctionCode().equals(FunctionCode.ONLINE_READING_RESPONSE)) {
            data = new ReadingResponseStructure(clock, getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(FunctionCode.CONCENTRATOR_STATUS_RESPONSE)) {
            data = new ConcentratorStatusResponseStructure(clock, getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(FunctionCode.CONTACTOR_RESPONSE)) {
            data = new ContactorResponseStructure(clock, getTimeZone(), FunctionCode.CONTACTOR_RESPONSE).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(FunctionCode.DISCOVER_REPEATERS_RESPONSE)) {
            data = new DiscoverRepeatersResponseStructure(clock, getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(FunctionCode.NOT_EXECUTED_RESPONSE)) {
            data = new NotExecutedErrorResponseStructure(getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(FunctionCode.RADIO_PARAMETERS_RESPONSE)) {
            data = new RadioParametersResponseStructure().parse(data.getBytes(), 0);
        } else {
            throw new ParsingException("Failed to parse data of the response frame, encountered unknown function code " + function.getFunctionCode().getFunctionCode());
        }
    }

    public Address getSourceAddress() {
        return sourceAddress;
    }

    public Address getDestinationAddress() {
        return destinationAddress;
    }

    public Function getFunction() {
        return function;
    }

    public Part getPart() {
        return part;
    }

    public ExtendedFunction getExtendedFunction() {
        return extendedFunction;
    }

    public Data getData() {
        return data;
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

    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public void validateCRC() throws CrcMismatchException {
        if (generateCrc().getCrc() != crc.getCrc()) {
            throw new CrcMismatchException("The CRC is not valid!");
        }
    }

    @Override
    public int getLength() {
        int length = destinationAddress.getLength();
        length += function.getLength();
        if(!function.getFunctionCode().usesShortFrameFormat()) {
            length += sourceAddress.getLength();
        }
        if(function.getFunctionCode().usesExtendedFrameFormat()) {
            length += extendedFunction.getLength();
            length += part.getLength();
        }
        length += data.getLength();
        length += crc.getLength();
        return length;
    }
}