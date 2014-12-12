package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Part;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ContactorMode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.DateTime;
import com.energyict.protocolimplv2.elster.garnet.structure.field.MeterSerialNumber;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;
import com.energyict.protocolimplv2.elster.garnet.structure.field.UserId;

import java.time.Clock;
import java.util.TimeZone;

/**
 * @author sva
 * @since 27/05/2014 - 13:57
 */
public class ContactorRequestStructure extends Data<ContactorRequestStructure> {

    private static final FunctionCode FUNCTION_CODE = FunctionCode.CONTACTOR_REQUEST;
    private static final int PADDING_DATA_LENGTH = 2;

    private DateTime dateTime;
    private Part disp;
    private UserId userId;
    private MeterSerialNumber serialNumber;
    private ContactorMode mode;
    private PaddingData paddingData;

   private final TimeZone timeZone;

    public ContactorRequestStructure(Clock clock, TimeZone timeZones) {
        super(FUNCTION_CODE);
        this.timeZone = timeZones;
        this.dateTime = new DateTime(clock, timeZone);
        this.disp = new Part();
        this.userId = new UserId();
        this.serialNumber = new MeterSerialNumber();
        this.mode = new ContactorMode();
        this.paddingData = new PaddingData(PADDING_DATA_LENGTH);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                dateTime.getBytes(),
                disp.getBytes(),
                userId.getBytes(),
                serialNumber.getBytes(),
                mode.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public ContactorRequestStructure parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.dateTime.parse(rawData, ptr);
        ptr += dateTime.getLength();

        this.disp.parse(rawData, ptr);
        ptr += disp.getLength();

        this.userId.parse(rawData, ptr);
        ptr += userId.getLength();

        this.serialNumber.parse(rawData, ptr);
        ptr += serialNumber.getLength();

        this.mode.parse(rawData, ptr);
        ptr += mode.getLength();

        this.paddingData.parse(rawData, ptr);
        return this;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Part getDisp() {
        return disp;
    }

    public void setDisp(Part disp) {
        this.disp = disp;
    }

    public UserId getUserId() {
        return userId;
    }

    public void setUserId(UserId userId) {
        this.userId = userId;
    }

    public MeterSerialNumber getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(MeterSerialNumber serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber.setSerialNumber(serialNumber);
    }

    public ContactorMode getMode() {
        return mode;
    }

    public void setMode(ContactorMode mode) {
        this.mode = mode;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }

    public void setPaddingData(PaddingData paddingData) {
        this.paddingData = paddingData;
    }
}