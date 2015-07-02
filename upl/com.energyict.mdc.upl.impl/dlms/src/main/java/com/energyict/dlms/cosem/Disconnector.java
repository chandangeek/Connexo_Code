/**
 *
 */
package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.RegisterReadable;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.DisconnectControlAttribute;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 * @author gna
 *
 * Disconnect Control States and State Transitions:
 *
 * 					** States **
 * ------------------------------------------------
 * StateN�	|	StateName	|	State Description
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
public class Disconnector extends AbstractCosemObject implements RegisterReadable {

	public static boolean DEBUG = true;

	/* Attributes */
	private BooleanObject outputState = null; 	// Shows the actual physical state of the disconnect unit, i.e. if an electricity breaker or gas valvue is open or closed
	private TypeEnum controlState = null; 		// Shows the internal state of the disconnect control object
	private TypeEnum controlMode = null;		// Configures the behavior of the disconnect control object for all triggers

	/* Method invoke */
	private static final int METHOD_REMOTE_DISCONNECT = 1;
	private static final int METHOD_REMOTE_RECONNECT = 2;

	/* Method ShortName writes */
	private static final int METHOD_REMOTE_DISCONNECT_SN = 0x20;
	private static final int METHOD_REMOTE_RECONNECT_SN = 0x28;

	public Disconnector(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	protected int getClassId() {
		return DLMSClassId.DISCONNECT_CONTROL.getClassId();
	}

	/**
	 * Get the logicalname of the object. Identifies the object instance.
	 * @return
	 */
	public OctetString getLogicalName() {
		try {
			return new OctetString(getResponseData(DisconnectControlAttribute.LOGICAL_NAME), 0);
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Just returns the outputState that we previously read from the meter, if it is null, then we read it anyway
	 * @return
	 * @throws java.io.IOException
	 */
	public BooleanObject getOutputState() throws InvalidBooleanStateException {
		if(this.outputState == null){
			readOutputState(); // do a dummy read
		}
		return this.outputState;
	}

	/**
	 * Returns the state of the ouputState
	 * @return true or false
	 * @throws java.io.IOException
	 */
	public boolean getState() throws InvalidBooleanStateException {
		BooleanObject state = getOutputState();
		return state != null ? state.getState() : false;
	}

	/**
	 * Reads the outputState from the meter
	 * @return
	 * @throws java.io.IOException
	 */
	public BooleanObject readOutputState() throws InvalidBooleanStateException {
		try {
			this.outputState = new BooleanObject(getResponseData(DisconnectControlAttribute.OUTPUT_STATE),0);
		} catch (IOException e) {
            throw new InvalidBooleanStateException("Could not get a correct state value: " + e.getMessage());
        }
		return this.outputState;
	}

	/**
	 * Sets the outputState in the given state
	 * @param outputStat
	 * @throws java.io.IOException
	 */
	public void writeOutputState(BooleanObject outputStat) throws IOException {
		write(DisconnectControlAttribute.OUTPUT_STATE, outputStat.getBEREncodedByteArray());
		this.outputState = outputStat;
	}

	/**
	 * Read the controlState from the meter
	 * @return
	 * @throws java.io.IOException
	 */
	public TypeEnum readControlState() {
		try {
			this.controlState = new TypeEnum(getResponseData(DisconnectControlAttribute.CONTROL_STATE), 0);
		} catch (IOException e) {
            this.controlState = new TypeEnum(-1);
        }
		return this.controlState;
	}

	/**
	 * Get the latest retrieved control state
	 * @return
	 * @throws java.io.IOException
	 */
	public TypeEnum getControlState() {
		if(this.controlState == null){
			readControlState();	// do a dummy read
		}
		return this.controlState;
	}

	/**
	 * Write a controlState to the meter
	 * @param controlStat
	 * @throws java.io.IOException
	 */
	public void writeControlState(TypeEnum controlStat) throws IOException {
		write(DisconnectControlAttribute.CONTROL_STATE, controlStat.getBEREncodedByteArray());
		this.controlState = controlStat;
	}

	/**
	 * Get the latest retrieved controlMode
	 * @return
	 * @throws java.io.IOException
	 */
	public TypeEnum getControlMode() {
		if(this.controlMode == null){
			readControlMode(); 	// do a dummy read
		}
		return this.controlMode;
	}

	/**
	 * Read the control mode out of the meter
	 * @return
	 * @throws java.io.IOException
	 */
	public TypeEnum readControlMode() {
		try {
			this.controlMode = new TypeEnum(getResponseData(DisconnectControlAttribute.CONTROL_MODE),0);
		} catch (IOException e) {}
		return this.controlMode;
	}

	/**
	 * Note: 	The ControlMode attribute is a static attribute ... so I suppose you can not write it.
	 * 			But wrote a protected method to write it, if it's needed, just make it public.
	 * @param ctrlMode
	 * @throws java.io.IOException
	 */
	public void writeControlMode(TypeEnum ctrlMode) throws IOException {
		write(DisconnectControlAttribute.CONTROL_MODE, ctrlMode.getBEREncodedByteArray());
		this.controlMode = ctrlMode;
	}

	/**
	 * Forces the disconnect control object into 'Disconnected' state if remote disconnection
	 * is enabled (control mode > 0)
	 * @throws java.io.IOException
	 */
	public void remoteDisconnect() throws IOException {
		if(getObjectReference().isLNReference()){
			invoke(METHOD_REMOTE_DISCONNECT, new Integer8(0).getBEREncodedByteArray());
		} else {
			write(METHOD_REMOTE_DISCONNECT_SN, new Integer8(0).getBEREncodedByteArray());
		}
	}

	/**
	 * Forces the disconnect control object into the 'ready_for_reconnection' state if a direct
	 * remote reconnection is disabled (control mode = 1, 3, 5, 6)
	 *
	 * Forces the disconnect control object into the 'connected' state if a direct remote
	 * reconnection is enabled (control mode = 2, 4)
	 * @throws java.io.IOException
	 */
	public void remoteReconnect() throws IOException {
		if(getObjectReference().isLNReference()){
			invoke(METHOD_REMOTE_RECONNECT, new Integer8(0).getBEREncodedByteArray());
		} else {
			write(METHOD_REMOTE_RECONNECT_SN, new Integer8(0).getBEREncodedByteArray());
		}

	}

	public RegisterValue asRegisterValue() {
		return new RegisterValue(getDefaultObisCode(), super.toString());
	}

	public static ObisCode getDefaultObisCode() {
		return ObisCode.fromString("0.0.96.3.10.255");
	}

	public RegisterValue asRegisterValue(int attributeNumber) {
		DisconnectControlAttribute attribute = DisconnectControlAttribute.findByAttributeNumber(attributeNumber);
		if (attribute != null) {
			switch (attribute) {
				case LOGICAL_NAME:
					OctetString ln = getLogicalName();
					return new RegisterValue(getDefaultObisCode(), ln != null ? ObisCode.fromByteArray(ln.getOctetStr()).toString() : "null");
				case OUTPUT_STATE:
                    BooleanObject output = null;
                    try {
                        output = readOutputState();
					return new RegisterValue(getDefaultObisCode(), output != null ? String.valueOf(output.getState()) : "null");
                    } catch (InvalidBooleanStateException e) {
                        return new RegisterValue(getDefaultObisCode(), output != null ? String.valueOf(output.getState()) : "Unknown state");
                    }
				case CONTROL_STATE:
					TypeEnum ctrlStat = readControlState();
					return new RegisterValue(getDefaultObisCode(), ctrlStat != null ? String.valueOf(ctrlStat.getValue()) : "null");
				case CONTROL_MODE:
					TypeEnum ctrlMode = readControlMode();
					return new RegisterValue(getDefaultObisCode(), ctrlMode != null ? String.valueOf(ctrlMode.getValue()) : "null");
			}
		}
		return null;
	}
}
