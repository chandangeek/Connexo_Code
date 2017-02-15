package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.GenericHeader;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.OperatingMode;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflowDLMS.WaveFlowDLMSException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class AbstractRadioCommand {

    protected enum RadioCommandId {

        ReadParameter(0x18, true),
        WriteParameter(0x19),
        CurrentRegisterReading(0x01),
        ExtendedDatalogginTable(0x07),
        DailyReading(0x03),
        LeakageEventTable(0x04),
        ReadTouBuckets(0x06),
        WriteIndexes(0x02),
        ReadEncoderInternalData(0x0B),
        RouteConfiguration(0x0A),
        EncoderModelDetection(0x0C),
        ControlWaterValve(0x30),
        ValveStatus(0x31),
        RssiLevel(0x20),
        FirmwareVersion(0x28);

        private int commandId;
        private boolean returnsOperationMode;
        private boolean isGenericHeader;

        final boolean isReturnsOperationMode() {
            return returnsOperationMode;
        }

        final boolean isGenericHeader() {
            return isGenericHeader;
        }

        public final int getCommandId() {
            return commandId;
        }

        RadioCommandId(final int commandId) {
            this(commandId, false);
        }

        RadioCommandId(final int commandId, final boolean returnsOperationMode) {
            this(commandId, returnsOperationMode, false);
        }

        RadioCommandId(final int commandId, final boolean returnsOperationMode, final boolean genericHeader) {
            this.commandId = commandId;
            this.returnsOperationMode = returnsOperationMode;
            this.isGenericHeader = genericHeader;
        }
    }

    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    protected GenericHeader genericHeader = null;
    private RTM rtm;
    protected int operationMode = 0;

    protected AbstractRadioCommand(PropertySpecService propertySpecService, RTM rtm, NlsService nlsService) {
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.rtm = rtm;
    }

    public GenericHeader getGenericHeader() {
        if (genericHeader == null) {
            genericHeader = new GenericHeader(getRTM(), this.propertySpecService, this.nlsService);
        }
        return genericHeader;
    }

    public int getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(int operationMode) {
        this.operationMode = operationMode;
    }

    /**
     * Operating mode write mask
     */
    protected int mask = 0;

    public int getMask() {
        return mask;
    }

    protected final void setMask(int mask) {
        this.mask = this.mask | mask;
    }

    public void setOperatingMode(OperatingMode operatingMode) {
        this.operationMode = operatingMode.getOperationMode();
        this.mask = operatingMode.getMask();
    }

    protected final RTM getRTM() {
        return rtm;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    public NlsService getNlsService() {
        return nlsService;
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
                parseResponse(getRTM().getWaveFlowConnect().sendData(baos.toByteArray()));
                return;
            } catch (ConnectionException e) {
                if (retry++ >= getRTM().getInfoTypeProtocolRetriesProperty()) {
                    throw new WaveFlowDLMSException(e.getMessage() + ", gave up after [" + getRTM().getInfoTypeProtocolRetriesProperty() + "] reties!");
                } else {
                    getRTM().getLogger().warning(e.getMessage() + ", retry [" + retry + "]");
                }
            } catch (WaveFlowDLMSException e) {
                if (retry++ >= getRTM().getInfoTypeProtocolRetriesProperty()) {
                    throw new WaveFlowDLMSException(e.getMessage() + ", gave up after [" + getRTM().getInfoTypeProtocolRetriesProperty() + "] reties!");
                } else {
                    getRTM().getLogger().warning(e.getMessage() + ", retry [" + retry + "]");
                }
            } finally {
                if (baos != null) {
                    try {
                        baos.close();
                    } catch (IOException e) {
                        getRTM().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
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
            parseResponse(getRTM().getWaveFlowConnect().sendData(baos.toByteArray()));
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    getRTM().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
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
                } else if (getRadioCommandId().isReturnsOperationMode()) {
                    operationMode = WaveflowProtocolUtils.toInt(dais.readShort());  //The operation mode is 2 bytes
                }

                byte[] temp = new byte[dais.available()];
                dais.read(temp);
                parse(temp);
            }
        } finally {
            if (dais != null) {
                try {
                    dais.close();
                } catch (IOException e) {
                    getRTM().getLogger().severe(com.energyict.protocolimpl.utils.ProtocolTools.stack2string((e)));
                }
            }
        }
    }
}