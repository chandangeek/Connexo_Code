package com.energyict.protocolimplv2.elster.garnet.structure.logbook;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.structure.field.BitMapCollection;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.AffectedMeterMask;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterInstallationStatusBitMaskField;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class CustomerConfigurationEvent extends AbstractField<CustomerConfigurationEvent> implements  LogBookEvent{

    public static final int LENGTH = 16;
    private static final int LENGTH_OF_AFFECTED_METERS = 2;
    private static final int NR_OF_METERS = 12;
    private static final int LENGTH_OF_METER_INSTALLATION_STATUSES = 3;

    private BitMapCollection<AffectedMeterMask> affectedMetersMask;
    private BitMapCollection<MeterInstallationStatusBitMaskField> meterInstallationStatusMask;
    private PaddingData paddingData;

    public CustomerConfigurationEvent() {
        this.affectedMetersMask = new BitMapCollection<>(LENGTH_OF_AFFECTED_METERS, NR_OF_METERS, AffectedMeterMask.class);
        this.meterInstallationStatusMask = new BitMapCollection<>(LENGTH_OF_METER_INSTALLATION_STATUSES, NR_OF_METERS, MeterInstallationStatusBitMaskField.class);
        this.paddingData = new PaddingData(11);
    }

    public CustomerConfigurationEvent(BitMapCollection<AffectedMeterMask> affectedMetersMask, BitMapCollection<MeterInstallationStatusBitMaskField> meterInstallationStatusMask) {
        this.affectedMetersMask = affectedMetersMask;
        this.meterInstallationStatusMask = meterInstallationStatusMask;
        this.paddingData = new PaddingData(11);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                affectedMetersMask.getBytes(),
                meterInstallationStatusMask.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public CustomerConfigurationEvent parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.affectedMetersMask.parse(rawData, ptr);
        ptr += affectedMetersMask.getLength();

        this.meterInstallationStatusMask.parse(rawData, ptr);
        ptr += meterInstallationStatusMask.getLength();

        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    @Override
    public String getEventDescription() {
        return "Installation config has changed: " +
                "Mask: " + getBitStringFromByteArray(affectedMetersMask.getBytes()) +
                " - New installation config: " + getBitStringFromByteArray(meterInstallationStatusMask.getBytes());
    }

    public BitMapCollection<AffectedMeterMask> getAffectedMetersMask() {
        return affectedMetersMask;
    }

    public BitMapCollection<MeterInstallationStatusBitMaskField> getMeterInstallationStatusMask() {
        return meterInstallationStatusMask;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}