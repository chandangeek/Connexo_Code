package com.energyict.protocols.mdc.inbound.dlms;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.ReceiveBuffer;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ExceptionResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Enhanced version of {@link com.energyict.dlms.TCPIPConnection}, containing some extra stuff <br></br>
 * E.g.: includes a method to parse response frames. This way we are able to extract the content out of the frame,
 * without need for other classes/complicated logic.
 *
 * @author sva
 * @since 8/04/13 - 13:32
 */
public class EnhancedTCPIPConnection extends TCPIPConnection {

    public EnhancedTCPIPConnection(InputStream inputStream, OutputStream outputStream, int timeout, int forceDelay, int maxRetries, int clientAddress, int serverAddress, Logger logger) throws IOException {
        super(inputStream, outputStream, timeout, forceDelay, maxRetries, clientAddress, serverAddress, logger);
    }

    public AbstractDataType parseResponse(byte[] responseBytes) throws IOException {
        byte[] responseData = checkCosemPDUResponseHeader(responseBytes);
        AbstractDataType abstractData = AXDRDecoder.decode(responseData);
        return abstractData;
    }

    protected byte[] checkCosemPDUResponseHeader(byte[] responseData) throws IOException {
        int i;
        ReceiveBuffer receiveBuffer = new ReceiveBuffer();

        i = DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET;

        switch (responseData[i]) {
            case DLMSCOSEMGlobals.COSEM_GETRESPONSE: {
                i++; // skip tag
                switch (responseData[i]) {
                    case DLMSCOSEMGlobals.COSEM_GETRESPONSE_NORMAL: {
                        i++; // skip tag
                        i++; // skip invoke id & priority
                        switch (responseData[i]) {
                            case 0: // data
                                i++;
                                receiveBuffer.addArray(responseData, i);
                                return receiveBuffer.getArray();

                            case 1: // data-access-result
                            {
                                i++;
                                evalDataAccessResult(responseData[i]);
                                //debug("Data access result OK");
                            }
                            break; // data-access-result

                            default:
                                throw new IOException("Failed to parse the PDU response header of the packet: unknown DLMSCOSEMGlobals.COSEM_GETRESPONSE_NORMAL,  " + responseData[i]);
                        } // switch(responseData[i])
                    }
                    break;
                    default:
                        throw new IOException("Failed to parse the PDU response header of the packet: Unknown/unimplemented DLMSCOSEMGlobals.COSEM_GETRESPONSE, " + responseData[i]);
                }
            }
            break;

            case DLMSCOSEMGlobals.COSEM_EXCEPTION_RESPONSE: {
                throw new ExceptionResponseException(responseData[i + 1], responseData[i + 2]);
            }
            default: {
                throw new IOException("Failed to parse the PDU response header of the packet.");
            }
        }
        throw new IOException("Failed to parse the PDU response header of the packet.");
    }

    /**
     * @param bVal
     * @throws java.io.IOException
     */
    private void evalDataAccessResult(byte bVal) throws IOException {
        if (bVal != 0) {
            throw new DataAccessResultException(bVal & 0xFF);
        }
    }
}
