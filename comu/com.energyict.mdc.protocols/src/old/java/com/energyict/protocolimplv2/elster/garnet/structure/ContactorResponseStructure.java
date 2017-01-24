package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ContactorFeedback;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ContactorMode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.DateTime;
import com.energyict.protocolimplv2.elster.garnet.structure.field.MeterInstallationStatusField;
import com.energyict.protocolimplv2.elster.garnet.structure.field.MeterSerialNumber;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;

import java.time.Clock;
import java.util.TimeZone;

/**
 * @author sva
 * @since 27/05/2014 - 13:57
 */
public class ContactorResponseStructure extends Data<ContactorResponseStructure> {

    private static final FunctionCode FUNCTION_CODE = FunctionCode.CONTACTOR_RESPONSE;
    private static final int NR_OF_METERS = 3;
    private static final int LENGTH_OF_CONTACTOR_STATUS_MASK = 1;
    private static final int PADDING_DATA_LENGTH = 5;

    private final DateTime dateTime;
    private MeterSerialNumber serialNumber;
    private ContactorFeedback contactorFeedback;
    private ContactorMode mode;
    private MeterInstallationStatusField meterInstallationStatus;
    private PaddingData paddingData;

    private final TimeZone timeZone;
    private final FunctionCode functionCode;

    public ContactorResponseStructure(Clock clock, TimeZone timeZones, FunctionCode functionCode) {
        super(FUNCTION_CODE);
        this.timeZone = timeZones;
        this.functionCode = functionCode;
        this.dateTime = new DateTime(clock, timeZone);
        this.serialNumber = new MeterSerialNumber();
        this.contactorFeedback = new ContactorFeedback();
        this.mode = new ContactorMode();
        this.meterInstallationStatus = new MeterInstallationStatusField();
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                dateTime.getBytes(),
                serialNumber.getBytes(),
                contactorFeedback.getBytes(),
                mode.getBytes(),
                meterInstallationStatus.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public ContactorResponseStructure parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.dateTime.parse(rawData, ptr);
        ptr += dateTime.getLength();

        this.serialNumber.parse(rawData, ptr);
        ptr += serialNumber.getLength();

        this.contactorFeedback.parse(rawData, ptr);
        ptr += contactorFeedback.getLength();

        this.mode.parse(rawData, ptr);
        ptr += mode.getLength();

        this.meterInstallationStatus.parse(rawData, ptr);
        ptr += meterInstallationStatus.getLength();

        this.paddingData.parse(rawData, ptr);
        return this;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public FunctionCode getFunctionCode() {
        return functionCode;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public MeterSerialNumber getSerialNumber() {
        return serialNumber;
    }

    public ContactorFeedback getContactorFeedback() {
        return contactorFeedback;
    }

    public ContactorMode getMode() {
        return mode;
    }

    public MeterInstallationStatusField getMeterInstallationStatus() {
        return meterInstallationStatus;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}