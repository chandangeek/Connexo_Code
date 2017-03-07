package com.energyict.protocolimpl.edmi.common.command;

import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.edmi.common.core.ResponseData;

import java.io.IOException;



/**
 *
 * @author koen
 */
abstract public class AbstractCommand {
    
    private CommandFactory commandFactory;
    private ResponseData responseData;
    
    abstract protected byte[] prepareBuild();
    abstract protected void parse(byte[] data) throws CommandResponseException, ProtocolException;
    
    /** Creates a new instance of AbstractCommand */
    public AbstractCommand(CommandFactory commandFactory) {
        this.commandFactory=commandFactory;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public void invoke() {
        int retries = 0;
        try {
            byte[] cmdData = prepareBuild();
            responseData = getCommandFactory().getProtocol().getCommandLineConnection().sendCommand(cmdData);
            parseResponseData();
        } catch (IOException e) {
            if (retries++ >= getCommandFactory().getProtocol().getMaxNrOfRetries()) {
                throw ConnectionCommunicationException.numberOfRetriesReached(e, retries);
            }
        }
    }

    private void parseResponseData() throws IOException {
        if (responseData.isCAN()) {
            switch (responseData.getCANCode()) {

                case ResponseData.CANNOT_WRITE: { // e.g. serial number already set
                    throw CommunicationException.unexpectedResponse(new CommandResponseException(1, "Cannot write (CAN response code 1)"));
                } // CANNOT_WRITE

                case ResponseData.UNIMPLEMENTED_OPERATION: {
                    throw CommunicationException.unexpectedResponse(new CommandResponseException(2, "Unimplemented operation (CAN response code 2)"));
                } // UNIMPLEMENTED_OPERATION

                case ResponseData.REGISTER_NOT_FOUND: {
                    throw CommunicationException.unexpectedResponse(new CommandResponseException(3, "Register not found (CAN response code 3)"));
                } // REGISTER_NOT_FOUND

                case ResponseData.ACCESS_DENIED: { // Security reasons
                    throw CommunicationException.unexpectedResponse(new CommandResponseException(4, "Access denied because of security reason (CAN response code 4)"));
                } // ACCESS_DENIED

                case ResponseData.WRONG_LENGTH: { // Number of byte in request was incorrect
                    throw CommunicationException.unexpectedResponse(new CommandResponseException(5, "Wrong request data length (CAN response code 5)"));
                } // WRONG_LENGTH

                case ResponseData.BAD_TYPE_CODE: { // Internal error
                    throw CommunicationException.unexpectedResponse(new CommandResponseException(6, "Bad type code/Internal error (CAN response code 6)"));
                } // BAD_TYPE_CODE

                case ResponseData.DATA_NOT_READY_YET: { // Still processing. Try again later.
                    throw CommunicationException.unexpectedResponse(new CommandResponseException(7, "Data not ready yet (still processing). Try again later. (CAN response code 7)"));
                } // DATA_NOT_READY_YET

                case ResponseData.OUT_OF_RANGE: { // Written value was out of defined ranges.
                    throw CommunicationException.unexpectedResponse(new CommandResponseException(8, "The written value was out of defined range (CAN response code 8)"));
                } // OUT_OF_RANGE

                case ResponseData.NOT_LOGGED_IN: { // Not logged in.
                    throw CommunicationException.unexpectedResponse(new CommandResponseException(9, "Not logged in (CAN response code 9)"));
                } // NOT_LOGGED_IN

                case -1: {
                    throw CommunicationException.unexpectedResponse(new CommandResponseException(-1, "CAN response without code (possibly wrong password and/or deviceId (user name)"));
                }

                default:
                    throw CommunicationException.unexpectedResponse(new CommandResponseException(responseData.getCANCode(), "Invalid CAN response code " + responseData.getCANCode()));
            }
        } else if (responseData.isACK()) {
            // absorb OK
        } else {
            // absorb, responseData is available here!
            parse(getResponseData().getData());
        }
    }

    public ResponseData getResponseData() {
        return responseData;
    }
}