/*
 * CosemMovingPeakScriptTableBuilder.java
 *
 * Created on 7 december 2007, 16:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;


import com.energyict.dlms.axrdencoding.*;
import com.energyict.protocolimpl.edf.messages.objects.MovingPeak;
import com.energyict.protocolimpl.edf.messages.objects.MovingPeakScript;

import java.util.Iterator;

/**
 *
 * @author kvds
 */
public class CosemMovingPeakScriptTableBuilder {
    
    MovingPeak movingPeak;
    
    /** Creates a new instance of CosemMovingPeakScriptTableBuilder */
    public CosemMovingPeakScriptTableBuilder(MovingPeak movingPeak) {
        this.movingPeak=movingPeak;
    }
    
    public Array scripts() {
        Array array = new Array();
        Iterator it = movingPeak.getScripts().iterator();
        while(it.hasNext()) {
            MovingPeakScript mps = (MovingPeakScript)it.next();
            Structure structureScript = new Structure();
            structureScript.addDataType(new Unsigned16(mps.getScriptId()));
            structureScript.addDataType(new TypeEnum(mps.getServiceId()));
            structureScript.addDataType(new Unsigned16(mps.getClassId()));
            structureScript.addDataType(OctetString.fromByteArray(mps.getLogicalNameOctets()));
            structureScript.addDataType(new Integer16(mps.getIndex()));
            structureScript.addDataType(new NullData());
        }
        return array;
    }
    
}
