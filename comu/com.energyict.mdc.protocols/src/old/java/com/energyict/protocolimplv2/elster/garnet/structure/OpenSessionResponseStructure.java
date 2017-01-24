package com.energyict.protocolimplv2.elster.garnet.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.frame.field.Data;
import com.energyict.protocolimplv2.elster.garnet.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ConcentratorModel;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ConcentratorSerialNumber;
import com.energyict.protocolimplv2.elster.garnet.structure.field.DateTime;
import com.energyict.protocolimplv2.elster.garnet.structure.field.SessionKeyPart;
import com.energyict.protocolimplv2.elster.garnet.structure.field.Version;

import java.time.Clock;
import java.util.TimeZone;

/**
 * @author sva
 * @since 27/05/2014 - 10:29
 */
public class OpenSessionResponseStructure extends Data<OpenSessionResponseStructure> {

    public static final FunctionCode FUNCTION_CODE = FunctionCode.OPEN_SESSION_RESPONSE;

    private final Clock clock;
    private DateTime dateTime;
    private SessionKeyPart secondPartOfSessionKey;
    private ConcentratorModel concentratorModel;
    private Version version;
    private ConcentratorSerialNumber serialNumber;

    private final TimeZone timeZone;

    public OpenSessionResponseStructure(Clock clock, TimeZone timeZone) {
        super(FUNCTION_CODE);
        this.clock = clock;
        this.timeZone = timeZone;
        this.dateTime = new DateTime(clock, timeZone);
        this.secondPartOfSessionKey = new SessionKeyPart();
        this.secondPartOfSessionKey.generateRandomHalfOfSessionKey();
        this.concentratorModel = new ConcentratorModel();
        this.version = new Version();
        this.serialNumber = new ConcentratorSerialNumber();
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                dateTime.getBytes(),
                secondPartOfSessionKey.getBytes(),
                concentratorModel.getBytes(),
                serialNumber.getBytes(),
                version.getBytes()
        );
    }

    @Override
    public OpenSessionResponseStructure parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.dateTime = new DateTime(this.clock, getTimeZone()).parse(rawData, ptr);
        ptr += dateTime.getLength();

        this.secondPartOfSessionKey = new SessionKeyPart().parse(rawData, ptr);
        ptr += secondPartOfSessionKey.getLength();

        this.concentratorModel = new ConcentratorModel().parse(rawData, ptr);
        ptr += concentratorModel.getLength();

        this.serialNumber = new ConcentratorSerialNumber().parse(rawData, ptr);
        ptr += serialNumber.getLength();

        this.version = new Version().parse(rawData, ptr);
        ptr += version.getLength();

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

    public SessionKeyPart getSecondPartOfSessionKey() {
        return secondPartOfSessionKey;
    }

    public ConcentratorModel getConcentratorModel() {
        return concentratorModel;
    }

    public Version getVersion() {
        return version;
    }

    public ConcentratorSerialNumber getSerialNumber() {
        return serialNumber;
    }
}