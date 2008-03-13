package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.ObjectIdentification;

public class PLCCMeterList extends AbstractPLCCObject {
 
    private List meterList;
    
    public PLCCMeterList(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }
    
    public List getMeterList( ) throws IOException {
        return meterList;
    }

    protected void doInvoke() throws IOException {
        meterList = new ArrayList();
        AbstractDataType dt = readGeneric();

        Array array = (Array)dt.getArray(); 
        int size = array.nrOfDataTypes();

        for( int i = 0; i < size; i ++ ){
            addMeter( getMeterStructure(array, i) );
        }
        //System.out.println( dt );
    }
    
    private Structure getMeterStructure(Array array, int idx) {
        AbstractDataType adt = (AbstractDataType) array.getDataType( idx );
        Structure structure = (Structure)adt;
        Structure meterStructure = (Structure)structure.getDataType(1);
        
        return meterStructure;
    }

    private boolean addMeter(Structure meterStructure) {
        return meterList.add( toPLCCMeterListBlocData(meterStructure) );
    }

    private PLCCMeterListBlocData toPLCCMeterListBlocData(Structure structure) {
        return new PLCCMeterListBlocData(getPLCCObjectFactory(), structure );
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification( "0.0.98.141.0.255", 7 );
    }

    protected AbstractDataType toAbstractDataType() {
        // TODO Auto-generated method stub
        return null;
    }
    
}
