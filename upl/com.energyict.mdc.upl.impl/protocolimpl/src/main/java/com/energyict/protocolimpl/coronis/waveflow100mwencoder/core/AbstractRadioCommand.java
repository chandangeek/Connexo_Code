package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.WaveFlow100mW.MeterProtocolType;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractRadioCommand {

    private static final int SERVICE_RESPONSE = 0x82;

    enum EncoderRadioCommandId {

        ReadParameter(0x18),
        WriteParameter(0x19),
        EncoderCurrentReading(0x01,true),
        EncoderDataloggingTable(0x07,true),
        EncoderReadLeakageEventTable(0x04,true),
        EncoderInternalData(0x0B,true),
        MBusInternalLogs(0x0D,true),
        LeakageEventTable(0x04,true),
        MeterDetection(0x0C),
        AlarmRoute(0x0A),
        FirmwareVersion(0x28),
        VoltageRequest(0x1F, false, true);

        private int commandId;
        private boolean readGenericHeader;
        private boolean isServiceRequest = false;  //Only the voltage request uses ServiceRequest command

        final boolean isReadGenericHeader() {
            return readGenericHeader;
        }

        final int getCommandId() {
            return commandId;
        }

        public boolean isServiceRequest() {
            return isServiceRequest;
        }

        EncoderRadioCommandId(final int commandId) {
            this(commandId,false);
        }

        EncoderRadioCommandId(final int commandId, final boolean readGenericHeader) {
            this.commandId=commandId;
            this.readGenericHeader=readGenericHeader;
        }

        EncoderRadioCommandId(final int commandId, final boolean readGenericHeader, final boolean isServiceRequest) {
            this.commandId = commandId;
            this.readGenericHeader = readGenericHeader;
            this.isServiceRequest = isServiceRequest;
        }
    } // enum EncoderRadioCommandId

    private GenericHeader genericHeader;

    public final GenericHeader getEncoderGenericHeader() {
        return genericHeader;
    }

    /**
     * The reference to the Waveflow100mW protocol implementation class
     */
    private WaveFlow100mW waveFlow100mW;

    final WaveFlow100mW getWaveFlow100mW() {
        return waveFlow100mW;
    }

    AbstractRadioCommand(WaveFlow100mW waveFlow100mW) {
        this.waveFlow100mW = waveFlow100mW;
    }

    abstract void parse(byte[] data) throws IOException;

    abstract byte[] prepare() throws IOException;

    abstract EncoderRadioCommandId getEncoderRadioCommandId();

    void invoke() throws IOException {

        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeByte(getEncoderRadioCommandId().getCommandId());
            daos.write(prepare()); // write 1 parameter
            parseRead(getWaveFlow100mW().getWaveFlowConnect().sendData(baos.toByteArray()));
        }
        finally {
            if (baos != null) {
                try {
                    baos.close();
                }
                catch(IOException e) {
                    getWaveFlow100mW().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }

    private void parseRead(byte[] data) throws IOException {
        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));

            int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());

            if (getEncoderRadioCommandId().isServiceRequest()) {
                parseServiceResponse(data);
                return;
            }

            if (commandIdAck != (0x80 | getEncoderRadioCommandId().getCommandId())) {
                throw new WaveFlow100mwEncoderException("Invalid response tag ["+WaveflowProtocolUtils.toHexString(commandIdAck)+"]");
            }
            else {

                if ((commandIdAck == (0x80 | EncoderRadioCommandId.EncoderDataloggingTable.getCommandId())) &&
                        (data.length == 2) &&
                        (WaveflowProtocolUtils.toInt(data[1]) == 0xff)) {
                    throw new WaveFlow100mwEncoderException("Datalogging not yet available...");
                }

                if (getEncoderRadioCommandId().isReadGenericHeader()) {

                    if (waveFlow100mW.getMeterProtocolType()==MeterProtocolType.SM150E)	{
                        genericHeader = new EncoderGenericHeader(dais, getWaveFlow100mW().getLogger(), getWaveFlow100mW().getTimeZone());
                    }
                    else if (waveFlow100mW.getMeterProtocolType()==MeterProtocolType.ECHODIS) {
                        genericHeader = new MBusGenericHeader(dais, getWaveFlow100mW().getLogger(), getWaveFlow100mW().getTimeZone());
                    }

                    waveFlow100mW.setCachedGenericHeader(genericHeader);
                }
                byte[] temp = new byte[dais.available()];
                dais.read(temp);
                parse(temp);
            }
        }
        finally {
            if (dais != null) {
                try {
                    dais.close();
                }
                catch(IOException e) {
                    getWaveFlow100mW().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }

    private void parseServiceResponse(byte[] data) throws IOException {
        int responseTag = data[0] & 0xFF;
        if (responseTag == SERVICE_RESPONSE) {
            data = ProtocolTools.getSubArray(data, 7);    //Skip type (1) and address
            int expectedCommandId = getEncoderRadioCommandId().getCommandId();
            int receivedCommandId = data[0] & 0xFF;
            if (receivedCommandId == expectedCommandId) {
                parse(ProtocolTools.getSubArray(data, 1));   //Skip Service Command ID
            } else {
                throw new WaveFlow100mwEncoderException("Invalid Command ID tag [" + WaveflowProtocolUtils.toHexString(receivedCommandId) + "], expected [" + WaveflowProtocolUtils.toHexString(expectedCommandId) + "] (" + getEncoderRadioCommandId().name() + ")");
            }
        } else {
            throw new WaveFlow100mwEncoderException("Invalid response tag [" + WaveflowProtocolUtils.toHexString(responseTag) + "], expected [" + WaveflowProtocolUtils.toHexString(SERVICE_RESPONSE) + "] (response to service request)");
        }
    }
}
