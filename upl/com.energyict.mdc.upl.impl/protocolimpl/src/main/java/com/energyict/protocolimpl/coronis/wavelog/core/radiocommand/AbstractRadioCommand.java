package com.energyict.protocolimpl.coronis.wavelog.core.radiocommand;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflowDLMS.WaveFlowDLMSException;
import com.energyict.protocolimpl.coronis.wavelog.WaveLog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractRadioCommand {

    protected enum RadioCommandId {

        ReadParameter(0x10, true),
        WriteParameter(0x11),
        InitializeAlarmRoute(0x23),
        ResetEventTable(0x04),
        FirmwareVersion(0x28),
        ReadCurrentRTC(0x12),
        ReadCurrentState(0x01),
        WriteCurrentRTC(0x13),
        WriteOutputs(0x02),
        RssiLevel(0x20),
        ReadLast10Events(0x03),
        ReadEventsTable(0x06);

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
    private WaveLog waveLog;

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

    protected final WaveLog getWaveLog() {
        return waveLog;
    }

    protected AbstractRadioCommand(WaveLog waveLog) {
        this.waveLog = waveLog;
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
                parseResponse(getWaveLog().getWaveFlowConnect().sendData(baos.toByteArray()));
                return;
            }
            catch (ConnectionException e) {
                if (retry++ >= getWaveLog().getInfoTypeProtocolRetriesProperty()) {
                    throw new WaveFlowDLMSException(e.getMessage() + ", gave up after [" + getWaveLog().getInfoTypeProtocolRetriesProperty() + "] reties!");
                } else {
                    getWaveLog().getLogger().warning(e.getMessage() + ", retry [" + retry + "]");
                }
            }
            catch (WaveFlowDLMSException e) {
                if (retry++ >= getWaveLog().getInfoTypeProtocolRetriesProperty()) {
                    throw new WaveFlowDLMSException(e.getMessage() + ", gave up after [" + getWaveLog().getInfoTypeProtocolRetriesProperty() + "] reties!");
                } else {
                    getWaveLog().getLogger().warning(e.getMessage() + ", retry [" + retry + "]");
                }
            }
            finally {
                if (baos != null) {
                    try {
                        baos.close();
                    }
                    catch (IOException e) {
                        getWaveLog().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
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
            parseResponse(getWaveLog().getWaveFlowConnect().sendData(baos.toByteArray()));
        }
        finally {
            if (baos != null) {
                try {
                    baos.close();
                }
                catch (IOException e) {
                    getWaveLog().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
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
                    getWaveLog().getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
                }
            }
        }
    }
}