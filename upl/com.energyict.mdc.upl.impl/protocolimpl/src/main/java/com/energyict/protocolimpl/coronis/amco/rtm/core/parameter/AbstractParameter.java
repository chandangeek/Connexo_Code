package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand.AbstractRadioCommand;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractParameter extends AbstractRadioCommand {

    static final int PARAM_UPDATE_OK = 0x00;

    enum ParameterId {

        Rtc(0x04, 7, "Time"),
        LeakageDetectionStatus(0x02, 1, "Leakage detection status"),
        ProfileType(0x05, 1, "Profile type"),
        ApplicationStatus(0x01, 1, "Application Status"),
        AlarmConfiguration(0x58, 1, "Alarm configuration"),
        TouBuckets(0x60, 7, "Write the TOU bucket start hours"),
        AlarmWindowConfiguration(0x57, 1, "Alarm window configuration"),
        NumberOfRecords(0x14, 2, "Number of records in the logging table"),

        NumberOfRepeaters(0x59, 1, "Number of repeaters"),
        RepeaterAddress1(0x5A, 6, "Repeater address 1"),
        RepeaterAddress2(0x5B, 6, "Repeater address 2"),
        RepeaterAddress3(0x5C, 6, "Repeater address 3"),
        RecipientAddress(0x5D, 6, "Recipient address"),

        SamplingPeriod(0x07, 1, "Sampling period in seconds"),
        SamplingActivationType(0x08, 1, "Start hour for data logging in periodic steps"),
        MeasurementPeriodMultiplier(0x10, 1, "Multiplier for the sampling period, to get the measurement period"),
        DayOfWeekOrMonth(0x12, 1, "Day of week or month to log data on"),
        HourOfMeasurement(0x13, 1, "Hour to log data on (in weekly/monthly mode)"),

        StartOfPseudoBubbleUpMechanism(0x68, 3, "Starting hour, minute and second of the pseudo bubble up mechanism"),
        PseudoBubbleUpTransmissionPeriod(0x69, 1, "Pseudo bubble up transmission period"),
        PushCommandBuffer(0x6A, 7, "Push frame command buffer"),
        MaxCancelTimeout(0x6B, 1, "Maximum cancellation timeout"),
        EndHourOfPseudoBubbleUpPeriod(0x6C, 1, "End hour of bubble up period"),

        ResidualLeakageThresholdA(0x21, 1, "Residual leakage flow (low threshold) for input A"),
        ResidualLeakageThresholdB(0x28, 1, "Residual leakage flow (low threshold) for input B"),
        ResidualLeakageThresholdC(0x30, 1, "Residual leakage flow (low threshold) for input C"),
        ResidualLeakageThresholdD(0x38, 1, "Residual leakage flow (low threshold) for input D"),

        ResidualLeakageDetectionPeriodA(0x22, 1, "Residual leakage detection period for input A"),
        ResidualLeakageDetectionPeriodB(0x29, 1, "Residual leakage detection period for input B"),
        ResidualLeakageDetectionPeriodC(0x31, 1, "Residual leakage detection period for input C"),
        ResidualLeakageDetectionPeriodD(0x39, 1, "Residual leakage detection period for input D"),

        ExtremeLeakageThresholdA(0x23, 2, "Extreme leakage flow (high threshold) for input A"),
        ExtremeLeakageThresholdB(0x2A, 2, "Extreme leakage flow (high threshold) for input B"),
        ExtremeLeakageThresholdC(0x32, 2, "Extreme leakage flow (high threshold) for input C"),
        ExtremeLeakageThresholdD(0x3A, 2, "Extreme leakage flow (high threshold) for input D"),

        ExtremeLeakageDetectionPeriodA(0x24, 1, "Extreme leakage detection period for input A"),
        ExtremeLeakageDetectionPeriodB(0x2B, 1, "Extreme leakage detection period for input B"),
        ExtremeLeakageDetectionPeriodC(0x33, 1, "Extreme leakage detection period for input C"),
        ExtremeLeakageDetectionPeriodD(0x3B, 1, "Extreme leakage detection period for input D"),

        EncoderUnitA(0x1F, 2, "Encoder unit on port A"),
        EncoderUnitB(0x20, 2, "Encoder unit on port B"),

        BackflowDetectionDateA(0x3E, 7, "Back flow detection date on Port A"),
        BackflowDetectionDateB(0x3F, 7, "Back flow detection date on Port B"),

        BackflowDetectionPeriodA(0x40, 1, "Backflow detection period (in multiple of profile interval) on input A"),
        BackflowDetectionBeforeIndicationA(0x41, 1, "Backflow detection before indication on port A"),
        BackflowDetectionThresholdA(0x42, 1, "Backflow threshold (same unit as decoder) on input A"),
        BackflowDetectionFlagsA(0x43, 2, "Backflow detection flags on port A"),

        BackflowDetectionPeriodB(0x44, 1, "Backflow detection period (in multiple of profile interval) on input B"),
        BackflowDetectionBeforeIndicationB(0x45, 1, "Backflow detection before indication on port B"),
        BackflowDetectionThresholdB(0x46, 1, "Backflow threshold (same unit as decoder) on input B"),
        BackflowDetectionFlagsB(0x47, 2, "Backflow detection flags on port B"),

        TamperDetectionDateA(0x48, 7, "Tamper detection date on port A"),
        TamperDetectionDateB(0x49, 7, "Tamper detection date on port B"),
        TamperDetectionDateC(0x4A, 7, "Tamper detection date on port C"),
        TamperDetectionDateD(0x4B, 7, "Tamper detection date on port D"),

        MeterModelA(0x15, 1, "Meter model on port A"),
        MeterModelB(0x16, 1, "Meter model on port B"),
        MeterModelC(0x17, 1, "Meter model on port C"),
        MeterModelD(0x18, 1, "Meter model on port D"),

        EncoderModelOnPortA(0x1D, 2, "Encoder model on port A"),
        EncoderModelOnPortB(0x1E, 2, "Encoder model on port B"),

        DefinePulseWeightA(0x19, 1, "Define pulse weight for input channel A"),
        DefinePulseWeightB(0x1A, 1, "Define pulse weight for input channel B"),
        DefinePulseWeightC(0x1B, 1, "Define pulse weight for input channel C"),
        DefinePulseWeightD(0x1C, 1, "Define pulse weight for input channel D"),

        BatteryLifeDurationCounter(0x50, 3, "Battery life duration counter"),
        BatteryLowDetectionDate(0x51, 7, "Low battery detection date"),

        CommunicationErrorDetectionDateA(0x4C, 7, "Communication error detection date"),
        CommunicationErrorDetectionDateB(0x4D, 7, "Communication error detection date"),
        ReadingErrorDetectionDateA(0x4E, 7, "Reading error detection data"),
        ReadingErrorDetectionDateB(0x4F, 7, "Reading error detection data"),
        ValveCommunicationErrorDetectionDate(0x7C, 7, "Valve Communication error detection date"),

        WalkByOrDriveByWakeUpChannel(0x06, 1, "WalkBy/DriveBy Wake up channel"),
        DriveByEmissionNumber(0x09, 1, "DriveBy emission number"),
        DriveByInterAnswerDelay(0x0C, 3, "DriveBy inter-answer delay"),
        DriveByMinimumRSSI(0x26, 1, "DriveBy: minimum RSSI");

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

    AbstractParameter(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        super(propertySpecService, rtm, nlsService);
    }

    abstract ParameterId getParameterId() throws WaveFlowException;

    void write() throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeByte(RadioCommandId.WriteParameter.getCommandId());

            daos.writeShort(operationMode);
            daos.writeShort(mask);

            //Special case: only write the operation mode (2 bytes)
            if (getParameterId() == null) {
                daos.writeByte(0);
            } else {
                daos.writeByte(1); // write 1 parameter
                daos.writeByte(getParameterId().id);
                daos.writeByte(getParameterId().length);
                daos.write(prepare());
            }

            parseWriteResponse(getRTM().getWaveFlowConnect().sendData(baos.toByteArray()));

        }
        finally {
            if (baos != null) {
                try {
                    baos.close();
                }
                catch (IOException e) {
                    getRTM().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }

    void writeBubbleUpConfiguration(int command, int portMask, int numberOfReadings, int offset, int transmissionPeriod) throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = getWriteBubbleUpConfigCommand(command, portMask, numberOfReadings, offset, transmissionPeriod);
            parseBubbleUpConfigResponse(getRTM().getWaveFlowConnect().sendData(baos.toByteArray()));
        }
        finally {
            if (baos != null) {
                try {
                    baos.close();
                }
                catch (IOException e) {
                    getRTM().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }


    private final void parseBubbleUpConfigResponse(final byte[] data) throws IOException {
        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));
            int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
            if (commandIdAck != (0x80 | RadioCommandId.WriteParameter.getCommandId())) {
                throw new WaveFlowException("Invalid response tag [" + WaveflowProtocolUtils.toHexString(commandIdAck) + "]");
            } else {
                operationMode = dais.readShort();  //Is always sent in the response

                int nrOfParameters = WaveflowProtocolUtils.toInt(dais.readByte());
                if (nrOfParameters != 5) {
                    throw new WaveFlowException("Wrote 5 parameters, returned " + nrOfParameters);
                }

                for (int i = 0; i < 5; i++) {
                    ParameterId pid = ParameterId.fromId(WaveflowProtocolUtils.toInt(dais.readByte()));
                    int result = WaveflowProtocolUtils.toInt(dais.readByte());
                    if (result != PARAM_UPDATE_OK) {
                        throw new WaveFlowException("Update parameter [" + pid + "] failed. Result code [" + WaveflowProtocolUtils.toHexString(result) + "]");
                    }
                }
            }
        }
        finally {
            if (dais != null) {
                try {
                    dais.close();
                }
                catch (IOException e) {
                    getRTM().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
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

                operationMode = dais.readShort();  //Is always sent in the response

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
                    getRTM().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
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
                daos.writeByte(0); // write 0 parameter, only read the operating mode
            } else {
                daos.writeByte(1); // write 1 parameter
                daos.writeByte(getParameterId().id);
                daos.writeByte(getParameterId().length);
            }
            parseReadResponse(getRTM().getWaveFlowConnect().sendData(baos.toByteArray()));
        }
        finally {
            if (baos != null) {
                try {
                    baos.close();
                }
                catch (IOException e) {
                    getRTM().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
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

                operationMode = dais.readShort();

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
                    getRTM().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }

    //Unused method, though it has to be implemented

    protected RadioCommandId getRadioCommandId() {
        return null;
    }

    /**
     * Prepares a byte[] to set all bubble up configuration parameters at once
     */
    private ByteArrayOutputStream getWriteBubbleUpConfigCommand(int command, int portMask, int numberOfReadings, int offset, int transmissionPeriod) throws IOException {
        ByteArrayOutputStream baos;
        baos = new ByteArrayOutputStream();
        DataOutputStream daos = new DataOutputStream(baos);
        daos.writeByte(RadioCommandId.WriteParameter.getCommandId());

        int startMoment = getRTM().getBubbleUpStartMoment();
        int startHour = (startMoment / 3600);
        startMoment -= startHour * 3600;
        int startMinute = startMoment / 60;
        startMoment -= (startMinute * 60);
        int startSeconds = startMoment;

        daos.writeShort(0x0800);
        daos.writeShort(0x0800);
        daos.writeByte(5); // write 5 configuration parameters

        daos.writeByte(ParameterId.StartOfPseudoBubbleUpMechanism.id);
        daos.writeByte(ParameterId.StartOfPseudoBubbleUpMechanism.length);
        daos.writeByte(startHour);
        daos.writeByte(startMinute);
        daos.writeByte(startSeconds);

        daos.writeByte(ParameterId.PseudoBubbleUpTransmissionPeriod.id);
        daos.writeByte(ParameterId.PseudoBubbleUpTransmissionPeriod.length);
        daos.writeByte(transmissionPeriod);   //Default value

        daos.writeByte(ParameterId.PushCommandBuffer.id);
        daos.writeByte(ParameterId.PushCommandBuffer.length);
        byte[] buffer = {(byte) ((portMask == 0) ? 1 : 6), (byte) command, (byte) portMask};
        buffer = ProtocolTools.concatByteArrays(buffer, ProtocolTools.getBytesFromInt(numberOfReadings, 2), ProtocolTools.getBytesFromInt(offset, 2));
        daos.write(buffer);

        daos.writeByte(ParameterId.MaxCancelTimeout.id);
        daos.writeByte(ParameterId.MaxCancelTimeout.length);
        daos.writeByte(0x05);   //Default value

        daos.writeByte(ParameterId.EndHourOfPseudoBubbleUpPeriod.id);
        daos.writeByte(ParameterId.EndHourOfPseudoBubbleUpPeriod.length);
        daos.writeByte(getRTM().getBubbleUpEndHour() / 3600);
        return baos;
    }
}