package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.ObjectIdentification;


public class PLCCSelectedMeter extends AbstractPLCCObject {

    private String meterSerialNumber;
    private MeterReadMode meterReadMode;
    
    public PLCCSelectedMeter(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    public PLCCSelectedMeter(PLCCObjectFactory objectFactory, String serial) {
        super(objectFactory);
        this.meterSerialNumber = serial;
    }
    
    protected void doInvoke() throws IOException {
        getCosemObjectFactory().writeObject(getObisCode(),getClassId(), 2,toAbstractDataType().getBEREncodedByteArray() );
    }
    
    protected ObjectIdentification getId() {
        return new ObjectIdentification( "0.0.145.3.11.255", 1 );
    }

    public void setMeterSerialNumber(String serial) {
        this.meterSerialNumber = serial;
    }
    
    public void setMeterReadMode(MeterReadMode mrm) {
        this.meterReadMode = mrm;
    }
    
    public MeterReadMode getMeterReadMode( ) {
        return meterReadMode;
    }

    private AbstractDataType toAbstractDataType() {
        return new Structure()
                    .addDataType( new VisibleString(meterSerialNumber,20))
                    .addDataType( new TypeEnum(0) );
    }
    
}
