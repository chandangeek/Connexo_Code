/**
 * 
 */
package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;

/**
 * @author gna
 * 
 * Some more info about certain attributes:
 * 
	 * The monitoredValue structure contains:
	 * 		- a long-unsigned(Unsigned16) class_id
	 * 		- an octet-string(OctetString) logical_name 
	 * 		- an integer(Integer8) attribute_index

	 * The emergencyProfile structure contains:
	 * 		- a long-unsigned(Unsigned16) emergency_profile_id	
	 *		- an octet-string(OctetString) emergency_activation_time	-> Defines date and time when the emergency_profile activated
	 *		- a double-long-unsigned(Unsigned32) emergency_duration		-> Defines the duration in seconds, for which the emergency_profile is activated
			 
	 * The emergencyProfile can be activated only if emergency_profile_id element of the emergencyProfile type matches one of the
	 * elements on the emergencyProfileGroupIdList.
	 * (All Array elements are long-unsigned(Unsigned16))

	 * The actions structure contains:
	 * 		- an actionItem(Structure) action_over_threshold 	-> Defines the action when the value of the attribute monitored crosses the threshold in upwards
	 * 															-> direction and remains over threshold for minimal overThresholdDuration time
	 * 		- an actionItem(Structure) action_under_threshold	-> Defines the action when the value of the attribute monitored crosses the threshold in downwards
	 * 															-> direction and remains under threshold for minimal underThresholdDuration time 
	 
	 * The actionItem structure contains:	-> indicates a certain script table		
	 * 		- an octet-string(OctetString) script_logical_name
	 * 		- a long-unsigned(Unsigned16) script_selector
 */
public class Limiter extends AbstractCosemObject{
	
	private int CLASSID = 71;
	
	/** Attributes */
	private Structure monitoredValue = null;	// Defines an attribute of an object to be monitored. Only simple data types allowed.
	private AbstractDataType thresholdActive = null;	// Provides the active threshold value to which the attribute monitored is compared.
	private AbstractDataType thresholdNormal = null; // Provides the threshold value to which the attribute monitored is compared when in normal operation
	private AbstractDataType thresholdEmergency = null; 	// Provides the threshold value to which the attribute monitored is compared when an emergency profile is active
	private Unsigned32 minOverThresholdDuration = null;	// Defines the minimal over threshold duration in seconds required to execute the over threshold action
	private Unsigned32 minUnderThresholdDuration = null;	// Defines the minimal under threshold duration in seconds required to execute the under threshold action
	private Structure emergencyProfile = null; 	// An emergencyProfile is activated if the emergencyProfileId element matches one of the elements on the
												// emergencyProfileGroupIdList, an time matches the emergencyActivationTime and emergencyDuration element.
	private Array emergencyProfileGroupIdList = null;	// Defines a list of group id-s of the emergency profile
	private BooleanObject emergencyProfileActive = null;	// Indicates that the emergencyProfile is active
	private Structure actions = null;	// Defines the scripts to be executed when the monitored value crosses the threshold for minimal duration time
	
	/** Attribute numbers */
	static private final int ATTRB_MONITORED_VALUE = 2;
	static private final int ATTRB_THRESHOLD_ACTIVE = 3;
	static private final int ATTRB_THRESHOLD_NORMAL = 4;
	static private final int ATTRB_THRESHOLD_EMERGENCY = 5;
	static private final int ATTRB_MIN_OVER_THRESHOLD_DURATION = 6;
	static private final int ATTRB_MIN_UNDER_THRESHOLD_DURATION = 7;
	static private final int ATTRB_EMERGENCY_PROFILE = 8;
	static private final int ATTRB_EMERGENCY_PROFILE_GROUP_ID_LIST = 9;
	static private final int ATTRB_EMERGENCY_PROFILE_ACTIVE = 10;
	static private final int ATTRB_ACTIONS = 11;
	
	/** Method invoke */
	// none
	
	public Limiter(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	protected int getClassId() {
		return this.CLASSID;
	}
	
	/**
	 * Read the monitoredValue structure from the device
	 * @return
	 * @throws IOException
	 */
	public Structure readMonitoredValue() throws IOException{
		try {
			this.monitoredValue = new Structure(getLNResponseData(ATTRB_MONITORED_VALUE), 0, 0);
			return this.monitoredValue;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not read the monitoredValue" + e.getMessage());
		}
	}
	
	/**
	 * Get the latest retrieved monitoredValue, if its not there yet, read if from device
	 * @return
	 * @throws IOException
	 */
	public Structure getMonitoredValue() throws IOException{
		if(this.monitoredValue == null) {
			readMonitoredValue(); // do a dummy read
		}
		return this.monitoredValue;
	}
	
	/**
	 * Write the given monitoredValue structure to the device
	 * @param monitoredValue
	 * @throws IOException
	 */
	public void writeMonitoredValue(Structure monitoredValue) throws IOException{
		write(ATTRB_MONITORED_VALUE, monitoredValue.getBEREncodedByteArray());
		this.monitoredValue = monitoredValue;
	}
	
	/**
	 * Retrieve the current thresholdActive object from the device
	 * @return
	 * @throws IOException
	 */
	public AbstractDataType readThresholdActive() throws IOException{
		try{
			this.thresholdActive = AXDRDecoder.decode(getLNResponseData(ATTRB_THRESHOLD_ACTIVE));
			return this.thresholdActive;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not retrieve the thresholdActive value." + e.getMessage());
		}
	}
	
	/**
	 * Write the given thresholdActive value to the device.
	 * Note: Don't know if the writing of this object is allowed ...
	 * @param thresholdActive
	 * @throws IOException
	 */
	public void writeThresholdActive(AbstractDataType thresholdActive) throws IOException{
		try{
			write(ATTRB_THRESHOLD_ACTIVE, thresholdActive.getBEREncodedByteArray());
			this.thresholdActive = thresholdActive;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not write the thresholdActive value." + e.getMessage());
		}
	}
	
	/**
	 * Retrieve the current thresholdNormal value from the device
	 * @return 
	 * @throws IOException
	 */
	public AbstractDataType readThresholdNormal() throws IOException{
		try{
			this.thresholdNormal = AXDRDecoder.decode(getLNResponseData(ATTRB_THRESHOLD_NORMAL));
			return this.thresholdNormal;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not retrieve the thresholdNormal value." + e.getMessage());
		}
	}
	
	/**
	 * Write the given thresholdNormal value to the device
	 * @param thresholdNormal
	 * @throws IOException
	 */
	public void writeThresholdNormal(AbstractDataType thresholdNormal) throws IOException{
		try{
			write(ATTRB_THRESHOLD_NORMAL, thresholdNormal.getBEREncodedByteArray());
			this.thresholdNormal = thresholdNormal;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not write the thresholdNormal value." + e.getMessage());
		}
	}
	
	/**
	 * Retrieve the current thresholdEmergency value from the device
	 * @return 
	 * @throws IOException
	 */
	public AbstractDataType readThresholdEmergency() throws IOException{
		try{
			this.thresholdEmergency = AXDRDecoder.decode(getLNResponseData(ATTRB_THRESHOLD_EMERGENCY));
			return this.thresholdEmergency;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not retrieve the thresholdEmergency value." + e.getMessage());
		}
	}
	
	/**
	 * Write the given thresholdEmergency value to the device
	 * @param thresholdNormal
	 * @throws IOException
	 */
	public void writeThresholdEmergency(AbstractDataType thresholdEmergency) throws IOException{
		try{
			write(ATTRB_THRESHOLD_EMERGENCY, thresholdNormal.getBEREncodedByteArray());
			this.thresholdEmergency = thresholdEmergency;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not write the thresholdEmergency value." + e.getMessage());
		}
	}
	
	/**
	 * Retrieve the current minOverThresholdDuration value from the device
	 * @return
	 * @throws IOException
	 */
	public Unsigned32 readMinOverThresholdDuration() throws IOException {
		try{
			this.minOverThresholdDuration = new Unsigned32(getLNResponseData(ATTRB_MIN_OVER_THRESHOLD_DURATION), 0);
			return this.minOverThresholdDuration;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not retrieve the minOverThresholdDuration value." + e.getMessage());
		}
	}
	
	/**
	 * Write the given minOverThresholdDuration value to the device
	 * @param minOverThresholdDuration
	 * @throws IOException
	 */
	public void writeMinOverThresholdDuration(Unsigned32 minOverThresholdDuration) throws IOException{
		try{
			write(ATTRB_MIN_OVER_THRESHOLD_DURATION, minOverThresholdDuration.getBEREncodedByteArray());
			this.minOverThresholdDuration = minOverThresholdDuration;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not write the minOverThresholdDuration value." + e.getMessage());
		}
	}
	
	/**
	 * Retrieve the current minUnderThresholdDuration value from the device
	 * @return
	 * @throws IOException
	 */
	public Unsigned32 readMinUnderThresholdDuration() throws IOException {
		try{
			this.minUnderThresholdDuration = new Unsigned32(getLNResponseData(ATTRB_MIN_UNDER_THRESHOLD_DURATION), 0);
			return this.minUnderThresholdDuration;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not retrieve the minUnderThresholdDuration value." + e.getMessage());
		}
	}
	
	/**
	 * Write the given minUnderThresholdDuration value to the device
	 * @param minOverThresholdDuration
	 * @throws IOException
	 */
	public void writeMinUnderThresholdDuration(Unsigned32 minUnderThresholdDuration) throws IOException{
		try{
			write(ATTRB_MIN_UNDER_THRESHOLD_DURATION, minUnderThresholdDuration.getBEREncodedByteArray());
			this.minUnderThresholdDuration = minUnderThresholdDuration;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("Could not write the minUnderThresholdDuration value." + e.getMessage());
		}
	}
	
	public Structure readEmergencyProfile() throws IOException{
		try{
			this.emergencyProfile = new Structure(getLNResponseData(ATTRB_EMERGENCY_PROFILE), 0, 0);
			return this.emergencyProfile;
		} catch (IOException e){
			e.printStackTrace();
			throw new IOException("could not retrieve the emergencyProfile." + e.getMessage());
		}
	}
	
	public void writeEmergencyProfile(Structure emergencyProfile) throws IOException{
//		try{
//			
//		} catch (IOException e){
//			e.print
//		}
	}

}
