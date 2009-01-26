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
 * Changes:
 * GNA |26012009| Extended the object with all getters and setters for all attributes
 * 				NOTE: check the executedScript, it is possible that you have to use a ScriptTable object instead of a Structure
 */
public class SingleActionSchedule extends AbstractCosemObject{
	
	/** Attributes */
//	private ScriptTable executedScript = null;
	private Structure executedScript = null;	// The Structure is a combination of the script_LogicalName and the script_Selector
	private TypeEnum type = null;				// Let's you define wildcards in the datetime
	private Array executionTime = null;	
	
	/** Attribute numbers */
	static private final int ATTRB_EXECUTED_SCRIPT = 2;
	static private final int ATTRB_TYPE = 3;
	static private final int ATTRB_EXECUTION_TIME = 4;
	
	/** Method invoke */
	// none
	
	public SingleActionSchedule(ProtocolLink protocolLink,ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	protected int getClassId() {
		return AbstractCosemObject.CLASSID_SINGLE_ACTION_SCHEDULE;
	}
	
	/**
	 * Get the latest retrieved executedScript
	 * @return
	 * @throws IOException
	 */
	public Structure getExecutedScript() throws IOException{
		if(this.executedScript == null){
			readExecutedScript(); // do a dummy read
		}
		return this.executedScript;
	}
	
	/**
	 * Read the executedScript from the device
	 * @return
	 * @throws IOException
	 */
	public Structure readExecutedScript() throws IOException{
		this.executedScript = new Structure(getLNResponseData(ATTRB_EXECUTED_SCRIPT), 0, 0);
		return this.executedScript;
	}
	
	/**
	 * Get the latest retrieved type
	 * @return
	 * @throws IOException
	 */
	public TypeEnum getType() throws IOException{
		if(this.type == null){
			readType();	// do a dummy read
		}
		return this.type;
	}
	
	/**
	 * Read the type from the device
	 * @return
	 * @throws IOException
	 */
	public TypeEnum readType() throws IOException{
		this.type = new TypeEnum(getLNResponseData(ATTRB_TYPE), 0);
		return this.type;
	}
	
	/**
	 * Write a type to the device
	 * @param type
	 * @throws IOException
	 */
	public void writeType(TypeEnum type) throws IOException{
		write(ATTRB_TYPE, type.getBEREncodedByteArray());
		this.type = type;
	}
	
	/**
	 * Write a specific scriptStructure to the device
	 * @param script
	 * @throws IOException
	 */
	public void writeExecutedScript(Structure script) throws IOException{
		write(ATTRB_EXECUTED_SCRIPT, script.getBEREncodedByteArray());
		this.executedScript = script;
	}

	/**
	 * Reads the executionTime from the device
	 * @return
	 * @throws IOException
	 */
	public Array getExecutionTime() throws IOException {
		if (executionTime == null){
			executionTime = (Array)AXDRDecoder.decode(getLNResponseData(ATTRB_EXECUTION_TIME));
		}
		return executionTime;
	}
	
	/**
	 * Writes an ExecutionTime to the device
	 * @param executionTime
	 * @throws IOException
	 */
    public void writeExecutionTime(Array executionTime) throws IOException {
        write(ATTRB_EXECUTION_TIME, executionTime.getBEREncodedByteArray());
        this.executionTime=executionTime;
    }
    
}
