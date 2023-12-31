package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.util.DateTimeOctetString;
import com.energyict.dlms.cosem.attributes.ConcentratorSetupAttributes;
import com.energyict.dlms.cosem.methods.ConcentratorSetupMethods;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Concentrator setup IC (for the Beacon 3100).
 * 
 * Specified in :   https://confluence.eict.vpdc/display/G3IntBeacon3100/DLMS+concentrator+specification.
 *                  https://confluence.eict.vpdc/display/G3IntBeacon3100/Manage+DC+operations
 * 
 * @author alex
 */
public final class ConcentratorSetup extends AbstractCosemObject {
	
	/** Beacon 3100 OBIS code. */
	public static final ObjectReference DEFAULT_OBIS_CODE = new ObjectReference(new byte[] { 0, 0, (byte)128, 0, 18, (byte)255 });
    /** ObisCode for Beacon version  1.11 */
    public static final ObisCode NEW_LOGICAL_NAME = ObisCode.fromString("0.187.96.128.0.255");

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
				final DateTimeOctetString startTime = new DateTimeOctetString(structure.getDataType(1, OctetString.class));
				final DateTimeOctetString endTime = new DateTimeOctetString(structure.getDataType(2, OctetString.class));

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
     * Removes all meter data associated with the given logical device, and removed the logical device from the SAP list.
     *
     */
    public final void removeLogicalDevice(final byte[] mac) throws IOException {
        OctetString osMC = OctetString.fromByteArray(mac);
        this.methodInvoke(ConcentratorSetupMethods.REMOVE_LOGICAL_DEVICE, osMC.getBEREncodedByteArray());
    }

    /**
     * Removes all meter data associated with the given logical device
     *
     */
    public final void resetLogicalDevice(final byte[] mac) throws IOException {
        OctetString osMC = OctetString.fromByteArray(mac);
        this.methodInvoke(ConcentratorSetupMethods.RESET_LOGICAL_DEVICE, osMC.getBEREncodedByteArray());
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

    /** Returns information about the mirror devices known to the concentrator.
     *
     * @return raw AXDR encoded meter infor arry
     */
    public Array getMeterInfoArray() throws IOException {
        return this.readDataType(ConcentratorSetupAttributes.METER_INFO, Array.class);
    }

    /**
     * @return boolean DLMS object, true the concentrator component is running
     */
    public BooleanObject isActive() throws IOException {
        return this.readDataType(ConcentratorSetupAttributes.IS_ACTIVE, BooleanObject.class);
    }

    /**
     * @return Gets the maximum amount of concurrent meter sessions that are allowed.
     * @throws IOException
     */
    public Unsigned16 getMaxConcurrentSessions() throws IOException {
        return this.readDataType(ConcentratorSetupAttributes.MAX_CONCURENT_SESSIONS, Unsigned16.class);
    }

    /**
    * @return Gets the protocol event log level.
    * @throws IOException
    */
    public TypeEnum getProtocolEventLogLevel() throws IOException {
        return this.readDataType(ConcentratorSetupAttributes.PROTOCOL_LOG_LEVEL, TypeEnum.class);
    }

    /** Translates an enum log level into text
     *    protocol_log_level ::= ENUMERATION
     {
     [0]:    OFF,
     [1]:    WARNING,
     [2]:    INFO,
     [3]:    DEBUG
     }
     * @param logLevel
     * @return
     */
    public static String getLogLevelDescription(TypeEnum logLevel) {
        switch (logLevel.getValue()){
            case 0: return "OFF";
            case 1: return "WARNING";
            case 2: return "INFO";
            case 3: return "DEBUG";
        }
        return logLevel.toString();
    }

    /**
     * Transforms the array of meterInfos into a JSON array, with all the details about the device
     * @param meterInfos
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static String buildMeterInfoJSON(Array meterInfos) throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        Calendar now = Calendar.getInstance();
        for (final AbstractDataType entry : meterInfos) {
            MeterInfo meterInfo = MeterInfo.fromStructure(entry.getStructure());

            JSONObject jsonMeterInfo = new JSONObject();

            jsonMeterInfo.put("serial", meterInfo.getSerialNumber());
            jsonMeterInfo.put("state", meterInfo.getSchedulingState().toString());
            jsonMeterInfo.put("id", ProtocolTools.getHexStringFromBytes(meterInfo.getId(), ""));
            jsonMeterInfo.put("tz", meterInfo.getTimezone().getDisplayName());

            JSONArray jsonDeviceTypes = new JSONArray();
            for(DeviceTypeAssignment deviceType : meterInfo.getDeviceTypeAssignments()){
                JSONObject jsonDeviceType = new JSONObject();
                jsonDeviceType.put("tid", deviceType.getDeviceTypeId());
                jsonDeviceType.put("st", dateFormat.format(deviceType.getStartTime().getTime()));
                if (now.compareTo(deviceType.getEndTime())==1) {
                    jsonDeviceType.put("et", "*");                 // future date
                } else {
                    jsonDeviceType.put("et", dateFormat.format(deviceType.getEndTime().getTime()));
                }
                jsonDeviceTypes.put(jsonDeviceType);
            }

            jsonMeterInfo.put("deviceTypes", jsonDeviceTypes);

            jsonArray.put(jsonMeterInfo);
        }

        return jsonArray.toString();
    }

    /**
     * Transforms the meterInfo structures into a list of serial numbers
     *
     * @param meterInfos
     * @return
     * @throws IOException
     */
    public static String buildMeterInfoAsSerial(Array meterInfos) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        for (final AbstractDataType entry : meterInfos) {
            MeterInfo meterInfo = MeterInfo.fromStructure(entry.getStructure());
            stringBuilder.append(meterInfo.getSerialNumber()).append(",");
        }

        return stringBuilder.toString();
    }

    /**
     * Transforms the meterInfo structures into a list of MAC addresses
     *
     * @param meterInfos
     * @return
     * @throws IOException
     */
    public static String buildMeterInfoAsMAC(Array meterInfos) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        for (final AbstractDataType entry : meterInfos) {
            MeterInfo meterInfo = MeterInfo.fromStructure(entry.getStructure());
            String mac = ProtocolTools.getHexStringFromBytes(meterInfo.getId(),"");
            stringBuilder.append(mac).append(",");
        }

        return stringBuilder.toString();
    }

}
