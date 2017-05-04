/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.frame;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Address;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Channel;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Cpa;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Crc;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Profi;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.StructureCode;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.AckStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.ArrayEventsQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.ArrayQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.IdentificationResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.NackStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.RegisterQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.TableDECFQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.TableDECQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.Trace_CQueryResponseStructure;

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
        this(false);
    }

    public AbstractCTRFrame(boolean longFrame) {
        this.address = new Address();
        this.profi = new Profi();
        this.functionCode = new FunctionCode();
        this.structureCode = new StructureCode();
        this.channel = new Channel();
        this.data = new Data(longFrame);
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

    /**
     * Parses a given byte array and creates a frame object
     * @param rawData: the byte array
     * @param offset: position to start in the byte array
     * @return a frame object
     * @throws CTRParsingException
     */
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

        if (getFunctionCode().getEncryptionStatus().isEncrypted()) {
            data = new Data(getProfi().isLongFrame()).parse(rawData, ptr);
        } else {
            parseDataField(rawData, ptr);
        }
        ptr += getData().getLength();

        cpa = new Cpa().parse(rawData, ptr);
        ptr += cpa.getLength();

        crc = new Crc().parse(rawData, ptr);
        ptr += crc.getLength();

        return (T) this;
    }

    /**
     * Check which kind of data field is in the frame.
     * @param rawData: the given byte array
     * @param offset: start position in the array
     * @throws CTRParsingException
     */
    private void parseDataField(byte[] rawData, int offset) throws CTRParsingException {
        int ptr = offset;
        if (isIdentificationReply()) {
            data = new IdentificationResponseStructure(getProfi().isLongFrame()).parse(rawData, ptr);
        } else if (functionCode.isNack()) {
            data = new NackStructure(getProfi().isLongFrame()).parse(rawData, ptr);
        } else if (functionCode.isAck()) {
            data = new AckStructure().parse(rawData, ptr);
        } else if (isRegisterQueryReply()) {
            data = new RegisterQueryResponseStructure(getProfi().isLongFrame()).parse(rawData, ptr);
        } else if (isArrayQueryReply()) {
            data = new ArrayQueryResponseStructure(getProfi().isLongFrame()).parse(rawData, ptr);
        } else if (isTableDECFQueryResponseStructure()) {
            data = new TableDECFQueryResponseStructure(getProfi().isLongFrame()).parse(rawData, ptr);
        } else if (isTableDECQueryResponseStructure()) {
            data = new TableDECQueryResponseStructure(getProfi().isLongFrame()).parse(rawData, ptr);
        } else if (isTrace_CQueryResponseStructure()) {
            data = new Trace_CQueryResponseStructure(getProfi().isLongFrame()).parse(rawData, ptr);
        } else if (isEventArrayQueryReponseStructure()) {
            data = new ArrayEventsQueryResponseStructure(getProfi().isLongFrame()).parse(rawData, ptr);
        } else {
            data = new Data(getProfi().isLongFrame()).parse(rawData, ptr);
        }
    }

    private boolean isTrace_CQueryResponseStructure() {
        return structureCode.isTrace_C() && functionCode.isMeterResponse();
    }

    private boolean isEventArrayQueryReponseStructure() {
        return structureCode.isEvent_Array() && functionCode.isMeterResponse();
    }

    private boolean isTableDECFQueryResponseStructure() {
        return structureCode.isDECFTable() && functionCode.isMeterResponse();
    }

    private boolean isTableDECQueryResponseStructure() {
        return structureCode.isDECTable() && functionCode.isMeterResponse();
    }

    private boolean isIdentificationReply() {
        return structureCode.isIdentification() && functionCode.isIdentificationReply();
    }

    private boolean isRegisterQueryReply() {
        return structureCode.isRegister() && functionCode.isMeterResponse();
    }

    private boolean isArrayQueryReply() {
        return structureCode.isArray() && functionCode.isMeterResponse();
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
        this.cpa = generateCpa(key.clone());
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

        return new Cpa().generateCpa(cpaData, key.clone());
    }

    /**
     * @param key
     * @return
     */
    public boolean validCpa(byte[] key) {
        return getCpa().isUndefined() || generateCpa(key).equals(getCpa());
    }

    /**
     * @return
     */
    public Crc getCrc() {
        return crc;
    }

    /**
     *
     */
    public void setCrc() {
        crc = generateCrc();
    }

    /**
     * Generates a Crc (Cyclic redundancy code)
     * @return a generated Crc
     */
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
