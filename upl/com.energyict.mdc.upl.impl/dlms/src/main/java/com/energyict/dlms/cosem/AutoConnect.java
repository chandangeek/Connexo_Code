package com.energyict.dlms.cosem;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;

import java.io.IOException;

/**
 *
 * @author gna
 *	This IC allows modeling the management of data transfer from the device to one or several
 *	destinations.
 *	The messages to be sent, the conditions on which they shall be sent and the relation between the
 *	various modes, the calling windows and destinations are not defined here.
 *	Depending on the mode, one or more instances of this IC may be necessary to perform the function
 *	of sending out messages.
 * @beginChanges
 * GNA |01042009| Adding and deleting from the destinationList is not updated immediately, the user(programmer) has to use the updatePhoneList
 * 					method to send the list to the meter.
 * @endChanges
 */

public class AutoConnect extends AbstractCosemObject {

	static final byte[] LN = new byte[] { 0, 0, 2, 1, 0, (byte) 255 };

	/** Attributes */
	private TypeEnum mode = null; // Defines the working mode of the line when the device is auto answer
	private Unsigned8 repetitions = null; // Maximum number of trials in the case of unsuccessful dialling attempts
	private Unsigned16 repetitionDelay = null; // The time delay, expressed in seconds until an unsuccessful dial attempt can be repeated
	private Array callingWindow = null; // contains the start and end date/time stamp when the window becomes active or inactive
	private Array destinationList = null; // contains a list of destinations(phone numbers, email addresses, combination) where the message have to be sent

	/** Attribute numbers */
	private static final int ATTRB_MODE = 2;
	private static final int ATTRB_REPETITIONS = 3;
	private static final int ATTRB_REPETITION_DELAY = 4;
	private static final int ATTRB_CALLING_WINDOW = 5;
	private static final int ATTRB_DESTINATION_LIST = 6;
    private static final int ATTRB_CALLING_WINDOW_LENGTH = -1;
    private static final int ATTRB_IDLE_TIMEOUT = -2;

	/** Methods */
	//none

	public AutoConnect(ProtocolLink protocolLink){
		super(protocolLink, new ObjectReference(LN));
	}

	public AutoConnect(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	protected int getClassId() {
		return DLMSClassId.AUTO_CONNECT.getClassId();
	}

	/**
	 * Read the current mode from the device
	 * @return
	 * @throws java.io.IOException
	 */
	public TypeEnum readMode() throws IOException {
		try{
			return this.mode = new TypeEnum(getLNResponseData(ATTRB_MODE), 0);
		} catch (IOException e){
			throw new NestedIOException(e, "Could not read the mode. " + e.getMessage());
		}
	}

	/**
	 * Get the current mode, if it's not read yet, read if from the device
	 * @return
	 * @throws java.io.IOException
	 */
	public TypeEnum getMode() throws IOException {
		if(this.mode == null){
			return readMode();
		}
		return this.mode;
	}

	/**
	 * Write the given mode to the device
	 * @param mode
	 * @throws java.io.IOException
	 */
	public void writeMode(TypeEnum mode) throws IOException {
		try{
			write(ATTRB_MODE, mode.getBEREncodedByteArray());
			this.mode = mode;
		} catch (IOException e){
			throw new NestedIOException(e, "Could not write the mode. " + e.getMessage());
		}
	}

	/**
	 * Write the given mode to the device
	 * @param mode - > possible values are:
	 * <pre>
	 * (0)  no auto dialling,
	 * (1)  auto dialling allowed anytime,
	 * (2)  auto dialling allowed within the validity time of the calling window,
	 * (3)  �regular� auto dialling allowed within the validity time of the calling window; �alarm� initiated auto dialling allowed anytime,
	 * (4)  SMS sending via Public Land Mobile Network (PLMN),
	 * (5)  SMS sending via PSTN,
	 * (6)  email  sending,
	 * (200..255)   manufacturer specific modes
	 * </pre>
	 * @throws java.io.IOException
	 */
	public void writeMode(int mode) throws IOException {
		writeMode(new TypeEnum(mode));
	}

	/**
	 * Read the repetition value from the device
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned8 readRepetitions() throws IOException {
		try{
			return this.repetitions = new Unsigned8(getLNResponseData(ATTRB_REPETITIONS), 0);
		} catch (IOException e){
			throw new NestedIOException(e, "Could not read the repetitions value. " + e.getMessage());
		}
	}

	/**
	 * Get the repetition value, if it's not read yet, read it from the device
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned8 getRepetitions() throws IOException {
		if(this.repetitions == null){
			return readRepetitions();
		}
		return this.repetitions;
	}

	/**
	 * Write the given repetitions to the device
	 * @param repetitions
	 * @throws java.io.IOException
	 */
	public void writeRepetitions(Unsigned8 repetitions) throws IOException {
		try{
			write(ATTRB_REPETITIONS, repetitions.getBEREncodedByteArray());
		} catch (IOException e){
			throw new NestedIOException(e, "Could not write the repetions value. " + e.getMessage());
		}
	}

	/**
	 * Write the given repetitions to the device
	 * @param repetitions
	 * @throws java.io.IOException
	 */
	public void writeRepetitions(int repetitions) throws IOException {
		writeRepetitions(new Unsigned8(repetitions));
	}

	/**
	 * Read the repetitionDelay from the device
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned16 readRepetitionDelay() throws IOException {
		try{
			return this.repetitionDelay = new Unsigned16(getLNResponseData(ATTRB_REPETITION_DELAY), 0);
		} catch (IOException e){
			throw new NestedIOException(e, "Could not read the repetitiondelay. " + e.getMessage());
		}
	}

	/**
	 * Get the repetitionDelay, if it's not read yet, read it from the device
	 * @return
	 * @throws java.io.IOException
	 */
	public Unsigned16 getRepetitionDelay() throws IOException {
		if(this.repetitionDelay == null){
			return readRepetitionDelay();
		}
		return this.repetitionDelay;
	}

	/**
	 * Write the given repetitionDelay to the device
	 * @param repetitionDelay
	 * @throws java.io.IOException
	 */
	public void writeRepetitionDelay(Unsigned16 repetitionDelay) throws IOException {
		try{
			write(ATTRB_REPETITION_DELAY, repetitionDelay.getBEREncodedByteArray());
		} catch(IOException e){
			throw new NestedIOException(e, "Could not write the repetitiondelay. " + e.getMessage());
		}
	}

	/**
	 * Write the given repetitionDelay to the device
	 * @param repetitionDelay
	 * @throws java.io.IOException
	 */
	public void writeRepetitionDelay(int repetitionDelay) throws IOException {
		writeRepetitionDelay(new Unsigned16(repetitionDelay));
	}

	/**
	 * Read the callingWindow array from the device
	 * @return
	 * @throws java.io.IOException
	 */
	public Array readCallingWindow() throws IOException {
		try{
			return this.callingWindow = new Array(getLNResponseData(ATTRB_CALLING_WINDOW), 0, 0);
		} catch (IOException e){
			throw new NestedIOException(e, "Could not read the calling window array. " + e.getMessage());
		}
	}

	/**
	 * Get the callingWindow, if it's not read yet, read it from the device
	 * @return
	 * @throws java.io.IOException
	 */
	public Array getCallingWindow() throws IOException {
		if(this.callingWindow == null){
			return readCallingWindow();
		}
		return this.callingWindow;
	}

	/**
	 * Write the given callingWindow to the device
	 * @param callingWindow
	 * @throws java.io.IOException
	 */
	public void writeCallingWindow(Array callingWindow) throws IOException {
		try{
			write(ATTRB_CALLING_WINDOW, callingWindow.getBEREncodedByteArray());
		}catch (IOException e){
			throw new NestedIOException(e, "Could not write the calling window. " + e.getMessage());
		}
	}

	/**
	 * Read the destinationList from the device
	 * @return
	 * @throws java.io.IOException
	 */
	public Array readDestinationList() throws IOException {
		try{
			return this.destinationList = new Array(getLNResponseData(ATTRB_DESTINATION_LIST), 0, 0);
		}catch (IOException e){
			throw new NestedIOException(e, "Could not read the destinationList. " + e.getMessage());
		}
	}

	/**
	 * Get the destinationList, if it's not read yet, read it from the device
	 * @return
	 * @throws java.io.IOException
	 */
	public Array getDestinationList() throws IOException {
		if(this.destinationList == null){
			return readDestinationList();
		}
		return this.destinationList;
	}

	/**
	 * Write the given destinationList to the device
	 * @param destinationList
	 * @throws java.io.IOException
	 */
	public void writeDestinationList(Array destinationList) throws IOException {
		try{
			write(ATTRB_DESTINATION_LIST, destinationList.getBEREncodedByteArray());
		} catch (IOException e){
			throw new NestedIOException(e, "Could not write the given destinationList. " + e.getMessage());
		}
	}

	/**
	 * Add a phone or email address to the destinationList
	 * @param number
	 * @throws java.io.IOException
	 */
	public void addNumberToDestinationList(OctetString number) throws IOException {
		try{
//			writeDestinationList(getDestinationList().addDataType(number));
			getDestinationList().addDataType(number);
		} catch (IOException e){
			throw new NestedIOException(e, "Could not add " + number.toString() + " to the destinationList." + e.getMessage());
		}
	}

	/**
	 * Add a phone or email address to the destinationList
	 * @param number
	 * @throws java.io.IOException
	 */
	public void addNumberToDestinationList(String number) throws IOException {
		addNumberToDestinationList(OctetString.fromString(number));
	}

	/**
	 * Delete a phone or email address from the destinationList
	 * @param number
	 * @throws java.io.IOException
	 */
	public void deleteFromDestinationList(OctetString number) throws IOException {
		try{
			Array temp = new Array();
			for(int i = 0; i < getDestinationList().nrOfDataTypes(); i++){
				if(!java.util.Arrays.equals(getDestinationList().getDataType(i).getBEREncodedByteArray(), number.getBEREncodedByteArray())){
					temp.addDataType(getDestinationList().getDataType(i));
				}
			}
//			writeDestinationList(temp);
		} catch (IOException e){
			throw new NestedIOException(e, "Could not delete " + number.toString() + " from the destinationList." + e.getMessage());
		}
	}

	/**
	 * Delete a phone or email address from the destinationList
	 * @param number
	 * @throws java.io.IOException
	 */
	public void deleteFromDestinationList(String number) throws IOException {
		deleteFromDestinationList(OctetString.fromString(number));
	}

	/**
	 * Clear the complete whiteList in the device
	 * (added this because it's not possible to show the list in eiserver...
	 * @throws java.io.IOException
	 */
	public void clearWhiteList() throws IOException {
		Array temp = new Array();
		temp.addDataType(OctetString.fromString(""));
		writeDestinationList(temp);
	}

	/**
	 * Write the updated phoneList to the device
	 * @throws java.io.IOException
	 */
	public void updatePhoneList() throws IOException {
		writeDestinationList(getDestinationList());
	}

    /**
     * Write the given wakeup calling window length to the device
     * @param callingWindowLength
     * @throws IOException
     */
    public void writeWakeupCallingWindowLength(Unsigned32 callingWindowLength) throws IOException{
        try{
            write(ATTRB_CALLING_WINDOW_LENGTH, callingWindowLength.getBEREncodedByteArray());
        } catch (IOException e){
            throw new IOException("Could not write the given wakeup calling window length. " + e.getMessage());
        }
    }

    /**
     * Write the given wakeup idle timeout to the device
     * @param idleTimeout
     * @throws IOException
     */
    public void writeWakeupIdleTimeoutLength(Unsigned32 idleTimeout) throws IOException{
        try{
            write(ATTRB_IDLE_TIMEOUT, idleTimeout.getBEREncodedByteArray());
        } catch (IOException e){
            throw new IOException("Could not write the given wakeup idle timeout length. " + e.getMessage());
        }
    }
}
