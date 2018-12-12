package com.elster.us.protocolimplv2.mercury.minimax.frame;

import static com.elster.us.protocolimplv2.mercury.minimax.Command.*;

import com.elster.us.protocolimplv2.mercury.minimax.Command;
import com.elster.us.protocolimplv2.mercury.minimax.frame.data.*;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Represents a frame received as a response from the device
 * The included data will be of different types depending on the
 * format of data expected in the specific response
 *
 * @author James Fox
 */
public class ResponseFrame {

    private BasicResponseData data;
    private Logger logger;

    /**
     * Sets the data into the frame
     * @param data The {@link BasicResponseData} representing the data
     */
    public void setData(BasicResponseData data) {
        this.data = data;
    }

    /**
     * Gets the data from the response frame
     * @return the {@link BasicResponseData} representing the data in the response frame
     */
    public BasicResponseData getData() {
        return data;
    }

    /**
     * Constructs a new instance of {@link BasicResponseData}
     * @param bytes The bytes received in the response frame
     * @param lastCommand The last command sent to the device
     * @throws IOException
     */
    public ResponseFrame(byte[] bytes, Command lastCommand, Logger logger) throws IOException {
        this.logger = logger;
        parseBytes(bytes, lastCommand);
    }

    protected void parseBytes(byte[] bytes, Command lastCommandSent) throws IOException {
        // The "|| bytes.length == 2" covers the case where we sent a command
        // expecting a more detailed response, but we get back a basic response
        // containing an error...
        if (SN.equals(lastCommandSent) || SF.equals(lastCommandSent)
                || WD.equals(lastCommandSent) || bytes.length == 2) {
            this.setData(new BasicResponseData(bytes));
        } else if (RD.equals(lastCommandSent)) {
            this.setData(new SingleReadResponseData(bytes));
        } else if (RG.equals(lastCommandSent)) {
            this.setData(new MultiReadResponseData(bytes));
        } else if (EM.equals(lastCommandSent)) {
            this.setData(new EventResponseDataEM(bytes));
        } else if (RE.equals(lastCommandSent)) {
            this.setData(new EventResponseDataRE(bytes));
        } else if (DM.equals(lastCommandSent)) {
            this.setData(new DMResponseData(bytes, logger));
        } else{
            logger.warning("Cannot determine the type of data to construct: " + lastCommandSent);
        }
    }

    public boolean isOK() throws IOException {
        return getData().isOK();
    }

    public String getError() throws IOException {
        return getData().getError();
    }

    public byte[] generateCRC() throws IOException {
        return getData().generateCRC();
    }
}
