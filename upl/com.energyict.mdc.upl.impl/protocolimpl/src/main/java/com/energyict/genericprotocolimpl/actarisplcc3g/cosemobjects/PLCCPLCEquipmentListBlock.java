/*
 * PLCCPLCEquipmentListBlock.java
 *
 * Created on 16 oktober 2007, 9:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import com.energyict.dlms.cosem.ObjectIdentification;
import java.io.*;

/**
 * @author kvds
 */
public class PLCCPLCEquipmentListBlock extends AbstractPLCCObject {
    
    String pLCESerialNumber;
    String pLCEType;
    String plCEVersion;
    
    public final int PLC_EQUIPMENT_AVAILABLE=0;
    public final int PLC_EQUIPMENT_DISAPPEARED=1;
    public final int PLC_EQUIPMENT_LOST=2;
    
    int pLCEStatus;
            
    public PLCCPLCEquipmentListBlock(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected void doInvoke() throws IOException {
    }
    
    protected ObjectIdentification getId() {
        return null;
    }

}
