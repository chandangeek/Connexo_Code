package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;

public class SpecialDaysTable extends AbstractCosemObject {

    	/* Method writes SN */
	private static final int INSERT_SPECIAL_DAY_SN = 0x10;
	private static final int DELETE_SPECIAL_DAY_SN = 0x18;


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
        if (getObjectReference().isLNReference()) {
            invoke(1, structure.getBEREncodedByteArray());
        } else {
            write(INSERT_SPECIAL_DAY_SN, structure.getBEREncodedByteArray());
        }
    }

    public void delete(int index) throws IOException {
    	Unsigned16 u16 = new Unsigned16(index);
        invoke(2,u16.getBEREncodedByteArray());
    }

}
