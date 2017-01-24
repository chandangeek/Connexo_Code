package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Code;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Group;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Identify;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Segment;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;

/**
 * Copyrights EnergyICT
 * Date: 10/10/11
 * Time: 16:37
 */
public class DownloadRequestStructure extends Data<DownloadRequestStructure> {


    private ReferenceDate dateOfValidity;
    private WriteDataBlock writeDataBlock;
    private Identify identify;
    private Group group_s;
    private Group group_c;
    private Segment segment;
    private Code code;


    public DownloadRequestStructure(boolean longFrame) {
        super(longFrame);
    }

     @Override
    public byte[] getBytes() {
        return padData(ProtocolTools.concatByteArrays(
                dateOfValidity.getBytes(),
                writeDataBlock.getBytes(),
                identify.getBytes(),
                group_s.getBytes(),
                group_c.getBytes(),
                segment.getBytes(),
                code.getBytes()
        ));
    }

    /**
     * Create a CTR Structure Object representing the given byte array
     * @param rawData: a given byte array
     * @param offset: the start position in the array
     * @return the CTR Structure Object
     * @throws CTRParsingException
     */
    @Override
    public DownloadRequestStructure parse(byte[] rawData, int offset) throws CTRParsingException {

        int ptr = offset;
        dateOfValidity = new ReferenceDate().parse(rawData, ptr);
        ptr += 3;
        writeDataBlock = new WriteDataBlock().parse(rawData, ptr);
        ptr += 1;
        identify = new Identify().parse(rawData, ptr);
        ptr +=4;
        group_s = new Group().parse(rawData, ptr);
        ptr += 1;
        group_c = new Group().parse(rawData, ptr);
        ptr += 1;
        segment = new Segment().parse(rawData, ptr);
        ptr += 2;
        code = new Code().parse(rawData, ptr);
        return this;
    }

    public ReferenceDate getDateofValidity() {
        return dateOfValidity;
    }

    public void setDateofValidity(ReferenceDate DateofValidity) {
        this.dateOfValidity = DateofValidity;
    }

    public WriteDataBlock getWriteDataBlock() {
        return writeDataBlock;
    }

    public void setWriteDataBlock(WriteDataBlock WriteDataBlock) {
        this.writeDataBlock = WriteDataBlock;
    }

    public Identify getIdentify() {
        return identify;
    }

    public void setIdentify(Identify Identify) {
        this.identify = Identify;
    }

    public Segment getSegment() {
        return segment;
    }

    public void setSegment(Segment Segment) {
        this.segment = Segment;
    }

    public Code getCode() {
        return code;
    }

    public void setCode(Code Code) {
        this.code = Code;
    }
}
