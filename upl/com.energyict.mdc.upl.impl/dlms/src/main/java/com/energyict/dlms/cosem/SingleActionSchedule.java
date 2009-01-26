/**
 * 
 */
package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;

/**
 * @author gna
 *
 */
public class SingleActionSchedule extends AbstractCosemObject{
	
	/** Attributes */
//	private ScriptTable executedScript = null;
	private Structure executedScript = null;
	private TypeEnum type = null;
	private Array executionTime = null;
	
	/** Attribute numbers */
	static private final int ATTRB_EXECUTED_SCRIPT = 2;
	static private final int ATTRB_TYPE = 3;
	static private final int ATTRB_EXECUTION_TIME = 4;
	
	/** Method invoke */

	public SingleActionSchedule(ProtocolLink protocolLink,ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	protected int getClassId() {
		return AbstractCosemObject.CLASSID_SINGLE_ACTION_SCHEDULE;
	}

	public Array getExecutionTime() throws IOException {
		if (executionTime == null){
			executionTime = (Array)AXDRDecoder.decode(getLNResponseData(ATTRB_EXECUTION_TIME));
		}
		return executionTime;
	}
	
    public void writeExecutionTime(Array executionTime) throws IOException {
        write(ATTRB_EXECUTION_TIME, executionTime.getBEREncodedByteArray());
        this.executionTime=executionTime;
    }
    
}
