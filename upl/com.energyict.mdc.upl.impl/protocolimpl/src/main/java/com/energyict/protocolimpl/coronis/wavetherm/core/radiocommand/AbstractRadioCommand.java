package com.energyict.protocolimpl.coronis.wavetherm.core.radiocommand;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflowDLMS.WaveFlowDLMSException;
import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractRadioCommand {

    protected enum RadioCommandId {

        ReadParameter(0x10, true),
        WriteParameter(0x11),
        FirmwareVersion(0x28),
        ExtendedDataloggingTable(0x06),
        ReadCurrentRTC(0x12),
        SensorInitialization(0x04),
        ReadCurrentValue(0x01),
        WriteCurrentRTC(0x13),
        RssiLevel(0x20),
        AlarmTable(0x05);


        private int commandId;
        private boolean status;
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
     * The reference to the Waveflow protocol implementation class
     */
    private WaveTherm waveTherm;

    /**
     * the 1 byte operation mode send together with some of the radio command responses
     */
    protected int operationMode = -1;

    /**
     * the 1 byte application status send together with some of the radio command responses
     */
    private int applicationStatus = -1;

    public final int getOperationMode() {
        return operationMode;
    }

    final int getApplicationStatus() {
        return applicationStatus;
    }

    protected final WaveTherm getWaveTherm() {
        return waveTherm;
    }

    protected AbstractRadioCommand(WaveTherm waveTherm) {
        this.waveTherm = waveTherm;
    }

    protected abstract void parse(byte[] data) throws IOException;

    protected abstract byte[] prepare() throws IOException;

    protected abstract RadioCommandId getRadioCommandId();

    /**
     * Send the request, parse the response
     *
     * @throws java.io.IOException if the communication fails
     */
    public void invoke() throws IOException {
        int retry = 0;
        while (true) {
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                DataOutputStream daos = new DataOutputStream(baos);
                daos.writeByte(getRadioCommandId().getCommandId());
                daos.write(prepare()); // write 1 parameter
                parseResponse(getWaveTherm().getWaveFlowConnect().sendData(baos.toByteArray()));
                return;
            }
            catch (ConnectionException e) {
                if (retry++ >= getWaveTherm().getInfoTypeProtocolRetriesProperty()) {
                    throw new WaveFlowDLMSException(e.getMessage() + ", gave up after [" + getWaveTherm().getInfoTypeProtocolRetriesProperty() + "] reties!");
                } else {
                    getWaveTherm().getLogger().warning(e.getMessage() + ", retry [" + retry + "]");
                }
            }
            catch (WaveFlowDLMSException e) {
                if (retry++ >= getWaveTherm().getInfoTypeProtocolRetriesProperty()) {
                    throw new WaveFlowDLMSException(e.getMessage() + ", gave up after [" + getWaveTherm().getInfoTypeProtocolRetriesProperty() + "] reties!");
                } else {
                    getWaveTherm().getLogger().warning(e.getMessage() + ", retry [" + retry + "]");
                }
            }
            finally {
                if (baos != null) {
                    try {
                        baos.close();
                    }
                    catch (IOException e) {
                        getWaveTherm().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
                    }
                }
            }
        }
    }

    /**
     * Set the Radio parameter initiated value.
     *
     * @throws java.io.IOException
     */
    public void set() throws IOException {

        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeByte(getRadioCommandId().getCommandId());
            daos.write(prepare()); // write 1 parameter
            parseResponse(getWaveTherm().getWaveFlowConnect().sendData(baos.toByteArray()));
        }
        finally {
            if (baos != null) {
                try {
                    baos.close();
                }
                catch (IOException e) {
                    getWaveTherm().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
                }
            }
        }
    }


    private void parseResponse(byte[] data) throws IOException {
        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));

            int commandIdAck = WaveflowProtocolUtils.toInt(dais.readByte());
            if (commandIdAck != (0x80 | getRadioCommandId().getCommandId())) {
                throw new WaveFlowException("Invalid response tag [" + WaveflowProtocolUtils.toHexString(commandIdAck) + "]");
            } else {

                if ((commandIdAck == (0x80 | RadioCommandId.ExtendedDataloggingTable.getCommandId())) &&
                        (data.length == 2) &&
                        (WaveflowProtocolUtils.toInt(data[1]) == 0xff)) {
                    throw new WaveFlowException("Datalogging not yet available...");
                }

                if (getRadioCommandId().isGenericHeader()) {
                    byte[] temp = new byte[23];
                    dais.read(temp);
                } else if (getRadioCommandId().isStatus()) {
                    operationMode = WaveflowProtocolUtils.toInt(dais.readByte());
                    applicationStatus = WaveflowProtocolUtils.toInt(dais.readByte());
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
                catch (IOException e) {
                    getWaveTherm().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
                }
            }
        }
    }
}