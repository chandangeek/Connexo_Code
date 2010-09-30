package com.energyict.genericprotocolimpl.elster.ctr.frame;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 16:46:07
 */
public class AbstractCTRFrame<T extends AbstractCTRFrame> extends AbstractField<T> implements Frame<T> {

    public static final int LENGTH = 128 + 12;

    private Address address;
    private Profi profi;
    private FunctionCode functionCode;
    private StructureCode structureCode;
    private Channel channel;
    private Data data;
    private Cpa cpa;
    private Crc crc;

    public byte[] getBytes() {
        return ProtocolTools.concatByteArrays(
                address.getBytes(),
                profi.getBytes(),
                functionCode.getBytes(),
                structureCode.getBytes(),
                channel.getBytes(),
                data.getBytes(),
                cpa.getBytes(),
                crc.getBytes()
        );
    }

    public T parse(byte[] rawData, int offset) {
        int ptr = offset;

        address = new Address().parse(rawData, offset);
        ptr += Address.LENGTH;

        profi = new Profi().parse(rawData, offset);
        ptr += Profi.LENGTH;

        functionCode = new FunctionCode().parse(rawData, offset);
        ptr += FunctionCode.LENGTH;

        structureCode = new StructureCode().parse(rawData, offset);
        ptr += StructureCode.LENGTH;

        channel = new Channel().parse(rawData, offset);
        ptr += Channel.LENGTH;

        data = new Data().parse(rawData, offset);
        ptr += Data.LENGTH;

        cpa = new Cpa().parse(rawData, offset);
        ptr += Cpa.LENGTH;

        crc = new Crc().parse(rawData, offset);
        ptr += Crc.LENGTH;

        return (T) this;
    }

    public Crc generateCrc() {
        byte[] crcData = ProtocolTools.concatByteArrays(
                address.getBytes(),
                profi.getBytes(),
                functionCode.getBytes(),
                structureCode.getBytes(),
                channel.getBytes(),
                data.getBytes()
        );
        return new Crc().calculateAndSetCrc(crcData);
    }

    public boolean isValidCrc() {
        return generateCrc().getCrc() == crc.getCrc();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " = " + ProtocolTools.getHexStringFromBytes(getBytes());
    }

}
