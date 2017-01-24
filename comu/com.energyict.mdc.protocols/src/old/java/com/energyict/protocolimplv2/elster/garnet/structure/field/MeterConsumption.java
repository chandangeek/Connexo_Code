package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 15:58
 */
public class MeterConsumption extends AbstractField<MeterConsumption> {

    public static final int LENGTH = 16;
    public static final int SINGLE_CONSUMPTION_LENGTH = 4;

    private MeterSerialNumber serialNumber;
    private int activeEnergy;
    private int reactiveEnergy;

    public MeterConsumption() {
        this.serialNumber = new MeterSerialNumber();
        this.activeEnergy = 0;
        this.reactiveEnergy = 0;
    }

    @Override
    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                serialNumber.getBytes(),
                getBytesFromInt(activeEnergy, SINGLE_CONSUMPTION_LENGTH),
                getBytesFromInt(reactiveEnergy, SINGLE_CONSUMPTION_LENGTH)
        );
    }

    @Override
    public MeterConsumption parse(byte[] rawData, int offset) throws ParsingException {
        int ptr = offset;

        serialNumber = new MeterSerialNumber().parse(rawData, ptr);
        ptr += serialNumber.getLength();

        activeEnergy = getIntFromBytes(rawData, ptr, SINGLE_CONSUMPTION_LENGTH);
        ptr += SINGLE_CONSUMPTION_LENGTH;

        reactiveEnergy = getIntFromBytes(rawData, ptr, SINGLE_CONSUMPTION_LENGTH);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public MeterSerialNumber getSerialNumber() {
        return serialNumber;
    }

    public int getActiveEnergy() {
        return activeEnergy;
    }

    public int getReactiveEnergy() {
        return reactiveEnergy;
    }
}