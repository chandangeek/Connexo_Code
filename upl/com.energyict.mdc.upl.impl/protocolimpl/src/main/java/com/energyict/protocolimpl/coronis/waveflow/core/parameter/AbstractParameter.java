package com.energyict.protocolimpl.coronis.waveflow.core.parameter;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow.core.ParameterType;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

abstract public class AbstractParameter extends AbstractRadioCommand {

    static final int PARAM_UPDATE_OK = 0x00;
    protected ParameterType parameterType = ParameterType.WaveFlowV2; //By default, a parameter is handled as if it's a WaveFlow V2 parameter.

    protected enum ParameterId {

        BatteryLifeDurationCounter(0xA2, 2, "Battery life duration counter"),
        BatteryLifeDateEnd(0x90, 6, "Battery life end date"),
        ApplicationStatus(0x20, 1, "Application Status"),
        ValveApplicationStatus(0x21, 1, "Valve application Status"),
        AlarmFramesTimeAssignement(0x24, 1, "Time affection for alarm frames"),
        AlarmConfig(0x22, 1, "Alarm configuration byte"),
        NumberOfRepeaters(0xB0, 1, "Number of repeaters"),
        RepeaterAddress1(0xB1, 6, "Repeater address 1"),
        RepeaterAddress2(0xB2, 6, "Repeater address 2"),
        RepeaterAddress3(0xB3, 6, "Repeater address 3"),
        RecipientAddress(0xB4, 6, "Recipient address"),
        PushCommandBuffer(0x6A, 7, "Push frame command buffer"),
        MaxCancelTimeout(0x6B, 1, "Maximum cancellation timeout"),
        StartOfPushMechanism(0x68, 3, "Start of push frames (hh:mm:ss)"),
        SamplingPeriod(0x80, 1, "Reading the sampling period"),
        TransmissionPeriod(0x69, 1, "Reading the push frame transmission period"),
        SamplingActivationStartHour(0x81, 1, "Sampling activation start hour (00=00:00, 01=01:00,... , 0x17=23:00)"),
        MinuteOfMeasurement(0x84, 1, "Start minute of measurement for periodic logging"),
        HourOfDailyLogging(0x30, 1, "Hour of daily index storage"),
        DateOfLastDailyIndexStorage(0x31, 1, "Date of the last daily index storage"),
        OperationMode(0x01, 1, "Operation mode"),
        WireCutDetectionDateInputA(0x91, 6, "Date of the wirecut detection on input A"),
        WireCutDetectionDateInputB(0x92, 6, "Date of the wirecut detection on input B"),
        ReedFaultDetectionDateInputA(0x93, 6, "Date of the reed fault detection on input A"),
        ReedFaultDetectionDateInputB(0x94, 6, "Date of the reed fault detection on input B"),
        WireCutDetectionDateInputC(0x95, 6, "Date of the wirecut detection on input C"),
        WireCutDetectionDateInputD(0x96, 6, "Date of the wirecut detection on input D"),
        DayOfWeek(0x82, 1, "Day of week for the weekly datalogging"),
        TimeOfMeasurement(0x83, 1, "Start hour of measurement for the weekly/monthly datalogging"),
        DefinePulseWeightA(0xA3, 1, "Define pulse weight for input channel A"),
        DefinePulseWeightB(0xA4, 1, "Define pulse weight for input channel B"),
        DefinePulseWeightC(0xA5, 1, "Define pulse weight for input channel C"),
        DefinePulseWeightD(0xA6, 1, "Define pulse weight for input channel D"),

        ExtendedOperationMode(0x0A, 1, "Extended Operation Mode"),
        NrOfLoggedRecords(0x0B, 2, "Number of records in the datalogging table"),

        //Wake up parameters
        WakeUpSystemStatusWord(0x02, 1, "WakeUp system status word"),
        DefaultWakeUpPeriod(0x03, 1, "Default WakeUp period (in second)"),
        StartTimeForTimeWindow1(0x04, 1, "Start time for 1st time window "),
        WakeUpPeriodForTimeWindow1(0x05, 1, "WakeUp period for 1st time window (in second) "),
        StartTimeForTimeWindow2(0x06, 1, "Start time for 2nd time window"),
        WakeUpPeriodForTimeWindow2(0x07, 1, "WakeUp period for 2nd time window (in second) "),
        EnableTimeWindowsByDayOfWeek(0x08, 1, "Enable time windows by day of the week "),
        EnableWakeUpPeriodsByDayOfWeek(0x09, 1, "Enable WakeUp periods by day of the week"),

        //Hydreka parameters
        TimeDateRTC(0x04, 7, "RTC date and time", ParameterType.Hydreka),
        ReadingHourLeakageStatus(0x05, 1, "Daily reading hour of the leakage status", ParameterType.Hydreka),
        ReadingHourHistogram(0x06, 1, "Daily reading hour of the histogram", ParameterType.Hydreka),
        RTCResynchPeriod(0x07, 1, "Period of the RTC resynchronization", ParameterType.Hydreka),
        ModuleEndOfBatteryTimestamp(0x90, 7, "Battery of module low timestamp", ParameterType.Hydreka),
        TamperTimestamp(0x91, 6, "Tamper detection timestamp", ParameterType.Hydreka),
        LeakageTimestamp(0x92, 6, "Leakage detection timestamp", ParameterType.Hydreka),
        ProbeEndOfBatteryTimestamp(0x93, 6, "Battery of probe low timestamp", ParameterType.Hydreka),
        BatteryLifeDurationCounterHydreka(0xA2, 3, "Battery life duration counter", ParameterType.Hydreka),

        //Hidden parameters
        ProfileType(0xE7, 1, "Profile type indicating the functionality supported by the module"),
        NumberOfSentFrames(0xE9, 2, "Number of frames sent by the module"),
        NumberOfReceivedFrames(0xE8, 2, "Number of frames received by the module"),
        ElapsedDays(0xED, 2, "Number of days elapsed by the module"),
        TimeDurationRxAndTx(0xEB, 4, "Time duration in RX and TX"),
        NumberOfFrameRxAndTx(0xEA, 2, "Number of frame in RX and TX"),

        //V1 433 MHz
        NumberOfRelayedFramesV1(0xEB, 2, "Number of frames relayed and alarm frames transmitted by the V1 module", ParameterType.WaveFlowV1_433MHz),

        LeakageMeasurementStep(0xC4, 1, "Measurement step, expressed in multiple of minutes"),
        ResidualLeakageFlowA(0x88, 1, "Residual leakage flow (low threshold) for input A"),
        ResidualLeakageFlowB(0x8B, 1, "Residual leakage flow (low threshold) for input B"),
        ResidualLeakageFlowC(0x98, 1, "Residual leakage flow (low threshold) for input C"),
        ResidualLeakageFlowD(0x9B, 1, "Residual leakage flow (low threshold) for input D"),

        ExtremeLeakageFlowA(0x89, 2, "Extreme leakage flow (high threshold) for input A"),         //LSB first!
        ExtremeLeakageFlowB(0x8C, 2, "Extreme leakage flow (high threshold) for input B"),         //LSB first!
        ExtremeLeakageFlowC(0x99, 2, "Extreme leakage flow (high threshold) for input C"),         //LSB first!
        ExtremeLeakageFlowD(0x9C, 2, "Extreme leakage flow (high threshold) for input D"),         //LSB first!

        ResidualLeakageDetectionPeriodA(0x8A, 1, "Residual leakage detection period for input A"),
        ResidualLeakageDetectionPeriodB(0x8D, 1, "Residual leakage detection period for input B"),
        ResidualLeakageDetectionPeriodC(0x9A, 1, "Residual leakage detection period for input C"),
        ResidualLeakageDetectionPeriodD(0x9D, 1, "Residual leakage detection period for input D"),

        ExtremeLeakageDetectionPeriodA(0xC0, 1, "Extreme leakage detection period for input A"),
        ExtremeLeakageDetectionPeriodB(0xC1, 1, "Extreme leakage detection period for input B"),
        ExtremeLeakageDetectionPeriodC(0xC2, 1, "Extreme leakage detection period for input C"),
        ExtremeLeakageDetectionPeriodD(0xC3, 1, "Extreme leakage detection period for input D"),

        AdvancedBackflowDetectionPeriodA(0xCB, 1, "Advanced backflow detection period (in multiple of 10 minutes) on input A"),
        AdvancedBackflowDetectionPeriodB(0xCD, 1, "Advanced backflow detection period (in multiple of 10 minutes) on input B"),
        AdvancedBackflowThresholdA(0xCC, 1, "Advanced backflow threshold (in pulses per detection period) on input A"),
        AdvancedBackflowThresholdB(0xCE, 1, "Advanced backflow threshold (in pulses per detection period) on input B"),

        SimpleBackflowDetectionPeriodA(0xC5, 1, "Simple backflow detection period (in hours) on input A"),
        SimpleBackflowDetectionPeriodB(0xC7, 1, "Simple backflow detection period (in hours) on input B"),
        SimpleBackflowThresholdA(0xC6, 1, "Simple backflow threshold (in pulses per detection period) on input A"),
        SimpleBackflowThresholdB(0xC8, 1, "Simple backflow threshold (in pulses per detection period) on input B"),
        SimpleBackflowDetectionFlagsPortA(0xC9, 2, "Simple backflow detection flags port A"),    //Flag indicating simple backflow detections per month, for input A
        SimpleBackflowDetectionFlagsPortB(0xCA, 2, "Simple backflow detection flags port B");    //Flag indicating simple backflow detections per month, for input B

        private int id;
        private int length;
        private String description;
        private ParameterType type;

        ParameterId(final int id, final int length, final String description, ParameterType type) {
            this.id = id;
            this.length = length;
            this.description = description;
            this.type = type;
        }

        ParameterId(final int id, final int length, final String description) {
            this(id, length, description, ParameterType.WaveFlowV2);
        }

        public String toString() {
            return WaveflowProtocolUtils.toHexString(id) + ", " + description;
        }

        static ParameterId fromId(final int id, ParameterType type) {
            for (ParameterId pid : values()) {
                if (pid.id == id && pid.type == type) {
                    return pid;
                }
            }
            return null;
        }

    } // enum ParameterId

    /**
     * The working mode of the waveflow device is implicit to the write and read command
     * This is a concatenation of the extended operation mode byte and the operation mode byte
     */
    private int workingMode;

    /**
     * Working mode write mask
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


    public AbstractParameter(WaveFlow waveFlow) {
        super(waveFlow);
    }

    protected abstract ParameterId getParameterId() throws WaveFlowException;

    public void write() throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            int writeId = RadioCommandId.WriteParameter.getCommandId();
            if (getWaveFlow().isV1()) {
                writeId = RadioCommandId.WriteParameterLegacy.getCommandId(); //Use the legacy parameter in case of the V1 module
            }
            daos.writeByte(writeId);
            if (getParameterId() == null) {
                daos.writeShort(workingMode); // update the working mode
                daos.writeShort(mask); // mask to update the working mode
                daos.writeByte(0); // write 0 parameter, only update the working mode
            } else {
                if (!getWaveFlow().isV1()) {
                    daos.writeShort(0);     //V2 modules need to send the working mode and its mask first.
                    daos.writeShort(0);     //This is skipped in case of V1 module.
                }
                daos.writeByte(1);  // write 1 parameter
                daos.writeByte(getParameterId().id);
                daos.writeByte(getParameterId().length);
                daos.write(prepare());
            }

            parseWriteResponse(getWaveFlow().getWaveFlowConnect().sendData(baos.toByteArray()));

        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    getWaveFlow().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }

    private final void parseWriteResponse(final byte[] data) throws IOException {
        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));

            int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
            int writeId = RadioCommandId.WriteParameter.getCommandId();
            if (getWaveFlow().isV1()) {
                writeId = RadioCommandId.WriteParameterLegacy.getCommandId(); //Use the legacy parameter in case of the V1 module
            }
            if (commandIdAck != (0x80 | writeId)) {
                throw new WaveFlowException("Invalid response tag [" + WaveflowProtocolUtils.toHexString(commandIdAck) + "]");
            } else {
                if (!getWaveFlow().isV1()) {
                    workingMode = dais.readShort();     //Only V2 modules send the workingmode in the response.                    
                    operationMode = workingMode & 0xFF;
                    getWaveFlow().getParameterFactory().setExtendedOperationMode((workingMode >> 8) & 0xFF);
                    getWaveFlow().getParameterFactory().setOperatingMode(operationMode);
                }
                if (getParameterId() != null) {
                    int nrOfParameters = WaveflowProtocolUtils.toInt(dais.readByte());
                    if (nrOfParameters != 1) {
                        throw new WaveFlowException("Writing only 1 parameter at a time allowed, returned [" + nrOfParameters + "] parameters!");
                    }

                    ParameterId pid = ParameterId.fromId(WaveflowProtocolUtils.toInt(dais.readByte()), parameterType);
                    if (pid != getParameterId()) {
                        throw new WaveFlowException("Invalid parameter returned expected [" + getParameterId() + "], returned [" + pid + "]");
                    }
                }

                int result = WaveflowProtocolUtils.toInt(dais.readByte());
                if (result != PARAM_UPDATE_OK) {
                    throw new WaveFlowException("Update parameter [" + getParameterId() + "] failed. Result code [" + WaveflowProtocolUtils.toHexString(result) + "]");
                }
            }
        } finally {
            if (dais != null) {
                try {
                    dais.close();
                } catch (IOException e) {
                    getWaveFlow().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }

    public void read() throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            int readId = RadioCommandId.ReadParameter.getCommandId();
            if (getWaveFlow().isV1()) {
                readId = RadioCommandId.ReadParameterLegacy.getCommandId();    //In case of communication with an old V1 module, the legacy read parameter has to be used
            }
            daos.writeByte(readId);
            if (getParameterId() == null) {
                daos.writeByte(0); // write 0 parameter, only update the operating mode
            } else {
                daos.writeByte(1); // write 1 parameter
                daos.writeByte(getParameterId().id);
                daos.writeByte(getParameterId().length);
            }
            parseReadResponse(getWaveFlow().getWaveFlowConnect().sendData(baos.toByteArray()));
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    getWaveFlow().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }

    private void parseReadResponse(final byte[] data) throws IOException {
        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));
            int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
            int readId = RadioCommandId.ReadParameter.getCommandId();
            if (getWaveFlow().isV1()) {
                readId = RadioCommandId.ReadParameterLegacy.getCommandId();    //In case of communication with an old V1 module, the legacy read parameter has to be used
            }
            if (commandIdAck != (0x80 | readId)) {
                throw new WaveFlowException("Invalid response tag [" + WaveflowProtocolUtils.toHexString(commandIdAck) + "]");
            } else {
                if (!getWaveFlow().isV1()) {
                    workingMode = dais.readShort(); //The working mode is only received from V2 modules.                    
                    operationMode = workingMode & 0xFF;
                    getWaveFlow().getParameterFactory().setExtendedOperationMode((workingMode >> 8) & 0xFF);
                    getWaveFlow().getParameterFactory().setOperatingMode(operationMode);
                }
                if (getParameterId() != null) {
                    int nrOfParameters = WaveflowProtocolUtils.toInt(dais.readByte());
                    if (nrOfParameters != 1) {
                        throw new WaveFlowException("Reading only 1 parameter at a time allowed, returned [" + nrOfParameters + "] parameters!");
                    }

                    ParameterId pid = ParameterId.fromId(WaveflowProtocolUtils.toInt(dais.readByte()), parameterType);
                    if (pid != getParameterId()) {
                        throw new WaveFlowException("Invalid parameter returned expected [" + getParameterId() + "], returned [" + pid + "]");
                    }

                    int length = WaveflowProtocolUtils.toInt(dais.readByte());
                    if (length != getParameterId().length) {
                        throw new WaveFlowException("Error reading parameter [" + getParameterId().toString() + "]. Invalid length returned: expected [" + getParameterId().length + "], returned [" + length + "]");
                    }

                    byte[] resultData = new byte[dais.available()];
                    dais.read(resultData);
                    parse(resultData);
                }
            }
        } finally {
            if (dais != null) {
                try {
                    dais.close();
                } catch (IOException e) {
                    getWaveFlow().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }

    /**
     * Because we have implement a abstract parameter read/write class, we don't use this method...
     */
    protected RadioCommandId getRadioCommandId() {
        return null;
    }

    void writeBubbleUpConfiguration(int command, int transmissionPeriod) throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = getWriteBubbleUpConfigCommand(command, transmissionPeriod);
            parseBubbleUpConfigResponse(getWaveFlow().getWaveFlowConnect().sendData(baos.toByteArray()));
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    getWaveFlow().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }

    /**
     * Prepares a byte[] to set all bubble up configuration parameters at once
     */
    private ByteArrayOutputStream getWriteBubbleUpConfigCommand(int command, int transmissionPeriod) throws IOException {
        ByteArrayOutputStream baos;
        baos = new ByteArrayOutputStream();
        DataOutputStream daos = new DataOutputStream(baos);
        daos.writeByte(RadioCommandId.WriteParameter.getCommandId());

        int startMoment = getWaveFlow().getBubbleUpStartMoment();
        int startHour = (startMoment / 3600);
        startMoment -= startHour * 3600;
        int startMinute = startMoment / 60;
        startMoment -= (startMinute * 60);
        int startSeconds = startMoment;

        daos.writeShort(0x0800);
        daos.writeShort(0x0800);
        daos.writeByte(4); // write 5 configuration parameters

        daos.writeByte(ParameterId.StartOfPushMechanism.id);
        daos.writeByte(ParameterId.StartOfPushMechanism.length);
        daos.writeByte(startHour);
        daos.writeByte(startMinute);
        daos.writeByte(startSeconds);

        daos.writeByte(ParameterId.TransmissionPeriod.id);
        daos.writeByte(ParameterId.TransmissionPeriod.length);
        daos.writeByte(transmissionPeriod);

        daos.writeByte(ParameterId.PushCommandBuffer.id);
        daos.writeByte(ParameterId.PushCommandBuffer.length);
        daos.write(new byte[]{(byte) 1, (byte) command, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0});

        daos.writeByte(ParameterId.MaxCancelTimeout.id);
        daos.writeByte(ParameterId.MaxCancelTimeout.length);
        daos.writeByte(0x05);   //Default value
        return baos;
    }


    private final void parseBubbleUpConfigResponse(final byte[] data) throws IOException {
        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));
            int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
            if (commandIdAck != (0x80 | RadioCommandId.WriteParameter.getCommandId())) {
                throw new WaveFlowException("Invalid response tag [" + WaveflowProtocolUtils.toHexString(commandIdAck) + "]");
            } else {
                workingMode = dais.readShort();  //Is always sent in the response
                operationMode = workingMode & 0xFF;
                getWaveFlow().getParameterFactory().setExtendedOperationMode((workingMode >> 8) & 0xFF);
                getWaveFlow().getParameterFactory().setOperatingMode(operationMode);

                int nrOfParameters = WaveflowProtocolUtils.toInt(dais.readByte());
                if (nrOfParameters != 4) {
                    throw new WaveFlowException("Wrote 4 parameters, returned " + nrOfParameters);
                }

                for (int i = 0; i < 4; i++) {
                    ParameterId pid = ParameterId.fromId(WaveflowProtocolUtils.toInt(dais.readByte()), parameterType);
                    int result = WaveflowProtocolUtils.toInt(dais.readByte());
                    if (result != PARAM_UPDATE_OK) {
                        throw new WaveFlowException("Update parameter [" + pid + "] failed. Result code [" + WaveflowProtocolUtils.toHexString(result) + "]");
                    }
                }
            }
        } finally {
            if (dais != null) {
                try {
                    dais.close();
                } catch (IOException e) {
                    getWaveFlow().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }
}