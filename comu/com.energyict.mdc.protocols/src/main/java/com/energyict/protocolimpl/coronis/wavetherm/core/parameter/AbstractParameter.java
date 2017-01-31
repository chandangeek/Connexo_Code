/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;
import com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand.AbstractRadioCommand;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

abstract public class AbstractParameter extends AbstractRadioCommand {

    static final int PARAM_UPDATE_OK = 0x00;
    static final int PARAM_UPDATE_ERROR = 0xFF;
    private static final int RESPONSE_ACK_NEW_READMODE = 0x98;        //Old read mode response: 0x90, indicates the presence of the working mode bytes in the response.
    private static final int RESPONSE_ACK_NEW_WRITEMODE = 0x99;       //Old write mode response: 0x91, indicates the presence of the working mode bytes in the response.
    private static final int REQUEST_NEW_WRITEMODE = 0x19;            //Old write mode request: 0x11

    enum ParameterId {

        OperationMode(0x01, 1, "Operation mode"),
        ApplicationStatus(0x20, 1, "Application Status"),
        BatteryLifeDurationCounter(0xA2, 2, "Battery life duration counter"),
        NumberOfRetriesForAlarmTransmission(0x19, 1, "Number of retries for alarm transmission"),
        TimeBetweenEachRetry(0x1A, 1, "Time between each alarm transmission retry"),
        AlarmConfig(0x22, 1, "Alarm configuration byte"),
        SamplingActivationStartHour(0x81, 1, "Start hour for the periodic data logging"),
        DayOfWeek(0x82, 1, "Day of week for the weekly datalogging"),
        BatteryLifeDateEnd(0x90, 6, "Date of end of battery life detection"),
        TimeOfMeasurement(0x83, 1, "Start hour for the weekly / monthly data logging"),
        NumberOfLoggedValues(0x0B, 2, "Number of logged values"),
        NumberOfLogLoops(0x0A, 1, "Number of loops in the log table"),

        DetectionMeasurementPeriod(0x23, 1, "Measurement period for threshold detection (in minutes)"),

        HighThresholdSensor1(0x15, 2, "High threshold on sensor 1 (in °C)"),
        LowThresholdSensor1(0x16, 2, "Low threshold on sensor 1 (in °C)"),
        HighThresholdAlarmDurationSensor1(0x17, 1, "High threshold alarm duration, for sensor 1"),
        LowThresholdAlarmDurationSensor1(0x18, 1, "Low threshold alarm duration, for sensor 1"),

        HighThresholdSensor2(0x2B, 2, "High threshold on sensor 2  (in °C)"),
        LowThresholdSensor2(0x2C, 2, "Low threshold on sensor 2 (in °C)"),
        HighThresholdAlarmDurationSensor2(0x2D, 1, "High threshold alarm duration, for sensor 2"),
        LowThresholdAlarmDurationSensor2(0x2E, 1, "Low threshold alarm duration, for sensor 1"),

        NumberOfRepeaters(0xB0, 1, "Number of repeaters"),
        RepeaterAddress1(0xB1, 6, "Repeater address 1"),
        RepeaterAddress2(0xB2, 6, "Repeater address 2"),
        RepeaterAddress3(0xB3, 6, "Repeater address 3"),
        RecipientAddress(0xB4, 6, "Recipient address"),

        SamplingPeriod(0x80, 1, "Measurement period, in seconds");

        private int id;
        private int length;
        private String description;

        ParameterId(final int id, final int length, final String description) {
            this.id = id;
            this.length = length;
            this.description = description;
        }

        public String toString() {
            return WaveflowProtocolUtils.toHexString(id) + ", " + description;
        }

        static ParameterId fromId(final int id) {
            for (ParameterId pid : values()) {
                if (pid.id == id) {
                    return pid;
                }
            }
            return null;
        }

    }

    /**
     * The working mode of the wavetherm device is implicit to the write and read command
     */
    private int workingMode;

    /**
     * Operating mode write mask
     */
    private int mask = 0xffff;

    final void setMask(int mask) {
        this.mask = mask;
    }

    public final int getWorkingMode() {
        return workingMode;
    }

    final void setWorkingMode(int workingMode) {
        this.workingMode = workingMode;
    }


    AbstractParameter(WaveTherm waveTherm) {
        super(waveTherm);
    }

    abstract ParameterId getParameterId();

    void write() throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeByte(RadioCommandId.WriteParameter.getCommandId());
            if (getParameterId() == null) {
                daos.writeShort(workingMode); // update the working mode (concat of ext operation mode and operation mode)
                daos.writeShort(mask); // mask to update the working mode
                daos.writeByte(0); // write 0 parameter, only update the working mode
            } else {
                if (RadioCommandId.WriteParameter.getCommandId() == REQUEST_NEW_WRITEMODE) {
                    daos.writeShort(0); // don't update the working mode, value don't care
                    daos.writeShort(0); // don't update the working mode, mask = 0
                }
                daos.writeByte(1); // write 1 parameter
                daos.writeByte(getParameterId().id);
                daos.writeByte(getParameterId().length);
                daos.write(prepare());
            }

            parseWriteResponse(getWaveTherm().getWaveFlowConnect().sendData(baos.toByteArray()));

        }
        finally {
            if (baos != null) {
                try {
                    baos.close();
                }
                catch (IOException e) {
                    getWaveTherm().getLogger().severe(ProtocolUtils.stack2string(e));
                }
            }
        }
    }

    private final void parseWriteResponse(final byte[] data) throws IOException {
        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));

            int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
            if (commandIdAck != (0x80 | RadioCommandId.WriteParameter.getCommandId())) {
                throw new WaveFlowException("Invalid response tag [" + WaveflowProtocolUtils.toHexString(commandIdAck) + "]");
            } else {

                if (commandIdAck == RESPONSE_ACK_NEW_WRITEMODE) {
                    workingMode = dais.readShort();
                }

                if (getParameterId() != null) {
                    int nrOfParameters = WaveflowProtocolUtils.toInt(dais.readByte());
                    if (nrOfParameters != 1) {
                        throw new WaveFlowException("Writing only 1 parameter at a time allowed, returned [" + nrOfParameters + "] parameters!");
                    }

                    ParameterId pid = ParameterId.fromId(WaveflowProtocolUtils.toInt(dais.readByte()));
                    if (pid != getParameterId()) {
                        throw new WaveFlowException("Invalid parameter returned expected [" + getParameterId() + "], returned [" + pid + "]");
                    }
                }

                int result = WaveflowProtocolUtils.toInt(dais.readByte());
                if (result != PARAM_UPDATE_OK) {
                    throw new WaveFlowException("Update parameter [" + getParameterId() + "] failed. Result code [" + WaveflowProtocolUtils.toHexString(result) + "]");
                }
            }
        }
        finally {
            if (dais != null) {
                try {
                    dais.close();
                }
                catch (IOException e) {
                    getWaveTherm().getLogger().severe(ProtocolUtils.stack2string(e));
                }
            }
        }
    }

    public void read() throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeByte(RadioCommandId.ReadParameter.getCommandId());
            if (getParameterId() == null) {
                daos.writeByte(0); // write 0 parameter, only update the operating mode
            } else {
                daos.writeByte(1); // write 1 parameter
                daos.writeByte(getParameterId().id);
                daos.writeByte(getParameterId().length);
            }
            parseReadResponse(getWaveTherm().getWaveFlowConnect().sendData(baos.toByteArray()));
        }
        finally {
            if (baos != null) {
                try {
                    baos.close();
                }
                catch (IOException e) {
                    getWaveTherm().getLogger().severe(ProtocolUtils.stack2string(e));
                }
            }
        }
    }

    private void parseReadResponse(final byte[] data) throws IOException {
        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));
            int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
            if (commandIdAck != (0x80 | RadioCommandId.ReadParameter.getCommandId())) {
                throw new WaveFlowException("Invalid response tag [" + WaveflowProtocolUtils.toHexString(commandIdAck) + "]");
            } else {

                if (commandIdAck == RESPONSE_ACK_NEW_READMODE) {       //The operating mode is only received in case of the new parameter read.
                    workingMode = dais.readShort();
                }

                if (getParameterId() != null) {
                    int nrOfParameters = WaveflowProtocolUtils.toInt(dais.readByte());
                    if (nrOfParameters != 1) {
                        throw new WaveFlowException("Reading only 1 parameter at a time allowed, returned [" + nrOfParameters + "] parameters!");
                    }

                    ParameterId pid = ParameterId.fromId(WaveflowProtocolUtils.toInt(dais.readByte()));
                    if (pid != getParameterId()) {
                        throw new WaveFlowException("Invalid parameter returned expected [" + getParameterId() + "], returned [" + pid + "]");
                    }

                    int length = WaveflowProtocolUtils.toInt(dais.readByte());
                    if (length != getParameterId().length) {
                        throw new WaveFlowException("Invalid length returned expected [" + getParameterId().length + "], returned [" + length + "]");
                    }

                    byte[] resultData = new byte[dais.available()];
                    dais.read(resultData);
                    parse(resultData);
                }
            }
        }
        finally {
            if (dais != null) {
                try {
                    dais.close();
                }
                catch (IOException e) {
                    getWaveTherm().getLogger().severe(ProtocolUtils.stack2string(e));
                }
            }
        }
    }

    //Unused method, though it has to be implemented

    protected RadioCommandId getRadioCommandId() {
        return null;
    }
}