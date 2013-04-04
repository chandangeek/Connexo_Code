/*
 * AbstractCosemObject.java
 * Created on 18 augustus 2004, 11:57
 */

package com.energyict.dlms.cosem;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.AdaptorConnection;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.InvokeIdAndPriorityHandler;
import com.energyict.dlms.NonIncrementalInvokeIdAndPriorityHandler;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ReceiveBuffer;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.attributes.DLMSClassAttributes;
import com.energyict.dlms.cosem.methods.DLMSClassMethods;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Koen
 */
public abstract class AbstractCosemObject {

    private static final boolean WRITE_WITH_BLOCK_ENABLED = false;
    private static final int DEFAULT_MAX_REC_PDU_SERVER = 1024;
    protected static final int MAX_NR_OF_INVOKE_ID_MISMATCH = 25;

    private static final byte READRESPONSE_DATA_TAG = 0;
    private static final byte READRESPONSE_DATAACCESSERROR_TAG = 1;
    private static final byte READRESPONSE_DATABLOCK_RESULT_TAG = 2;
    private static final int ASSUMED_MAX_HEADER_LENGTH = 60;

    protected ProtocolLink protocolLink = null;
    private ObjectReference objectReference = null;
    private int nrOfInvalidResponseFrames = 0;
    private InvokeIdAndPriorityHandler invokeIdAndPriorityHandler;


    /**
     * Getter for the dlms class id
     *
     * @return the id of the dlms class
     */
    protected abstract int getClassId();

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public AbstractCosemObject(ProtocolLink protocolLink, ObjectReference objectReference) {
        this.objectReference = objectReference;
        this.protocolLink = protocolLink;
        if (this.protocolLink != null && getDLMSConnection() != null) {
            this.invokeIdAndPriorityHandler = getDLMSConnection().getInvokeIdAndPriorityHandler();
        } else {
            this.invokeIdAndPriorityHandler = new NonIncrementalInvokeIdAndPriorityHandler((byte) 0x42);
        }
    }

    /**
     * @return
     */
    public byte[] getCompoundData() {
        AdaptorConnection conn = (AdaptorConnection) getDLMSConnection();
        if (conn != null) {
            return conn.getCompoundData();
        } else {
            return null;
        }
    }

    /**
     * @param attribute
     * @return
     * @throws IOException
     */
    protected long getLongData(int attribute) throws IOException {
        try {
            byte[] request = null;
            if (this.objectReference.isLNReference()) {
                request = buildGetRequest(getClassId(), this.objectReference.getLn(), DLMSUtils.attrSN2LN(attribute), null);
            } else if (this.objectReference.isSNReference()) {
                request = buildReadRequest((short) this.objectReference.getSn(), attribute, null);
            }

            byte[] responseData = sendAndReceiveValidResponse(request);
            return DLMSUtils.parseValue2long(checkCosemPDUResponseHeader(responseData));
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    /**
     * @param methodId
     * @return raw data returned from the method invocation
     * @throws IOException
     * @deprecated use {@link #methodInvoke(com.energyict.dlms.cosem.methods.DLMSClassMethods, byte[])} instead. Should be converted to a private method.
     */
    public byte[] invoke(int methodId) throws IOException {
        return invoke(methodId, null);
    }

    /**
     * Invoke a given method with some data
     *
     * @param methodId The method id to invoke
     * @param data     The data to send to the meter
     * @return raw data returned from the method invocation or the axdr encoded NullData object in case of an unconfirmed association
     * @throws IOException
     * @deprecated use {@link #methodInvoke(com.energyict.dlms.cosem.methods.DLMSClassMethods, byte[])} instead. Should be converted to a private method.
     */
    public byte[] invoke(int methodId, byte[] data) throws IOException {
        try {
            if (objectReference.isLNReference()) {
                byte[] request = buildActionRequest(getClassId(), this.objectReference.getLn(), methodId, data);
                if (isConfirmedAssociation()) {
                    byte[] responseData = sendAndReceiveValidResponse(request);
                    return checkCosemPDUResponseHeader(responseData);
                } else {
                    sendUnconfirmedRequest(request);
                    return new NullData().getBEREncodedByteArray();
                }
            } else {
                return write(methodId, data);
            }
        } catch (DataAccessResultException e) {
            throw (e);
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    /**
     * CHeck using the InvokeIdAndPriority if the association is confirmed or unconfirmed
     *
     * @return True if the current association is confirmed
     */
    private final boolean isConfirmedAssociation() {
        return this.invokeIdAndPriorityHandler != null ? this.invokeIdAndPriorityHandler.getCurrentInvokeIdAndPriorityObject().needsResponse() : true;
    }

    /**
     * @param attribute
     * @param data
     * @return
     * @throws IOException
     * @deprecated use {@link #write(com.energyict.dlms.cosem.attributes.DLMSClassAttributes, byte[])} instead. Should be converted to a private method.
     */
    protected byte[] write(int attribute, byte[] data) throws IOException {
        try {
            byte[] responseData = null;
            if (this.objectReference.isLNReference()) {
                byte[] request = buildSetRequest(getClassId(), this.objectReference.getLn(), (byte) attribute, data);
                // Server max receive pdu size exceeded: we should use write request with block transfer
                if ((request.length >= getMaxRecPduServer()) && (!this.protocolLink.getMeterConfig().isSL7000())) {
                    responseData = sendSetRequestWithBlockTransfer(attribute, data);
                    if (!isConfirmedAssociation()) {
                        return null;
                    }
                } else {
                    if (isConfirmedAssociation()) {
                        // The Actaris SL7000 meter never uses block transfer requests, but always uses the regular one.
                        responseData = sendAndReceiveValidResponse(request);
                    } else {
                        sendUnconfirmedRequest(request);
                        return null;
                    }
                }
            } else if (this.objectReference.isSNReference()) {

                // very dirty trick because there is a lot of legacy code that passes the attribute as
                // an correct offset to the base address... 	However, the later (better) dlms framework objects
                // use the method for write in this abstract class with attribute id 0,1,...
                // so, only for attribute = 8 or a multiple of 8 can be a problem....
                if ((attribute < 8) || ((attribute % 8) != 0)) {
                    attribute = (attribute - 1) * 8;
                }

                byte[] request = buildWriteRequest((short) this.objectReference.getSn(), attribute, data);

                // Server max receive pdu size exceeded: we should use write request with block transfer
                if (WRITE_WITH_BLOCK_ENABLED && (request.length >= getMaxRecPduServer())) {
                    responseData = sendWriteRequestWithBlockTransfer(request);
                } else {
                    responseData = sendAndReceiveValidResponse(request);
                }
            }

            if (getDLMSConnection() instanceof AdaptorConnection) {
                return responseData;
            } else {
                return checkCosemPDUResponseHeader(responseData);
            }
        } catch (DataAccessResultException e) {
            throw (e);
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    private byte[] sendSetRequestWithBlockTransfer(int attribute, byte[] request) throws IOException {

        // Some calculations that can be useful further on ...
        final int maxBlockSize = getMaxRecPduServer(); // Keep some space for the headers ...
        final int numberOfFullSizedBlocks = request.length / maxBlockSize;
        final int lastBlockSize = request.length % maxBlockSize;
        final int numberOfBlocksToSend = lastBlockSize != 0 ? numberOfFullSizedBlocks + 1 : numberOfFullSizedBlocks;

        int blockNumber = 1;
        int offset = 0;

        while (true) {
            // Check if this is the last block
            boolean firstBlock = (blockNumber == 1);
            boolean lastBlock = (blockNumber == numberOfBlocksToSend);

            // Get the length and the data to send for this particular block
            byte[] dataToSend = new byte[lastBlock ? lastBlockSize : maxBlockSize];
            System.arraycopy(request, offset, dataToSend, 0, dataToSend.length);

            // Send the block to the device. Validate the response of the last block
            if (lastBlock) {
                return sendSetRequestBlock(dataToSend, attribute, blockNumber, lastBlock, firstBlock);
            } else {
                sendSetRequestBlock(dataToSend, attribute, blockNumber, lastBlock, firstBlock);
            }

            // And finally, set everything ready for the next block
            offset += dataToSend.length;
            blockNumber++;

        }
    }

    private byte[] sendWriteRequestWithBlockTransfer(byte[] requestToSend) throws IOException {

        // Strip the legacy bytes and the original write request tag (3 + 1 = total 4 bytes to strip)
        byte[] request = new byte[requestToSend.length - 4];
        System.arraycopy(requestToSend, 4, request, 0, request.length);

        // Some calculations that can be useful further on ...
        final int maxBlockSize = getMaxRecPduServer(); // Keep some space for the headers
        final int numberOfFullSizedBlocks = request.length / maxBlockSize;
        final int lastBlockSize = request.length % maxBlockSize;
        final int numberOfBlocksToSend = lastBlockSize != 0 ? numberOfFullSizedBlocks + 1 : numberOfFullSizedBlocks;

        int blockNumber = 1;
        int offset = 0;

        while (true) {
            // Check if this is the last block
            boolean lastBlock = blockNumber == numberOfBlocksToSend;

            // Get the length and the data to send for this particular block
            byte[] dataToSend = new byte[lastBlock ? lastBlockSize : maxBlockSize];
            System.arraycopy(request, offset, dataToSend, 0, dataToSend.length);

            // Send the block to the device. For the last block, the response is validated in the calling methods
            if (lastBlock) {
                return sendWriteRequestBlock(dataToSend, blockNumber, lastBlock);
            } else {
                sendWriteRequestBlock(dataToSend, blockNumber, lastBlock);
            }

            // And finally, set everything ready for the next block
            offset += dataToSend.length;
            blockNumber++;
        }
    }

    /**
     * Only used in case of LN-reference. Used for SET request with data blocks
     *
     * @param dataToSend  The block of data to send to the meter
     * @param attribute   The attribute number
     * @param blockNumber The block number
     * @param lastBlock   Indication if this block is the last block
     * @param firstBlock  Indication if this block is the first block
     * @return The response from the device or 'null' in case of unconfirmed write
     * @throws IOException
     */
    private byte[] sendSetRequestBlock(byte[] dataToSend, int attribute, int blockNumber, boolean lastBlock, boolean firstBlock) throws IOException {
        byte[] request = firstBlock ?
                new byte[3 + 13 + 6 + dataToSend.length + (dataToSend.length > 127 ? 2 : 0)] :
                new byte[3 + 3 + 6 + dataToSend.length + (dataToSend.length > 127 ? 2 : 0)];

        int ptr = 0;
        // As usual, add the 3 legacy bytes :)
        request[ptr++] = (byte) 0xE6; // Destination_LSAP
        request[ptr++] = (byte) 0xE6; // Source_LSAP
        request[ptr++] = 0x00;        // LLC_Quality

        if (firstBlock) {
            // Create the SetRequest with first data block header (13 bytes)
            request[ptr++] = DLMSCOSEMGlobals.COSEM_SETREQUEST;
            request[ptr++] = DLMSCOSEMGlobals.COSEM_SETREQUEST_WITH_FIRST_DATABLOCK; // Set request with first data blocks
            request[ptr++] = this.invokeIdAndPriorityHandler.getNextInvokeIdAndPriority(); // next invoke id and priority

            request[ptr++] = (byte) (getClassId() >> 8); // Dlms class id
            request[ptr++] = (byte) getClassId();

            request[ptr++] = getObisCode().getLN()[0]; // ObisCode
            request[ptr++] = getObisCode().getLN()[1];
            request[ptr++] = getObisCode().getLN()[2];
            request[ptr++] = getObisCode().getLN()[3];
            request[ptr++] = getObisCode().getLN()[4];
            request[ptr++] = getObisCode().getLN()[5];

            request[ptr++] = (byte) (attribute & 0x0FF); // AttributeId
            request[ptr++] = 0; // No selective access field (OPTIONAL)
        } else {
            // Create the SetRequest with data block header (3 bytes)
            request[ptr++] = DLMSCOSEMGlobals.COSEM_SETREQUEST;
            request[ptr++] = DLMSCOSEMGlobals.COSEM_SETREQUEST_WITH_DATABLOCK; // Set request with data blocks
            request[ptr++] = this.invokeIdAndPriorityHandler.getCurrentInvokeIdAndPriority(); // Reuse the current invoke id and priority
        }
        request[ptr++] = (byte) (lastBlock ? 1 : 0); // LastBlock value

        // Block number
        request[ptr++] = (byte) (blockNumber >> 24);
        request[ptr++] = (byte) (blockNumber >> 16);
        request[ptr++] = (byte) (blockNumber >> 8);
        request[ptr++] = (byte) (blockNumber >> 0);

        if (dataToSend.length > 127) {
            request[ptr++] = (byte) 0x082;
            request[ptr++] = (byte) ((dataToSend.length >> 8) & 0x0FF);
            request[ptr++] = (byte) (dataToSend.length & 0x0FF);
        } else {
            request[ptr++] = (byte) (dataToSend.length & 0x0FF);
        }

        for (int i = 0; i < dataToSend.length; i++) {
            request[ptr++] = dataToSend[i];
        }

        if (isConfirmedAssociation()) {
            byte[] response = sendAndReceiveValidResponse(request);
            if (!lastBlock) {
                checkCosemPDUResponseHeader(response);
            }
            return response;
        } else {
            sendUnconfirmedRequest(request);
            return null;
        }
    }

    private byte[] sendWriteRequestBlock(byte[] dataToSend, int blockNumber, boolean lastBlock) throws IOException {
        byte[] request = new byte[3 + 9 + dataToSend.length];

        int ptr = 0;
        // As usual, add the 3 legacy bytes :)
        request[ptr++] = (byte) 0xE6; // Destination_LSAP
        request[ptr++] = (byte) 0xE6; // Source_LSAP
        request[ptr++] = 0x00;        // LLC_Quality

        // Create the WriteRequest with data blocks header (9 bytes)
        request[ptr++] = DLMSCOSEMGlobals.COSEM_WRITEREQUEST;
        request[ptr++] = 0x01; // One 'VariableDataSpec'
        request[ptr++] = 0x07; // Write data block access tag
        request[ptr++] = (byte) (lastBlock ? 0x01 : 0x00);  // Last block bool
        request[ptr++] = (byte) ((blockNumber >> 8) & 0x0FF); // Block number HIGH
        request[ptr++] = (byte) (blockNumber & 0x0FF);      // Block number LOW
        request[ptr++] = 0x01; // One data part
        request[ptr++] = 0x09; // OctetString tag
        request[ptr++] = (byte) (dataToSend.length & 0x0FF); // Actual data length

        // Add the actual content of the write data block
        System.arraycopy(dataToSend, 0, request, ptr, dataToSend.length);

        // Send it to the device
        byte[] response = sendAndReceiveValidResponse(request);

        // Validate the response. For the last block, the response is validated in the calling methods
        if (!lastBlock) {
            checkCosemPDUResponseHeader(response);
        }

        return response;
    }

    private int getMaxRecPduServer() {
        ApplicationServiceObject applicationServiceObject = getProtocolLink().getDLMSConnection().getApplicationServiceObject();
        if (applicationServiceObject != null) {
            final int maxRecPDUServerSize = applicationServiceObject.getAssociationControlServiceElement().getXdlmsAse().getMaxRecPDUServerSize();
            if (maxRecPDUServerSize > 0) {
                return maxRecPDUServerSize - ASSUMED_MAX_HEADER_LENGTH;
            } else {
                return DEFAULT_MAX_REC_PDU_SERVER;
            }
        } else {
            return DEFAULT_MAX_REC_PDU_SERVER;
        }
    }

    /**
     * DLMS LN and SN supported write of the given DLMSClassAttribute to the device
     *
     * @param attribute the attribute to write
     * @param data      the data to write
     * @return any additional response data
     * @throws IOException if for some reason the write failed
     */
    protected byte[] write(DLMSClassAttributes attribute, byte[] data) throws IOException {
        if (getObjectReference().isSNReference()) {
            return write(attribute.getShortName(), data);
        } else {
            return write(attribute.getAttributeNumber(), data);
        }
    }

    /**
     * Write the given data to the given attribute on the device.
     *
     * @param attribute   The attribute to write.
     * @param dataToWrite The data to write.
     * @throws IOException In case an IO error occurs during the write.
     * @return Any response data that was returned as a response to the set request.
     */
    protected final <T extends AbstractDataType> byte[] write(final DLMSClassAttributes attribute, final T dataToWrite) throws IOException {
        return this.write(attribute, dataToWrite.getBEREncodedByteArray());
    }

    /**
     * Attribute as defined in the object docs
     * 1 - logical name
     * 2..n attribute 2..n
     *
     * @param attribute the attribute to read
     * @return the response from the device
     * @throws IOException
     * @deprecated use {@link #getResponseData(com.energyict.dlms.cosem.attributes.DLMSClassAttributes)} instead
     */
    protected byte[] getLNResponseData(int attribute) throws IOException {
        return getLNResponseData(attribute, null, null);
    }

    /**
     * @param attribute
     * @param from
     * @param to
     * @return
     * @throws IOException
     */
    protected byte[] getLNResponseData(int attribute, Calendar from, Calendar to) throws IOException {
        return getResponseData((attribute - 1) * 8, from, to);
    }

    /**
     * Attribute as defined in the object docs for short name reference
     * 0 = logical name attribute 1
     * 8,16,24,..n attribute 2..n
     *
     * @param attribute
     * @return
     * @throws IOException
     * @deprecated use {@link #getResponseData(com.energyict.dlms.cosem.attributes.DLMSClassAttributes)} instead. Should be converted to a private method
     */
    protected byte[] getResponseData(int attribute) throws IOException {
        return getResponseData(attribute, null, null);
    }

    /**
     * DLMS LN and SN supported get of the given {@link com.energyict.dlms.cosem.attributes.DLMSClassAttributes}
     *
     * @param attribute the attribute to read from the device
     * @return the value of the requested attribute
     * @throws IOException if an exception occurs during the reading
     */
    protected byte[] getResponseData(DLMSClassAttributes attribute) throws IOException {
        return getResponseData(attribute, null);
    }

    /**
     * DLMS LN and SN supported get of the given {@link com.energyict.dlms.cosem.attributes.DLMSClassAttributes} with selective access
     *
     * @param attribute       the attribute to read from the device
     * @param selectiveAccess The selectiveAccess attribute
     * @return the value of the requested attribute
     * @throws IOException if an exception occurs during the reading
     */
    protected byte[] getResponseData(final DLMSClassAttributes attribute, final AbstractDataType selectiveAccess) throws IOException {
        if (getObjectReference().isSNReference()) {
            if (selectiveAccess == null) {
                return getResponseData(attribute.getShortName());
            } else {
                return getResponseData(attribute.getShortName(), selectiveAccess);
            }
        } else {
            if (selectiveAccess == null) {
                return getLNResponseData(attribute.getAttributeNumber());
            } else {
                return getResponseData((attribute.getAttributeNumber() - 1) * 8, selectiveAccess);
            }
        }
    }

    /**
     * DLMS LN and SN supported get of the given {@link com.energyict.dlms.cosem.attributes.DLMSClassAttributes}
     *
     * @param attribute     the attribute to read from the device
     * @param expectedClass The expected data type class
     * @param <T>           The class type of the expected data
     * @return the value of the requested attribute
     * @throws IOException if an exception occurs during the reading
     */
    protected <T extends AbstractDataType> T readDataType(DLMSClassAttributes attribute, AbstractDataType selectiveAccess, Class<T> expectedClass) throws IOException {
        byte[] rawData = getResponseData(attribute, selectiveAccess);
        return AXDRDecoder.decode(rawData, expectedClass);
    }

    /**
     * DLMS LN and SN supported get of the given {@link com.energyict.dlms.cosem.attributes.DLMSClassAttributes}
     *
     * @param attribute     the attribute to read from the device
     * @param expectedClass The expected data type class
     * @param <T>           The class type of the expected data
     * @return the value of the requested attribute
     * @throws IOException if an exception occurs during the reading
     */
    protected <T extends AbstractDataType> T readDataType(DLMSClassAttributes attribute, Class<T> expectedClass) throws IOException {
        return readDataType(attribute, null, expectedClass);
    }

    /**
     * @param attribute
     * @return
     * @throws IOException
     */
    protected final AbstractDataType readDataType(DLMSClassAttributes attribute) throws IOException {
        return readDataType(attribute, AbstractDataType.class);
    }

    /**
     * @param attributes
     * @return
     * @throws IOException
     */
    protected byte[][] getResponseDataWithList(DLMSAttribute[] attributes) throws IOException {
        byte[][] result = new byte[attributes.length][];
        byte[] request = buildGetWithListRequest(attributes);
        byte[] responseData = sendAndReceiveValidResponse(request);
        responseData = checkCosemPDUResponseHeader(responseData);

        int ptr = 0;
        for (int i = 0; i < attributes.length; i++) {
            switch (responseData[ptr]) {
                case 0x00: // Data
                    AbstractDataType dataType = AXDRDecoder.decode(responseData, ptr + 1);
                    int objectLength = dataType.getBEREncodedByteArray().length;
                    result[i] = DLMSUtils.getSubArray(responseData, ptr, ptr + objectLength + 1);
                    ptr += objectLength + 1;
                    break;
                case 0x01: // Data-access-result
                    result[i] = DLMSUtils.getSubArray(responseData, ptr, ptr + 2);
                    ptr += 2;
                    break;
                default:
                    throw new IOException("Invalid state while parsing GetResponseWithList: expected '0' or '1' but was " + responseData[i]);
            }
        }

        return result;
    }

    /**
     * @param method        the method to invoke
     * @param data          the additional data to write with he method
     * @param expectedClass The expected data type class
     * @param <T>           The class type of the expected data
     * @return the value of the requested attribute
     * @throws IOException if an exception occurs during the method call
     */
    protected <T extends AbstractDataType> T methodInvoke(DLMSClassMethods method, AbstractDataType data, Class<T> expectedClass) throws IOException {
        final byte[] response = methodInvoke(method, data);
        return AXDRDecoder.decode(response, expectedClass);
    }

    /**
     * DLMS LN and SN supported invocation of the methods based on the given {@link com.energyict.dlms.cosem.methods.DLMSClassMethods}
     *
     * @param method the method to invoke
     * @param data   the additional data to write with he method
     * @return raw data returned from the method invocation
     * @throws IOException if an exception occurs during the method call
     */
    protected byte[] methodInvoke(DLMSClassMethods method, AbstractDataType data) throws IOException {
        return methodInvoke(method, data.getBEREncodedByteArray());
    }

    /**
     * DLMS LN and SN supported invocation of the methods based on the given {@link com.energyict.dlms.cosem.methods.DLMSClassMethods}
     *
     * @param method      the method to invoke
     * @param encodedData the ber-encoded additional data to write with he method
     * @return raw data returned from the method invocation
     * @throws IOException if an exception occurs during the method call
     */
    protected byte[] methodInvoke(DLMSClassMethods method, byte[] encodedData) throws IOException {
        if (getObjectReference().isSNReference()) {
            return write(method.getShortName(), encodedData);
        } else {
            return invoke(method.getMethodNumber(), encodedData);
        }
    }

    /**
     * DLMS LN and SN supported invocation of the methods based on the given {@link com.energyict.dlms.cosem.methods.DLMSClassMethods}
     *
     * @param method the method to invoke
     * @return raw data returned from the method invocation
     * @throws IOException if an exception occurs during the method call
     */
    protected byte[] methodInvoke(DLMSClassMethods method) throws IOException {
        return methodInvoke(method, (byte[]) null);
    }

    /**
     * Build up the request, send it to the device and return the checked
     * response data as byte[]
     *
     * @param attribute - the DLMS attribute id
     * @param from      - the from date as {@link Calendar}
     * @param to        - the to date as {@link Calendar}
     * @return the validate response as byte[]
     * @throws IOException
     */
    protected byte[] getResponseData(int attribute, Calendar from, Calendar to) throws IOException {
        try {
            byte[] responseData = null;
            byte[] request = null;
            if (this.objectReference.isLNReference()) {
                byte[] selectiveBuffer = (from == null ? null : getBufferRangeDescriptor(from, to));
                request = buildGetRequest(getClassId(), this.objectReference.getLn(), DLMSUtils.attrSN2LN(attribute), selectiveBuffer);
            } else if (this.objectReference.isSNReference()) {
                byte[] selectiveBuffer = (from == null ? null : getBufferRangeDescriptor(from, to));
                request = buildReadRequest((short) this.objectReference.getSn(), attribute, selectiveBuffer);
            }
            responseData = sendAndReceiveValidResponse(request);
            return checkCosemPDUResponseHeader(responseData);
        } catch (DataAccessResultException e) {
            throw (e);
        } catch (IOException e) {
            throw new NestedIOException(e);
        } catch (IndexOutOfBoundsException e) {
            throw new NestedIOException(e, "Received partial response or invalid packet from device!");
        }
    }

    /**
     * Build up the request, send it to the device and return the checked
     * response data as byte[]
     *
     * @param attribute - the DLMS attribute id
     * @return the validate response as byte[]
     * @throws IOException
     */
    protected byte[] getResponseData(int attribute, final AbstractDataType selectiveAccess) throws IOException {
        try {
            byte[] selectiveBuffer = selectiveAccess == null ? null : DLMSUtils.concatByteArrays(new byte[]{0x01}, selectiveAccess.getBEREncodedByteArray());

            final byte[] request;
            if (this.objectReference.isLNReference()) {
                request = buildGetRequest(getClassId(), this.objectReference.getLn(), DLMSUtils.attrSN2LN(attribute), selectiveBuffer);
            } else {
                request = buildReadRequest((short) this.objectReference.getSn(), attribute, selectiveBuffer);
            }

            final byte[] responseData = sendAndReceiveValidResponse(request);
            return checkCosemPDUResponseHeader(responseData);
        } catch (DataAccessResultException e) {
            throw (e);
        } catch (IOException e) {
            throw new NestedIOException(e);
        } catch (IndexOutOfBoundsException e) {
            throw new NestedIOException(e, "Received partial response or invalid packet from device!");
        }
    }

    protected byte[] getResponseData(int attribute, long from, long to) throws IOException {
        try {
            byte[] responseData = null;
            byte[] request = null;
            if (this.objectReference.isLNReference()) {
                byte[] selectiveBuffer = (from == 0 ? null : getBufferRangeDescriptor(from, to));
                request = buildGetRequest(getClassId(), this.objectReference.getLn(), DLMSUtils.attrSN2LN(attribute), selectiveBuffer);
            } else if (this.objectReference.isSNReference()) {
                byte[] selectiveBuffer = (from == 0 ? null : getBufferRangeDescriptor(from, to));
                request = buildReadRequest((short) this.objectReference.getSn(), attribute, selectiveBuffer);
            }
            responseData = sendAndReceiveValidResponse(request);
            return checkCosemPDUResponseHeader(responseData);
        } catch (DataAccessResultException e) {
            throw (e);
        } catch (IOException e) {
            throw new NestedIOException(e);
        } catch (IndexOutOfBoundsException e) {
            throw new NestedIOException(e, "Received partial response or invalid packet from device!");
        }
    }

    /**
     * Build up the request, send it to the device and return the checked
     * response data as byte[]
     *
     * @param attribute - the DLMS attribute id
     * @param from      - the from date as {@link Calendar}
     * @param to        - the to date as {@link Calendar}
     * @param channels  - a list of channels that should be read out
     * @return the validate response as byte[]
     * @throws IOException
     */
    protected byte[] getResponseData(int attribute, Calendar from, Calendar to, List<CapturedObject> channels) throws IOException {
        try {
            byte[] responseData = null;
            byte[] request = null;
            if (this.objectReference.isLNReference()) {
                byte[] selectiveBuffer = (from == null ? null : getBufferRangeDescriptor(from, to, channels));
                request = buildGetRequest(getClassId(), this.objectReference.getLn(), DLMSUtils.attrSN2LN(attribute), selectiveBuffer);
            } else if (this.objectReference.isSNReference()) {
                byte[] selectiveBuffer = (from == null ? null : getBufferRangeDescriptor(from, to, channels));
                request = buildReadRequest((short) this.objectReference.getSn(), attribute, selectiveBuffer);
            }
            responseData = sendAndReceiveValidResponse(request);
            return checkCosemPDUResponseHeader(responseData);
        } catch (DataAccessResultException e) {
            throw (e);
        } catch (IOException e) {
            throw new NestedIOException(e);
        } catch (IndexOutOfBoundsException e) {
            throw new NestedIOException(e, "Received partial response or invalid packet from device!");
        }
    }

    /**
     * Build up the request, send it to the device and return the checked
     * response data as byte[]
     */
    protected byte[] getResponseData(int attribute, int fromEntry, int toEntry, int fromValue, int toValue) throws IOException {
        try {
            byte[] responseData = null;
            byte[] request = null;
            if (this.objectReference.isLNReference()) {
                byte[] selectiveBuffer = (fromEntry == 0 ? null : getBufferEntryDescriptor(fromEntry, toEntry, fromValue, toValue));
                request = buildGetRequest(getClassId(), this.objectReference.getLn(), DLMSUtils.attrSN2LN(attribute), selectiveBuffer);
            } else if (this.objectReference.isSNReference()) {
                byte[] selectiveBuffer = (fromEntry == 0 ? null : getBufferEntryDescriptor(fromEntry, toEntry, fromValue, toValue));
                request = buildReadRequest((short) this.objectReference.getSn(), attribute, selectiveBuffer);
            }
            responseData = sendAndReceiveValidResponse(request);
            return checkCosemPDUResponseHeader(responseData);
        } catch (DataAccessResultException e) {
            throw (e);
        } catch (IOException e) {
            throw new NestedIOException(e);
        } catch (IndexOutOfBoundsException e) {
            throw new NestedIOException(e, "Received partial response or invalid packet from device!");
        }
    }

    /**
     * @param blockNr
     * @return
     */
    private byte[] buildReadRequestNext(int blockNr) {
        // KV 06052009
        byte[] readRequestArray = new byte[DLMSCOSEMGlobals.READREQUEST_DATA_SIZE];
        readRequestArray[0] = (byte) 0xE6; // Destination_LSAP
        readRequestArray[1] = (byte) 0xE6; // Source_LSAP
        readRequestArray[2] = 0x00; // LLC_Quality
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET] = DLMSCOSEMGlobals.COSEM_READREQUEST;
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_LENGTH_OFFSET] = 0x01; // length of the variable length SEQUENCE OF
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_TAG_OFFSET] = 0x05; // block-number-access
        readRequestArray[DLMSCOSEMGlobals.READREQUEST_BLOCKNR_MSB] = (byte) (((blockNr) >> 8) & 0x00FF);
        readRequestArray[DLMSCOSEMGlobals.READREQUEST_BLOCKNR_LSB] = (byte) ((blockNr) & 0x00FF);
        return readRequestArray;
    }

    /**
     * @param iObj
     * @param iAttr
     * @param byteSelectiveBuffer
     * @return
     */
    private byte[] buildReadRequest(int iObj, int iAttr, byte[] byteSelectiveBuffer) {
        byte[] readRequestArray = new byte[DLMSCOSEMGlobals.READREQUEST_DATA_SIZE];

        readRequestArray[0] = (byte) 0xE6; // Destination_LSAP
        readRequestArray[1] = (byte) 0xE6; // Source_LSAP
        readRequestArray[2] = 0x00; // LLC_Quality
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET] = DLMSCOSEMGlobals.COSEM_READREQUEST;
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_LENGTH_OFFSET] = 0x01; // length of the variable length SEQUENCE OF
        if (byteSelectiveBuffer == null) {
            readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_TAG_OFFSET] = 0x02; // implicit objectname
        } else {
            readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_TAG_OFFSET] = 0x04; // object name integer data
        }

        readRequestArray[DLMSCOSEMGlobals.READREQUEST_SN_MSB] = (byte) (((iObj + iAttr) >> 8) & 0x00FF);
        readRequestArray[DLMSCOSEMGlobals.READREQUEST_SN_LSB] = (byte) ((iObj + iAttr) & 0x00FF);

        if (byteSelectiveBuffer != null) {
            // Concatenate 2 byte arrays into requestData.
            byte[] requestData = new byte[readRequestArray.length + byteSelectiveBuffer.length];
            for (int i = 0; i < DLMSCOSEMGlobals.READREQUEST_DATA_SIZE; i++) {
                requestData[i] = readRequestArray[i];
            }
            for (int i = DLMSCOSEMGlobals.READREQUEST_DATA_SIZE; i < requestData.length; i++) {
                requestData[i] = byteSelectiveBuffer[i - DLMSCOSEMGlobals.READREQUEST_DATA_SIZE];
            }
            return requestData;
        } else {
            return readRequestArray;
        }
    }

    /**
     * @param iObj
     * @param iAttr
     * @param byteSelectiveBuffer
     * @return
     * @throws IOException
     */
    private byte[] buildWriteRequest(int iObj, int iAttr, byte[] byteSelectiveBuffer) throws IOException {
        byte[] writeRequestArray = new byte[DLMSCOSEMGlobals.WRITEREQUEST_DATA_SIZE];

        writeRequestArray[0] = (byte) 0xE6; // Destination_LSAP
        writeRequestArray[1] = (byte) 0xE6; // Source_LSAP
        writeRequestArray[2] = 0x00; // LLC_Quality

        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET] = DLMSCOSEMGlobals.COSEM_WRITEREQUEST;
        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_LENGTH_OFFSET] = 0x01; // length of the variable length SEQUENCE OF
        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_TAG_OFFSET] = 0x02; // implicit objectname
        writeRequestArray[DLMSCOSEMGlobals.READREQUEST_SN_MSB] = (byte) (((iObj + iAttr) >> 8) & 0x00FF);
        writeRequestArray[DLMSCOSEMGlobals.READREQUEST_SN_LSB] = (byte) ((iObj + iAttr) & 0x00FF);
        writeRequestArray[DLMSCOSEMGlobals.WRITEREQUEST_NR_OF_OBJECTS] = 0x01; // one object

        if (byteSelectiveBuffer != null) {
            // Concatenate 2 byte arrays into requestData.
            byte[] requestData = new byte[writeRequestArray.length + byteSelectiveBuffer.length];
            for (int i = 0; i < DLMSCOSEMGlobals.WRITEREQUEST_DATA_SIZE; i++) {
                requestData[i] = writeRequestArray[i];
            }
            for (int i = DLMSCOSEMGlobals.WRITEREQUEST_DATA_SIZE; i < requestData.length; i++) {
                requestData[i] = byteSelectiveBuffer[i - DLMSCOSEMGlobals.WRITEREQUEST_DATA_SIZE];
            }
            return requestData;
        } else {
            return writeRequestArray;
        }
    }

    /**
     * @param classId
     * @param LN
     * @param bAttr
     * @param byteSelectiveBuffer
     * @return
     */
    private byte[] buildGetRequest(int classId, byte[] LN, byte bAttr, byte[] byteSelectiveBuffer) {
        byte[] readRequestArray = new byte[DLMSCOSEMGlobals.GETREQUEST_DATA_SIZE];

        readRequestArray[0] = (byte) 0xE6; // Destination_LSAP
        readRequestArray[1] = (byte) 0xE6; // Source_LSAP
        readRequestArray[2] = 0x00; // LLC_Quality
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET] = DLMSCOSEMGlobals.COSEM_GETREQUEST;
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 1] = DLMSCOSEMGlobals.COSEM_GETREQUEST_NORMAL; // get request normal
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2] = this.invokeIdAndPriorityHandler.getNextInvokeIdAndPriority(); // next invoke id and priority
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_CID] = (byte) (classId >> 8);
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_CID + 1] = (byte) classId;

        for (int i = 0; i < 6; i++) {
            readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_LN + i] = LN[i];
        }

        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_ATTR] = bAttr;

        if (byteSelectiveBuffer == null) {
            readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_ACCESS_SELECTOR] = 0; // Selective access descriptor NOT present
            return readRequestArray;
        } else {
            readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_ACCESS_SELECTOR] = 1; // Selective access descriptor present

            // Concatenate 2 byte arrays into requestData.
            byte[] requestData = new byte[readRequestArray.length + byteSelectiveBuffer.length];
            for (int i = 0; i < DLMSCOSEMGlobals.GETREQUEST_DATA_SIZE; i++) {
                requestData[i] = readRequestArray[i];
            }
            for (int i = DLMSCOSEMGlobals.GETREQUEST_DATA_SIZE; i < requestData.length; i++) {
                requestData[i] = byteSelectiveBuffer[i - (DLMSCOSEMGlobals.GETREQUEST_DATA_SIZE)];
            }
            return requestData;
        }

    }

    private byte[] buildGetWithListRequest(DLMSAttribute... attributes) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.write(0xE6); // Destination_LSAP
        buffer.write(0xE6); // Source_LSAP
        buffer.write(0x00);
        if (getObjectReference().isLNReference()) {
            buffer.write(getLnGetWithListRequest(attributes));
        } else {
            buffer.write(getSnGetWithListRequest(attributes));
        }
        return buffer.toByteArray();
    }

    private byte[] getLnGetWithListRequest(DLMSAttribute... attributes) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.write(DLMSCOSEMGlobals.COSEM_GETREQUEST);
        buffer.write(DLMSCOSEMGlobals.COSEM_GETREQUEST_WITH_LIST);
        buffer.write(this.invokeIdAndPriorityHandler.getNextInvokeIdAndPriority()); // next invoke id and priority
        buffer.write(attributes.length); // Number of items
        for (DLMSAttribute dlmsAttribute : attributes) {
            // cosem-attribute-descriptor
            try {
                buffer.write(new Unsigned16(dlmsAttribute.getClassId()).getContentByteArray()); // cosem-class-id
                buffer.write(dlmsAttribute.getObisCode().getLN());                              // cosem-object-inctance-id
                buffer.write(new Integer8(dlmsAttribute.getAttribute()).getContentByteArray()); // cosem-attribute-id
                // cosem-selective-access-descriptor (Optional)
                buffer.write(0x00); // Not available (unused)
            } catch (IOException e) {
                // ByteArrayOutputStream never throws exception
            }
        }
        return buffer.toByteArray();
    }

    private byte[] getSnGetWithListRequest(DLMSAttribute... attributes) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.write(DLMSCOSEMGlobals.COSEM_READREQUEST);
        buffer.write(attributes.length); // Number of items
        for (DLMSAttribute dlmsAttribute : attributes) {
            buffer.write(0x02); // implicit objectName
            int shortName = getShortNameFromObjectList(dlmsAttribute.getObisCode());
            buffer.write((byte) ((shortName + dlmsAttribute.getSnAttribute()) >> 8) & 0x00FF);
            buffer.write((byte) (shortName + dlmsAttribute.getSnAttribute()) & 0x00FF);
        }
        return buffer.toByteArray();
    }

    protected int getShortNameFromObjectList(ObisCode logicalName) {
        UniversalObject[] uos = getProtocolLink().getMeterConfig().getInstantiatedObjectList();
        for (UniversalObject uo : uos) {
            if (uo.getObisCode().equals(logicalName)) {
                return uo.getBaseName();
            }
        }
        return -1;
    }

    /**
     * @param iBlockNumber
     * @return
     */
    private byte[] buildGetRequestNext(int iBlockNumber) {
        byte[] readRequestArray = new byte[DLMSCOSEMGlobals.GETREQUESTNEXT_DATA_SIZE];
        readRequestArray[0] = (byte) 0xE6; // Destination_LSAP
        readRequestArray[1] = (byte) 0xE6; // Source_LSAP
        readRequestArray[2] = 0x00; // LLC_Quality
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET] = DLMSCOSEMGlobals.COSEM_GETREQUEST;
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 1] = DLMSCOSEMGlobals.COSEM_GETREQUEST_NEXT; // get request next
        // Reuse the current invoke id and priority! (each primitive of a GET-REQUEST-NEXT should have the same invoke id and priority - see Green Book 7th p182 [9.4.6.3 Protocol for the GET service])
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2] = this.invokeIdAndPriorityHandler.getCurrentInvokeIdAndPriority();
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3] = (byte) (iBlockNumber >> 24);
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 4] = (byte) (iBlockNumber >> 16);
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 5] = (byte) (iBlockNumber >> 8);
        readRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 6] = (byte) iBlockNumber;
        return readRequestArray;
    }

    /**
     * @param classId
     * @param LN
     * @param bAttr
     * @param byteSelectiveBuffer
     * @return
     */
    private byte[] buildSetRequest(int classId, byte[] LN, byte bAttr, byte[] byteSelectiveBuffer) {
        byte[] writeRequestArray = new byte[DLMSCOSEMGlobals.SETREQUEST_DATA_SIZE];
        int i;

        writeRequestArray[0] = (byte) 0xE6; // Destination_LSAP
        writeRequestArray[1] = (byte) 0xE6; // Source_LSAP
        writeRequestArray[2] = 0x00; // LLC_Quality
        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET] = DLMSCOSEMGlobals.COSEM_SETREQUEST;
        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 1] = DLMSCOSEMGlobals.COSEM_SETREQUEST_NORMAL; // get request normal
        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2] = this.invokeIdAndPriorityHandler.getNextInvokeIdAndPriority(); // next invoke id and priority
        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_CID] = (byte) (classId >> 8);
        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_CID + 1] = (byte) classId;

        for (i = 0; i < 6; i++) {
            writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_LN + i] = LN[i];
        }
        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_ATTR] = bAttr;

        if (byteSelectiveBuffer == null) {
            writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_ACCESS_SELECTOR] = 0;
            return writeRequestArray;
        } else {
            writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_ACCESS_SELECTOR] = 0;
            // Concatenate 2 byte arrays into requestData.
            byte[] requestData = new byte[writeRequestArray.length + byteSelectiveBuffer.length];
            for (i = 0; i < DLMSCOSEMGlobals.GETREQUEST_DATA_SIZE; i++) {
                requestData[i] = writeRequestArray[i];
            }
            for (i = DLMSCOSEMGlobals.GETREQUEST_DATA_SIZE; i < requestData.length; i++) {
                requestData[i] = byteSelectiveBuffer[i - (DLMSCOSEMGlobals.GETREQUEST_DATA_SIZE)];
            }
            return requestData;
        }
    }

    /**
     * @param classId
     * @param LN
     * @param methodId
     * @param data
     * @return
     */
    private byte[] buildActionRequest(int classId, byte[] LN, int methodId, byte[] data) {
        byte[] writeRequestArray = new byte[DLMSCOSEMGlobals.ACTIONREQUEST_DATA_SIZE];

        writeRequestArray[0] = (byte) 0xE6; // Destination_LSAP
        writeRequestArray[1] = (byte) 0xE6; // Source_LSAP
        writeRequestArray[2] = 0x00; // LLC_Quality
        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET] = DLMSCOSEMGlobals.COSEM_ACTIONREQUEST;
        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 1] = DLMSCOSEMGlobals.COSEM_ACTIONREQUEST_NORMAL; // get request normal
        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2] = this.invokeIdAndPriorityHandler.getNextInvokeIdAndPriority(); // next invoke id and priority
        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_CID] = (byte) (classId >> 8);
        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_CID + 1] = (byte) classId;

        for (int i = 0; i < 6; i++) {
            writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_LN + i] = LN[i];
        }
        writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_ATTR] = (byte) methodId;

        if (data == null) {
            writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_ACCESS_SELECTOR] = 0;
            return writeRequestArray;
        } else {
            writeRequestArray[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET_ACCESS_SELECTOR] = 1;
            // Concatenate 2 byte arrays into requestData.
            byte[] requestData = new byte[writeRequestArray.length + data.length];
            for (int i = 0; i < DLMSCOSEMGlobals.ACTIONREQUEST_DATA_SIZE; i++) {
                requestData[i] = writeRequestArray[i];
            }
            for (int i = DLMSCOSEMGlobals.ACTIONREQUEST_DATA_SIZE; i < requestData.length; i++) {
                requestData[i] = data[i - (DLMSCOSEMGlobals.ACTIONREQUEST_DATA_SIZE)];
            }
            return requestData;
        }
    }

    /**
     * @param bVal
     * @throws IOException
     */
    private void evalDataAccessResult(byte bVal) throws IOException {
        if (bVal != 0) {
            throw new DataAccessResultException(bVal & 0xFF);
        }
    }

    /**
     * Sends the request to the device, validates the invoke id of the response - drops invalid responses if necessary - and returns the complete response as byte[] <br></br>
     * <b>Note: Should be used for all confirmed requests, you should use this method - DO NOT directly use the DLMSConnection, or else you bypass the validation</b>  <br></br>
     * Steps taken:
     * <ul>
     * <li>1. Send out the request on the DLMSConnection and retrieve the response (taking into account timeout and retries)</li>
     * <li>2. The response is validated to see if the invoke id of the response matches the invoke id of the request</li>
     * <li>2A. If the invoke ids do match, then the complete response byte[] is returned</li>
     * <li>2B. If the invoke ids do not match, the response is dropped. The DLSMConnection will start reading again (taking into account timeout and retries) and go back to step 2.</li>
     * </ul>
     *
     * @param request - the request, given as byte[]
     * @return the valid response frame as byte[]
     * @throws IOException
     */
    protected byte[] sendAndReceiveValidResponse(byte[] request) throws IOException {
        return sendAndReceiveValidResponse(request, false);
    }

    /**
     * Sends the request to the device, validates the invoke id of the response - drops invalid responses if necessary - and returns the complete response as byte[] <br></br>
     * <b>Note: Should be used for all confirmed requests, you should use this method - DO NOT directly use the DLMSConnection, or else you bypass the validation</b>  <br></br>
     * Steps taken:
     * <ul>
     * <li>1. Send out the request on the DLMSConnection and retrieve the response (taking into account timeout and retries)</li>
     * <li>2. The response is validated to see if the invoke id of the response matches the invoke id of the request</li>
     * <li>2A. If the invoke ids do match, then the complete response byte[] is returned</li>
     * <li>2B. If the invoke ids do not match, the response is dropped. The DLSMConnection will start reading again (taking into account timeout and retries) and go back to step 2.</li>
     * </ul>
     *
     * @param request            - the request, given as byte[]
     * @param isAlreadyEncrypted Boolean indicating the request is already encrypted
     * @return the valid response frame as byte[]
     * @throws IOException
     */
    protected byte[] sendAndReceiveValidResponse(byte[] request, boolean isAlreadyEncrypted) throws IOException {
        this.nrOfInvalidResponseFrames = 0;
        byte[] responseData = getDLMSConnection().sendRequest(request);
        if (responseData == null) {
            return responseData;
        }
        Byte invokeIdAndPriorityOfResponse = extractInvokeIdFromResponse(responseData);

        while (invokeIdAndPriorityOfResponse != null && !this.invokeIdAndPriorityHandler.validateInvokeId(invokeIdAndPriorityOfResponse)) {
            nrOfInvalidResponseFrames++;
            getLogger().log(Level.WARNING, "Invoke id of response frame does not match invoke id of request - response will be ignored.");
            if (nrOfInvalidResponseFrames == MAX_NR_OF_INVOKE_ID_MISMATCH) {
                throw new IOException("Failed to retrieve a valid response - received " + MAX_NR_OF_INVOKE_ID_MISMATCH + " response frames in a row having a wrong invoke id.");
            }
            responseData = getDLMSConnection().readResponseWithRetries(request, isAlreadyEncrypted);
            invokeIdAndPriorityOfResponse = extractInvokeIdFromResponse(responseData);
        }
        return responseData;
    }

    protected void sendUnconfirmedRequest(byte[] request) throws IOException {
        getDLMSConnection().sendUnconfirmedRequest(request);
    }

    /**
     * Validates the response frame and extracts the response data from it.
     *
     * @param responseData the complete response frame in byte[]
     * @return the response data, extracted from the response frame
     * @throws IOException Thrown in case the response is not valid or contains a DLMSCOSEMGlobals.COSEM_CONFIRMEDSERVICEERROR
     */
    protected byte[] checkCosemPDUResponseHeader(byte[] responseData) throws IOException {
        int i;

        boolean boolLastBlock = true;
        int iBlockNumber;
        int iBlockSize;
        int iArrayNROfEntries;
        ReceiveBuffer receiveBuffer = new ReceiveBuffer();

        do {
            i = DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET;

            switch (responseData[i]) {
                case DLMSCOSEMGlobals.COSEM_READRESPONSE: {
                    switch (responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2]) {

                        case READRESPONSE_DATA_TAG:
                            receiveBuffer.addArray(responseData, DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3);
                            return receiveBuffer.getArray();

                        case READRESPONSE_DATAACCESSERROR_TAG:
                            evalDataAccessResult(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]);
                            receiveBuffer.addArray(responseData, DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3);
                            return receiveBuffer.getArray();

                        case READRESPONSE_DATABLOCK_RESULT_TAG: {
                            i = DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3; // to point to the block last

                            boolLastBlock = (responseData[i] != 0x00);
                            i++; // skip lastblock
                            iBlockNumber = ProtocolUtils.getInt(responseData, i, 2);
                            i += 2; // skip iBlockNumber

                            iBlockSize = (int) DLMSUtils.getAXDRLength(responseData, i);

                            i += DLMSUtils.getAXDRLengthOffset(responseData, i);

                            if (iBlockNumber == 1) {
                                i += 2; // skip the tricky read response sequence of choice and data encoding 0100
                            }

                            getLogger().log(Level.FINEST, "last block=" + boolLastBlock + ", blockNumber=" + iBlockNumber + ", blockSize=" + iBlockSize + ", offset=" + i);

                            receiveBuffer.addArray(responseData, i);

                            if (!boolLastBlock) {
                                try {
                                    getLogger().log(Level.FINEST, "Acknowledge block " + iBlockNumber);
                                    responseData = sendAndReceiveValidResponse(buildReadRequestNext(iBlockNumber));
//									debug("next response data = " + ProtocolUtils.outputHexString(responseData));
                                } catch (IOException e) {
                                    throw new NestedIOException(e, "Error in DLMSCOSEMGlobals.COSEM_GETRESPONSE_WITH_DATABLOCK");
                                }
                            } else {
                                return (receiveBuffer.getArray());
                            }

                        }
                        break; // READRESPONSE_DATABLOCK_RESULT_TAG

                    } // switch(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET+2])

                }
                break; //COSEM_READRESPONSE

                case DLMSCOSEMGlobals.COSEM_WRITERESPONSE: {
                    if (responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2] == READRESPONSE_DATAACCESSERROR_TAG) {
                        evalDataAccessResult(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]);
                    }
                    receiveBuffer.addArray(responseData, DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3);
                    return receiveBuffer.getArray();
                } // DLMSCOSEMGlobals.COSEM_WRITERESPONSE

                case DLMSCOSEMGlobals.COSEM_CONFIRMEDSERVICEERROR: {

                    switch (responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 1]) {
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_INITIATEERROR_TAG: {
                            throw new IOException("Confirmed Service Error - 'Initiate error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_GETSTATUS_TAG: {
                            throw new IOException("Confirmed Sercie Error - 'GetStatus error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_GETNAMELIST_TAG: {
                            throw new IOException("Confirmed Service Error - 'GetNameList error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_GETVARIABLEATTRIBUTE_TAG: {
                            throw new IOException("Confirmed Service Error - 'GetVariableAttribute error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_READ_TAG: {
                            throw new IOException("Confirmed Service Error - 'Read error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_WRITE_TAG: {
                            throw new IOException("Confirmed Service Error - 'Write error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_GETDATASETATTRIBUTE_TAG: {
                            throw new IOException("Confirmed Service Error - 'GetDataSetAttribute' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_GETTIATTRIBUTE_TAG: {
                            throw new IOException("Confirmed Service Error - 'GetTIAttribute error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_CHANGESCOPE_TAG: {
                            throw new IOException("Confirmed Service Error - 'ChangeScope error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_START_TAG: {
                            throw new IOException("Confirmed Service Error - 'Start error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_RESUME_TAG: {
                            throw new IOException("Confirmed Service Error - 'Resume error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_MAKEUSABLE_TAG: {
                            throw new IOException("Confirmed Service Error - 'MakeUsable error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_INITIATELOAD_TAG: {
                            throw new IOException("Confirmed Service Error - 'InitiateLoad error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_LOADSEGMENT_TAG: {
                            throw new IOException("Confirmed Service Error - 'LoadSegment error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_TERMINATELOAD_TAG: {
                            throw new IOException("Confirmed Service Error - 'TerminateLoad error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_INITIATEUPLOAD_TAG: {
                            throw new IOException("Confirmed Service Error - 'InitiateUpload error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_UPLOADSEGMENT_TAG: {
                            throw new IOException("Confirmed Service Error - 'UploadSegment error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        case DLMSCOSEMGlobals.CONFIRMEDSERVICEERROR_TERMINATEUPLOAD_TAG: {
                            throw new IOException("Confirmed Service Error - 'TerminateUpload error' - Reason: "
                                    + getServiceError(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2], responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]));
                        }
                        default: {
                            throw new IOException("Unknown service error, " + responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 1] + responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2]
                                    + responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3]);
                        }
                    }
                } // !!! break !!! DLMSCOSEMGlobals.COSEM_CONFIRMEDSERVICEERROR

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
                                    throw new IOException("unknown DLMSCOSEMGlobals.COSEM_GETRESPONSE_NORMAL,  " + responseData[i]);

                            } // switch(responseData[i])

                        }
                        break; // case DLMSCOSEMGlobals.COSEM_GETRESPONSE_NORMAL:

                        case DLMSCOSEMGlobals.COSEM_GETRESPONSE_WITH_DATABLOCK: {
                            i++; // skip tag
                            i++; // skip invoke id & priority

                            boolLastBlock = (responseData[i] != 0x00);
                            i++; // skip lastblock
                            iBlockNumber = ProtocolUtils.getInt(responseData, i);
                            i += 4; // skip iBlockNumber
                            switch (responseData[i]) {
                                case 0: // data
                                {
                                    i++; // skip tag

                                    if (iBlockNumber == 0) {
                                        iBlockSize = (int) DLMSUtils.getAXDRLength(responseData, i);
                                        i += DLMSUtils.getAXDRLengthOffset(responseData, i);
                                        receiveBuffer.addArray(responseData, i);
                                        i++; /// skip array tag
                                        iArrayNROfEntries = (int) DLMSUtils.getAXDRLength(responseData, i);
                                        i += DLMSUtils.getAXDRLengthOffset(responseData, i);
                                    } else {
                                        iBlockSize = (int) DLMSUtils.getAXDRLength(responseData, i);
                                        i += DLMSUtils.getAXDRLengthOffset(responseData, i);
                                        receiveBuffer.addArray(responseData, i);
                                    }

                                    if (!(boolLastBlock)) {
                                        try {
                                            responseData = sendAndReceiveValidResponse(buildGetRequestNext(iBlockNumber));
                                        } catch (IOException e) {
                                            throw new NestedIOException(e, "Error in DLMSCOSEMGlobals.COSEM_GETRESPONSE_WITH_DATABLOCK");
                                        }
                                    } else {
                                        return (receiveBuffer.getArray());
                                    }

                                }
                                break; // data

                                case 1: // data-access-result
                                {
                                    i++;
                                    evalDataAccessResult(responseData[i]);
                                    //debug("Data access result OK");

                                }
                                break; // data-access-result

                                default:
                                    throw new IOException("unknown DLMSCOSEMGlobals.COSEM_GETRESPONSE_WITH_DATABLOCK,  " + responseData[i]);
                            }

                        }
                        break; // case DLMSCOSEMGlobals.COSEM_GETRESPONSE_WITH_DATABLOCK:

                        case DLMSCOSEMGlobals.COSEM_GETRESPONSE_WITH_LIST: {
                            i++; // skip tag
                            i++; // skip invoke id & priority
                            i++; // nr of items
                            receiveBuffer.addArray(responseData, i);
                            return receiveBuffer.getArray();
                        }

                        default:
                            throw new IOException("Unknown/unimplemented DLMSCOSEMGlobals.COSEM_GETRESPONSE, " + responseData[i]);

                    } // switch(responseData[i])

                }
                break; // case DLMSCOSEMGlobals.COSEM_GETRESPONSE:

                case DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE: {

                    /* Implementation as from 28/01/2010 */

                    if ("OLD".equalsIgnoreCase(this.protocolLink.getMeterConfig().getExtra())) {

                        /*
                               * This is the INcorrect implementation of an
                               * ActionResponse
                               * We use this in old KP firmwareVersion
                               */

                        i++; // skip tag
                        switch (responseData[i]) {
                            case DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE_NORMAL: {
                                i++; // skip tag
                                i++; // skip invoke id & priority
                                //                            evalDataAccessResult(responseData[i]);
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
                                        throw new IOException("unknown DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE_NORMAL,  " + responseData[i]);

                                } // switch(responseData[i])
                            }
                            break; // case DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE_NORMAL:

                            default:
                                throw new IOException("Unknown/unimplemented DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE, " + responseData[i]);

                        } // switch(responseData[i])

                    } else {

                        /*
                               * This is the correct implementation of an
                               * ActionResponse
                               */

                        i++; // skipping the tag
                        switch (responseData[i]) { // ACTION-Response ::= choice

                            case DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE_NORMAL: {
                                i++; // skip tag [1]
                                i++; // Skip InvokeIdAndPriority

                                /*
                                         * Following is the
                                         * ActionResponseWithOptionalData
                                         */
                                evalDataAccessResult(responseData[i]);
                                i++; // skip the Action-Result if it was OK
                                if ((i < responseData.length) && (responseData[i] == 1)) { // Optional Get-Data-Result
                                    i++; // skip the tag [1]
                                    if (responseData[i] == 0) { // Data
                                        i++; // skip the tag [1]
                                        receiveBuffer.addArray(responseData, i);
                                        return receiveBuffer.getArray();
                                    } else if (responseData[i] == 1) { // Data-Access-Result
                                        i++; // skip the tag [1]
                                        evalDataAccessResult(responseData[i]);
                                        return receiveBuffer.getArray();
                                    }
                                } else {
                                    return receiveBuffer.getArray();
                                }

                            }
                            break;

                            case DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE_WITH_PBLOCK: {
                                throw new IOException("Unimplemented DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE, " + responseData[i]);
                            }

                            case DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE_WITH_LIST: {
                                throw new IOException("Unimplemented DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE, " + responseData[i]);
                            }

                            case DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE_NEXT_PBLOCK: {
                                throw new IOException("Unimplemented DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE, " + responseData[i]);
                            }
                            default:
                                throw new IOException("Unimplemented DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE, " + responseData[i]);
                        }
                    }
                }
                break; // case DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE:

                case DLMSCOSEMGlobals.COSEM_SETRESPONSE: {
                    i++; // skip tag
                    switch (responseData[i]) {
                        case DLMSCOSEMGlobals.COSEM_SETRESPONSE_NORMAL: {
                            i++; // skip DLMSCOSEMGlobals.COSEM_SETRESPONSE_NORMAL tag
                            i++; // skip invoke id & priority
                            evalDataAccessResult(responseData[i]);
                            receiveBuffer.addArray(responseData, i + 1);
                            return receiveBuffer.getArray();
                        }
                        case DLMSCOSEMGlobals.COSEM_SETRESPONSE_FOR_DATABLOCK: {
                            i++; // skip DLMSCOSEMGlobals.COSEM_SETRESPONSE_FOR_DATABLOCK tag
                            i++; // skip invoke id & priority
                            receiveBuffer.addArray(responseData, i);
                            return receiveBuffer.getArray();
                        }
                        case DLMSCOSEMGlobals.COSEM_SETRESPONSE_FOR_LAST_DATABLOCK: {
                            i++; // skip DLMSCOSEMGlobals.COSEM_SETRESPONSE_FOR_DATABLOCK tag
                            i++; // skip invoke id & priority
                            evalDataAccessResult(responseData[i]);
                            receiveBuffer.addArray(responseData, i + 1);
                            return receiveBuffer.getArray();
                        }
                        default: {
                            throw new IOException("Unknown/unimplemented DLMSCOSEMGlobals.COSEM_SETRESPONSE, " + responseData[i]);
                        }
                    } // switch(responseData[i])
                }

                case DLMSCOSEMGlobals.COSEM_EXCEPTION_RESPONSE: {
                    throw new ExceptionResponseException(responseData[i + 1], responseData[i + 2]);
                }

                default: {
                    throw new IOException("Unknown COSEM PDU, " + " 0x" + Integer.toHexString(ProtocolUtils.byte2int(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET])) + " 0x"
                            + Integer.toHexString(ProtocolUtils.byte2int(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 1])) + " 0x"
                            + Integer.toHexString(ProtocolUtils.byte2int(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 2])) + " 0x"
                            + Integer.toHexString(ProtocolUtils.byte2int(responseData[DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET + 3])));
                } // !!! break !!! default

            } // switch(responseData[i])

        } while (!(boolLastBlock));

        return null;

    }

    /**
     * Extract the invoke id and priority byte from the given response.
     * If the response doesn't contain the invoke id and priority byte, null will be returned.
     *
     * @param responseData the complete response frame in byte[]
     * @return the invoke id and priority byte
     */
    protected Byte extractInvokeIdFromResponse(byte[] responseData) {
        if (responseData == null) {
            return null;
        }
        int i = DLMSCOSEMGlobals.DL_COSEMPDU_OFFSET;

        switch (responseData[i]) {
            case DLMSCOSEMGlobals.COSEM_GETRESPONSE: {
                i++; // skip COSEM_GETRESPONSE tag
                i++; // skip COSEM_GETRESPONSE_NORMAL/COSEM_GETRESPONSE_WITH_DATABLOCK/COSEM_GETRESPONSE_WITH_LIST tag
                return responseData[i];
            }

            case DLMSCOSEMGlobals.COSEM_ACTIONRESPONSE: {
                i++; // skip COSEM_ACTIONRESPONSE tag
                i++; // skip COSEM_ACTIONRESPONSE_NORMAL tag
                return responseData[i];
            }

            case DLMSCOSEMGlobals.COSEM_SETRESPONSE: {
                i++; // skip COSEM_SETRESPONSE tag
                i++; // skip COSEM_SETRESPONSE_NORMAL/COSEM_SETRESPONSE_FOR_DATABLOCK/COSEM_SETRESPONSE_FOR_LAST_DATABLOCK tag
                return responseData[i];
            }
            default: {
                // In all other cases, the response does not include the invoke id and priority byte, so we return 0
                return null;
            }
        }
    }

    /**
     * @param b
     * @param c
     * @return
     */
    private String getServiceError(byte b, byte c) {
        switch (b) {
            case 0: { // Application-reference
                switch (c) {
                    case 0:
                        return "Application-reference - Other";
                    case 1:
                        return "Application-reference - Time out since request sent";
                    case 2:
                        return "Application-reference - Peer AEi not reachable";
                    case 3:
                        return "Application-reference - Addressing trouble";
                    case 4:
                        return "Application-reference - Application-context incompatibility";
                    case 5:
                        return "Application-reference - Error at the local or distant equipment";
                    case 6:
                        return "Application-reference - Error detected by the deciphering function";
                }
                break;
            }
            case 1: { // Hardware-resource
                switch (c) {
                    case 0:
                        return "Hardware-resource - Other";
                    case 1:
                        return "Hardware-resource - Memory unavailable";
                    case 2:
                        return "Hardware-resource - Processor-resource unavailable";
                    case 3:
                        return "Hardware-resource - Mass-storage unavailable";
                    case 4:
                        return "Hardware-resource - Other resource unavailable";
                }
                break;
            }
            case 2: { // VDE-State-error
                switch (c) {
                    case 0:
                        return "VDE-State-error - Other";
                    case 1:
                        return "VDE-State-error - No DLMS context";
                    case 2:
                        return "VDE-State-error - Loading data-set";
                    case 3:
                        return "VDE-State-error - Status nochange";
                    case 4:
                        return "VDE-State-error - Status inoperable";
                }
                break;
            }
            case 3: { // Service
                switch (c) {
                    case 0:
                        return "Service - Other";
                    case 1:
                        return "Service - PDU size to long";
                    case 2:
                        return "Service - Service unsupported";
                }
                break;
            }
            case 4: { // Definition
                switch (c) {
                    case 0:
                        return "Definition - Other";
                    case 1:
                        return "Definition - Object undefined";
                    case 2:
                        return "Definition - Object class inconsistent";
                    case 3:
                        return "Definition - Object attribute inconsistent";
                }
                break;
            }
            case 5: { // Access
                switch (c) {
                    case 0:
                        return "Access - Other";
                    case 1:
                        return "Access - Scope of access violated";
                    case 2:
                        return "Access - Object access violated";
                    case 3:
                        return "Access - Hardware-fault";
                    case 4:
                        return "Access - Object unavailable";
                }
                break;
            }
            case 6: { // Initiate
                switch (c) {
                    case 0:
                        return "Initiate - Other";
                    case 1:
                        return "Initiate - DLMS version too low";
                    case 2:
                        return "Initiate - Incompatible conformance";
                    case 3:
                        return "Initiate - PDU size too short";
                    case 4:
                        return "Initiate - Refused by the VDE Handler";
                }
                break;
            }
            case 7: { // Load-Data-Set
                switch (c) {
                    case 0:
                        return "Load-Data-Set - Other";
                    case 1:
                        return "Load-Data-Set - Primitive out of sequence";
                    case 2:
                        return "Load-Data-Set - Not loadable";
                    case 3:
                        return "Load-Data-Set - Evaluated data set size too large";
                    case 4:
                        return "Load-Data-Set - Proposed segment not awaited";
                    case 5:
                        return "Load-Data-Set - Segment interpretation error";
                    case 6:
                        return "Load-Data-Set - Segment storage error";
                    case 7:
                        return "Load-Data-Set - Data set not in correct state for uploading";
                }
                break;
            }
            case 8: { // Change scope
                return "Change Scope";
            }
            case 9: { // Task
                switch (c) {
                    case 0:
                        return "Task - Other";
                    case 1:
                        return "Task - Remote control parameter set to FALSE";
                    case 2:
                        return "Task - TI in stopped state";
                    case 3:
                        return "Task - TI in running state";
                    case 4:
                        return "Task - TI in unusable state";
                }
                break;
            }
            case 10: { // Other
                return "Other";
            }
            default: {
                return "Other";
            }
        }
        return "";
    }

    /**
     * @param fromCalendar
     * @param toCalendar
     * @return
     */
    private byte[] getBufferRangeDescriptor(Calendar fromCalendar, Calendar toCalendar) {
        if ((toCalendar == null) && this.protocolLink.getMeterConfig().isSLB()) {
            return getBufferRangeDescriptorSL7000(fromCalendar);
        } else if (this.protocolLink.getMeterConfig().isActarisPLCC()) {
            return getBufferRangeDescriptorActarisPLCC(fromCalendar, toCalendar);
        } else {
            return getBufferRangeDescriptorDefault(fromCalendar, toCalendar);
        }

    }

    private byte[] getBufferRangeDescriptor(long fromCalendar, long toCalendar) {
        return getBufferRangeDescriptorDefault(fromCalendar, toCalendar);
    }

    private byte[] getBufferRangeDescriptor(Calendar fromCalendar, Calendar toCalendar, List<CapturedObject> channels) {
        if ((toCalendar == null) && this.protocolLink.getMeterConfig().isSLB()) {
            return getBufferRangeDescriptorSL7000(fromCalendar);
        } else if (this.protocolLink.getMeterConfig().isActarisPLCC()) {
            return getBufferRangeDescriptorActarisPLCC(fromCalendar, toCalendar);
        } else {
            return getBufferRangeDescriptorDefault(fromCalendar, toCalendar, channels);
        }
    }

    private byte[] getBufferEntryDescriptor(int fromEntry, int toEntry, int fromValue, int toValue) {
        return getBufferEntryDescriptorDefault(fromEntry, toEntry, fromValue, toValue);
    }

    /**
     * @param fromCalendar
     * @param toCalendar
     * @return
     */
    private byte[] getBufferRangeDescriptorActarisPLCC(Calendar fromCalendar, Calendar toCalendar) {

        byte[] intreq = {
                (byte) 0x01, // range descriptor
                (byte) 0x02, // structure
                (byte) 0x04, // 4 items in structure
                // capture object definition
                (byte) 0x0F,
                (byte) 0x00,
                // from value
                (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xD2, (byte) 0x05, (byte) 23, (byte) 0xFF, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xFF,
                (byte) 0x80, (byte) 0x00, (byte) 0x00,
                // to value
                (byte) 0x09, (byte) 0x0C, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xFF, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xFF,
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                // selected values
        }; //(byte)0x00};

        final int CAPTURE_FROM_OFFSET = 5; // was 4
        final int CAPTURE_TO_OFFSET = 19; // was 18

        intreq[CAPTURE_FROM_OFFSET] = AxdrType.OCTET_STRING.getTag();
        intreq[CAPTURE_FROM_OFFSET + 1] = 12; // length
        intreq[CAPTURE_FROM_OFFSET + 2] = (byte) (fromCalendar.get(Calendar.YEAR) >> 8);
        intreq[CAPTURE_FROM_OFFSET + 3] = (byte) fromCalendar.get(Calendar.YEAR);
        intreq[CAPTURE_FROM_OFFSET + 4] = (byte) (fromCalendar.get(Calendar.MONTH) + 1);
        intreq[CAPTURE_FROM_OFFSET + 5] = (byte) fromCalendar.get(Calendar.DAY_OF_MONTH);
        intreq[CAPTURE_FROM_OFFSET + 6] = (byte) 0xff;
        intreq[CAPTURE_FROM_OFFSET + 7] = (byte) fromCalendar.get(Calendar.HOUR_OF_DAY);
        intreq[CAPTURE_FROM_OFFSET + 8] = (byte) fromCalendar.get(Calendar.MINUTE);
        intreq[CAPTURE_FROM_OFFSET + 9] = (byte) 0x00;
        intreq[CAPTURE_FROM_OFFSET + 10] = (byte) 0xFF;
        intreq[CAPTURE_FROM_OFFSET + 11] = (byte) 0x80;
        intreq[CAPTURE_FROM_OFFSET + 12] = 0x00;
        intreq[CAPTURE_FROM_OFFSET + 13] = 0x00;

        intreq[CAPTURE_TO_OFFSET] = AxdrType.OCTET_STRING.getTag();
        intreq[CAPTURE_TO_OFFSET + 1] = 12; // length
        intreq[CAPTURE_TO_OFFSET + 2] = toCalendar != null ? (byte) (toCalendar.get(Calendar.YEAR) >> 8) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 3] = toCalendar != null ? (byte) (toCalendar.get(Calendar.YEAR)) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 4] = toCalendar != null ? (byte) (toCalendar.get(Calendar.MONTH) + 1) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 5] = toCalendar != null ? (byte) (toCalendar.get(Calendar.DAY_OF_MONTH)) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 6] = (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 7] = toCalendar != null ? (byte) toCalendar.get(Calendar.HOUR_OF_DAY) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 8] = toCalendar != null ? (byte) toCalendar.get(Calendar.MINUTE) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 9] = (byte) 0x00;
        intreq[CAPTURE_TO_OFFSET + 10] = (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 11] = (byte) 0x80;
        intreq[CAPTURE_TO_OFFSET + 12] = (byte) 0x00;
        intreq[CAPTURE_TO_OFFSET + 13] = (byte) 0x00;

        return intreq;

    }

    /**
     * @param fromCalendar
     * @return
     */
    private byte[] getBufferRangeDescriptorSL7000(Calendar fromCalendar) {

        byte[] intreq = {
                (byte) 0x01, // range descriptor
                (byte) 0x02, // structure
                (byte) 0x04, // 4 items in structure
                // capture object definition
                (byte) 0x00,
                // from value
                (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xD2, (byte) 0x05, (byte) 23, (byte) 0xFF, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xFF,
                (byte) 0x80, (byte) 0x00, (byte) 0x00,
                // to value
                (byte) 0x09, (byte) 0x0C, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xFF, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xFF,
                (byte) 0xff, (byte) 0xff, (byte) 0xff,
                // selected values
                (byte) 0x00};

        final int CAPTURE_FROM_OFFSET = 4;
        final int CAPTURE_TO_OFFSET = 18;

        intreq[CAPTURE_FROM_OFFSET] = AxdrType.OCTET_STRING.getTag();
        intreq[CAPTURE_FROM_OFFSET + 1] = 12; // length
        intreq[CAPTURE_FROM_OFFSET + 2] = (byte) (fromCalendar.get(Calendar.YEAR) >> 8);
        intreq[CAPTURE_FROM_OFFSET + 3] = (byte) fromCalendar.get(Calendar.YEAR);
        intreq[CAPTURE_FROM_OFFSET + 4] = (byte) (fromCalendar.get(Calendar.MONTH) + 1);
        intreq[CAPTURE_FROM_OFFSET + 5] = (byte) fromCalendar.get(Calendar.DAY_OF_MONTH);
        intreq[CAPTURE_FROM_OFFSET + 6] = (byte) 0xff;
        intreq[CAPTURE_FROM_OFFSET + 7] = (byte) 0xff; //fromCalendar.get(Calendar.HOUR_OF_DAY);
        intreq[CAPTURE_FROM_OFFSET + 8] = (byte) 0xff; //fromCalendar.get(Calendar.MINUTE);
        intreq[CAPTURE_FROM_OFFSET + 9] = (byte) 0xFF;
        intreq[CAPTURE_FROM_OFFSET + 10] = (byte) 0xFF;
        intreq[CAPTURE_FROM_OFFSET + 11] = (byte) 0x80;
        intreq[CAPTURE_FROM_OFFSET + 12] = 0x00;
        intreq[CAPTURE_FROM_OFFSET + 13] = 0x00;

        intreq[CAPTURE_TO_OFFSET] = AxdrType.OCTET_STRING.getTag();
        intreq[CAPTURE_TO_OFFSET + 1] = 12; // length
        intreq[CAPTURE_TO_OFFSET + 2] = (byte) 0xff; //(toCalendar.get(Calendar.YEAR) >> 8);
        intreq[CAPTURE_TO_OFFSET + 3] = (byte) 0xff; //toCalendar.get(Calendar.YEAR);
        intreq[CAPTURE_TO_OFFSET + 4] = (byte) 0xff; //(toCalendar.get(Calendar.MONTH)+1);
        intreq[CAPTURE_TO_OFFSET + 5] = (byte) 0xff; //toCalendar.get(Calendar.DAY_OF_MONTH);
        intreq[CAPTURE_TO_OFFSET + 6] = (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 7] = (byte) 0xff; //toCalendar.get(Calendar.HOUR_OF_DAY);
        intreq[CAPTURE_TO_OFFSET + 8] = (byte) 0xff; //toCalendar.get(Calendar.MINUTE);
        intreq[CAPTURE_TO_OFFSET + 9] = (byte) 0xff; //0x00;
        intreq[CAPTURE_TO_OFFSET + 10] = (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 11] = (byte) 0xff; //0x80;
        intreq[CAPTURE_TO_OFFSET + 12] = (byte) 0xff; //0x00;
        intreq[CAPTURE_TO_OFFSET + 13] = (byte) 0xff; //0x00;

        return intreq;
    }

    /**
     * @param fromCalendar
     * @param toCalendar
     * @return
     */
    private byte[] getBufferRangeDescriptorDefault(Calendar fromCalendar, Calendar toCalendar) {

        byte[] intreq = {
                (byte) 0x01, // range descriptor
                (byte) 0x02, // structure
                (byte) 0x04, // 4 items in structure
                // capture object definition
                (byte) 0x02, (byte) 0x04, (byte) 0x12, (byte) 0x00, (byte) 0x08, (byte) 0x09, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
                (byte) 0x00, (byte) 0xFF, (byte) 0x0F, (byte) 0x02, (byte) 0x12, (byte) 0x00,
                (byte) 0x00,
                // from value
                (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xD2, (byte) 0x05, (byte) 23, (byte) 0xFF, (byte) 11, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
                (byte) 0x80, (byte) 0x00, (byte) 0x00,
                // to value
                (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xD2, (byte) 0x05, (byte) 23, (byte) 0xFF, (byte) 13, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
                (byte) 0x80, (byte) 0x00, (byte) 0x00,
                // selected values
                (byte) 0x01, (byte) 0x00};

        int CAPTURE_FROM_OFFSET = 21;
        int CAPTURE_TO_OFFSET = 35;

        intreq[CAPTURE_FROM_OFFSET] = AxdrType.OCTET_STRING.getTag();
        intreq[CAPTURE_FROM_OFFSET + 1] = 12; // length
        intreq[CAPTURE_FROM_OFFSET + 2] = (byte) (fromCalendar.get(Calendar.YEAR) >> 8);
        intreq[CAPTURE_FROM_OFFSET + 3] = (byte) fromCalendar.get(Calendar.YEAR);
        intreq[CAPTURE_FROM_OFFSET + 4] = (byte) (fromCalendar.get(Calendar.MONTH) + 1);
        intreq[CAPTURE_FROM_OFFSET + 5] = (byte) fromCalendar.get(Calendar.DAY_OF_MONTH);
        //             bDOW = (byte)fromCalendar.get(Calendar.DAY_OF_WEEK);
        //             intreq[CAPTURE_FROM_OFFSET+6]=bDOW--==1?(byte)7:bDOW;
        intreq[CAPTURE_FROM_OFFSET + 6] = (byte) 0xff;
        intreq[CAPTURE_FROM_OFFSET + 7] = (byte) fromCalendar.get(Calendar.HOUR_OF_DAY);
        intreq[CAPTURE_FROM_OFFSET + 8] = (byte) fromCalendar.get(Calendar.MINUTE);
        //             intreq[CAPTURE_FROM_OFFSET+9]=(byte)fromCalendar.get(Calendar.SECOND);

        if (this.protocolLink.getMeterConfig().isIskra()) {
            intreq[CAPTURE_FROM_OFFSET + 9] = 0;
        } else {
            intreq[CAPTURE_FROM_OFFSET + 9] = 0x01;
        }

        intreq[CAPTURE_FROM_OFFSET + 10] = (byte) 0xFF;
        intreq[CAPTURE_FROM_OFFSET + 11] = (byte) 0x80;
        intreq[CAPTURE_FROM_OFFSET + 12] = 0x00;

        if (this.protocolLink.getTimeZone().inDaylightTime(fromCalendar.getTime())) {
            intreq[CAPTURE_FROM_OFFSET + 13] = (byte) 0x80;
        } else {
            intreq[CAPTURE_FROM_OFFSET + 13] = 0x00;
        }

        intreq[CAPTURE_TO_OFFSET] = AxdrType.OCTET_STRING.getTag();
        intreq[CAPTURE_TO_OFFSET + 1] = 12; // length
        intreq[CAPTURE_TO_OFFSET + 2] = toCalendar != null ? (byte) (toCalendar.get(Calendar.YEAR) >> 8) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 3] = toCalendar != null ? (byte) toCalendar.get(Calendar.YEAR) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 4] = toCalendar != null ? (byte) (toCalendar.get(Calendar.MONTH) + 1) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 5] = toCalendar != null ? (byte) toCalendar.get(Calendar.DAY_OF_MONTH) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 6] = (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 7] = toCalendar != null ? (byte) toCalendar.get(Calendar.HOUR_OF_DAY) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 8] = toCalendar != null ? (byte) toCalendar.get(Calendar.MINUTE) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 9] = 0x00;
        intreq[CAPTURE_TO_OFFSET + 10] = (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 11] = (byte) 0x80;
        intreq[CAPTURE_TO_OFFSET + 12] = 0x00;

        if ((toCalendar != null) && this.protocolLink.getTimeZone().inDaylightTime(toCalendar.getTime())) {
            intreq[CAPTURE_TO_OFFSET + 13] = (byte) 0x80;
        } else {
            intreq[CAPTURE_TO_OFFSET + 13] = 0x00;
        }

        return intreq;
    }

    private byte[] getBufferRangeDescriptorDefault(long fromCalendar, long toCalendar) {

        byte[] intreq = {
                (byte) 0x01, // range descriptor
                (byte) 0x02, // structure
                (byte) 0x04, // 4 items in structure
                // capture object definition
                (byte) 0x02, (byte) 0x04, (byte) 0x12, (byte) 0x00, (byte) 0x08, (byte) 0x09, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
                (byte) 0x00, (byte) 0xFF, (byte) 0x0F, (byte) 0x02, (byte) 0x12, (byte) 0x00,
                (byte) 0x00,
                // from value
                (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                // to value
                (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                // selected values
                (byte) 0x01, (byte) 0x00};

        int CAPTURE_FROM_OFFSET = 21;

        intreq[CAPTURE_FROM_OFFSET] = AxdrType.DOUBLE_LONG_UNSIGNED.getTag();
        byte[] bytesFromCal = DLMSUtils.getBytesFromInt((int) fromCalendar, 4);
        intreq[CAPTURE_FROM_OFFSET + 1] = bytesFromCal[0];
        intreq[CAPTURE_FROM_OFFSET + 2] = bytesFromCal[1];
        intreq[CAPTURE_FROM_OFFSET + 3] = bytesFromCal[2];
        intreq[CAPTURE_FROM_OFFSET + 4] = bytesFromCal[3];

        intreq[CAPTURE_FROM_OFFSET + 5] = AxdrType.DOUBLE_LONG_UNSIGNED.getTag();
        byte[] bytesToCal = DLMSUtils.getBytesFromInt((int) toCalendar, 4);
        intreq[CAPTURE_FROM_OFFSET + 6] = toCalendar != 0 ? bytesToCal[0] : (byte) 0xFF;
        intreq[CAPTURE_FROM_OFFSET + 7] = toCalendar != 0 ? bytesToCal[1] : (byte) 0xFF;
        intreq[CAPTURE_FROM_OFFSET + 8] = toCalendar != 0 ? bytesToCal[2] : (byte) 0xFF;
        intreq[CAPTURE_FROM_OFFSET + 9] = toCalendar != 0 ? bytesToCal[3] : (byte) 0xFF;

        return intreq;
    }

    private byte[] getBufferEntryDescriptorDefault(int fromEntry, int toEntry, int fromValue, int toValue) {

        byte[] intreq = {
                (byte) 0x02, // entry descriptor
                (byte) 0x02, // structure
                (byte) 0x04, // 4 items in structure


                // from_entry - double-long-unsigned --> first entry to retrieve
                (byte) 0xFF, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,

                //to_entry - double-long-unsigned --> last entry to retrieve
                (byte) 0xFF, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,

                //from_selected_value - long-unsigned --> index of first value to retrieve
                (byte) 0xFF, (byte) 0x00, (byte) 0x01,

                //to_selected_value - long-unsigned --> index of last value to retrieve
                (byte) 0xFF, (byte) 0x00, (byte) 0x01,
        };

        int FROM_ENTRY_OFFSET = 3;

        intreq[FROM_ENTRY_OFFSET] = AxdrType.DOUBLE_LONG_UNSIGNED.getTag();
        byte[] bytesFromEntry = DLMSUtils.getBytesFromInt(fromEntry, 4);
        intreq[FROM_ENTRY_OFFSET + 1] = bytesFromEntry[0];
        intreq[FROM_ENTRY_OFFSET + 2] = bytesFromEntry[1];
        intreq[FROM_ENTRY_OFFSET + 3] = bytesFromEntry[2];
        intreq[FROM_ENTRY_OFFSET + 4] = bytesFromEntry[3];

        intreq[FROM_ENTRY_OFFSET + 5] = AxdrType.DOUBLE_LONG_UNSIGNED.getTag();
        byte[] bytesToEntry = DLMSUtils.getBytesFromInt(toEntry, 4);
        intreq[FROM_ENTRY_OFFSET + 6] = bytesToEntry[0];
        intreq[FROM_ENTRY_OFFSET + 7] = bytesToEntry[1];
        intreq[FROM_ENTRY_OFFSET + 8] = bytesToEntry[2];
        intreq[FROM_ENTRY_OFFSET + 9] = bytesToEntry[3];

        intreq[FROM_ENTRY_OFFSET + 10] = AxdrType.LONG_UNSIGNED.getTag();
        byte[] bytesFromSelectedValue = DLMSUtils.getBytesFromInt(fromValue, 2);
        intreq[FROM_ENTRY_OFFSET + 11] = bytesFromSelectedValue[0];
        intreq[FROM_ENTRY_OFFSET + 12] = bytesFromSelectedValue[1];

        intreq[FROM_ENTRY_OFFSET + 13] = AxdrType.LONG_UNSIGNED.getTag();
        byte[] bytesToSelectedValue = DLMSUtils.getBytesFromInt(toValue, 2);
        intreq[FROM_ENTRY_OFFSET + 14] = bytesToSelectedValue[0];
        intreq[FROM_ENTRY_OFFSET + 15] = bytesToSelectedValue[1];

        return intreq;
    }

    private byte[] getBufferRangeDescriptorDefault(Calendar fromCalendar, Calendar toCalendar, List<CapturedObject> channels) {

        byte[] selectedValues = new byte[]{(byte) 0x01, (byte) 0x00};        //Default is empty array, fetching all channels
        if (channels != null && channels.size() != 0) {
            Array array = new Array();
            for (CapturedObject channel : channels) {
                Structure structure = new Structure();
                structure.addDataType(new Unsigned16(channel.getClassId()));
                structure.addDataType(OctetString.fromByteArray(channel.getLogicalName().getObisCode().getLN()));
                structure.addDataType(new Integer8(channel.getAttributeIndex()));
                structure.addDataType(new Unsigned16(channel.getDataIndex()));
                array.addDataType(structure);
            }
            selectedValues = array.getBEREncodedByteArray();
        }

        byte[] intreq = {
                (byte) 0x01, // range descriptor
                (byte) 0x02, // structure
                (byte) 0x04, // 4 items in structure
                // capture object definition
                (byte) 0x02, (byte) 0x04, (byte) 0x12, (byte) 0x00, (byte) 0x08, (byte) 0x09, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
                (byte) 0x00, (byte) 0xFF, (byte) 0x0F, (byte) 0x02, (byte) 0x12, (byte) 0x00,
                (byte) 0x00,
                // from value
                (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xD2, (byte) 0x05, (byte) 23, (byte) 0xFF, (byte) 11, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
                (byte) 0x80, (byte) 0x00, (byte) 0x00,
                // to value
                (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xD2, (byte) 0x05, (byte) 23, (byte) 0xFF, (byte) 13, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
                (byte) 0x80, (byte) 0x00, (byte) 0x00,
        };

        // selected values
        intreq = DLMSUtils.concatByteArrays(intreq, selectedValues);

        int CAPTURE_FROM_OFFSET = 21;
        int CAPTURE_TO_OFFSET = 35;

        intreq[CAPTURE_FROM_OFFSET] = AxdrType.OCTET_STRING.getTag();
        intreq[CAPTURE_FROM_OFFSET + 1] = 12; // length
        intreq[CAPTURE_FROM_OFFSET + 2] = (byte) (fromCalendar.get(Calendar.YEAR) >> 8);
        intreq[CAPTURE_FROM_OFFSET + 3] = (byte) fromCalendar.get(Calendar.YEAR);
        intreq[CAPTURE_FROM_OFFSET + 4] = (byte) (fromCalendar.get(Calendar.MONTH) + 1);
        intreq[CAPTURE_FROM_OFFSET + 5] = (byte) fromCalendar.get(Calendar.DAY_OF_MONTH);
        //             bDOW = (byte)fromCalendar.get(Calendar.DAY_OF_WEEK);
        //             intreq[CAPTURE_FROM_OFFSET+6]=bDOW--==1?(byte)7:bDOW;
        intreq[CAPTURE_FROM_OFFSET + 6] = (byte) 0xff;
        intreq[CAPTURE_FROM_OFFSET + 7] = (byte) fromCalendar.get(Calendar.HOUR_OF_DAY);
        intreq[CAPTURE_FROM_OFFSET + 8] = (byte) fromCalendar.get(Calendar.MINUTE);
        //             intreq[CAPTURE_FROM_OFFSET+9]=(byte)fromCalendar.get(Calendar.SECOND);

        if (this.protocolLink.getMeterConfig().isIskra()) {
            intreq[CAPTURE_FROM_OFFSET + 9] = 0;
        } else {
            intreq[CAPTURE_FROM_OFFSET + 9] = 0x01;
        }

        intreq[CAPTURE_FROM_OFFSET + 10] = (byte) 0xFF;
        intreq[CAPTURE_FROM_OFFSET + 11] = (byte) 0x80;
        intreq[CAPTURE_FROM_OFFSET + 12] = 0x00;

        if (this.protocolLink.getTimeZone().inDaylightTime(fromCalendar.getTime())) {
            intreq[CAPTURE_FROM_OFFSET + 13] = (byte) 0x80;
        } else {
            intreq[CAPTURE_FROM_OFFSET + 13] = 0x00;
        }

        intreq[CAPTURE_TO_OFFSET] = AxdrType.OCTET_STRING.getTag();
        intreq[CAPTURE_TO_OFFSET + 1] = 12; // length
        intreq[CAPTURE_TO_OFFSET + 2] = toCalendar != null ? (byte) (toCalendar.get(Calendar.YEAR) >> 8) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 3] = toCalendar != null ? (byte) toCalendar.get(Calendar.YEAR) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 4] = toCalendar != null ? (byte) (toCalendar.get(Calendar.MONTH) + 1) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 5] = toCalendar != null ? (byte) toCalendar.get(Calendar.DAY_OF_MONTH) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 6] = (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 7] = toCalendar != null ? (byte) toCalendar.get(Calendar.HOUR_OF_DAY) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 8] = toCalendar != null ? (byte) toCalendar.get(Calendar.MINUTE) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 9] = 0x00;
        intreq[CAPTURE_TO_OFFSET + 10] = (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 11] = (byte) 0x80;
        intreq[CAPTURE_TO_OFFSET + 12] = 0x00;

        if ((toCalendar != null) && this.protocolLink.getTimeZone().inDaylightTime(toCalendar.getTime())) {
            intreq[CAPTURE_TO_OFFSET + 13] = (byte) 0x80;
        } else {
            intreq[CAPTURE_TO_OFFSET + 13] = 0x00;
        }

        return intreq;
    }

    /**
     * @param responseData
     * @return
     * @throws IOException
     */
    protected UniversalObject[] data2UOL(byte[] responseData) throws IOException {
        long lNrOfItemsInArray = 0;
        int itemInArray;
        byte bOffset = 0;
        short sBaseName, sClassID;
        byte A, B, C, D, E, F;
        int t = 0, iFieldIndex;
        UniversalObject[] universalObject = null;
        int level = 0;
//		debug("KV_DEBUG> responseData=" + ProtocolUtils.outputHexString(responseData));
        List values = new ArrayList();
        try {

            if (responseData[0] == AxdrType.ARRAY.getTag()) {
                if ((responseData[1] & 0x80) != 0) {
                    bOffset = (byte) (responseData[1] & (byte) 0x7F);
                    for (int i = 0; i < bOffset; i++) {
                        lNrOfItemsInArray = lNrOfItemsInArray << 8;
                        lNrOfItemsInArray |= ((long) responseData[2 + i] & 0x000000ff);
                    }
                } else {
                    lNrOfItemsInArray = (long) responseData[1] & 0x000000FF;
                }

                universalObject = new UniversalObject[(int) lNrOfItemsInArray];

                t = 2 + bOffset;
                for (itemInArray = 0; itemInArray < lNrOfItemsInArray; itemInArray++) {

                    if (responseData[t] == AxdrType.STRUCTURE.getTag()) {
                        int iNROfItems;
                        int iIndex = 0;

                        t++; // skip structure tag
                        iNROfItems = responseData[t];
                        t++; // skip nr of items in structure

                        values.clear();

                        for (iFieldIndex = 0; iFieldIndex < iNROfItems; iFieldIndex++) {
                            if ((responseData[t] == AxdrType.LONG.getTag()) || (responseData[t] == AxdrType.LONG_UNSIGNED.getTag())) {
                                t++; // skip tag
                                values.add(new Long((long) ProtocolUtils.getShort(responseData, t) & 0x0000FFFF));
                                t += 2; // skip (unsigned) long (2byte) value
                            } else if ((responseData[t] == AxdrType.OCTET_STRING.getTag()) || (responseData[t] == AxdrType.VISIBLE_STRING.getTag())) {
                                t++; // skip tag
                                int iLength = responseData[t];
                                t++; // skip length byte
                                int temp;
                                for (temp = 0; temp < iLength; temp++) {
                                    values.add(new Long((long) responseData[t + temp] & 0x000000FF));
                                }
                                t += iLength; // skip string, iLength bytes
                            } else if ((responseData[t] == AxdrType.DOUBLE_LONG_UNSIGNED.getTag()) || (responseData[t] == AxdrType.DOUBLE_LONG.getTag())) {
                                t++; // skip tag
                                values.add(new Long(ProtocolUtils.getInt(responseData, t)));
                                t += 4; // skip double unsigned long (4byte) value
                            } else if ((responseData[t] == AxdrType.BOOLEAN.getTag()) || (responseData[t] == AxdrType.INTEGER.getTag()) || (responseData[t] == AxdrType.UNSIGNED.getTag())) {
                                t++; // skip tag
                                values.add(new Long((long) responseData[t] & 0x000000FF));
                                t++; // skip (1byte) value
                            }
                            // KV 29072004
                            else if (responseData[t] == AxdrType.LONG64.getTag()) {
                                t++; // skip tag
                                values.add(new Long(ProtocolUtils.getLong(responseData, t))); // KV 09/10/2006
                                t += 8; // skip double unsigned long (8byte) value
                            } else if (responseData[t] == AxdrType.STRUCTURE.getTag()) {
                                t = skipStructure(responseData, t);
                            } else {
                                throw new IOException("Error parsing objectlistdata, unknown type.");
                            }

                        } // for (iFieldIndex=0;iFieldIndex<universalObject[(int)i].lFields.length;iFieldIndex++)

                        universalObject[itemInArray] = new UniversalObject(values, this.protocolLink.getReference());

                    } // if (responseData[t] == DLMSCOSEMGlobals.TYPEDESC_STRUCTURE)  // structure
                    else {
                        throw new IOException("Error parsing objectlistdata, no structure found.");
                    }

                } // for (i=0; i<lNrOfItemsInArray;i++)

            } // if (responseData[0] == DLMSCOSEMGlobals.TYPEDESC_ARRAY)
            else {
                throw new IOException("Error parsing objectlistdata, no array found.");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("Error bad data received, index out of bounds, datalength=" + responseData.length + ", lNrOfItemsInArray="
                    + lNrOfItemsInArray + ", t=" + t + ", bOffset=" + bOffset);
        }
        return universalObject;

    }

    /**
     * @param responseData
     * @param t
     * @return
     * @throws IOException
     */
    private int skipStructure(byte[] responseData, int t) throws IOException {

        int level = 0;
        long elementsInArray = 0;
        int[] membersInStructure = new int[10]; // max structure depth = 10!!!!
        t++; //skip structure tag
        membersInStructure[level] = responseData[t];
        t++; // skip structure nr of members
        while ((level > 0) || ((level == 0) && (membersInStructure[level] > 0))) {
            if ((responseData[t] == AxdrType.LONG.getTag()) || (responseData[t] == AxdrType.LONG_UNSIGNED.getTag())) {
                t++; // skip tag
                t += 2; // skip (unsigned) long (2byte) value
                membersInStructure[level]--;
            } else if ((responseData[t] == AxdrType.OCTET_STRING.getTag()) || (responseData[t] == AxdrType.VISIBLE_STRING.getTag())) {
                t++; // skip tag
                t += (responseData[t] + 1); // skip string, iLength bytes
                membersInStructure[level]--;
            } else if ((responseData[t] == AxdrType.DOUBLE_LONG_UNSIGNED.getTag()) || (responseData[t] == AxdrType.DOUBLE_LONG.getTag())) {
                t++; // skip tag
                t += 4; // skip double unsigned long (4byte) value
                membersInStructure[level]--;
            } else if ((responseData[t] == AxdrType.BOOLEAN.getTag()) || (responseData[t] == AxdrType.INTEGER.getTag()) || (responseData[t] == AxdrType.UNSIGNED.getTag())) {
                t++; // skip tag
                t++; // skip (1byte) value
                membersInStructure[level]--;
            }
            // KV 28072004
            else if (responseData[t] == AxdrType.LONG64.getTag()) {
                t++; // skip tag
                t += 8; // skip (8byte) value
                membersInStructure[level]--;
            }
            // Skip the access rights structure in case of long name referencing...
            else if (responseData[t] == AxdrType.STRUCTURE.getTag()) {
                t++; // skip structure tag
                membersInStructure[level]--;
                level++;
                membersInStructure[level] = responseData[t];
                t++; // skip nr of members
            } else if (responseData[t] == AxdrType.ARRAY.getTag()) {
                t++; // skip array tag
                int offset = 0;
                if ((responseData[t] & 0x80) != 0) {
                    offset = (int) (responseData[t + 1] & (byte) 0x7F);
                    for (int i = 0; i < offset; i++) {
                        elementsInArray = elementsInArray << 8;
                        elementsInArray |= ((long) responseData[t + 2 + (int) i] & 0x000000ff);
                    }
                } else {
                    elementsInArray = (long) responseData[t] & 0x000000FF;
                }
                t += (offset + 1); // skip nr of elements

                membersInStructure[level]--;
                level++;
                membersInStructure[level] = (int) elementsInArray;

            } else if (responseData[t] == AxdrType.NULL.getTag()) {
                t++; // skip tag
                membersInStructure[level]--;
            }
            // KV 05042007
            else if (responseData[t] == AxdrType.ENUM.getTag()) {
                t++; // skip tag
                t++; // skip (1byte) value
                membersInStructure[level]--;
            } else {
                throw new IOException("AbstractCosemObject, skipStructure, Error parsing objectlistdata, unknown response tag " + responseData[t]);
            }

            // if all members of a structure are handled, decrement level...
            while (level > 0) {
                if (membersInStructure[level] == 0) {
                    level--;
                } else {
                    break;
                }
            }

        } // while((level>0) && (membersInStructure[level]>0))

        return t;

    }

    /**
     * @param ln
     * @param sn
     * @return
     * @throws IOException
     */
    protected ObjectReference getObjectReference(byte[] ln, int sn) throws IOException {
        if (this.protocolLink.getReference() == ProtocolLink.LN_REFERENCE) {
            return new ObjectReference(ln);
        } else if (this.protocolLink.getReference() == ProtocolLink.SN_REFERENCE) {
            return new ObjectReference(sn);
        }
        throw new IOException("AbstractCosemObject, getObjectReference, invalid reference type " + this.protocolLink.getReference());
    }

    /**
     * Getter for property objectReference.
     *
     * @return Value of property objectReference.
     */
    public com.energyict.dlms.cosem.ObjectReference getObjectReference() {
        return this.objectReference;
    }

    public void setObjectReference(ObjectReference objectReference) {
        this.objectReference = objectReference;
    }

    public ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    /**
     * Getter for the {@link DLMSConnection} of the {@link ProtocolLink} <br></br>
     * <b>WARNING:</b> Do not use this directly for sending of requests, cause you will bypass the invoke id check mechanism.<br></br>
     * Instead you should use methods #sendAndReceiveValidResponse(byte[]) and #sendUnconfirmedRequest(byte[]).
     *
     * @return the DLMSConnection
     * @see AbstractCosemObject#sendAndReceiveValidResponse(byte[])
     * @see AbstractCosemObject#sendUnconfirmedRequest(byte[])
     */
    protected DLMSConnection getDLMSConnection() {
        return this.protocolLink.getDLMSConnection();
    }

    /**
     * Get the current logger from the protocol link. If there is no logger defined,
     * create a new logger using the class name as logger name.
     *
     * @return The protocolLink logger or a new logger.
     */
    public Logger getLogger() {
        Logger logger = getProtocolLink() != null ? getProtocolLink().getLogger() : null;
        return logger != null ? logger : Logger.getLogger(getClass().getName());
    }

    public ObisCode getObisCode() {
        return getObjectReference() != null ? getObjectReference().getObisCode() : null;
    }

    public AbstractDataType getAttrbAbstractDataType(int attribute) throws IOException {
        return AXDRDecoder.decode(getLNResponseData(attribute));
    }

    public InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler() {
        return invokeIdAndPriorityHandler;
    }

    protected void setInvokeIdAndPriorityHandler(InvokeIdAndPriorityHandler invokeIdAndPriorityHandler) {
        this.invokeIdAndPriorityHandler = invokeIdAndPriorityHandler;
    }

    protected int getNrOfInvalidResponseFrames() {
        return nrOfInvalidResponseFrames;
    }
}
