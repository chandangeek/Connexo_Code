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

        address = new Address().parse(rawData, ptr);
        ptr += Address.LENGTH;

        profi = new Profi().parse(rawData, ptr);
        ptr += Profi.LENGTH;

        functionCode = new FunctionCode().parse(rawData, ptr);
        ptr += FunctionCode.LENGTH;

        structureCode = new StructureCode().parse(rawData, ptr);
        ptr += StructureCode.LENGTH;

        channel = new Channel().parse(rawData, ptr);
        ptr += Channel.LENGTH;

        data = new Data().parse(rawData, ptr);
        ptr += Data.LENGTH;

        cpa = new Cpa().parse(rawData, ptr);
        ptr += Cpa.LENGTH;

        crc = new Crc().parse(rawData, ptr);
        ptr += Crc.LENGTH;

        return (T) this;
    }


    public void setCrc() {
        crc = generateCrc();
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
        return new Crc().generateAndSetCrc(crcData);
    }

    public boolean isValidCrc() {
        return generateCrc().getCrc() == crc.getCrc();
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Cpa getCpa() {
        return cpa;
    }

    public void setCpa(Cpa cpa) {
        this.cpa = cpa;
    }

    public Crc getCrc() {
        return crc;
    }

    public void setCrc(Crc crc) {
        this.crc = crc;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public FunctionCode getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(FunctionCode functionCode) {
        this.functionCode = functionCode;
    }

    public Profi getProfi() {
        return profi;
    }

    public void setProfi(Profi profi) {
        this.profi = profi;
    }

    public StructureCode getStructureCode() {
        return structureCode;
    }

    public void setStructureCode(StructureCode structureCode) {
        this.structureCode = structureCode;
    }

}
