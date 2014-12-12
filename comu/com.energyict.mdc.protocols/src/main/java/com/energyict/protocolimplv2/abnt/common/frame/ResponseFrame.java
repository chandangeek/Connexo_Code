package com.energyict.protocolimplv2.abnt.common.frame;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.CrcMismatchException;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.exception.UnknownFunctionCodeParsingException;
import com.energyict.protocolimplv2.abnt.common.frame.field.BlockCount;
import com.energyict.protocolimplv2.abnt.common.frame.field.Crc;
import com.energyict.protocolimplv2.abnt.common.frame.field.Data;
import com.energyict.protocolimplv2.abnt.common.frame.field.Function;
import com.energyict.protocolimplv2.abnt.common.frame.field.MeterSerialNumber;
import com.energyict.protocolimplv2.abnt.common.structure.ConfigureAutomaticDemandResetResponse;
import com.energyict.protocolimplv2.abnt.common.structure.ConfigureDstResponse;
import com.energyict.protocolimplv2.abnt.common.structure.ConfigureHolidayListResponse;
import com.energyict.protocolimplv2.abnt.common.structure.DateModificationResponse;
import com.energyict.protocolimplv2.abnt.common.structure.HistoryLogResponse;
import com.energyict.protocolimplv2.abnt.common.structure.InstrumentationPageResponse;
import com.energyict.protocolimplv2.abnt.common.structure.LoadProfileReadoutResponse;
import com.energyict.protocolimplv2.abnt.common.structure.PowerFailLogResponse;
import com.energyict.protocolimplv2.abnt.common.structure.ReadInstallationCodeResponse;
import com.energyict.protocolimplv2.abnt.common.structure.ReadParametersResponse;
import com.energyict.protocolimplv2.abnt.common.structure.RegisterReadResponse;
import com.energyict.protocolimplv2.abnt.common.structure.TimeModificationResponse;

import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 14:05
 */
public class ResponseFrame implements Frame<ResponseFrame> {

    public static final int RESPONSE_DATA_LENGTH = 251;
    public static final int RESPONSE_DATA_LENGTH_IN_CASE_OF_SEGMENTATION = 249;

    private Function function;
    private MeterSerialNumber meterSerialNumber;
    private BlockCount blockCount;
    private Data data;
    private Crc crc;

    private final TimeZone timeZone;

    public ResponseFrame(TimeZone timeZone) {
        this.function = new Function();
        this.meterSerialNumber = new MeterSerialNumber();
        this.blockCount = new BlockCount();
        this.data = new Data(RESPONSE_DATA_LENGTH, timeZone);
        this.timeZone = timeZone;
        this.crc = new Crc();
    }

    @Override
    public byte[] getBytes() {
        if (Function.isRegularFunction(function)) {
            if (Function.allowsSegmentation(function)) {
                return ProtocolTools.concatByteArrays(
                        function.getBytes(),
                        meterSerialNumber.getBytes(),
                        blockCount.getBytes(),
                        data.getBytes(),
                        crc.getBytes()
                );
            } else {
                return ProtocolTools.concatByteArrays(
                        function.getBytes(),
                        meterSerialNumber.getBytes(),
                        data.getBytes(),
                        crc.getBytes()
                );
            }
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

            if (Function.allowsSegmentation(function)) {
                data = new Data(RESPONSE_DATA_LENGTH_IN_CASE_OF_SEGMENTATION, getTimeZone());
                blockCount.parse(rawData, ptr);
                ptr += blockCount.getLength();
            }

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
     * @throws ParsingException in case the data could not be parsed correct
     * @throws UnknownFunctionCodeParsingException in case the data contains invalid/unknown function code
     */
    public void doParseData() throws ParsingException {
        switch (function.getFunctionCode()) {
            case ACTUAL_PARAMETERS_WITH_DEMAND_RESET:
            case ACTUAL_PARAMETERS:
            case PREVIOUS_PARAMETERS:
            case ACTUAL_PARAMETERS_WITH_SELECTOR:
                data = new ReadParametersResponse(getTimeZone()).parse(data.getBytes(), 0);
                break;
            case CURRENT_REGISTERS:
            case PREVIOUS_REGISTERS:
                data = new RegisterReadResponse(getTimeZone()).parse(data.getBytes(), 0);
                break;
            case LP_OF_CURRENT_BILLING:
            case LP_OF_PREVIOUS_BILLING:
            case LP_DATA_WITH_SELECTOR:
                data = new LoadProfileReadoutResponse(getTimeZone(), data.getLength()).parse(data.getBytes(), 0);
                break;
            case INSTRUMENTATION_PAGE:
                data = new InstrumentationPageResponse(getTimeZone()).parse(data.getBytes(), 0);
                break;
            case POWER_FAIL_LOG:
                data = new PowerFailLogResponse(getTimeZone()).parse(data.getBytes(), 0);
                break;
            case HISTORY_LOG:
                data = new HistoryLogResponse(getTimeZone()).parse(data.getBytes(), 0);
                break;
            case DATE_CHANGE:
                data = new DateModificationResponse(getTimeZone()).parse(data.getBytes(), 0);
                break;
            case TIME_CHANGE:
                data = new TimeModificationResponse(getTimeZone()).parse(data.getBytes(), 0);
                break;
            case CONFIGURE_HOLIDAY_LIST:
                data = new ConfigureHolidayListResponse(getTimeZone()).parse(data.getBytes(), 0);
                break;
            case CONFIGURE_AUTOMATIC_DEMAND_RESET:
                data = new ConfigureAutomaticDemandResetResponse(getTimeZone()).parse(data.getBytes(), 0);
                break;
            case CONFIGURE_DST:
                data = new ConfigureDstResponse(getTimeZone()).parse(data.getBytes(), 0);
                break;
            case READ_INSTALLATION_CODE:
                data = new ReadInstallationCodeResponse(getTimeZone()).parse(data.getBytes(), 0);
                break;
            case ENQ:
            case ACK:
            case NACK:
                break;
            default:
                throw new UnknownFunctionCodeParsingException("Encountered unknown function code " + function.getFunctionCode().getFunctionCode());
        }
    }

    public MeterSerialNumber getMeterSerialNumber() {
        return meterSerialNumber;
    }

    public Function getFunction() {
        return function;
    }

    public BlockCount getBlockCount() {
        return blockCount;
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
        if (!Function.isRegularFunction(function)) {
            function.getLength();
        }

        if (!Function.allowsSegmentation(function)) {
            return function.getLength() +
                    meterSerialNumber.getLength() +
                    data.getLength() +
                    crc.getLength();
        } else {
            return function.getLength() +
                    meterSerialNumber.getLength() +
                    blockCount.getLength() +
                    data.getLength() +
                    crc.getLength();
        }
    }
}