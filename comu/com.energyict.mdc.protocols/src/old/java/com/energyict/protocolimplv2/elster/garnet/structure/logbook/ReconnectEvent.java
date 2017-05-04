/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.logbook;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.structure.field.BitMapCollection;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.AffectedMeterMask;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.ContactorReconnectStatus;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class ReconnectEvent extends AbstractField<ReconnectEvent> implements  LogBookEvent{

    public static final int LENGTH = 16;
    private static final int LENGTH_OF_AFFECTED_METERS = 2;
    private static final int LENGTH_OF_CONTACTOR_STATUSES = 3;
    private static final int NR_OF_METERS = 12;

    private BitMapCollection<AffectedMeterMask> affectedMetersMask;
    private BitMapCollection<ContactorReconnectStatus> contactorStatusMask;
    private PaddingData paddingData;

    public ReconnectEvent() {
        this.affectedMetersMask = new BitMapCollection<>(LENGTH_OF_AFFECTED_METERS, NR_OF_METERS, AffectedMeterMask.class);
        this.contactorStatusMask = new BitMapCollection<>(LENGTH_OF_CONTACTOR_STATUSES, NR_OF_METERS, ContactorReconnectStatus.class);
        this.paddingData = new PaddingData(11);
    }

    public ReconnectEvent(BitMapCollection<AffectedMeterMask> affectedMetersMask, BitMapCollection<ContactorReconnectStatus> contactorStatusMask) {
        this.affectedMetersMask = affectedMetersMask;
        this.contactorStatusMask = contactorStatusMask;
        this.paddingData = new PaddingData(11);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                affectedMetersMask.getBytes(),
                contactorStatusMask.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public ReconnectEvent parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.affectedMetersMask.parse(rawData, ptr);
        ptr += affectedMetersMask.getLength();

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
        return "Contactor reconnect: " +
                "Mask: " + getBitStringFromByteArray(affectedMetersMask.getBytes()) +
                " - Response: " + getBitStringFromByteArray(contactorStatusMask.getBytes());
    }

    public BitMapCollection<ContactorReconnectStatus> getContactorStatusMask() {
        return contactorStatusMask;
    }

    public BitMapCollection<AffectedMeterMask> getAffectedMetersMask() {
        return affectedMetersMask;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}