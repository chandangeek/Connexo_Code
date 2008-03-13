/*
 * AbstractPLCCObject.java
 *
 * Created on 16 oktober 2007, 14:44
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import com.energyict.dlms.cosem.DataAccessResultException;
import java.io.IOException;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.obis.ObisCode;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.ObjectIdentification;


abstract public class AbstractPLCCObject { 

    abstract protected ObjectIdentification getId();
    abstract protected void doInvoke() throws IOException;
    
    private PLCCObjectFactory pLCCObjectFactory;
    
    /** Creates a new instance of AbstractPLCCObject */
    
    public AbstractPLCCObject(PLCCObjectFactory pLCCObjectFactory) {
        this.setPLCCObjectFactory(pLCCObjectFactory);
    }

    public void invoke() throws IOException {
        int retries=0;
//        while(true) {
            try {
               doInvoke();
               return;
            }
            catch(DataAccessResultException e) {
                DataAccessResult o = DataAccessResult.findDataAccessResult(e.getDataAccessResult());
                //if ((!o.isCPLError()) || (retries++>=1)) {
                // KV_TO_DO
            // action f(data access result)
                if (e.isEvalDataAccessResultStandard())
                    throw e;
                else
                    throw new DataAccessResultException(e.getDataAccessResult(),o.toString());
                //}
            }
  //      }
    }
    
    public ObisCode getObisCode( ) {
        if( getId() != null )
            return getId().getObisCode();
        return null;
    }
    
    public int getClassId( ){
        if( getId() != null )
            return getId().getClassId();
        return 1;
    }
    
    public PLCCObjectFactory getPLCCObjectFactory( ){
        return pLCCObjectFactory;
    }
    
    public CosemObjectFactory getCosemObjectFactory( ) {
        return getPLCCObjectFactory().getCosemObjectFactory();
    }
    
    public AbstractDataType readGeneric( ) throws IOException {
        return 
            AXDRDecoder.decode( 
                getCosemObjectFactory()
                    .getProfileGeneric( getObisCode() )
                        .getBufferData() );
        
    }

    private void setPLCCObjectFactory(PLCCObjectFactory pLCCObjectFactory) {
        this.pLCCObjectFactory = pLCCObjectFactory;
    }
    
    
    
}