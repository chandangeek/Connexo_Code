package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import java.io.*;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.dlms.cosem.ObjectIdentification;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.ScriptTable;

/**
 *
 * @author kvds
 */
public class PLCCMeterMovingPeak extends AbstractPLCCObject {
    
    ScriptTable scriptTable=null;
    
    /** Creates a new instance of PLCCMeterMovingPeak */
    public PLCCMeterMovingPeak(PLCCObjectFactory objectFactory) { 
        super(objectFactory);
    }
    
    protected ObjectIdentification getId() {
        return new ObjectIdentification(ObisCode.fromString("0.0.10.0.125.255"), AbstractCosemObject.CLASSID_SCRIPTTABLE);
    }
    
    protected void doInvoke() throws IOException {
        scriptTable = getCosemObjectFactory().getScriptTable(getId().getObisCode());
    }
    
    public ScriptTable getScriptTable() throws IOException {
        return scriptTable;
    }
    
    public void execute(int scriptId) throws IOException {
        scriptTable.execute(scriptId);
    }
    public void executeBroadcast(int scriptId) throws IOException {
        Unsigned16 u16 = new Unsigned16(scriptId);
        scriptTable.invoke(130,u16.getBEREncodedByteArray());
    }
    
    // returns R/W denied
    public com.energyict.edf.messages.objects.MovingPeak readMovingPeak() throws IOException {
        // convert to message cosem object to write into a register...
        MovingPeakBuilder movingPeakBuilder = new MovingPeakBuilder(this);
        return movingPeakBuilder.toMovingPeak();
    }
    
    public void writeMovingPeak(com.energyict.edf.messages.objects.MovingPeak mp) throws IOException {
        CosemMovingPeakScriptTableBuilder cosemMovingPeakScriptTableBuilder = new CosemMovingPeakScriptTableBuilder(mp);
        scriptTable.writeScripts(cosemMovingPeakScriptTableBuilder.scripts());
    }
}
