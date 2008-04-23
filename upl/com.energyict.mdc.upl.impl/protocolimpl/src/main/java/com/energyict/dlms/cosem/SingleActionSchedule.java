/**
 * 
 */
package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;

/**
 * @author gna
 *
 */
public class SingleActionSchedule extends AbstractCosemObject{
	
	private ScriptTable executedScript = null;
	private Array executionTime = null;

	public SingleActionSchedule(ProtocolLink protocolLink,ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	@Override
	protected int getClassId() {
		return AbstractCosemObject.CLASSID_SINGLE_ACTION_SCHEDULE;
	}

	public Array getExecutionTime() throws IOException {
		if (executionTime == null){
			executionTime = (Array)AXDRDecoder.decode(getLNResponseData(4));
		}
		return executionTime;
	}
	
    public void writeExecutionTime(Array executionTime) throws IOException {
        write(4, executionTime.getBEREncodedByteArray());
        this.executionTime=executionTime;
    }
    
}
