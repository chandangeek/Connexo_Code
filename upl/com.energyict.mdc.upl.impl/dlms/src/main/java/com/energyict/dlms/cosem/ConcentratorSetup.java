package com.energyict.dlms.cosem;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.attributes.ConcentratorSetupAttributes;
import com.energyict.dlms.cosem.methods.ConcentratorSetupMethods;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Concentrator setup IC (for the Beacon 3100).
 * 
 * Specified in : https://confluence.eict.vpdc/display/G3IntBeacon3100/DLMS+concentrator+specification.
 * 
 * @author alex
 */
public final class ConcentratorSetup extends AbstractCosemObject {
	
	/** Beacon 3100 OBIS code. */
	public static final ObjectReference DEFAULT_OBIS_CODE = new ObjectReference(new byte[] { 0, 0, (byte)128, 0, 18, (byte)255 });
	
	/**
	 * Enumerates the scheduling states of the meters.
	 * 
	 * @author alex
	 */
	public enum SchedulingState {
		
		NOT_SCHEDULED(0),
		SCHEDULED(1),
		PAUSED(2);
		
		/**
		 * Returns the corresponding state.
		 * 
		 * @param 	id		The ID.
		 * 
		 * @return	The corresponding state, <code>null</code> if not found. 
		 */
		private static final SchedulingState forId(final int id) {
			for (final SchedulingState state : SchedulingState.values()) {
				if (state.id == id) {
					return state;
				}
			}
			
			return null;
		}
		
		/** ID. */
		private final int id;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 	id		The ID.
		 */
		private SchedulingState(final int id) {
			this.id = id;
		}
		
		
	}
	/**
	 * device_type_assignment ::= STRUCTURE
	 * {
     *	 	device_type_id:     double-long-unsigned    Meter device type ID of device type to be assigned to the meter
     *		start_date:         cosem-date-time         Start date for data captured for this device type assignment.
     *		end_date:           cosem-date-time         End date for data captured for this device type assignment.
	 * }
	 */
	public static final class DeviceTypeAssignment {
		
		/** Device type ID. */
		private final long deviceTypeId;
		
		/** Start time of the period where the assignment is valid. */
		private final Calendar startTime;
		
		/** End time of the perdiod where the assignment is valid. */
		private final Calendar endTime;
		
		/**
		 * Create a new instance based on a {@link Structure}.
		 * 
		 * @param 		structure		The {@link Structure}.
		 * 
		 * @return		The parsed instance.		
		 */
		private static final DeviceTypeAssignment fromStructure(final Structure structure) throws IOException {
			if (structure.nrOfDataTypes() == 3) {
				final Unsigned32 deviceTypeId = structure.getDataType(0, Unsigned32.class);
				final DateTime startTime = structure.getDataType(1, DateTime.class);
				final DateTime endTime = structure.getDataType(2, DateTime.class);
				
				return new DeviceTypeAssignment(deviceTypeId.getValue(), startTime.getValue(), endTime.getValue());
			} else {
				throw new IOException("Expected a Structure of size 3, instead got a structure of size [" + structure.nrOfDataTypes() + "]");
			}
		}
		
		/**
		 * Create a new instance when we only have the device type ID (for older devices).
		 * 
		 * @param 		deviceTypeId		The device type ID.
		 * 
		 * @return		The device type assignment.
		 * 
		 * @throws 		IOException			If an IO error occurs;
		 */
		private static final DeviceTypeAssignment fromUnsigned32(final Unsigned32 deviceTypeId) {
			return new DeviceTypeAssignment(deviceTypeId.getValue(), null, null);
		}
		
		/**
		 * Create a new instance.
		 * 
		 * @param 	deviceTypeId		The device type ID.
		 * @param 	startTime			The start time.
		 * @param 	endTime				The end time.
		 */
		private DeviceTypeAssignment(final long deviceTypeId, final Calendar startTime, final Calendar endTime) {
			this.deviceTypeId = deviceTypeId;
			this.startTime = startTime;
			this.endTime = endTime;
		}

		/**
		 * Returns the device type ID.
		 * 
		 * @return	The device type ID.
		 */
		public final long getDeviceTypeId() {
			return this.deviceTypeId;
		}
		
		/**
		 * Returns the start time of the period where this assignment is valid. <code>null</code> if since the dawn of time.
		 * 
		 * @return	The start time of the period where this assignment is valid.
		 */
		public final Calendar getStartTime() {
			return this.startTime;
		}

		/**
		 * Returns the end time of the period where this assignment is valid. <code>null</code> for open-ended.
		 * 
		 * @return	The end time of the period where this assignment is valid. 
		 */
		public final Calendar getEndTime() {
			return this.endTime;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final String toString() {
			final StringBuilder builder = new StringBuilder();
			
			builder.append("device type : [").append(this.deviceTypeId).append("], ");
			builder.append("assignment start time : [").append(this.startTime).append("], ");
			builder.append("assignment end time : [").append(this.endTime).append("]");
			
			return builder.toString();
		}
	}
	
	/**
	 * meter_info ::= STRUCTURE
	 * {
     * 		meter_identifier:       OCTET-STRING                        The meter identifier
     *		meter_serial:           OCTET-STRING,                       The meter serial.
     *		meter_time_zone:        OCTET-STRING,                       The meter time zone
     *		meter_device_types:     ARRAY OF device_type_assignment,    List of associated device types
     *		meter_scheduler_state:  scheduling_state                    The scheduler state of the meter
	 * }
	 * 
	 * @author alex
	 *
	 */
	public static final class MeterInfo {
		
		/** Identifier of the meter. */
		private final byte[] id;
		
		/** Serial number of the meter. */
		private final String serialNumber;
		
		/** Time zone. */
		private final TimeZone timezone;
		
		/** Device type assignments. */
		private final List<DeviceTypeAssignment> deviceTypeAssignments;
		
		/** The current scheduling state. */
		private final SchedulingState schedulingState;
		
		/**
		 * Creates a new instance based on the data in the given {@link Structure}.
		 * 
		 * @param 		structure		The structure.
		 * 
		 * @return		The {@link MeterInfo}.
		 * 
		 * @throws 		IOException		If an IO error occurs.
		 */
		private static final MeterInfo fromStructure(final Structure structure) throws IOException {
			if (structure.nrOfDataTypes() == 5) {
				final List<DeviceTypeAssignment> deviceTypeAssignments = new ArrayList<>();
				
				final OctetString id = structure.getDataType(0, OctetString.class);
				final OctetString serialNumber = structure.getDataType(1, OctetString.class);
				final OctetString timezone = structure.getDataType(2, OctetString.class);
				
				// Older device types have an double-long-unsigned here, newer an array of device-type-assignment structures.
				final AbstractDataType deviceTypeAssignmentData = structure.getDataType(3);
				
				if (deviceTypeAssignmentData.isUnsigned32()) {
					deviceTypeAssignments.add(DeviceTypeAssignment.fromUnsigned32(deviceTypeAssignmentData.getUnsigned32()));
				} else if (deviceTypeAssignmentData.isArray()) {
					for (final AbstractDataType assignment : deviceTypeAssignmentData.getArray()) {
						deviceTypeAssignments.add(DeviceTypeAssignment.fromStructure(assignment.getStructure()));
					}
				}
				
				final TypeEnum schedulingState = structure.getDataType(4, TypeEnum.class);
				
				return new MeterInfo(id.getContentByteArray(), serialNumber.stringValue(), TimeZone.getTimeZone(timezone.stringValue()), deviceTypeAssignments, SchedulingState.forId(schedulingState.getValue()));
			} else {
				throw new IOException("Expected a structure of 5 elements, instead got one with [" + structure.nrOfDataTypes() + "] elements !");
			}
		}
		
		/**
		 * Create a new instance.
		 * 
		 * @param 	id							The ID.
		 * @param 	serialNumber				The serial number.
		 * @param 	timeZone					The {@link TimeZone}.
		 * @param 	deviceTypeAssignments		The device type assignments.
		 * @param 	schedulingState				The scheduling state.
		 */
		private MeterInfo(final byte[] id, final String serialNumber, final TimeZone timeZone, final List<DeviceTypeAssignment> deviceTypeAssignments, final SchedulingState schedulingState) {
			this.id = id;
			this.serialNumber = serialNumber;
			this.timezone = timeZone;
			this.deviceTypeAssignments = deviceTypeAssignments;
			this.schedulingState = schedulingState;
		}

		/**
		 * Returns the {@link SchedulingState}.
		 * 
		 * @return	The {@link SchedulingState}.
		 */
		public final SchedulingState getSchedulingState() {
			return this.schedulingState;
		}

		/**
		 * Returns the ID.
		 * 
		 * @return	The ID.
		 */
		public final byte[] getId() {
			return this.id;
		}

		/**
		 * Returns the serial number.
		 * 
		 * @return	The serial number.
		 */
		public final String getSerialNumber() {
			return this.serialNumber;
		}

		/**
		 * Returns the time zone.
		 * 
		 * @return	The {@link TimeZone}.
		 */
		public final TimeZone getTimezone() {
			return this.timezone;
		}

		/**
		 * Returns the {@link DeviceTypeAssignment}s.
		 * 
		 * @return	The {@link DeviceTypeAssignment}s.
		 */
		public final List<DeviceTypeAssignment> getDeviceTypeAssignments() {
			return this.deviceTypeAssignments;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final String toString() {
			final StringBuilder builder = new StringBuilder();
			
			builder.append("id : [").append(ProtocolTools.getHexStringFromBytes(this.id)).append("], ");
			builder.append("serial number : [").append(this.serialNumber).append("], ");
			builder.append("time zone : [").append(this.timezone).append("], ");
			builder.append("device type assignments : [").append(this.deviceTypeAssignments).append("], ");
			builder.append("scheduling state : [").append(this.schedulingState).append("]");
			
			return builder.toString();
		}
	}
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	protocolLink		The protocol link.
	 * @param 	objectReference		The object reference, if any.
	 */
	public ConcentratorSetup(final ProtocolLink protocolLink, final ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final int getClassId() {
		return DLMSClassId.CONCENTRATOR_SETUP.getClassId();
	}
	
	/**
	 * Drops all persisted data in the concentrator: logical devices, meter reading data and setup (scheduler, device types, etc.)
	 *
	 * (Meters that are currently on the network will have to reassociate.)
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	public final void reset() throws IOException {
		this.methodInvoke(ConcentratorSetupMethods.RESET_CONCENTRATOR);
	}
	
	/**
	 * Trigger the preliminary protocol for a particular meter (identified by it's MAC address (EUI64)).
	 * 
	 * @param	mac					The MAC address of the meter to trigger the the protocol for.
	 * @param	protocolName		The name of the protocol to use.
	 */
	public final void triggerPreliminaryProtocol(final byte[] mac, final String protocolName) throws IOException {
		if (mac == null || mac.length != 8) {
			throw new IllegalArgumentException("MAC address should not be null and should be of length 8 !");
		}
		
		final Structure argument = new Structure();
		argument.addDataType(new OctetString(mac));
		argument.addDataType(new OctetString(protocolName.getBytes(StandardCharsets.US_ASCII)));
		
		this.methodInvoke(ConcentratorSetupMethods.TRIGGER_PRELIMINARY_PROTOCOL, argument);
	}

	/**
	 * Sets the log level for the protocol execution engine.
	 * 
	 * @param 	level			The new log level.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
    public final void setDeviceLogLevel(final TypeEnum level) throws IOException {
        write(ConcentratorSetupAttributes.PROTOCOL_LOG_LEVEL, level.getBEREncodedByteArray());
    }
    
    /**
     * Returns information about the mirror devices known to the concentrator.
     * 
     * @return	Information about the mirror devices known to the concentrator.
     * 
     * @throws 	IOException		If an error occurs.
     */
    public final List<MeterInfo> getMeterInfo() throws IOException {
    	final List<MeterInfo> meterInformation = new ArrayList<>();
    	
    	final Array meterInfos = this.readDataType(ConcentratorSetupAttributes.METER_INFO, Array.class);
    	
    	for (final AbstractDataType entry : meterInfos) {
    		meterInformation.add(MeterInfo.fromStructure(entry.getStructure()));
    	}
    	
    	return meterInformation;
    }
}
