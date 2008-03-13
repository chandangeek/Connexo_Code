package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import com.energyict.dlms.axrdencoding.*;
import java.io.IOException;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ObjectIdentification;
import java.math.*;

public class PLCCMeterContactorState extends AbstractPLCCObject {

    private Data data;
    Unsigned8 value=null;
    
    public PLCCMeterContactorState(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification( "0.0.128.30.22.255", 1 );
    }
    
    protected void doInvoke() throws IOException {
        data = getCosemObjectFactory().getData( getObisCode() );
    }
    
    // this broadcasts a timesync to all meters...
    public void controlBreaker(int val)  throws IOException {
        if (value == null)
            value = new Unsigned8(val);
        else
            value.setValue(val);
        data.invoke(129, value.getBEREncodedByteArray());
    }    
    
    public int readState() throws IOException {
        value = (Unsigned8)AXDRDecoder.decode(data.getData());
        return value.getValue();
    }
    
    public void writeState(int state) throws IOException {
        if (value == null)
            value = new Unsigned8(state);
        else
            value.setValue(state);
        byte [] ber = value.getBEREncodedByteArray();
        getCosemObjectFactory().writeObject(getObisCode(), getClassId(), 2, ber);
    }
    
}
