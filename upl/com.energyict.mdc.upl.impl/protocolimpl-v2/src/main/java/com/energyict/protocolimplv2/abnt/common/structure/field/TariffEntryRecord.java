package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class TariffEntryRecord extends AbstractField<TariffEntryRecord> {

    public static final int LENGTH = 4;

    private String tariffActivationTime;
    private String tariffDeactivationTime;

    public TariffEntryRecord() {
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                getBCDFromHexString(tariffActivationTime, LENGTH/2),
                getBCDFromHexString(tariffDeactivationTime, LENGTH/2)
        );
    }

    @Override
    public TariffEntryRecord parse(byte[] rawData, int offset) throws ParsingException {
        tariffActivationTime = getHexStringFromBCD(rawData, offset, LENGTH/2);
        tariffDeactivationTime = getHexStringFromBCD(rawData, offset + LENGTH / 2, LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public String getTariffActivationTime() {
        return tariffActivationTime;
    }

    public String getTariffDeactivationTime() {
        return tariffDeactivationTime;
    }
}