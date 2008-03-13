package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;

import java.io.*;

public class PLCCMeterListBlocData extends AbstractPLCCObject {

    private String meterSerialNumber;
    private String meterType;
    private String meterVersion;
    private String plceAssociatedSerialNumber;
    private MeterAvailabilityStatus meterAvailabilityStatus;
    
    public PLCCMeterListBlocData(PLCCObjectFactory objectFactory, Structure structure) {
        
        super(objectFactory);
        
        meterSerialNumber = extractString(structure, 0);
        meterType = extractString(structure, 1);
        meterVersion = extractString(structure, 2);
        plceAssociatedSerialNumber = extractString(structure, 3);
       
        int s = structure.getDataType(5).getTypeEnum().getValue();
        meterAvailabilityStatus = MeterAvailabilityStatus.get(s);
        
    }

    protected void doInvoke() throws IOException {
        
    }
    
    protected ObjectIdentification getId() {
        return null;
    }
    
    private String extractString(Structure structure, int idx) {
        String result = structure.getDataType(idx).getVisibleString().getStr();

        if( result != null ) 
            result = result.trim();
        
        return result;
    }

    
    public String getSerialNumber() {
        return meterSerialNumber;
    }

    public String getType() {
        return meterType;
    }

    public String getVersion() {
        return meterVersion;
    }

    public String getPlceAssociatedSerialNumber() {
        return plceAssociatedSerialNumber;
    }

    public MeterAvailabilityStatus getAvailabilityStatus() {
        return meterAvailabilityStatus;
    }
    
    public String toString( ){
        return "PLCCMeterListBlocData [ " +
            "serialNumber " + meterSerialNumber + ", " +
            "type " + meterType + ", " + 
            "version " + meterVersion + ", " + 
            "plceAssociatedSerialNumber " + plceAssociatedSerialNumber + ", " +
            meterAvailabilityStatus +
            "]";
    }

    protected AbstractDataType toAbstractDataType() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
