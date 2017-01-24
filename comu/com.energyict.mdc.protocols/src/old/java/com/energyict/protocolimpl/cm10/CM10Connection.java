package com.energyict.protocolimpl.cm10;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

public class CM10Connection extends Connection implements ProtocolConnection {

	private final int timeout;
	private final int maxRetries;
	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final ResponseReceiver responseReceiver;
	private final CM10 cm10Protocol;
	private final Logger logger;

    public CM10Connection(InputStream in, OutputStream out, int timeout, int retries, long forcedDelay, int echoCancelling,
                          HalfDuplexController hdc, CM10 cm10) throws ConnectionException {
        super(in, out, forcedDelay, echoCancelling, hdc);
        this.timeout = timeout;
        this.maxRetries = retries;
        this.cm10Protocol = cm10;
        this.logger = cm10.getLogger();
        this.responseReceiver = new ResponseReceiver(this);
    }

    public CM10 getCM10Protocol() {
		return cm10Protocol;
	}

	protected int getTimeout() {
		return timeout;
	}

    public Response sendCommand(Command command) throws IOException {
        int tries = 0;
        do {
            try {
                sendOut(command.getBytes());
                Response response = receiveResponse(command);
                if (command.sendAckAfterThisCommand()) { // for testing power fail details
                    sendOut(command.getAckCommand().getBytes());
                }
                outputStream.write(command.getAckCommand().getBytes());
                return response;
            } catch (ConnectionException e) {
                tries++;
                logger.severe("Caught ConnectionException on try [" + tries + "]: " + e.getMessage());
                if (e.getReason() == PROTOCOL_ERROR) {
                    throw new ProtocolConnectionException("sendCommand() error, " + e.getMessage());
                }
            }
        } while ((tries <= maxRetries));
        throw new ProtocolConnectionException("sendCommand() error maxRetries (" + maxRetries + ")");
    }

    public Response receiveResponse(Command command) throws IOException {
        return responseReceiver.receiveResponse(command);
    }

    int readNext() throws IOException {
    	return readIn();
    }

    byte getTimeoutError() {
    	return TIMEOUT_ERROR;
    }

    void echoCancellation() {
    	this.copyEchoBuffer();
    }

	protected ByteArrayOutputStream getOutputStream() {
		return this.outputStream;
	}

	public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException {
		return null;
	}

	public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {
		return null;
	}

	public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {

	}

	public HHUSignOn getHhuSignOn() {
		return null;
	}

	public void setHHUSignOn(HHUSignOn hhuSignOn) {
	}

}
