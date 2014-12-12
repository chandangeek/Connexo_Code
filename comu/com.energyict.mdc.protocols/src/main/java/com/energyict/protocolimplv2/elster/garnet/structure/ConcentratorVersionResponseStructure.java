package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ConcentratorFirmwareVersion;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ConcentratorModel;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ConcentratorSerialNumber;
import com.energyict.protocolimplv2.elster.garnet.structure.field.CustomerCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.DateTime;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

import java.time.Clock;
import java.util.TimeZone;

/**
 * @author sva
 * @since 23/05/2014 - 13:28
 */
public class ConcentratorVersionResponseStructure extends Data<ConcentratorVersionResponseStructure> {

    public static final int PADDING_DATA_LENGTH = 2;
    public static final FunctionCode FUNCTION_CODE = FunctionCode.CONCENTRATOR_VERSION_RESPONSE;

    private final Clock clock;
    private DateTime dateTime;
    private ConcentratorModel concentratorModel;
    private ConcentratorSerialNumber serialNumber;
    private ConcentratorFirmwareVersion firmwareVersion;
    private CustomerCode customerCode;
    private PaddingData paddingData;

    private final TimeZone timeZone;

    public ConcentratorVersionResponseStructure(Clock clock, TimeZone timeZone) {
        super(FUNCTION_CODE);
        this.clock = clock;
        this.timeZone = timeZone;
        this.dateTime = new DateTime(clock, timeZone);
        this.concentratorModel = new ConcentratorModel();
        this.serialNumber = new ConcentratorSerialNumber();
        this.firmwareVersion = new ConcentratorFirmwareVersion();
        this.customerCode = new CustomerCode();
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                dateTime.getBytes(),
                concentratorModel.getBytes(),
                serialNumber.getBytes(),
                firmwareVersion.getBytes(),
                customerCode.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public ConcentratorVersionResponseStructure parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.dateTime = new DateTime(this.clock, getTimeZone()).parse(rawData, ptr);
        ptr += dateTime.getLength();

        this.concentratorModel = new ConcentratorModel().parse(rawData, ptr);
        ptr += concentratorModel.getLength();

        this.serialNumber = new ConcentratorSerialNumber().parse(rawData, ptr);
        ptr += serialNumber.getLength();

        this.firmwareVersion = new ConcentratorFirmwareVersion().parse(rawData, ptr);
        ptr += firmwareVersion.getLength();

        this.customerCode = new CustomerCode().parse(rawData, ptr);
        ptr += customerCode.getLength();

        this.paddingData = new PaddingData(PADDING_DATA_LENGTH).parse(rawData, ptr);
        ptr += paddingData.getLength();

        return this;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public ConcentratorModel getConcentratorModel() {
        return concentratorModel;
    }
    public ConcentratorSerialNumber getSerialNumber() {
        return serialNumber;
    }

    public ConcentratorFirmwareVersion getFirmwareVersion() {
        return firmwareVersion;
    }

    public CustomerCode getCustomerCode() {
        return customerCode;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}