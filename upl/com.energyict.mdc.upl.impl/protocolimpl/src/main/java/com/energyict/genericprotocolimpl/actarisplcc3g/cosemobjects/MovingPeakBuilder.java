/*
 * MovingPeakBuilder.java
 *
 * Created on 7 december 2007, 17:44
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.*;
import com.energyict.edf.messages.objects.MovingPeak;
import com.energyict.edf.messages.objects.MovingPeakScript;

import java.io.*;
import java.util.*;

/**
 *
 * @author kvds
 */
public class MovingPeakBuilder {
    
    PLCCMeterMovingPeak pLCCMeterMovingPeak;
    MovingPeak movingPeak=null;
    
    
    /** Creates a new instance of MovingPeakBuilder */
    public MovingPeakBuilder(PLCCMeterMovingPeak pLCCMeterMovingPeak) {
        this.pLCCMeterMovingPeak=pLCCMeterMovingPeak;
        
    }
    
    public MovingPeak toMovingPeak() throws IOException {
        movingPeak = new MovingPeak();
        Array array = pLCCMeterMovingPeak.getScriptTable().readScripts();
        List scripts = new ArrayList();
        for (int index=0;index<array.nrOfDataTypes();index++) {
            Structure structure = array.getDataType(index).getStructure();
            MovingPeakScript mps = new MovingPeakScript();
            mps.setScriptId(structure.getDataType(0).intValue());
            mps.setServiceId(structure.getDataType(1).intValue());
            mps.setClassId(structure.getDataType(2).intValue());
            mps.setLogicalNameOctets(structure.getDataType(3).getOctetString().getOctetStr());
            mps.setIndex(structure.getDataType(4).intValue());
            scripts.add(mps);
        }
        movingPeak.setScripts(scripts);
        return movingPeak;
    }
    
    
}
