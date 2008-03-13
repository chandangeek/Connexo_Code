/*
 * PLCCPLCEquipmentList.java
 *
 * Created on 16 oktober 2007, 9:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import com.energyict.dlms.cosem.ObjectIdentification;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.ProfileGeneric;


/**
 *
 * @author kvds
 */
public class PLCCPLCEquipmentList extends AbstractPLCCObject {
    
    List equipmentList;
    ProfileGeneric profileGeneric;
    
    /**
     * Creates a new instance of PLCCPLCEquipmentList 
     */
    public PLCCPLCEquipmentList(PLCCObjectFactory pLCCObjectFactory) {
        super(pLCCObjectFactory);
    }
    
    protected void doInvoke() throws IOException {
        profileGeneric = getCosemObjectFactory().getProfileGeneric(getId().getObisCode());
    }
    
    List getEquipmentList( ){
        return new ArrayList();
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification( "0.0.98.139.0.255", AbstractCosemObject.CLASSID_PROFILE_GENERIC);
    }

    public void initialDiscover() throws IOException {
        profileGeneric.invoke(1);
    }
    public void discover() throws IOException {
        profileGeneric.invoke(132);
    }
    public void setAllNew() throws IOException {
        profileGeneric.invoke(136);
    }
    
}
