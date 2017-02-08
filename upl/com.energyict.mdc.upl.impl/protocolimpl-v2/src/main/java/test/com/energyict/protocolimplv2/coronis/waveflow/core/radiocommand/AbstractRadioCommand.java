package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand;

import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ProtocolRuntimeException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import test.com.energyict.protocolimplv2.coronis.common.WaveFlowException;
import test.com.energyict.protocolimplv2.coronis.common.WaveflowProtocolUtils;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;

public abstract class AbstractRadioCommand {

    public enum RadioCommandId {

        ReadParameterLegacy(0x10, true),    //Used for V1 devices, who are not compatible with the new V2 parameters
        WriteParameterLegacy(0x11),

        //Only used by the V210 modules
        ExtendedApplicativeCommand(0x51),
        WriteDataFeature(0x51),
        ReadDataFeature(0x52),         //This is an extended operation mode
        WriteTariffMode(0x53),
        ReadTariffMode(0x54),
        InitializeAlarmRoute(0x23),
        WritePeakFlowSettings(0x56),
        ReadPeakFlowSettings(0x57),
        ReadPeakFlowData(0x58),
        WriteDateOfInstallation(0x59),
        ReadDateOfInstallation(0x5A),
        WriteCustomerNumber(0x5B),
        ReadCustomerNumber(0x5C),
        ReadCurrentFlowRate(0x5D),
        ReadCountOfTransmission(0x5E),
        ReadCumulativeNoFlowTime(0x50),
        ReadOverspeedAlarmInfo(0x61),
        ReadCumulativeFlowDaily(0x63),
        ReadCumulativeFlowVolume(0x64),
        WriteCumulativeFlowVolumeParameters(0x66),
        Read7BandCumulativeVolumeParameters(0x68),
        Write4DailySegmentsParameters(0x65),
        Read4DailySegmentsParameters(0x69),
        WriteOverspeedParameters(0x67),
        ReadOverspeedParameters(0x6A),

        ReadParameter(0x18, true),
        WriteParameter(0x19),
        WriteIndexes(0x02),
        AcknowledgeAlarmFrame(0xC0),
        DataloggingTableAB(0x03),       // for V1 module compatibility. This contains 24 entries of profile data
        DataloggingTableCD(0x07),       // for V1 module compatibility. This contains 24 entries of profile data
        ExtendedDataloggingTable(0x09), // page 62 waveflow V2 document: contains the profile data!
        ReadCurrentRTCLegacy(0x12),     // For compatibility with waveflow v1 modules
        WriteCurrentRTCLegacy(0x13),    // For compatibility with waveflow v1 modules
        ReadCurrentRTC(0x14),           // page 38 waveflow V2 document: used to read the clock
        WriteCurrentRTC(0x15),          // page 38 waveflow V2 document: used to set the clock
        CurrentIndexReading(0x01),
        GlobalIndexReading(0x05, true),  // page 42 waveflow V2 document: used to read out the current meter indexes
        LeakageEventTable(0x04),        // contains latest 5 leak events
        BackFlowEventTable(0x08),       // contains latest 4 back flow events
        FirmwareVersion(0x28),          // contains the firmware version
        ModuleType(0x20),               // contains the module type
        ExtendedIndexReading(0x06),     // contains the meter config settings.
        ControlWaterValve(0x30),        // command to open the water valve, only works for water valve profiles.
        ReadValveStatus(0x31),
        AddCreditBeforeClosing(0x33),
        DailyConsumption(0x27);  //Use the generic header in the parse

        private int commandId;
        /**
         * Some of the radio commands return the 1 byte operation mode and 1 byte application status
         */
        private boolean status;
        /**
         * some of the radio commands return a 23 bytes generic header, explained at page 22 of the waveflow V2
         */
        private boolean genericHeader;

        final boolean isStatus() {
            return status;
        }


        final boolean isGenericHeader() {
            return genericHeader;
        }


        public final int getCommandId() {
            return commandId;
        }


        RadioCommandId(final int commandId) {
            this(commandId, false);
        }

        RadioCommandId(final int commandId, final boolean status) {
            this(commandId, status, false);
        }

        RadioCommandId(final int commandId, final boolean status, final boolean genericHeader) {
            this.commandId = commandId;
            this.status = status;
            this.genericHeader = genericHeader;
        }

    }

    /**
     * The reference to the Waveflow protocol implementation class (V1, V2 or V210)
     */
    private WaveFlow waveFlow;

    /**
     * the 1 byte operation mode send together with some of the radio command responses
     */
    protected int operationMode = -1;

    /**
     * the 1 byte application status send together with some of the radio command responses
     */
    protected int applicationStatus = -1;

    public final int getOperationMode() {
        return operationMode;
    }

    public final int getApplicationStatus() {
        return applicationStatus;
    }

    protected final WaveFlow getWaveFlow() {
        return waveFlow;
    }

    protected AbstractRadioCommand(WaveFlow waveFlow) {
        this.waveFlow = waveFlow;
    }

    protected abstract void parse(byte[] data);

    protected abstract byte[] prepare();

    protected abstract RadioCommandId getRadioCommandId();

    protected RadioCommandId getSubCommandId() {
        return null;        //The V210 classes override this method. The others don't use it.
    }

    /**
     * Set the Radio parameter initiated value.
     */
    public void set() {
        byte[] request = new byte[]{(byte) getRadioCommandId().getCommandId()};
        if (waveFlow.isV210() && (RadioCommandId.ExtendedApplicativeCommand.equals(getRadioCommandId()))) {
            request = ProtocolTools.concatByteArrays(request, new byte[]{(byte) getSubCommandId().getCommandId()});     //V210 uses an extra sub command
        }
        request = ProtocolTools.concatByteArrays(request, prepare());
        parseResponse(getWaveFlow().getWaveFlowConnect().sendData(request));
    }


    private void parseResponse(byte[] data) {
        int offset = 0;
        int commandIdAck = data[offset] & 0xFF;
        offset++;
        if (commandIdAck != (0x80 | getRadioCommandId().getCommandId())) {
            throw createWaveFlowException("Invalid response tag [" + WaveflowProtocolUtils.toHexString(commandIdAck) + "]");
        } else {
            if ((commandIdAck == (0x80 | RadioCommandId.ExtendedDataloggingTable.getCommandId())) && (data.length == 2) && ((data[offset] & 0xFF) == 0xff)) {
                throw createWaveFlowException("Datalogging is disabled on the module...");
            }

            if (getRadioCommandId().isGenericHeader()) {
                offset += 23;
            } else if (getRadioCommandId().isStatus()) {
                operationMode = data[offset];
                offset++;
                applicationStatus = data[offset];
                offset++;

                waveFlow.getParameterFactory().setOperatingMode(operationMode);
                waveFlow.getParameterFactory().setApplicationStatus(applicationStatus);
            }

            if (waveFlow.isV210() && (RadioCommandId.ExtendedApplicativeCommand.equals(getRadioCommandId()))) {         //The V210 send an extra ack, for the sub command
                int subCommandIdAck = data[offset];
                offset++;
                if (subCommandIdAck != (0x80 | getSubCommandId().getCommandId())) {
                    throw createWaveFlowException("Invalid response tag [" + WaveflowProtocolUtils.toHexString(subCommandIdAck) + "]");
                }
            }
            parse(ProtocolTools.getSubArray(data, offset));
        }
    }

    public int convertBCD(byte[] bytes, boolean ignoreLastNibble) {
        String bcd = ProtocolTools.getHexStringFromBytes(bytes, "");
        if (ignoreLastNibble) {
            bcd = bcd.substring(0, bcd.length() - 1);       //Ignore last nibble, according to the documentation
        }
        return Integer.parseInt(bcd);
    }

    public int convertBCD(byte[] bytes, int offset, int length, boolean ignoreLastNibble) {
        bytes = ProtocolTools.getSubArray(bytes, offset, offset + length);
        return convertBCD(bytes, ignoreLastNibble);
    }

    public byte[] getByteFromBCD(int value, int digits) {
        String bcd = String.valueOf(value);
        while (bcd.length() < digits) {
            bcd = "0" + bcd;
        }
        return ProtocolTools.getBytesFromHexString(bcd, "");
    }

    /**
     * Common method for all commands / parameters to create a WaveFlowException in case something specific went wrong
     */
    protected static ProtocolRuntimeException createWaveFlowException(String description) {
        WaveFlowException e = new WaveFlowException(description);
        return CommunicationException.unexpectedResponse(e);
    }
}