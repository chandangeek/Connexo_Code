/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.commands;

import java.util.HashMap;
import java.util.Map;

/**
 * Straightforward implementation of all known errorCodes on read- and write commands
 * 
 * @author gna
 * @since 24-mrt-2010
 *
 */
public enum ErrorCodes {

	WRONGADDRESS(1, "Wrong (unknown) Address"),
	WRONGADDRESS_OBJ_NOT_AVAILABLE(2, "Wrong address - Object not available"),
	WRONGADDRESS_ENTITY_NOT_AVAILABLE(3, "Wrong address - Entity for object not available"),
	WRONGADDRESS_UNKNOWN_ATTRIBUTE(4, "Wrong address - Unknown attribute"),
	WRONGADDRESS_ATTRIBUTE_NOT_AVAILABLE(5, "Attribute for object not available"),
	VALUE_OUTSIDE_RANGE(6, "Value outside allowed range"),
	WRITE_COMMAND_NOT_EXECUTABLE(9, "Write command on constant not executable"),
	NO_VALUE_RANGE_AVAILABLE(11, "No value range available, because no input is allowed"),
	WRONG_INPUT(13, "Wrong input"),
	UNKNOWN_UNITS_CODE(14, "Unknown units code"),
	WRONG_ACCESS_CODE(17, "Wrong access code"),
	NO_READ_AUTHORITY(18, "No read authority"),
	NO_WRITE_AUTHORITY(19, "No write authority"),
	FUNCTION_IS_LOCKED(20, "Function is locked"),
	ARCHIVE_NOT_AVAILABLE(100, "Archive number not available"),
	VALUE_POSITION_NOT_AVAILABLE(101, "Value position not available"),
	ARCHIVE_EMPTY(103, "Archive empty"),
	LOWER_LIMIT_NOT_FOUND(104, "Lower limit (from-value) not found"),
	UPPER_LIMIT_NOT_FOUND(105, "Upper limit (to-value) not founde"),
	MAX_LIMIT_OPEN_ARCHIVES(108, "Maximum limit of simultaneously opened archives exceeded"),
	ARCHIVE_WAS_OVERWRITTEN(109, "Archive entry was overwritten while reading out"),
	CRC_ERROR_ARCHIVE(110, "CRC error in archive data record"),
	SOURCE_NOT_ALLOWED(180, "Source not allowed"),
	SYNTAX_ERROR_IN_1107(200, "Syntax error in 1107-telegram"),
	WRONG_PASSWORD_IN_1107(201, "Wrong password in 1107-telegram"),
	EEPROM_READ_ERROR(222, "EEPROM read error"),
	EEPROM_WRITE_ERROR(223, "EEPROM write error"),
	ENCODER_NOT_POSSIBLE(249, "a/ Encoder mode not possible, or b/ Counter reading cannot be changed");
	
	/** The errorCode from the Device */
	private final int errorCode;
	/** The errorMessage from the doc */
	private final String errorMessage;
	
	/** Contains a list of possible {@link ErrorCodes} */
	private static Map<Integer, String> instances;
	
	/**
	 * Create for each value an entry in the instanceMap
	 * @return
	 */
	private static Map<Integer, String> getInstances() {
		if (instances == null) {
			instances = new HashMap<Integer, String>(8);
		}
		return instances;
	}
	
	/**
	 * Private constructor
	 * 
	 * @param errorCode
	 * 				- the errorcode returned from the device
	 * 
	 * @param errorMessage
	 * 				- the errormessage related to the errorcode (copied from the docs)
	 */
	private ErrorCodes(int errorCode, String errorMessage){
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		getInstances().put(errorCode, errorMessage);
	}
	
	public static String getMessageForCode(int errorCode){
		return instances.get(errorCode);
	}

	/**
	 * @return the errorCode
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	
}
