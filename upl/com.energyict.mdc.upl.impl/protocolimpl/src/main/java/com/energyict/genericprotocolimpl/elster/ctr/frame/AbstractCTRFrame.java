package com.energyict.genericprotocolimpl.elster.ctr.frame;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.structure.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 29-sep-2010
 * Time: 16:46:07
 */
public class AbstractCTRFrame<T extends AbstractCTRFrame> extends AbstractField<T> implements Frame<T> {

    private Address address;
    private Profi profi;
    private FunctionCode functionCode;
    private StructureCode structureCode;
    private Channel channel;
    private Data data;
    private Cpa cpa;
    private Crc crc;

    public AbstractCTRFrame() {
        this.address = new Address();
        this.profi = new Profi();
        this.functionCode = new FunctionCode();
        this.structureCode = new StructureCode();
        this.channel = new Channel();
        this.data = new Data(false);
        this.cpa = new Cpa();
        this.crc = new Crc();
    }

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

    public T parse(byte[] rawData, int offset) throws CTRParsingException {
        int ptr = offset;

        address = new Address().parse(rawData, ptr);
        ptr += address.getLength();

        profi = new Profi().parse(rawData, ptr);
        ptr += profi.getLength();

        functionCode = new FunctionCode().parse(rawData, ptr);
        ptr += functionCode.getLength();

        structureCode = new StructureCode().parse(rawData, ptr);
        ptr += structureCode.getLength();

        channel = new Channel().parse(rawData, ptr);
        ptr += channel.getLength();

        parseDataField(rawData, ptr);
        ptr += getData().getLength();

        cpa = new Cpa().parse(rawData, ptr);
        ptr += cpa.getLength();

        crc = new Crc().parse(rawData, ptr);
        ptr += crc.getLength();

        return (T) this;
    }

    private void parseDataField(byte[] rawData, int offset) throws CTRParsingException {
        if (isIdentificationReply()) {
            data = new IdentificationResponseStructure(getProfi().isLongFrame()).parse(rawData, offset);
        } else if (functionCode.isNack()) {
            data = new NackStructure(getProfi().isLongFrame()).parse(rawData, offset);
        } else if (functionCode.isAck()) {
            data = new AckStructure().parse(rawData, offset);
        } else if (isRegisterQueryReply()) {
            data = new RegisterQueryResponseStructure(getProfi().isLongFrame()).parse(rawData, offset);
        } else if (isArrayQueryReply()) {
            data = new ArrayQueryResponseStructure(getProfi().isLongFrame()).parse(rawData, offset);
        } else if (isTableDECFQueryResponseStructure()) {
            data = new TableDECFQueryResponseStructure(getProfi().isLongFrame()).parse(rawData, offset);
        }
        else {
            data = new Data(getProfi().isLongFrame()).parse(rawData, offset);
        }
    }

    private boolean isTableDECFQueryResponseStructure() {
        return structureCode.isDECFTable() && functionCode.isAnswer();
    }

    private boolean isIdentificationReply() {
        return structureCode.isIdentification() && functionCode.isIdentificationReply();
    }

    private boolean isRegisterQueryReply() {
        return structureCode.isRegister() && functionCode.isAnswer();
    }

    private boolean isArrayQueryReply() {
        return structureCode.isArray() && functionCode.isAnswer();
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

    public void generateAndSetCpa(byte[] key) {
        this.cpa = generateCpa(key);
    }

    private Cpa generateCpa(byte[] key) {
        byte[] cpaData = ProtocolTools.concatByteArrays(
                address.getBytes(),
                profi.getBytes(),
                functionCode.getBytes(),
                structureCode.getBytes(),
                channel.getBytes(),
                data.getBytes()
        );
        return new Cpa().generateCpa(cpaData, key);
    }

    public boolean validCpa(byte[] key) {
        return (getCpa().getCpa() == 0) || (generateCpa(key).getCpa() == cpa.getCpa());
    }

    public Crc getCrc() {
        return crc;
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
                data.getBytes(),
                cpa.getBytes()
        );
        return new Crc().generateAndSetCrc(crcData);
    }

    public boolean isValidCrc() {
        return generateCrc().getCrc() == crc.getCrc();
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

    public T doParse() throws CTRParsingException {
        return parse(getBytes(), 0);
    }

    public int getLength() {
        return address.getLength() +
                profi.getLength() +
                functionCode.getLength() +
                structureCode.getLength() +
                channel.getLength() +
                data.getLength() +
                cpa.getLength() +
                crc.getLength();
    }
}
