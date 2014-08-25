package com.energyict.protocolimplv2.abnt.common.frame;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.CrcMismatchException;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.frame.field.Crc;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.frame.field.Function;
import com.energyict.protocolimplv2.abnt.common.frame.field.MeterSerialNumber;
import com.energyict.protocolimplv2.abnt.common.structure.HistoryLogResponse;
import com.energyict.protocolimplv2.abnt.common.structure.InstrumentationPageResponse;
import com.energyict.protocolimplv2.abnt.common.structure.LoadProfileReadoutResponse;
import com.energyict.protocolimplv2.abnt.common.structure.PowerFailLogResponse;
import com.energyict.protocolimplv2.abnt.common.structure.ReadParametersResponse;
import com.energyict.protocolimplv2.abnt.common.structure.RegisterReadResponse;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 14:05
 */
public class ResponseFrame implements Frame<ResponseFrame> {

    public static final int RESPONSE_DATA_LENGTH = 251;
    public static final int RESPONSE_FRAME_LENGTH = 258;

    private Function function;
    private MeterSerialNumber meterSerialNumber;
    private Data data;
    private Crc crc;

    private final TimeZone timeZone;

    public ResponseFrame(TimeZone timeZone) {
        this.function = new Function();
        this.meterSerialNumber = new MeterSerialNumber();
        this.data = new Data(RESPONSE_DATA_LENGTH, timeZone);
        this.timeZone = timeZone;
        this.crc = new Crc();
    }

    @Override
    public byte[] getBytes() {
        if (Function.isRegularFunction(function)) {
            return ProtocolTools.concatByteArrays(
                    function.getBytes(),
                    meterSerialNumber.getBytes(),
                    data.getBytes(),
                    crc.getBytes()
            );
        } else {
            return function.getBytes();
        }
    }

    @Override
    public ResponseFrame parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        function.parse(rawData, ptr);
        ptr += function.getLength();

        if (Function.isRegularFunction(function)) {
            meterSerialNumber.parse(rawData, ptr);
            ptr += meterSerialNumber.getLength();

            data.parse(rawData, ptr);
            ptr += data.getLength();

            crc.parse(rawData, ptr);
        }
        return this;
    }

    /**
     * <p>This method will check which kind of data is in the data field and parse it.<br></br>
     * If data is of unknown type or could not be parsed, then a ParsingException is thrown.
     * </p>
     * <b>Warning:</b> This method may only be used when data is already decrypted. So make sure to decrypt the frame first!
     *
     * @throws com.energyict.protocolimplv2.abnt.common.exception.ParsingException in case the data could not be parsed correct
     */
    public void doParseData() throws ParsingException {
        if (function.getFunctionCode().equals(Function.FunctionCode.ACTUAL_PARAMETERS_WITH_DEMAND_RESET) ||
                function.getFunctionCode().equals(Function.FunctionCode.ACTUAL_PARAMETERS) ||
                function.getFunctionCode().equals(Function.FunctionCode.PREVIOUS_PARAMETERS) ||
                function.getFunctionCode().equals(Function.FunctionCode.ACTUAL_PARAMETERS_WITH_FULL_LP)) {
            data = new ReadParametersResponse(getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(Function.FunctionCode.CURRENT_REGISTERS) ||
                function.getFunctionCode().equals(Function.FunctionCode.PREVIOUS_REGISTERS)) {
            data = new RegisterReadResponse(getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(Function.FunctionCode.POWER_FAIL_LOG)) {
            data = new PowerFailLogResponse(getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(Function.FunctionCode.HISTORY_LOG)) {
            data = new HistoryLogResponse(getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(Function.FunctionCode.INSTRUMENTATION_PAGE)) {
            data = new InstrumentationPageResponse(getTimeZone()).parse(data.getBytes(), 0);
        } else if (function.getFunctionCode().equals(Function.FunctionCode.LP_OF_CURRENT_BILLING) ||
                function.getFunctionCode().equals(Function.FunctionCode.LP_OF_PREVIOUS_BILLING) ||
                function.getFunctionCode().equals(Function.FunctionCode.LP_ALL_DATA)) {
            data = new LoadProfileReadoutResponse(getTimeZone()).parse(data.getBytes(), 0);
        } else {
            throw new ParsingException("Failed to parse data of the response frame, encountered unknown function code " + function.getFunctionCode().getFunctionCode());
        }
    }

    public MeterSerialNumber getMeterSerialNumber() {
        return meterSerialNumber;
    }

    public Function getFunction() {
        return function;
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
        byte[] partOfFrameToGenerateCrcFor = ProtocolTools.getSubArray(getBytes(), 0, getLength() - Crc.LENGTH);
        return new Crc().generateAndSetCrc(partOfFrameToGenerateCrcFor);
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
        return Function.isRegularFunction(function)
                ? RESPONSE_FRAME_LENGTH
                : function.getLength();
    }
}