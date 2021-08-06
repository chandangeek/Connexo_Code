package com.energyict.protocolimplv2.umi.ei4.events;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimplv2.umi.types.UmiCode;
import com.energyict.protocolimplv2.umi.types.UmiId;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EI4UmiStandardEventLog {
    protected TimeZone timeZone;

    /**
     * Container containing raw events
     */
    protected EI4UmiwanEventStatus umiwanEventStatus;
    protected List<EI4UmiwanEvent> umiwanEventList;
    protected List<MeterEvent> meterEvents;

    public EI4UmiStandardEventLog(TimeZone timeZone, EI4UmiwanEventStatus umiwanEventStatus, List<EI4UmiwanEvent> umiwanEventList) {
        this.timeZone = timeZone;
        this.umiwanEventStatus = umiwanEventStatus;
        this.umiwanEventList = new ArrayList<>(umiwanEventList);
    }

    public List<MeterEvent> getMeterEvents() {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        for (int i = 0; i < umiwanEventList.size(); i++) {
            EI4UmiwanEvent event = umiwanEventList.get(i);
            buildMeterEvent(meterEvents, event.getTimestamp(), event.getEventType(), event.getAssociatedData());
        }
        return meterEvents;
    }

    protected void buildMeterEvent(List<MeterEvent> meterEvents, Date eventTimeStamp, int eventId, byte[] associatedDataArray) {
        EI4UmiEventTypes eventType = EI4UmiEventTypes.fromValue(eventId);
        ByteBuffer associatedDataBuffer = ByteBuffer.wrap(associatedDataArray.clone()).order(ByteOrder.LITTLE_ENDIAN);
        switch (eventType) {
            case EGM_EVENT_VERIFICATION_WRITE:
                byte[] umiCodeArray = new byte[4];
                associatedDataBuffer.get(umiCodeArray);
                UmiCode umiCodeVW = new UmiCode(umiCodeArray);
                int errorCodeVW = Short.toUnsignedInt(associatedDataBuffer.getShort());
                if (errorCodeVW != 0) {
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ACCESS_WRITE, eventId, EI4UmiEventTypes.EGM_EVENT_VERIFICATION_WRITE.getDescription() + " " + umiCodeVW.getCode() + " failed with the code " + errorCodeVW));
                } else {
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.ACCESS_WRITE, eventId, EI4UmiEventTypes.EGM_EVENT_VERIFICATION_WRITE.getDescription() + " " + umiCodeVW.getCode()));
                }
                break;
            case EGM_EVENT_REVERSE_FLOW:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.GAS_REVERSE_FLOW_START, eventId, EI4UmiEventTypes.EGM_EVENT_REVERSE_FLOW.getDescription()));
                break;
            case EGM_EVENT_SOFTWARE_UPDATE:
                int reasonBuffer = Byte.toUnsignedInt(associatedDataBuffer.get());
                int errorCode = Byte.toUnsignedInt(associatedDataBuffer.get());
                switch (reasonBuffer) {
                    case 0: /* Upgrade started */
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_UPDATE_START, eventId, EI4UmiEventTypes.EGM_EVENT_SOFTWARE_UPDATE.getDescription() + " started"));
                        break;
                    case 1: /* Upgrade completed */
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_ACTIVATED, eventId, EI4UmiEventTypes.EGM_EVENT_SOFTWARE_UPDATE.getDescription() + " completed"));
                        break;
                    case 2: /* Upgrade failed/error */
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_UPDATE_ACTIVATION_FAILURE, eventId, EI4UmiEventTypes.EGM_EVENT_SOFTWARE_UPDATE.getDescription() + " failed with the code " + errorCode));
                        break;
                    default:
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.FIRMWARE_UPGRADE, eventId, EI4UmiEventTypes.EGM_EVENT_SOFTWARE_UPDATE.getDescription()));
                        break;
                }
                break;
            case EGM_EVENT_CONFIG_UPDATE:
                byte[] umiCodeCUArray = new byte[4];
                associatedDataBuffer.get(umiCodeCUArray);
                UmiCode umiCodeCU = new UmiCode(umiCodeCUArray);
                int errorCodeCU = Short.toUnsignedInt(associatedDataBuffer.getShort());
                if (errorCodeCU != 0) {
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATION_ERROR, eventId, EI4UmiEventTypes.EGM_EVENT_CONFIG_UPDATE.getDescription() + " " + umiCodeCU.toString() + " failed with the code " + umiCodeCU));
                } else {
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CONFIGURATIONCHANGE, eventId, EI4UmiEventTypes.EGM_EVENT_CONFIG_UPDATE.getDescription() + " " + umiCodeCU.toString()));
                }
                break;
            case EGM_EVENT_MISER_MODE:
                int miserModeCause = Byte.toUnsignedInt(associatedDataBuffer.get());
                associatedDataBuffer.get(); // unused byte
                long batteryCurrentEnergy = Integer.toUnsignedLong(associatedDataBuffer.getInt());
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_CONSUMPTION_HIGH, eventId, EI4UmiEventTypes.EGM_EVENT_MISER_MODE.getDescription()
                        + (miserModeCause == 0 ? " on. " : " off. ") + "Battery current energy is " + batteryCurrentEnergy + " mAs."));
                break;
            case EGM_EVENT_BATTERY:
                long batteryCause = associatedDataBuffer.getLong();
                switch ((int) batteryCause) {
                    case 0: /* Energy < 10% of initial energy */
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_LEVEL_BELOW_LOW_LEVEL_START, eventId,
                                EI4UmiEventTypes.EGM_EVENT_BATTERY.getDescription() + " energy < 10% of initial energy."));
                        break;
                    case 1: /* Energy < umi.1.1.64.21/3 */
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_LEVEL_BELOW_LOW_LEVEL_START, eventId,
                                EI4UmiEventTypes.EGM_EVENT_BATTERY.getDescription() + " energy < umi.1.1.64.21/3."));
                        break;
                    case 2: /* Voltage < umi.1.1.64.21/4 */
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_VOLTAGE_LOW, eventId,
                                EI4UmiEventTypes.EGM_EVENT_BATTERY.getDescription() + " voltage < umi.1.1.64.21/4."));
                        break;
                    case 3: /* Voltage < 2.5V (HLC damage) */
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_VOLTAGE_LOW, eventId,
                                EI4UmiEventTypes.EGM_EVENT_BATTERY.getDescription() + " voltage < 2.5V (HLC damage)."));
                        break;
                    case 4: /* Hibernation exit */
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_ABOVE_CRITICAL_LEVEL, eventId,
                                EI4UmiEventTypes.EGM_EVENT_BATTERY.getDescription() + " hibernation exit."));
                        break;
                    case 5: /* Hibernation entry (battery exhausted) */
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_BELOW_CRITICAL_LEVEL, eventId,
                                EI4UmiEventTypes.EGM_EVENT_BATTERY.getDescription() + " hibernation entry (battery exhausted)."));
                        break;
                    case 6: /* Battery inserted */
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_STATUS_ENABLED, eventId, EI4UmiEventTypes.EGM_EVENT_BATTERY.getDescription() + " inserted."));
                        break;
                    case 7: /* Battery removed */
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNAUTHORIZED_BATTERY_REMOVE, eventId, EI4UmiEventTypes.EGM_EVENT_BATTERY.getDescription() + " removed."));
                        break;
                    case 8: /* Battery change start #1 */
                    case 9: /* Battery change start #2 */
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REPLACE_BATTERY, eventId,
                                EI4UmiEventTypes.EGM_EVENT_BATTERY.getDescription() + "change start."));
                        break;
                    case 10: /* Battery change timeout #1 */
                    case 11: /* Battery change timeout #2 */
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REPLACE_BATTERY, eventId,
                                EI4UmiEventTypes.EGM_EVENT_BATTERY.getDescription() + "change timeout."));
                        break;
                    case 12: /* Miser Mode OFF (in DEBUG image only) */
                    case 13: /* Miser Mode ON (in DEBUG image only) */
                    case 14: /* OLD Battery */
                    case 15: /* NEW Battery */
                    case 16: /* Battery Replacing by Technician */
                        meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.REPLACE_BATTERY, eventId,
                                EI4UmiEventTypes.EGM_EVENT_BATTERY.getDescription() + " replacing by Technician."));
                        break;
                }
                break;
            case EGM_EVENT_CASE_OPENED:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.COVER_OPENED, eventId, EI4UmiEventTypes.EGM_EVENT_CASE_OPENED.getDescription()));
                break;
            case EGM_EVENT_CASE_CLOSED:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.METER_COVER_CLOSED, eventId, EI4UmiEventTypes.EGM_EVENT_CASE_CLOSED.getDescription()));
                break;
            case EGM_EVENT_MAGNET_ON:
                /**
                 * 0-1:  Sensor r2 value
                 * 2-3: r2 lower limit (temperature-related)
                 * 4-5: r2 upper limit (temperature-related)
                 * 6-7: Temperature in 0.01K
                 */
                int sensorValueOn = Short.toUnsignedInt(associatedDataBuffer.getShort());
                int sensorLowerLimitOn = Short.toUnsignedInt(associatedDataBuffer.getShort());
                int sensorUpperLimitOn = Short.toUnsignedInt(associatedDataBuffer.getShort());
                int temperatureOn = Short.toUnsignedInt(associatedDataBuffer.getShort());

                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.STRONG_DC_FIELD_DETECTED, eventId, EI4UmiEventTypes.EGM_EVENT_MAGNET_ON.getDescription()
                        + ". Sensor r2 value = " + sensorValueOn + ", r2 lower limit (temperature-related) = " + sensorLowerLimitOn
                        + ", r2 upper limit (temperature-related) = " + sensorUpperLimitOn + ", Temperature in 0.01K = " + temperatureOn));
                break;
            case EGM_EVENT_MAGNET_OFF:
                /**
                 * 0-1:  Sensor r2 value
                 * 2-3: r2 lower limit (temperature-related)
                 * 4-5: r2 upper limit (temperature-related)
                 * 6-7: Temperature in 0.01K
                 */
                int sensorValueOff = Short.toUnsignedInt(associatedDataBuffer.getShort());
                int sensorLowerLimitOff = Short.toUnsignedInt(associatedDataBuffer.getShort());
                int sensorUpperLimitOff = Short.toUnsignedInt(associatedDataBuffer.getShort());
                int temperatureOff = Short.toUnsignedInt(associatedDataBuffer.getShort());

                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.NO_STRONG_DC_FIELD_ANYMORE, eventId, EI4UmiEventTypes.EGM_EVENT_MAGNET_OFF.getDescription()
                        + ". Sensor r2 value = " + sensorValueOff + ", r2 lower limit (temperature-related) = " + sensorLowerLimitOff
                        + ", r2 upper limit (temperature-related) = " + sensorUpperLimitOff + ", Temperature in 0.01K = " + temperatureOff));
                break;
            case EGM_EVENT_UMI_CONTROL:
                /**
                 * 0-1: The UMI Port(s) controlled (bitfield)
                 * 2: 0=disable, 1=enable
                 * 3-7: Unused
                 */
                int status = Byte.toUnsignedInt(associatedDataBuffer.get());
                if (status == 0) {
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MODEM_DISCONNECTED, eventId, EI4UmiEventTypes.EGM_EVENT_UMI_CONTROL.getDescription() + ", modem disconnected."));
                } else {
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MODEM_CONNECTED, eventId, EI4UmiEventTypes.EGM_EVENT_UMI_CONTROL.getDescription() + ", modem connected."));
                }
                break;
            case EGM_EVENT_SOFTWARE_RESTART_REQUEST:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SOFTWARE_RESTART_REQUEST, eventId, EI4UmiEventTypes.EGM_EVENT_SOFTWARE_RESTART_REQUEST.getDescription()));
                break;
            case EGM_EVENT_SOFTWARE_RESTART:
                /**
                 * 0: Cause of reset
                 * 1-7: Unused
                 * Causes: see MSP430 SYSRSTIV register
                 */
                int causeOfReset = Byte.toUnsignedInt(associatedDataBuffer.get());
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.DEVICE_RESET, eventId,
                        EI4UmiEventTypes.EGM_EVENT_SOFTWARE_RESTART.getDescription() + " caused by: " + causeOfReset));
                break;
            case EGM_EVENT_OPTO_COMMUNICATIONS:
                /**
                 * 0-7: The UMI ID of the opto Peripheral (following a successful authentication), or 0xFFFFFFFFFFFFFFFF to indicate successful sign-on
                 */
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.LOCAL_COMM_START, eventId, EI4UmiEventTypes.EGM_EVENT_OPTO_COMMUNICATIONS.getDescription()));
                break;
            case EGM_EVENT_CLOCK_SET:
                /**
                 * 0-3: The adjustment, in seconds (+ve = forwards)
                 */
                long seconds = Integer.toUnsignedLong(associatedDataBuffer.getInt());
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SETCLOCK, eventId, EI4UmiEventTypes.EGM_EVENT_CLOCK_SET.getDescription() + ". The adjustment is " + seconds + " seconds"));
                break;
            case EGM_EVENT_LEAKY_VALVE:
                // EI4 doesn't have a valve
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, EI4UmiEventTypes.EGM_EVENT_LEAKY_VALVE.getDescription()));
                break;
            case EGM_EVENT_HIGH_FLOW_RATE:
                /**
                 * 0: Reason
                 * 1-7: Unused
                 * Reasons:
                 * 0 Sensor quadrature error
                 * 1 Flow rate calculation
                 */
                byte reason = associatedDataBuffer.get();
                if (reason == 0) {
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.GAS_FLOW_RATE_ABOVE_THRESHOLD_START, eventId,
                            EI4UmiEventTypes.EGM_EVENT_HIGH_FLOW_RATE.getDescription() + ". Sensor quadrature error"));
                } else {
                    meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.GAS_FLOW_RATE_ABOVE_THRESHOLD_START, eventId,
                            EI4UmiEventTypes.EGM_EVENT_HIGH_FLOW_RATE.getDescription() + ". Flow rate calculation"));
                }
                break;
            case EGM_EVENT_ENV_CONDITIONS:
                /**
                 * 0: Reason
                 * 1: Type of event
                 * 2-5: Additional Info
                 * 6-7: Unused
                 * Reasons:
                 * 0x01 –  Temperature value out of range
                 * 0x02 –  Pressure value out of range
                 * Event Types:
                 * 0x01 –  LOW range
                 * 0x02 –  HIGH range
                 * 0x04 –  Invalid
                 * Additional Info:
                 * 0xXXXXXXXX – Measured value
                 */
                // EI4 doesn't have either pressure or temperature sensors
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, EI4UmiEventTypes.EGM_EVENT_ENV_CONDITIONS.getDescription()));
                break;
            case EGM_EVENT_SOFTWARE_ALARM:
                /**
                 * 0-3: Program counter
                 * 4-5: Line number
                 * 6: Type of event
                 * 7: Unused
                 * Type of event:
                 * 0x01: General software event
                 * 0x02: Assertion failure
                 * 0xXX: Unused
                 */
                long programCounter = Integer.toUnsignedLong(associatedDataBuffer.getInt());
                int lineNumber = Short.toUnsignedInt(associatedDataBuffer.getShort());
                byte typeOfEvent = associatedDataBuffer.get();
                String typeOfEventDescription;
                if (typeOfEvent == 0) {
                    typeOfEventDescription = "General software event";
                } else {
                    typeOfEventDescription = "Assertion failure";
                }
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.APPLICATION_ALERT_START, eventId,
                        EI4UmiEventTypes.EGM_EVENT_SOFTWARE_ALARM.getDescription() + ". Program counter: " + programCounter
                                + ", line number: " + lineNumber + ", type of event: " + typeOfEventDescription));
                break;
            case EGM_EVENT_PERMLOG_90_PERCENT:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.PERMANENT_LOG_FILLED_UP_90_PERSENT, eventId, EI4UmiEventTypes.EGM_EVENT_PERMLOG_90_PERCENT.getDescription()));
                break;
            case EGM_EVENT_ERASE_EVENTS:
                /**
                 * 0-3: The bitfield used in the erase
                 * 4-7: The event bits actually erased
                 */
                long requestedBitfield = Integer.toUnsignedLong(associatedDataBuffer.getInt());
                long actualBitfield = Integer.toUnsignedLong(associatedDataBuffer.getInt());
                StringBuffer requestedBitfieldString = new StringBuffer(". Requested bitfield: ");
                requestedBitfieldString.append(String.format("%16s", Integer.toBinaryString((int) requestedBitfield)).replace(' ', '0'));
                StringBuffer actualBitfieldString = new StringBuffer(". Erased bitfield: ");
                requestedBitfieldString.append(String.format("%16s", Integer.toBinaryString((int) actualBitfield)).replace(' ', '0'));
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.EVENT_LOG_CLEARED, eventId, EI4UmiEventTypes.EGM_EVENT_ERASE_EVENTS.getDescription()
                        + requestedBitfieldString.toString() + actualBitfieldString.toString()));
                break;
            case EGM_EVENT_FAILED_AUTHENTICATION:
                /**
                 * 0-7: The UMI ID of the device that failed authentication.
                 */

                byte[] id = new byte[8];
                associatedDataBuffer.get(id);
                UmiId umiId = new UmiId(id, true);
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.UNAUTHORIZED_ACCESS, eventId, EI4UmiEventTypes.EGM_EVENT_FAILED_AUTHENTICATION.getDescription()
                        + ". The UMI ID of the device that failed authentication: " + umiId.toString()));
                break;
            case EGM_EVENT_SPARE26:
            case EGM_EVENT_SPARE31:
                break;
            case EGM_EVENT_SELFTEST_FAILURE:
                /**
                 * 0-1: Failure code
                 * 2-5: Additional data
                 * 6-7: Line number
                 * Failure codes are:
                 * 0 Unexpected interrupt occurred (additional data gives more information)
                 * 1 Active image is corrupt
                 * 2 Inactive image 0 is corrupt
                 * 3 Stack warning
                 * 4 FIFO log corruption detected
                 * 5 Permanent log corruption detected
                 * 6 Interval log corruption detected
                 * 7 General store corruption detected
                 * 8 RAM code corruption detected
                 * 9 Vector table corruption detected
                 * 10 Serial flash header invalid
                 * 11 Inactive image 1 is corrupt
                 * 12 Boot image is corrupt
                 * 13 Temperature sensor failure
                 * 14 Serial flash not ready
                 * 15 Serial flash not in binary mode
                 * 16 Pressure sensor failure
                 */
                int failureCode = Short.toUnsignedInt(associatedDataBuffer.getShort());
                String failureDescription = "";
                switch ((int) failureCode) {
                    case 0:
                        failureDescription = "Unexpected interrupt occurred (additional data gives more information).";
                        break;
                    case 1:
                        failureDescription = "Active image is corrupt.";
                        break;
                    case 2:
                        failureDescription = "Inactive image 0 is corrupt.";
                        break;
                    case 3:
                        failureDescription = "Stack warning.";
                        break;
                    case 4:
                        failureDescription = "FIFO log corruption detected.";
                        break;
                    case 5:
                        failureDescription = "Permanent log corruption detected.";
                        break;
                    case 6:
                        failureDescription = "Interval log corruption detected.";
                        break;
                    case 7:
                        failureDescription = "General store corruption detected.";
                        break;
                    case 8:
                        failureDescription = "RAM code corruption detected.";
                        break;
                    case 9:
                        failureDescription = "Vector table corruption detected.";
                        break;
                    case 10:
                        failureDescription = "Serial flash header invalid.";
                        break;
                    case 11:
                        failureDescription = "Inactive image 1 is corrupt.";
                        break;
                    case 12:
                        failureDescription = "Boot image is corrupt.";
                        break;
                    case 13:
                        failureDescription = "Temperature sensor failure.";
                        break;
                    case 14:
                        failureDescription = "Serial flash not ready.";
                        break;
                    case 15:
                        failureDescription = "Serial flash not in binary mode.";
                        break;
                    case 16:
                        failureDescription = "Pressure sensor failure.";
                        break;
                }
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.CRITICAL_SOFTWARE_ERROR, eventId,
                        EI4UmiEventTypes.EGM_EVENT_SELFTEST_FAILURE.getDescription() + ". Failure code: " + failureCode + ". " + failureDescription));
                break;
            case EGM_EVENT_BAD_PERIPHERAL:
                /**
                 * 0-1: Line number
                 * 2: Failure code
                 * 3: Peripheral number
                 * 4-5: UMI error code leading to this event
                 * Failure codes are:
                 * 1: Failed to initialise properly
                 * 2: Event publish failed
                 * 3: End session failed
                 * 4: Max on time reached without power
                 * off request by peripheral
                 * 5: Host power control failed
                 * 6: Power on failed for initialised device
                 * 7: Invalid UI
                 * 8: Invalid screen data
                 * 9: Invalid resource data
                 * 10: Start session failed
                 */
                int line = Short.toUnsignedInt(associatedDataBuffer.getShort());
                int failure = Byte.toUnsignedInt(associatedDataBuffer.get());
                int peripheralNumber = Byte.toUnsignedInt(associatedDataBuffer.get());
                int umiErrorCode = Short.toUnsignedInt(associatedDataBuffer.getShort());
                String failureString = "";
                switch (failure) {
                    case 1:
                        failureString = "Failed to initialise properly";
                        break;
                    case 2:
                        failureString = "Event publish failed";
                        break;
                    case 3:
                        failureString = "End session failed";
                        break;
                    case 4:
                        failureString = "Max on time reached without power off request by peripheral";
                        break;
                    case 5:
                        failureString = "Host power control failed";
                        break;
                    case 6:
                        failureString = "Power on failed for initialised device";
                        break;
                    case 7:
                        failureString = "Invalid UI";
                        break;
                    case 8:
                        failureString = "Invalid screen data";
                        break;
                    case 9:
                        failureString = "Invalid resource data";
                        break;
                    case 10:
                        failureString = "Start session failed";
                        break;

                }
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MODEM_SESSION_FAILED, eventId,
                        EI4UmiEventTypes.EGM_EVENT_BAD_PERIPHERAL.getDescription()
                                + ". Line number: " + line
                                + ". Failure code: " + failureString
                                + ". Peripheral number: " + peripheralNumber
                                + ". UMI error code leading to this event: " + umiErrorCode));
                break;
            case EGM_EVENT_PERIPHERAL_FOUND:
                /**
                 * 0-7: The UMI ID of the new Peripheral.
                 */
                byte[] idBytes = new byte[8];
                associatedDataBuffer.get(idBytes);
                UmiId peripheralUmiId = new UmiId(idBytes, true);
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MODEM_CONNECTED, eventId, EI4UmiEventTypes.EGM_EVENT_PERIPHERAL_FOUND.getDescription()
                        + ". The UMI ID of the new Peripheral: " + peripheralUmiId.toString()));
                break;
            case EGM_EVENT_BAD_DECRYPT:
                /**
                 * 0-7: The UMI ID of the new Peripheral.
                 */
                byte[] bytes = new byte[8];
                associatedDataBuffer.get(bytes);
                UmiId peripheralId = new UmiId(bytes, true);
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MODEM_SESSION_FAILED, eventId, EI4UmiEventTypes.EGM_EVENT_BAD_DECRYPT.getDescription()
                        + ". The UMI ID of the new Peripheral: " + peripheralId.toString()));
                break;
            case EGM_EVENT_GSM_AT_CMD_FAIL:
                /**
                 * 0: The number of the AT command that failed [Ref: AT CMD list]
                 * 1: CME ERROR=0, CMS ERROR =1, UNKNOWN = 0xFF
                 * 2-3: Returned CME/CMS ERROR code. [LSB2, MSB3]; [Ref: CME / CMS ERROR list]
                 * 4-7: Unused
                 */
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.MODEM_FAILS_RESPONSE, eventId, EI4UmiEventTypes.EGM_EVENT_GSM_AT_CMD_FAIL.getDescription()));
                break;
            case EGM_EVENT_GSM_VOLTAGE_LOW:
                /**
                 * 0-1: Configured minimum threshold host HLC voltage in mV. When under this value a call will not be attempted. Configured in 1.1.0.243 member 1 [LSB0, MSB1]
                 * 2-3: HLC Voltage from host in mV [LSB2, MSB3]
                 * 4-7: Unused.
                 */
                int minimumVoltage = Short.toUnsignedInt(associatedDataBuffer.getShort());
                int voltage = Short.toUnsignedInt(associatedDataBuffer.getShort());
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.BATTERY_VOLTAGE_LOW, eventId,
                        EI4UmiEventTypes.EGM_EVENT_GSM_VOLTAGE_LOW.getDescription()
                                + ". Configured minimum voltage threshold: " + minimumVoltage + ", voltage from host: " + voltage + " mV."));
                break;
            case EGM_EVENT_GSM_SIGNAL_STRENGTH:
                /**
                 * 0: return value from modem 0-31 or 99. Note: registration depends on umi object 1.194.1 Member 8
                 */
                int signalStrength = Byte.toUnsignedInt(associatedDataBuffer.get());
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.SIGNAL_QUALITY_LOW, eventId,
                        EI4UmiEventTypes.EGM_EVENT_GSM_SIGNAL_STRENGTH.getDescription()
                                + ". Signal strength is not sufficient: " + signalStrength + ". A call will not be attempted."));
                break;
            case EGM_EVENT_RETRIES_LEFT_FROM_MAX:
                /**
                 * 0:  The AT command that reported the data [Ref: AT CMD list]
                 * 1:  The number of attempts remaining (1 means last attempt failed)
                 * 2:  The status <stat> of the command response:
                 * [Ref: AT_Commands_User_Guide]
                 * 0: Not registered, MT is not currently searching
                 * 2: Not registered, but MT is currently searching
                 * 3: Registration denied
                 * 4: Unknown
                 * 5: Registered, roaming
                 */
                int commandAT = Byte.toUnsignedInt(associatedDataBuffer.get());
                int attemptsLeft = Byte.toUnsignedInt(associatedDataBuffer.get());
                int response = Byte.toUnsignedInt(associatedDataBuffer.get());
                String responseDescription = "";
                switch (response) {
                    case 0:
                        responseDescription = "Not registered, MT is not currently searching.";
                        break;
                    case 1:
                        responseDescription = ".";
                        break;
                    case 2:
                        responseDescription = "Not registered, but MT is currently searching.";
                        break;
                    case 3:
                        responseDescription = "Registration denied.";
                        break;
                    case 4:
                        responseDescription = "Unknown.";
                        break;
                    case 5:
                        responseDescription = "Registered, roaming.";
                        break;
                }
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.GSM_GPRS_REGISTRATION_FAIL, eventId,
                        EI4UmiEventTypes.EGM_EVENT_RETRIES_LEFT_FROM_MAX.getDescription()
                                + ". The AT command that reported the data [Ref: AT CMD list]: " + commandAT
                                + ". The number of attempts remaining (1 means last attempt failed):  " + attemptsLeft
                                + ". Response: " + responseDescription));
                break;
            default:
                meterEvents.add(new MeterEvent((Date) eventTimeStamp.clone(), MeterEvent.OTHER, eventId, "Unknown event code: " + eventId));
                break;
        }
    }
}
