package com.energyict.protocolimplv2.elster.garnet.structure.logbook;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.structure.field.BitMapCollection;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ContactorMode;
import com.energyict.protocolimplv2.elster.garnet.structure.field.MeterSerialNumber;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.ContactorShutdownStatus;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.ContactorStatus;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterInstallationStatusBitMaskField;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class AutomaticContactorOperationEvent extends AbstractField<AutomaticContactorOperationEvent> implements  LogBookEvent{

    public static final int LENGTH = 16;
    private static final int LENGTH_OF_CONTACTOR_STATUSES = 1;
    private static final int NR_OF_METERS = 1;
    private static final int LENGTH_OF_METER_INSTALLATION_STATUSES = 1;

    private MeterSerialNumber meterSerialNumber;
    private BitMapCollection contactorStatusMask;
    private ContactorMode contactorMode;
    private BitMapCollection<MeterInstallationStatusBitMaskField> meterInstallationStatusMask;
    private PaddingData paddingData;

    public AutomaticContactorOperationEvent() {
        this.meterSerialNumber = new MeterSerialNumber();
        this.contactorStatusMask = new BitMapCollection<>(LENGTH_OF_CONTACTOR_STATUSES, NR_OF_METERS, ContactorStatus.class);
        this.contactorMode = new ContactorMode();
        this.meterInstallationStatusMask = new BitMapCollection<>(LENGTH_OF_METER_INSTALLATION_STATUSES, NR_OF_METERS, MeterInstallationStatusBitMaskField.class);
        this.paddingData = new PaddingData(5);
    }

    public AutomaticContactorOperationEvent(MeterSerialNumber meterSerialNumber, BitMapCollection<ContactorStatus> contactorStatusMask, ContactorMode contactorMode, BitMapCollection<MeterInstallationStatusBitMaskField> meterInstallationStatusMask) {
        this.meterSerialNumber = meterSerialNumber;
        this.contactorStatusMask = contactorStatusMask;
        this.contactorMode = contactorMode;
        this.meterInstallationStatusMask = meterInstallationStatusMask;
        this.paddingData = new PaddingData(5);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                meterSerialNumber.getBytes(),
                contactorStatusMask.getBytes(),
                contactorMode.getBytes(),
                meterInstallationStatusMask.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public AutomaticContactorOperationEvent parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.meterSerialNumber.parse(rawData, ptr);
        ptr += meterSerialNumber.getLength();

        this.contactorMode.parse(rawData, ptr + contactorStatusMask.getLength());

        if (contactorMode.getContactorModeCode() == 0) {
            this.contactorStatusMask = new BitMapCollection<>(LENGTH_OF_CONTACTOR_STATUSES, NR_OF_METERS, ContactorShutdownStatus.class);
        } else {
            this.contactorStatusMask = new BitMapCollection<>(LENGTH_OF_CONTACTOR_STATUSES, NR_OF_METERS, ContactorShutdownStatus.class);
        }
        ptr += contactorStatusMask.getLength();
        ptr += contactorMode.getLength();

        this.meterInstallationStatusMask.parse(rawData, ptr);
        ptr += meterInstallationStatusMask.getLength();

        this.paddingData.parse(rawData, ptr);
        ptr += paddingData.getLength();

        this.contactorStatusMask.parse(rawData, ptr);
        ptr += contactorStatusMask.getLength();

        this.paddingData.parse(rawData, ptr);
        ptr += paddingData.getLength();

        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    @Override
    public String getEventDescription() {
        String response = contactorMode.getContactorModeCode() == 1
                ? "Automatic contactor reconnect"
                : " Automatic contactor disconnect";
        response += ": ";
        response += "Meter serial: " + meterSerialNumber.getSerialNumber();
        response += " - Mode: ";
        response += (contactorStatusMask.getAllBitMasks().size() > 0)
                ? ((ContactorStatus) contactorStatusMask.getAllBitMasks().get(0)).getContactorStatusInfo()
                 : getBitStringFromByteArray(contactorStatusMask.getBytes());
        response += " - Installation: ";
        response += (meterInstallationStatusMask.getAllBitMasks().size() > 0)
                ? meterInstallationStatusMask.getAllBitMasks().get(0).getInstallationStatusInfo()
                : getBitStringFromByteArray(meterInstallationStatusMask.getBytes());
        return response;
    }

    public BitMapCollection<MeterInstallationStatusBitMaskField> getMeterInstallationStatusMask() {
        return meterInstallationStatusMask;
    }

    public ContactorMode getContactorMode() {
        return contactorMode;
    }

    public BitMapCollection getContactorStatusMask() {
        return contactorStatusMask;
    }

    public MeterSerialNumber getMeterSerialNumber() {
        return meterSerialNumber;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}