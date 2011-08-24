/*
 * AbstractCosemObject.java
 * Created on 18 augustus 2004, 11:57
 */

package com.energyict.dlms.cosem;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.attributes.DLMSClassAttributes;
import com.energyict.dlms.cosem.methods.DLMSClassMethods;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
/**
 * @author Koen
 */
public abstract class AbstractCosemObject implements DLMSCOSEMGlobals {

	private static final boolean	DEBUG								= false;
    private static final boolean    WRITE_WITH_BLOCK_ENABLED            = false;

	private static final byte		READRESPONSE_DATA_TAG				= 0;
	private static final byte		READRESPONSE_DATAACCESSERROR_TAG	= 1;
	private static final byte		READRESPONSE_DATABLOCK_RESULT_TAG	= 2;

	protected ProtocolLink			protocolLink						= null;
	private ObjectReference			objectReference						= null;
	private byte					invokeIdAndPriority;

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
		if (this.protocolLink != null) {
			this.invokeIdAndPriority = this.protocolLink.getDLMSConnection().getInvokeIdAndPriority().getInvokeIdAndPriorityData();
		}
	}

	/**
	 * @return
	 */
	public byte[] getCompoundData() {
		AdaptorConnection conn = (AdaptorConnection) this.protocolLink.getDLMSConnection();
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
			byte[] responseData = null;
			if (this.objectReference.isLNReference()) {
				byte[] request = buildGetRequest(getClassId(), this.objectReference.getLn(), DLMSUtils.attrSN2LN(attribute), null);
				responseData = this.protocolLink.getDLMSConnection().sendRequest(request);
			} else if (this.objectReference.isSNReference()) {
				byte[] request = buildReadRequest((short) this.objectReference.getSn(), attribute, null);
				responseData = this.protocolLink.getDLMSConnection().sendRequest(request);
			}

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
	 * @param methodId
	 * @param data
	 * @return raw data returned from the method invocation
	 * @throws IOException
     * @deprecated use {@link #methodInvoke(com.energyict.dlms.cosem.methods.DLMSClassMethods, byte[])} instead. Should be converted to a private method.
	 */
	public byte[] invoke(int methodId, byte[] data) throws IOException {
		try {
			if (objectReference.isLNReference()) {
				byte[] responseData = null;
				byte[] request = buildActionRequest(getClassId(), this.objectReference.getLn(), methodId, data);
				responseData = this.protocolLink.getDLMSConnection().sendRequest(request);
				return checkCosemPDUResponseHeader(responseData);
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
				responseData = this.protocolLink.getDLMSConnection().sendRequest(request);
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
                    responseData = this.protocolLink.getDLMSConnection().sendRequest(request);
                }
			}

			if (this.protocolLink.getDLMSConnection() instanceof AdaptorConnection) {
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

    private byte[] sendWriteRequestWithBlockTransfer(byte[] requestToSend) throws IOException {

        // Strip the legacy bytes and the original write request tag (3 + 1 = total 4 bytes to strip)
        byte[] request = new byte[requestToSend.length - 4];
        System.arraycopy(requestToSend, 4, request, 0, request.length);

        // Some calculations that can be useful further on ...
        // WriteRequest with data block has 3 legacy + 9 header = 12 bytes extra
        final int maxBlockSize = getMaxRecPduServer() - 12;
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

            // Send the block to the device. Validate the response of the last block
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

    private byte[] sendWriteRequestBlock(byte[] dataToSend, int blockNumber, boolean lastBlock) throws IOException {
        byte[] request = new byte[3 + 9 + dataToSend.length];

        int ptr = 0;
        // As usual, add the 3 legacy bytes :)
        request[ptr++] = (byte) 0xE6; // Destination_LSAP
        request[ptr++] = (byte) 0xE6; // Source_LSAP
        request[ptr++] = 0x00;        // LLC_Quality

        // Create the WriteRequest with data blocks header (9 bytes)
        request[ptr++] = COSEM_WRITEREQUEST;
        request[ptr++] = 0x01; // One 'VariableDataSpec'
        request[ptr++] = 0x07; // Write data block access tag
        request[ptr++] = (byte) (lastBlock ? 0x01 : 0x00);  // Last block bool
        request[ptr++] = (byte) ((blockNumber>>8) & 0x0FF); // Block number HIGH
        request[ptr++] = (byte) (blockNumber & 0x0FF);      // Block number LOW
        request[ptr++] = 0x01; // One data part
        request[ptr++] = 0x09; // OctetString tag
        request[ptr++] = (byte) (dataToSend.length & 0x0FF); // Actual data length

        // Add the actual content of the write data block
        System.arraycopy(dataToSend, 0, request, ptr, dataToSend.length);

        // Send it to the device
        byte[] response = this.protocolLink.getDLMSConnection().sendRequest(request);

        // Validate the response. For the last block, the response is validated in the calling methods
        if (!lastBlock) {
            checkCosemPDUResponseHeader(response);
        }

        return response;
    }

    private int getMaxRecPduServer() {
        return getProtocolLink().getDLMSConnection().getApplicationServiceObject().getAssociationControlServiceElement().getXdlmsAse().getMaxRecPDUServerSize();
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
	 * Attribute as defined in the object docs fopr short name reference
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
		if (getObjectReference().isSNReference()) {
			return getResponseData(attribute.getShortName());
		} else {
			return getLNResponseData(attribute.getAttributeNumber());

		}
	}

    /**
     *
     * @param attributes
     * @return
     * @throws IOException
     */
    protected byte[][] getLNResponseDataWithList(DLMSAttribute[] attributes) throws IOException {
        byte[][] result = new byte[attributes.length][];
        byte[] request = buildGetWithListRequest(attributes);
        byte[] responseData = this.protocolLink.getDLMSConnection().sendRequest(request);
        responseData = checkCosemPDUResponseHeader(responseData);

        int ptr = 0;
        for (int i = 0; i < attributes.length; i++) {
            switch (responseData[ptr]) {
                case 0x00: // Data
                    AbstractDataType dataType = AXDRDecoder.decode(responseData, ptr + 1);
                    int objectLength = dataType.getBEREncodedByteArray().length;
                    result[i] = ProtocolTools.getSubArray(responseData, ptr, ptr + objectLength + 1);
                    ptr += objectLength + 1;
                    break;
                case 0x01: // Data-access-result
                    result[i] = ProtocolTools.getSubArray(responseData, ptr, ptr + 2);
                    ptr += 2;
                    break;
                default:
                    throw new IOException("Invalid state while parsing GetResponseWithList: expected '0' or '1' but was " + responseData[i]);
            }
        }

        return result;
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
     * @param method the method to invoke
     * @param encodedData   the ber-encoded additional data to write with he method
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
	 * Build up the request, send it to the device and return the checked
	 * response data as byte[]
	 *
	 * @param attribute - the DLMS attribute id
	 * @param from - the from date as {@link Calendar}
	 * @param to - the to date as {@link Calendar}
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
			responseData = this.protocolLink.getDLMSConnection().sendRequest(request);
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
	 * @param from - the from date as {@link Calendar}
	 * @param to - the to date as {@link Calendar}
	 * @param channels - a list of channels that should be read out
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
			responseData = this.protocolLink.getDLMSConnection().sendRequest(request);
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
		byte[] readRequestArray = new byte[READREQUEST_DATA_SIZE];
		readRequestArray[0] = (byte) 0xE6; // Destination_LSAP
		readRequestArray[1] = (byte) 0xE6; // Source_LSAP
		readRequestArray[2] = 0x00; // LLC_Quality
		readRequestArray[DL_COSEMPDU_OFFSET] = COSEM_READREQUEST;
		readRequestArray[DL_COSEMPDU_LENGTH_OFFSET] = 0x01; // length of the variable length SEQUENCE OF
		readRequestArray[DL_COSEMPDU_TAG_OFFSET] = 0x05; // block-number-access
		readRequestArray[READREQUEST_BLOCKNR_MSB] = (byte) (((blockNr) >> 8) & 0x00FF);
		readRequestArray[READREQUEST_BLOCKNR_LSB] = (byte) ((blockNr) & 0x00FF);
		return readRequestArray;
	}

	/**
	 * @param iObj
	 * @param iAttr
	 * @param byteSelectiveBuffer
	 * @return
	 */
	private byte[] buildReadRequest(int iObj, int iAttr, byte[] byteSelectiveBuffer) {
		byte[] readRequestArray = new byte[READREQUEST_DATA_SIZE];

		readRequestArray[0] = (byte) 0xE6; // Destination_LSAP
		readRequestArray[1] = (byte) 0xE6; // Source_LSAP
		readRequestArray[2] = 0x00; // LLC_Quality
		readRequestArray[DL_COSEMPDU_OFFSET] = COSEM_READREQUEST;
		readRequestArray[DL_COSEMPDU_LENGTH_OFFSET] = 0x01; // length of the variable length SEQUENCE OF
		if (byteSelectiveBuffer == null) {
			readRequestArray[DL_COSEMPDU_TAG_OFFSET] = 0x02; // implicit objectname
		} else {
			readRequestArray[DL_COSEMPDU_TAG_OFFSET] = 0x04; // object name integer data
		}

		readRequestArray[READREQUEST_SN_MSB] = (byte) (((iObj + iAttr) >> 8) & 0x00FF);
		readRequestArray[READREQUEST_SN_LSB] = (byte) ((iObj + iAttr) & 0x00FF);

		if (byteSelectiveBuffer != null) {
			// Concatenate 2 byte arrays into requestData.
			byte[] requestData = new byte[readRequestArray.length + byteSelectiveBuffer.length];
			for (int i = 0; i < READREQUEST_DATA_SIZE; i++) {
				requestData[i] = readRequestArray[i];
			}
			for (int i = READREQUEST_DATA_SIZE; i < requestData.length; i++) {
				requestData[i] = byteSelectiveBuffer[i - READREQUEST_DATA_SIZE];
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
		byte[] writeRequestArray = new byte[WRITEREQUEST_DATA_SIZE];

		writeRequestArray[0] = (byte) 0xE6; // Destination_LSAP
		writeRequestArray[1] = (byte) 0xE6; // Source_LSAP
		writeRequestArray[2] = 0x00; // LLC_Quality

		writeRequestArray[DL_COSEMPDU_OFFSET] = COSEM_WRITEREQUEST;
		writeRequestArray[DL_COSEMPDU_LENGTH_OFFSET] = 0x01; // length of the variable length SEQUENCE OF
		writeRequestArray[DL_COSEMPDU_TAG_OFFSET] = 0x02; // implicit objectname
		writeRequestArray[READREQUEST_SN_MSB] = (byte) (((iObj + iAttr) >> 8) & 0x00FF);
		writeRequestArray[READREQUEST_SN_LSB] = (byte) ((iObj + iAttr) & 0x00FF);
		writeRequestArray[WRITEREQUEST_NR_OF_OBJECTS] = 0x01; // one object

		if (byteSelectiveBuffer != null) {
			// Concatenate 2 byte arrays into requestData.
			byte[] requestData = new byte[writeRequestArray.length + byteSelectiveBuffer.length];
			for (int i = 0; i < WRITEREQUEST_DATA_SIZE; i++) {
				requestData[i] = writeRequestArray[i];
			}
			for (int i = WRITEREQUEST_DATA_SIZE; i < requestData.length; i++) {
				requestData[i] = byteSelectiveBuffer[i - WRITEREQUEST_DATA_SIZE];
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
		byte[] readRequestArray = new byte[GETREQUEST_DATA_SIZE];

		readRequestArray[0] = (byte) 0xE6; // Destination_LSAP
		readRequestArray[1] = (byte) 0xE6; // Source_LSAP
		readRequestArray[2] = 0x00; // LLC_Quality
		readRequestArray[DL_COSEMPDU_OFFSET] = COSEM_GETREQUEST;
		readRequestArray[DL_COSEMPDU_OFFSET + 1] = COSEM_GETREQUEST_NORMAL; // get request normal
		readRequestArray[DL_COSEMPDU_OFFSET + 2] = this.invokeIdAndPriority; //invoke id and priority
		readRequestArray[DL_COSEMPDU_OFFSET_CID] = (byte) (classId >> 8);
		readRequestArray[DL_COSEMPDU_OFFSET_CID + 1] = (byte) classId;

		for (int i = 0; i < 6; i++) {
			readRequestArray[DL_COSEMPDU_OFFSET_LN + i] = LN[i];
		}

		readRequestArray[DL_COSEMPDU_OFFSET_ATTR] = bAttr;

		if (byteSelectiveBuffer == null) {
			readRequestArray[DL_COSEMPDU_OFFSET_ACCESS_SELECTOR] = 0; // Selective access descriptor NOT present
			return readRequestArray;
		} else {
			readRequestArray[DL_COSEMPDU_OFFSET_ACCESS_SELECTOR] = 1; // Selective access descriptor present

			// Concatenate 2 byte arrays into requestData.
			byte[] requestData = new byte[readRequestArray.length + byteSelectiveBuffer.length];
			for (int i = 0; i < GETREQUEST_DATA_SIZE; i++) {
				requestData[i] = readRequestArray[i];
			}
			for (int i = GETREQUEST_DATA_SIZE; i < requestData.length; i++) {
				requestData[i] = byteSelectiveBuffer[i - (GETREQUEST_DATA_SIZE)];
			}
			return requestData;
		}

	}

    private byte[] buildGetWithListRequest(DLMSAttribute... attributes) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        buffer.write(0xE6); // Destination_LSAP
        buffer.write(0xE6); // Source_LSAP
        buffer.write(0x00);
        buffer.write(COSEM_GETREQUEST);
        buffer.write(COSEM_GETREQUEST_WITH_LIST);
        buffer.write(this.invokeIdAndPriority);
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

	/**
	 * @param iBlockNumber
	 * @return
	 */
	private byte[] buildGetRequestNext(int iBlockNumber) {
		byte[] readRequestArray = new byte[GETREQUESTNEXT_DATA_SIZE];
		readRequestArray[0] = (byte) 0xE6; // Destination_LSAP
		readRequestArray[1] = (byte) 0xE6; // Source_LSAP
		readRequestArray[2] = 0x00; // LLC_Quality
		readRequestArray[DL_COSEMPDU_OFFSET] = COSEM_GETREQUEST;
		readRequestArray[DL_COSEMPDU_OFFSET + 1] = COSEM_GETREQUEST_NEXT; // get request next
		readRequestArray[DL_COSEMPDU_OFFSET + 2] = this.invokeIdAndPriority; //invoke id and priority
		readRequestArray[DL_COSEMPDU_OFFSET + 3] = (byte) (iBlockNumber >> 24);
		readRequestArray[DL_COSEMPDU_OFFSET + 4] = (byte) (iBlockNumber >> 16);
		readRequestArray[DL_COSEMPDU_OFFSET + 5] = (byte) (iBlockNumber >> 8);
		readRequestArray[DL_COSEMPDU_OFFSET + 6] = (byte) iBlockNumber;
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
		byte[] writeRequestArray = new byte[SETREQUEST_DATA_SIZE];
		int i;

		writeRequestArray[0] = (byte) 0xE6; // Destination_LSAP
		writeRequestArray[1] = (byte) 0xE6; // Source_LSAP
		writeRequestArray[2] = 0x00; // LLC_Quality
		writeRequestArray[DL_COSEMPDU_OFFSET] = COSEM_SETREQUEST;
		writeRequestArray[DL_COSEMPDU_OFFSET + 1] = COSEM_SETREQUEST_NORMAL; // get request normal
		writeRequestArray[DL_COSEMPDU_OFFSET + 2] = this.invokeIdAndPriority; //invoke id and priority
		writeRequestArray[DL_COSEMPDU_OFFSET_CID] = (byte) (classId >> 8);
		writeRequestArray[DL_COSEMPDU_OFFSET_CID + 1] = (byte) classId;

		for (i = 0; i < 6; i++) {
			writeRequestArray[DL_COSEMPDU_OFFSET_LN + i] = LN[i];
		}
		writeRequestArray[DL_COSEMPDU_OFFSET_ATTR] = bAttr;

		if (byteSelectiveBuffer == null) {
			writeRequestArray[DL_COSEMPDU_OFFSET_ACCESS_SELECTOR] = 0;
			return writeRequestArray;
		} else {
			writeRequestArray[DL_COSEMPDU_OFFSET_ACCESS_SELECTOR] = 0;
			// Concatenate 2 byte arrays into requestData.
			byte[] requestData = new byte[writeRequestArray.length + byteSelectiveBuffer.length];
			for (i = 0; i < GETREQUEST_DATA_SIZE; i++) {
				requestData[i] = writeRequestArray[i];
			}
			for (i = GETREQUEST_DATA_SIZE; i < requestData.length; i++) {
				requestData[i] = byteSelectiveBuffer[i - (GETREQUEST_DATA_SIZE)];
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
		byte[] writeRequestArray = new byte[ACTIONREQUEST_DATA_SIZE];

		writeRequestArray[0] = (byte) 0xE6; // Destination_LSAP
		writeRequestArray[1] = (byte) 0xE6; // Source_LSAP
		writeRequestArray[2] = 0x00; // LLC_Quality
		writeRequestArray[DL_COSEMPDU_OFFSET] = COSEM_ACTIONREQUEST;
		writeRequestArray[DL_COSEMPDU_OFFSET + 1] = COSEM_ACTIONREQUEST_NORMAL; // get request normal
		writeRequestArray[DL_COSEMPDU_OFFSET + 2] = this.invokeIdAndPriority; //invoke id and priority
		writeRequestArray[DL_COSEMPDU_OFFSET_CID] = (byte) (classId >> 8);
		writeRequestArray[DL_COSEMPDU_OFFSET_CID + 1] = (byte) classId;

		for (int i = 0; i < 6; i++) {
			writeRequestArray[DL_COSEMPDU_OFFSET_LN + i] = LN[i];
		}
		writeRequestArray[DL_COSEMPDU_OFFSET_ATTR] = (byte) methodId;

		if (data == null) {
			writeRequestArray[DL_COSEMPDU_OFFSET_ACCESS_SELECTOR] = 0;
			return writeRequestArray;
		} else {
			writeRequestArray[DL_COSEMPDU_OFFSET_ACCESS_SELECTOR] = 1;
			// Concatenate 2 byte arrays into requestData.
			byte[] requestData = new byte[writeRequestArray.length + data.length];
			for (int i = 0; i < ACTIONREQUEST_DATA_SIZE; i++) {
				requestData[i] = writeRequestArray[i];
			}
			for (int i = ACTIONREQUEST_DATA_SIZE; i < requestData.length; i++) {
				requestData[i] = data[i - (ACTIONREQUEST_DATA_SIZE)];
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

	protected byte[] checkCosemPDUResponseHeader(byte[] responseData) throws IOException {
		int i;

		boolean boolLastBlock = true;
		int iBlockNumber;
		int iBlockSize;
		int iArrayNROfEntries;
		ReceiveBuffer receiveBuffer = new ReceiveBuffer();

		do {
			i = DL_COSEMPDU_OFFSET;

			switch (responseData[i]) {
				case COSEM_READRESPONSE: {
					switch (responseData[DL_COSEMPDU_OFFSET + 2]) {

						case READRESPONSE_DATA_TAG:
							receiveBuffer.addArray(responseData, DL_COSEMPDU_OFFSET + 3);
							return receiveBuffer.getArray();

						case READRESPONSE_DATAACCESSERROR_TAG:
							evalDataAccessResult(responseData[DL_COSEMPDU_OFFSET + 3]);
							receiveBuffer.addArray(responseData, DL_COSEMPDU_OFFSET + 3);
							return receiveBuffer.getArray();

						case READRESPONSE_DATABLOCK_RESULT_TAG: {
							i = DL_COSEMPDU_OFFSET + 3; // to point to the block last

							boolLastBlock = (responseData[i] != 0x00);
							i++; // skip lastblock
							iBlockNumber = ProtocolUtils.getInt(responseData, i, 2);
							i += 2; // skip iBlockNumber

							iBlockSize = (int) DLMSUtils.getAXDRLength(responseData, i);

							i += DLMSUtils.getAXDRLengthOffset(responseData, i);

							if (iBlockNumber == 1) {
								i += 2; // skip the tricky read response sequence of choice and data encoding 0100
							}

							debug("last block=" + boolLastBlock + ", blockNumber=" + iBlockNumber + ", blockSize=" + iBlockSize + ", offset=" + i);

							receiveBuffer.addArray(responseData, i);

							if (!boolLastBlock) {
								try {
									debug("Acknowledge block " + iBlockNumber);
									responseData = this.protocolLink.getDLMSConnection().sendRequest(buildReadRequestNext(iBlockNumber));
//									debug("next response data = " + ProtocolUtils.outputHexString(responseData));
								} catch (IOException e) {
									throw new NestedIOException(e, "Error in COSEM_GETRESPONSE_WITH_DATABLOCK");
								}
							} else {
								return (receiveBuffer.getArray());
							}

						}
							break; // READRESPONSE_DATABLOCK_RESULT_TAG

					} // switch(responseData[DL_COSEMPDU_OFFSET+2])

				}
					break; //COSEM_READRESPONSE

				case COSEM_WRITERESPONSE: {
					if (responseData[DL_COSEMPDU_OFFSET + 2] == READRESPONSE_DATAACCESSERROR_TAG) {
						evalDataAccessResult(responseData[DL_COSEMPDU_OFFSET + 3]);
					}
					receiveBuffer.addArray(responseData, DL_COSEMPDU_OFFSET + 3);
					return receiveBuffer.getArray();
				} // COSEM_WRITERESPONSE

				case COSEM_CONFIRMEDSERVICEERROR: {
					//                    if ((responseData[DL_COSEMPDU_OFFSET+1] == CONFIRMEDSERVICEERROR_READ_TAG) &&
					//                    (responseData[DL_COSEMPDU_OFFSET+2] == SERVICEERROR_ACCESS_TAG) &&
					//                    (responseData[DL_COSEMPDU_OFFSET+3] == ACCESS_AUTHORIZATION)) {
					//                        throw new IOException("Access denied through authorization!");
					//                    }
					//                    else {
					//                        throw new IOException("Unknown service error, "+responseData[DL_COSEMPDU_OFFSET+1]+
					//                        responseData[DL_COSEMPDU_OFFSET+2]+
					//                        responseData[DL_COSEMPDU_OFFSET+3]);
					//                    }

                    switch (responseData[DL_COSEMPDU_OFFSET + 1]) {
                        case CONFIRMEDSERVICEERROR_INITIATEERROR_TAG: {
                            throw new IOException("Confirmed Service Error - 'Initiate error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_GETSTATUS_TAG: {
                            throw new IOException("Confirmed Sercie Error - 'GetStatus error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_GETNAMELIST_TAG: {
                            throw new IOException("Confirmed Service Error - 'GetNameList error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_GETVARIABLEATTRIBUTE_TAG: {
                            throw new IOException("Confirmed Service Error - 'GetVariableAttribute error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_READ_TAG: {
                            throw new IOException("Confirmed Service Error - 'Read error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_WRITE_TAG: {
                            throw new IOException("Confirmed Service Error - 'Write error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_GETDATASETATTRIBUTE_TAG: {
                            throw new IOException("Confirmed Service Error - 'GetDataSetAttribute' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_GETTIATTRIBUTE_TAG: {
                            throw new IOException("Confirmed Service Error - 'GetTIAttribute error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_CHANGESCOPE_TAG: {
                            throw new IOException("Confirmed Service Error - 'ChangeScope error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_START_TAG: {
                            throw new IOException("Confirmed Service Error - 'Start error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_RESUME_TAG: {
                            throw new IOException("Confirmed Service Error - 'Resume error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_MAKEUSABLE_TAG: {
                            throw new IOException("Confirmed Service Error - 'MakeUsable error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_INITIATELOAD_TAG: {
                            throw new IOException("Confirmed Service Error - 'InitiateLoad error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_LOADSEGMENT_TAG: {
                            throw new IOException("Confirmed Service Error - 'LoadSegment error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_TERMINATELOAD_TAG: {
                            throw new IOException("Confirmed Service Error - 'TerminateLoad error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_INITIATEUPLOAD_TAG: {
                            throw new IOException("Confirmed Service Error - 'InitiateUpload error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_UPLOADSEGMENT_TAG: {
                            throw new IOException("Confirmed Service Error - 'UploadSegment error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        case CONFIRMEDSERVICEERROR_TERMINATEUPLOAD_TAG: {
                            throw new IOException("Confirmed Service Error - 'TerminateUpload error' - Reason: "
                                    + getServiceError(responseData[DL_COSEMPDU_OFFSET + 2], responseData[DL_COSEMPDU_OFFSET + 3]));
                        }
                        default: {
                            throw new IOException("Unknown service error, " + responseData[DL_COSEMPDU_OFFSET + 1] + responseData[DL_COSEMPDU_OFFSET + 2]
                                    + responseData[DL_COSEMPDU_OFFSET + 3]);
                        }
                    }
                } // !!! break !!! COSEM_CONFIRMEDSERVICEERROR

				case COSEM_GETRESPONSE: {
					i++; // skip tag
					switch (responseData[i]) {
						case COSEM_GETRESPONSE_NORMAL: {
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
									throw new IOException("unknown COSEM_GETRESPONSE_NORMAL,  " + responseData[i]);

							} // switch(responseData[i])

						}
							break; // case COSEM_GETRESPONSE_NORMAL:

						case COSEM_GETRESPONSE_WITH_DATABLOCK: {
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
											responseData = this.protocolLink.getDLMSConnection().sendRequest(buildGetRequestNext(iBlockNumber));
										} catch (IOException e) {
											throw new NestedIOException(e, "Error in COSEM_GETRESPONSE_WITH_DATABLOCK");
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
									throw new IOException("unknown COSEM_GETRESPONSE_WITH_DATABLOCK,  " + responseData[i]);
							}

						}
							break; // case COSEM_GETRESPONSE_WITH_DATABLOCK:

                        case COSEM_GETRESPONSE_WITH_LIST: {
                            i++; // skip tag
                            i++; // skip invoke id & priority
                            i++; // nr of items
                            receiveBuffer.addArray(responseData, i);
                            return receiveBuffer.getArray();
                        }

						default:
							throw new IOException("Unknown/unimplemented COSEM_GETRESPONSE, " + responseData[i]);

					} // switch(responseData[i])

				}
					break; // case COSEM_GETRESPONSE:

				case COSEM_ACTIONRESPONSE: {

					/* Implementation as from 28/01/2010 */

					if ("OLD".equalsIgnoreCase(this.protocolLink.getMeterConfig().getExtra())) {

						/*
						 * This is the INcorrect implementation of an
						 * ActionResponse
						 * We use this in old KP firmwareVersion
						 */

						i++; // skip tag
						switch (responseData[i]) {
							case COSEM_ACTIONRESPONSE_NORMAL: {
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
										throw new IOException("unknown COSEM_ACTIONRESPONSE_NORMAL,  " + responseData[i]);

								} // switch(responseData[i])
							}
								break; // case COSEM_ACTIONRESPONSE_NORMAL:

							default:
								throw new IOException("Unknown/unimplemented COSEM_ACTIONRESPONSE, " + responseData[i]);

						} // switch(responseData[i])

					} else {

						/*
						 * This is the correct implementation of an
						 * ActionResponse
						 */

						i++; // skipping the tag
						switch (responseData[i]) { // ACTION-Response ::= choice

							case COSEM_ACTIONRESPONSE_NORMAL: {
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

							case COSEM_ACTIONRESPONSE_WITH_PBLOCK: {
								throw new IOException("Unimplemented COSEM_ACTIONRESPONSE, " + responseData[i]);
							}

							case COSEM_ACTIONRESPONSE_WITH_LIST: {
								throw new IOException("Unimplemented COSEM_ACTIONRESPONSE, " + responseData[i]);
							}

							case COSEM_ACTIONRESPONSE_NEXT_PBLOCK: {
								throw new IOException("Unimplemented COSEM_ACTIONRESPONSE, " + responseData[i]);
							}
							default:
								throw new IOException("Unimplemented COSEM_ACTIONRESPONSE, " + responseData[i]);
						}
					}
				}
					break; // case COSEM_ACTIONRESPONSE:

				case COSEM_SETRESPONSE: {
					i++; // skip tag
                    switch (responseData[i]) {
                        case COSEM_SETRESPONSE_NORMAL: {
                            i++; // skip COSEM_SETRESPONSE_NORMAL tag
                            i++; // skip invoke id & priority
                            evalDataAccessResult(responseData[i]);
                            receiveBuffer.addArray(responseData, i+1);
                            return receiveBuffer.getArray();
                        }
                        default: {
                            throw new IOException("Unknown/unimplemented COSEM_SETRESPONSE, " + responseData[i]);
                        }
                    } // switch(responseData[i])
                }

                case COSEM_EXCEPTION_RESPONSE: {
                    throw new ExceptionResponseException(responseData[i+1], responseData[i+2]);
                }

				default: {
					throw new IOException("Unknown COSEM PDU, " + " 0x" + Integer.toHexString(ProtocolUtils.byte2int(responseData[DL_COSEMPDU_OFFSET])) + " 0x"
							+ Integer.toHexString(ProtocolUtils.byte2int(responseData[DL_COSEMPDU_OFFSET + 1])) + " 0x"
							+ Integer.toHexString(ProtocolUtils.byte2int(responseData[DL_COSEMPDU_OFFSET + 2])) + " 0x"
							+ Integer.toHexString(ProtocolUtils.byte2int(responseData[DL_COSEMPDU_OFFSET + 3])));
				} // !!! break !!! default

			} // switch(responseData[i])

		} while (!(boolLastBlock));

		return null;

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
		if ((toCalendar == null) && this.protocolLink.getMeterConfig().isSL7000()) {
			return getBufferRangeDescriptorSL7000(fromCalendar);
		} else if (this.protocolLink.getMeterConfig().isActarisPLCC()) {
			return getBufferRangeDescriptorActarisPLCC(fromCalendar, toCalendar);
		} else {
			return getBufferRangeDescriptorDefault(fromCalendar, toCalendar);
		}

	}
	private byte[] getBufferRangeDescriptor(Calendar fromCalendar, Calendar toCalendar, List<CapturedObject> channels) {
		if ((toCalendar == null) && this.protocolLink.getMeterConfig().isSL7000()) {
			return getBufferRangeDescriptorSL7000(fromCalendar);
		} else if (this.protocolLink.getMeterConfig().isActarisPLCC()) {
			return getBufferRangeDescriptorActarisPLCC(fromCalendar, toCalendar);
		} else {
			return getBufferRangeDescriptorDefault(fromCalendar, toCalendar, channels);
		}
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

		intreq[CAPTURE_FROM_OFFSET] = TYPEDESC_OCTET_STRING;
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

		intreq[CAPTURE_TO_OFFSET] = TYPEDESC_OCTET_STRING;
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
				(byte) 0x00 };

		final int CAPTURE_FROM_OFFSET = 4;
		final int CAPTURE_TO_OFFSET = 18;

		intreq[CAPTURE_FROM_OFFSET] = TYPEDESC_OCTET_STRING;
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

		intreq[CAPTURE_TO_OFFSET] = TYPEDESC_OCTET_STRING;
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
				(byte) 0x01, (byte) 0x00 };

		int CAPTURE_FROM_OFFSET = 21;
		int CAPTURE_TO_OFFSET = 35;

		intreq[CAPTURE_FROM_OFFSET] = TYPEDESC_OCTET_STRING;
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

		intreq[CAPTURE_TO_OFFSET] = TYPEDESC_OCTET_STRING;
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

	private byte[] getBufferRangeDescriptorDefault(Calendar fromCalendar, Calendar toCalendar, List<CapturedObject> channels) {

        byte[] selectedValues = new byte[]{(byte) 0x01 , (byte) 0x00};        //Default is empty array, fetching all channels
        if (channels != null && channels.size() != 0) {
            Array array = new Array();
            for (CapturedObject channel : channels) {
                Structure structure = new Structure();
                structure.addDataType(new Unsigned16(channel.getClassId()));
                structure.addDataType(new OctetString(channel.getLogicalName().getObisCode().getLN()));
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
        intreq = ProtocolTools.concatByteArrays(intreq, selectedValues);

		int CAPTURE_FROM_OFFSET = 21;
		int CAPTURE_TO_OFFSET = 35;

		intreq[CAPTURE_FROM_OFFSET] = TYPEDESC_OCTET_STRING;
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

		intreq[CAPTURE_TO_OFFSET] = TYPEDESC_OCTET_STRING;
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

			if (responseData[0] == TYPEDESC_ARRAY) {
				if ((responseData[1] & 0x80) != 0) {
					bOffset = (byte) (responseData[1] & (byte) 0x7F);
					for (int i = 0; i < bOffset; i++) {
						lNrOfItemsInArray = lNrOfItemsInArray << 8;
						lNrOfItemsInArray |= ((long) responseData[2 + i] & 0x000000ff);
					}
				} else {
					lNrOfItemsInArray = (long) responseData[1] & 0x000000FF;
				}

				if (lNrOfItemsInArray == 0) {
					this.protocolLink.getLogger().warning("DLMSZMD: No new profile data.");
				}
				universalObject = new UniversalObject[(int) lNrOfItemsInArray];

				t = 2 + bOffset;
				for (itemInArray = 0; itemInArray < lNrOfItemsInArray; itemInArray++) {

					debug("KV_DEBUG> itemInArray=" + itemInArray);

					if (responseData[t] == TYPEDESC_STRUCTURE) {
						debug("KV_DEBUG> TYPEDESC_STRUCTURE");
						int iNROfItems;
						int iIndex = 0;

						t++; // skip structure tag
						iNROfItems = responseData[t];
						t++; // skip nr of items in structure

						values.clear();

						for (iFieldIndex = 0; iFieldIndex < iNROfItems; iFieldIndex++) {
							debug("KV_DEBUG> iFieldIndex=" + iFieldIndex);
							if ((responseData[t] == TYPEDESC_LONG) || (responseData[t] == TYPEDESC_LONG_UNSIGNED)) {
								debug("KV_DEBUG> TYPEDESC_LONG | TYPEDESC_LONG_UNSIGNED");
								t++; // skip tag
								values.add(new Long((long) ProtocolUtils.getShort(responseData, t) & 0x0000FFFF));
								t += 2; // skip (unsigned) long (2byte) value
							} else if ((responseData[t] == TYPEDESC_OCTET_STRING) || (responseData[t] == TYPEDESC_VISIBLE_STRING)) {
								debug("KV_DEBUG> TYPEDESC_OCTET_STRING | TYPEDESC_VISIBLE_STRING");
								t++; // skip tag
								int iLength = responseData[t];
								t++; // skip length byte
								int temp;
								for (temp = 0; temp < iLength; temp++) {
									values.add(new Long((long) responseData[t + temp] & 0x000000FF));
								}
								t += iLength; // skip string, iLength bytes
							} else if ((responseData[t] == TYPEDESC_DOUBLE_LONG_UNSIGNED) || (responseData[t] == TYPEDESC_DOUBLE_LONG)) {
								debug("KV_DEBUG> TYPEDESC_DOUBLE_LONG_UNSIGNED | TYPEDESC_DOUBLE_LONG");
								t++; // skip tag
								values.add(new Long(ProtocolUtils.getInt(responseData, t)));
								t += 4; // skip double unsigned long (4byte) value
							} else if ((responseData[t] == TYPEDESC_BOOLEAN) || (responseData[t] == TYPEDESC_INTEGER) || (responseData[t] == TYPEDESC_UNSIGNED)) {
								debug("KV_DEBUG> TYPEDESC_BOOLEAN | TYPEDESC_INTEGER | TYPEDESC_UNSIGNED");
								t++; // skip tag
								values.add(new Long((long) responseData[t] & 0x000000FF));
								t++; // skip (1byte) value
							}
							// KV 29072004
							else if (responseData[t] == TYPEDESC_LONG64) {
								debug("KV_DEBUG> TYPEDESC_LONG64");
								t++; // skip tag
								values.add(new Long(ProtocolUtils.getLong(responseData, t))); // KV 09/10/2006
								t += 8; // skip double unsigned long (8byte) value
							} else if (responseData[t] == TYPEDESC_STRUCTURE) {
								debug("KV_DEBUG> TYPEDESC_STRUCTURE");
								t = skipStructure(responseData, t);
							} else {
								throw new IOException("Error parsing objectlistdata, unknown type.");
							}

						} // for (iFieldIndex=0;iFieldIndex<universalObject[(int)i].lFields.length;iFieldIndex++)

						universalObject[itemInArray] = new UniversalObject(values, this.protocolLink.getReference());

					} // if (responseData[t] == TYPEDESC_STRUCTURE)  // structure
					else {
						throw new IOException("Error parsing objectlistdata, no structure found.");
					}

				} // for (i=0; i<lNrOfItemsInArray;i++)

			} // if (responseData[0] == TYPEDESC_ARRAY)
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
			if ((responseData[t] == TYPEDESC_LONG) || (responseData[t] == TYPEDESC_LONG_UNSIGNED)) {
				t++; // skip tag
				t += 2; // skip (unsigned) long (2byte) value
				membersInStructure[level]--;
				debug("KV_DEBUG> skipStructure (t=" + t + "), TYPEDESC_LONG | TYPEDESC_LONG_UNSIGNED, level=" + level);
			} else if ((responseData[t] == TYPEDESC_OCTET_STRING) || (responseData[t] == TYPEDESC_VISIBLE_STRING)) {
				t++; // skip tag
				t += (responseData[t] + 1); // skip string, iLength bytes
				membersInStructure[level]--;
				debug("KV_DEBUG> skipStructure (t=" + t + "), TYPEDESC_OCTET_STRING | TYPEDESC_VISIBLE_STRING, level=" + level);
			} else if ((responseData[t] == TYPEDESC_DOUBLE_LONG_UNSIGNED) || (responseData[t] == TYPEDESC_DOUBLE_LONG)) {
				t++; // skip tag
				t += 4; // skip double unsigned long (4byte) value
				membersInStructure[level]--;
				debug("KV_DEBUG> skipStructure (t=" + t + "), TYPEDESC_DOUBLE_LONG_UNSIGNED | TYPEDESC_DOUBLE_LONG, level=" + level);
			} else if ((responseData[t] == TYPEDESC_BOOLEAN) || (responseData[t] == TYPEDESC_INTEGER) || (responseData[t] == TYPEDESC_UNSIGNED)) {
				t++; // skip tag
				t++; // skip (1byte) value
				membersInStructure[level]--;
				debug("KV_DEBUG> skipStructure (t=" + t + "), TYPEDESC_BOOLEAN | TYPEDESC_INTEGER | TYPEDESC_UNSIGNED, level=" + level);
			}
			// KV 28072004
			else if (responseData[t] == TYPEDESC_LONG64) {
				t++; // skip tag
				t += 8; // skip (8byte) value
				membersInStructure[level]--;
				debug("KV_DEBUG> skipStructure (t=" + t + "), TYPEDESC_LONG64, level=" + level);
			}
			// Skip the access rights structure in case of long name referencing...
			else if (responseData[t] == TYPEDESC_STRUCTURE) {
				t++; // skip structure tag
				membersInStructure[level]--;
				level++;
				membersInStructure[level] = responseData[t];
				t++; // skip nr of members
				debug("KV_DEBUG> skipStructure (t=" + t + "), TYPEDESC_STRUCTURE, level=" + level);
			} else if (responseData[t] == TYPEDESC_ARRAY) {
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

				debug("KV_DEBUG> skipStructure (t=" + t + "), TYPEDESC_ARRAY, level=" + level + ", elementsInArray=" + elementsInArray);
			} else if (responseData[t] == TYPEDESC_NULL) {
				t++; // skip tag
				membersInStructure[level]--;
				debug("KV_DEBUG> skipStructure (t=" + t + "), TYPEDESC_NULL, level=" + level);
			}
			// KV 05042007
			else if (responseData[t] == TYPEDESC_ENUM) {
				t++; // skip tag
				t++; // skip (1byte) value
				membersInStructure[level]--;
				debug("KV_DEBUG> skipStructure (t=" + t + "), TYPEDESC_ENUM, level=" + level);
			} else {
				throw new IOException("AbsrtactCosemObject, skipStructure, Error parsing objectlistdata, unknown response tag " + responseData[t]);
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
	 * @param message
	 */
	private void debug(String message) {
		if (DEBUG) {
			LogFactory.getLog(getClass()).debug(message);
		}
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

    public ObisCode getObisCode() {
        return getObjectReference() != null ? getObjectReference().getObisCode() : null;
    }

}
