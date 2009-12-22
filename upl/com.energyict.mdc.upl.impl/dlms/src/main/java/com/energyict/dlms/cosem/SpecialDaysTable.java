package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;

public class SpecialDaysTable extends AbstractCosemObject {

	private Array specialDays = null;

	public SpecialDaysTable(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }

    protected int getClassId() {
        return DLMSClassId.SPECIAL_DAYS_TABLE.getClassId();
    }

    public void writeSpecialDays(Array specialDays) throws IOException {
        write(2, specialDays.getBEREncodedByteArray());
        this.specialDays = specialDays;
    }

    public Array readSpecialDays() throws IOException {
        if (specialDays == null) {
        	specialDays = (Array) AXDRDecoder.decode(getLNResponseData(2));
        }
        return specialDays;
    }

    public void insert(Structure structure) throws IOException {
        invoke(1,structure.getBEREncodedByteArray());
    }

    public void delete(int index) throws IOException {
    	Unsigned16 u16 = new Unsigned16(index);
        invoke(2,u16.getBEREncodedByteArray());
    }

}
