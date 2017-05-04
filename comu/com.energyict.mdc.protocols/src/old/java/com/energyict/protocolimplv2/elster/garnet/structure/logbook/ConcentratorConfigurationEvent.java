/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.logbook;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;
import com.energyict.protocolimplv2.elster.garnet.structure.field.BitMapCollection;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ConcentratorConfiguration;
import com.energyict.protocolimplv2.elster.garnet.structure.field.PaddingData;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.AffectedMeterMask;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class ConcentratorConfigurationEvent extends AbstractField<ConcentratorConfigurationEvent> implements  LogBookEvent{

    public static final int LENGTH = 16;
    private static final int LENGTH_OF_AFFECTED_METERS = 2;
    private static final int NR_OF_METERS = 12;

    private BitMapCollection<AffectedMeterMask> affectedMetersMask;
    private ConcentratorConfiguration concentratorConfiguration;
    private PaddingData paddingData;

    public ConcentratorConfigurationEvent() {
        this.affectedMetersMask = new BitMapCollection<>(LENGTH_OF_AFFECTED_METERS, NR_OF_METERS, AffectedMeterMask.class);
        this.concentratorConfiguration = new ConcentratorConfiguration();
        this.paddingData = new PaddingData(14);
    }

    public ConcentratorConfigurationEvent(BitMapCollection<AffectedMeterMask> affectedMetersMask, ConcentratorConfiguration concentratorConfiguration) {
        this.affectedMetersMask = affectedMetersMask;
        this.concentratorConfiguration = concentratorConfiguration;
        this.paddingData = new PaddingData(14);
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                affectedMetersMask.getBytes(),
                concentratorConfiguration.getBytes(),
                paddingData.getBytes()
        );
    }

    @Override
    public ConcentratorConfigurationEvent parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        this.affectedMetersMask.parse(rawData, ptr);
        ptr += affectedMetersMask.getLength();

        this.concentratorConfiguration.parse(rawData, ptr);
        ptr += concentratorConfiguration.getLength();

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
        return "Concentrator config has changed: " +
                "Mask: " + getBitStringFromByteArray(affectedMetersMask.getBytes()) +
                " - New config: " + getBitStringFromByteArray(concentratorConfiguration.getBytes());
    }

    public BitMapCollection<AffectedMeterMask> getAffectedMetersMask() {
        return affectedMetersMask;
    }

    public ConcentratorConfiguration getConcentratorConfiguration() {
        return concentratorConfiguration;
    }

    public PaddingData getPaddingData() {
        return paddingData;
    }
}