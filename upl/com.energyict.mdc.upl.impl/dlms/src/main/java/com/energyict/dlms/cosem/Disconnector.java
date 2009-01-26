/**
 * 
 */
package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.TypeEnum;

/**
 * @author gna
 * 
 * Disconnect Control States and State Transitions:
 * 
 * 					** States **
 * ------------------------------------------------
 * StateN°	|	StateName	|	State Description
 * __________________________________________________________________________________________________________
 * 	0		| Disconnected	| The outputState is set to FALSE and the consumer is disconnected
 * 	1		| Connected		| The outputState is set to TRUE and the consumer is connected
 *  2		| Ready_for_	| The outputState is set to False and the consumer is disconnected. Reconnection
 *  		| reconnection	| requires manual intervention
 * 
 * 					** State Transitions **
 * ------------------------------------------------
 * Transition	|	Transition Name		|	State Description
 * __________________________________________________________________________________________________________
 *  a			| remote_reconnect		| Moves the Disconnect control object from the Disconnected(0) state
 *  			|						| directly to the Connected(1) state without manual intervention.
 *  b			| remote_disconnect		| Moves the Disconnect control object from the Connected(1) state
 *  			|						| to the Disconnected(0) state.
 *  c			| remote_disconnect		| Moves the Disconnect control object from the Ready_for-reconnection(2)
 *  			|						| state to the Disconnected(0) state.
 *  d			| remote_reconnect		| Moves the Disconnect control object from the Disconnected(0) state to the 
 *  			|						| Ready_for_reconnection(2) State.
 * 				|						| From this state, it is possible to move to the Connected(2) state via
 * 				|						| the manual_reconnect, transition(e) or local_reconnect, transition(h).
 * e			| manual_reconnect		| Moves the Disconnect control object from the Ready_for_connection(2)
 * 				|						| state to the Connected(1) state.
 * f			| manual_disconnect		| Moves the Disconnect control object from the Connected(1) state to the
 * 				|						| Ready_for_connection(2) state.
 * 				|						| From this state, it is possible to move back to the Connected(2) state
 * 				|						| via the manual_reconnect, transition(e) or local_reconnect, transition(h).
 * g			| local_disconnect		| Moves the Disconnect control object from the Connected(1) state to the
 * 				|						| Ready_for_connection(2) state.
 * 				|						| From this state, it is possible to move back to the Connected(2) state
 * 				|						| via the manual_reconnect, transition(e) or local_reconnect, transition(h).
 * 				|						| Note: Transition (f) and (g) are essentially the same, but their Trigger is different.
 * h			| local_reconnect		| Moves the Disconnect control object from the Ready_for_connection(2)
 * 				|						| state to the Connected(1) state
 * 				|						| Note: Transition (e) and (h) are essentially the same, but their Trigger is different.
 */
public class Disconnector extends AbstractCosemObject{
	
	static public boolean DEBUG = true;
	static public final int CLASSID = 70;
	
	/** Attributes */
	private BooleanObject outputState = null; 	// Shows the actual physical state of the disconnect unit, i.e. if an electricity breaker or gas valvue is open or closed
	private TypeEnum controlState = null; 		// Shows the internal state of the disconnect control object
	private TypeEnum controlMode = null;		// Configures the behavior of the disconnect control object for all triggers
	
	/** Attribute numbers */
	static private final int ATTRB_OUTPUT_STATE = 2;
	static private final int ATTRB_CONTROL_STATE = 3;
	static private final int ATTRB_CONTROL_MODE = 4;
	
	/** Method invoke */
	static private final int METHOD_REMOTE_DISCONNECT = 1;
	static private final int METHOD_REMOTE_RECONNECT = 2;

	public Disconnector(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}
	
	protected int getClassId() {
		return this.CLASSID;
	}
	
	/**
	 * Just returns the outputState that we previously read from the meter, if it is null, then we read it anyway
	 * @return
	 * @throws IOException
	 */
	public BooleanObject getOutputState() throws IOException{
		if(this.outputState == null){
			readOutputState();	// do a dummy read
		}
		return this.outputState;
	}
	
	/**
	 * Returns the state of the ouputState
	 * @return true or false
	 * @throws IOException
	 */
	public boolean getState() throws IOException{
		if(this.outputState == null){
			readOutputState();	// do a dummy read
		}
		return this.outputState.getState();
	}
	
	/**
	 * Reads the outputState from the meter
	 * @return
	 * @throws IOException
	 */
	public BooleanObject readOutputState() throws IOException{
		this.outputState = new BooleanObject(getLNResponseData(ATTRB_OUTPUT_STATE),0);
		return this.outputState;
	}

	/**
	 * Sets the outputState in the given state
	 * @param outputState
	 * @throws IOException
	 */
	public void writeOutputState(BooleanObject outputState) throws IOException{
		write(ATTRB_OUTPUT_STATE, outputState.getBEREncodedByteArray());
		this.outputState = outputState;
	}
	
	/**
	 * Read the controlState from the meter
	 * @return
	 * @throws IOException
	 */
	public TypeEnum readControlState() throws IOException {
		this.controlState = new TypeEnum(getLNResponseData(ATTRB_CONTROL_STATE), 0);
		return this.controlState;
	}
	
	/**
	 * Get the latest retrieved control state
	 * @return
	 * @throws IOException
	 */
	public TypeEnum getControlState() throws IOException {
		if(this.controlState == null){
			readControlState();	// do a dummy read
		}
		return this.controlState;
	}

	/**
	 * Write a controlState to the meter
	 * @param controlState
	 * @throws IOException
	 */
	public void writeControlState(TypeEnum controlState) throws IOException {
		write(ATTRB_CONTROL_STATE, controlState.getBEREncodedByteArray());
		this.controlState = controlState;
	}
	
	/**
	 * Get the latest retrieved controlMode
	 * @return
	 * @throws IOException
	 */
	public TypeEnum getControlMode() throws IOException {
		if(this.controlMode == null){
			readControlMode(); 	// do a dummy read
		}
		return this.controlMode;
	}

	/**
	 * Read the control mode out of the meter
	 * @return
	 * @throws IOException
	 */
	public TypeEnum readControlMode() throws IOException {
		this.controlMode = new TypeEnum(getLNResponseData(ATTRB_CONTROL_MODE),0);
		return this.controlMode;
	}
	
	/**
	 * Note: 	The ControlMode attribute is a static attribute ... so I suppose you can not write it.
	 * 			But wrote a protected method to write it, if it's needed, just make it public.
	 * @param controlMode
	 * @throws IOException
	 */
	protected void writeControlMode(TypeEnum controlMode) throws IOException {
		write(ATTRB_CONTROL_MODE, controlMode.getBEREncodedByteArray());
		this.controlMode = controlMode;
	}
	
	/**
	 * Forces the disconnect control object into 'Disconnected' state if remote disconnection
	 * is enabled (control mode > 0)
	 * @throws IOException 
	 */
	public void remoteDisconnect() throws IOException{
		invoke(METHOD_REMOTE_DISCONNECT);
	}
	
	/**
	 * Forces the disconnect control object into the 'ready_for_reconnection' state if a direct
	 * remote reconnection is disabled (control mode = 1, 3, 5, 6)
	 * 
	 * Forces the disconnect control object into the 'connected' state if a direct remote
	 * reconnection is enabled (control mode = 2, 4) 
	 * @throws IOException 
	 */
	public void remoteReconnect() throws IOException{
		invoke(METHOD_REMOTE_RECONNECT);
	}
}
