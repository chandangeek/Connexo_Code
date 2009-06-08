/*
 * AbstractCosemObject.java
 *
 * Created on 18 augustus 2004, 11:57
 */

package com.energyict.dlms.cosem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.AdaptorConnection;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ReceiveBuffer;
import com.energyict.dlms.UniversalObject;
import com.energyict.protocol.ProtocolUtils;
/**
 *
 * @author  Koen
 */
public abstract class AbstractCosemObject implements DLMSCOSEMGlobals {
    
    final int DEBUG=0;
    
    static public final int CLASSID_DATA=1;
    static public final int CLASSID_REGISTER=3;
    static public final int CLASSID_EXTENDED_REGISTER=4;
    static public final int CLASSID_DEMAND_REGISTER=5;
    static public final int CLASSID_PROFILE_GENERIC=7;
    static public final int CLASSID_CLOCK=8;
    static public final int CLASSID_SCRIPTTABLE=9;
    static public final int CLASSID_ACTIVITY_CALENDAR=20;
    static public final int CLASSID_REGISTER_MONITOR=21;
    static public final int CLASSID_SINGLE_ACTION_SCHEDULE=22;
    static public final int CLASSID_IPV4SETUP=42;
    static public final int CLASSID_SMTP_SETUP=46;
    static public final int CLASSID_SPECIAL_DAYS_TABLE=11;
    
    protected ProtocolLink protocolLink;
    private ObjectReference objectReference;
    
    abstract protected int getClassId();
    private byte INVOKE_ID_AND_PRIORITY;
    
    /** Creates a new instance of AbstractCosemObject */
    public AbstractCosemObject(ProtocolLink protocolLink,ObjectReference objectReference) {
        this.objectReference=objectReference;
        this.protocolLink=protocolLink;
        if (this.protocolLink != null)
        	this.INVOKE_ID_AND_PRIORITY = this.protocolLink.getDLMSConnection().getInvokeIdAndPriority().getInvokeIdAndPriorityData();
    }
    
    public byte[] getCompoundData() {
    	AdaptorConnection conn = (AdaptorConnection)protocolLink.getDLMSConnection();
    	if (conn!=null)
    		return conn.getCompoundData();
    	else 
    		return null;
    }
    
    protected long getLongData(int attribute) throws IOException {
        try {
            byte[] responseData=null;
            if (objectReference.isLNReference())
               responseData = protocolLink.getDLMSConnection().sendRequest(buildGetRequest(getClassId(),objectReference.getLn(),DLMSUtils.attrSN2LN(attribute),null));
            else if (objectReference.isSNReference())
               responseData = protocolLink.getDLMSConnection().sendRequest(buildReadRequest((short)objectReference.getSn(),attribute,null));
                
            return DLMSUtils.parseValue2long(CheckCosemPDUResponseHeader(responseData));
        }
        catch(IOException e) {
            throw new NestedIOException(e);
        }
    }
    
    public byte[] invoke(int methodId) throws IOException {
        return invoke(methodId,null);
    }
    public byte[] invoke(int methodId,byte[] data) throws IOException {
        try {
            byte[] responseData=null;
            responseData = protocolLink.getDLMSConnection().sendRequest(buildActionRequest(getClassId(),objectReference.getLn(),methodId,data));
            return CheckCosemPDUResponseHeader(responseData);
        }
        catch(DataAccessResultException e) {
            throw(e);
        }
        catch(IOException e) {
            throw new NestedIOException(e);
        }
    }
    
    protected byte[] write(int attribute,byte[] data) throws IOException {
        try {
            byte[] responseData=null;
            if (objectReference.isLNReference())
               //responseData = protocolLink.getDLMSConnection().sendRequest(buildSetRequest(getClassId(),objectReference.getLn(),DLMSUtils.attrSN2LN(attribute),data));
                responseData = protocolLink.getDLMSConnection().sendRequest(buildSetRequest(getClassId(),objectReference.getLn(),(byte)attribute,data));
            else if (objectReference.isSNReference()) {
            	
               // very dirty trick because there is a lot of legacy code that passes the attribute as
               // an correct offset to the base address... 	However, the later (better) dlms framework objects
               // use the method for write in this abstract class with attribute id 0,1,...
               // so, only for attribute = 8 or a multiple of 8 can be a problem.... 	
               if ((attribute < 8) || ((attribute % 8) != 0)) {
            	   attribute = (attribute-1)*8;
            	   byte[] data2 = new byte[data.length+1];
            	   System.arraycopy(data, 0, data2, 1, data.length);
            	   data = data2;
            	   data[0] = 0x01;
               }
               
               
               responseData = protocolLink.getDLMSConnection().sendRequest(buildWriteRequest((short)objectReference.getSn(),attribute,data));
            }
            if (protocolLink.getDLMSConnection() instanceof AdaptorConnection)
            	return responseData;
            else 
            	return CheckCosemPDUResponseHeader(responseData);
        }
        catch(DataAccessResultException e) {
            throw(e);
        }
        catch(IOException e) {
            throw new NestedIOException(e);
        }
    }
    /*
     *   attribute as defined in the object docs
     *   1 - logical name
     *   2..n attribute 2..n
     */
    protected byte[] getLNResponseData(int attribute) throws IOException {
        return getLNResponseData(attribute,null,null);
    }
    protected byte[] getLNResponseData(int attribute,Calendar from,Calendar to) throws IOException {
        return getResponseData((attribute-1)*8,from,to);
    }
    
    /*
     *   attribute as defined in the object docs fopr short name reference
     *   0 = logical name attribute 1
     *   8,16,24,..n attribute 2..n
     */
    protected byte[] getResponseData(int attribute) throws IOException {
        return getResponseData(attribute,null,null);
    }
    
    protected byte[] getResponseData(int attribute,Calendar from,Calendar to) throws IOException {
        try {
            byte[] responseData=null;
            if (objectReference.isLNReference()) {
//               System.out.println("KV_DEBUG> LN "+objectReference+", attr"+DLMSUtils.attrSN2LN(attribute)); 
               responseData = protocolLink.getDLMSConnection().sendRequest(buildGetRequest(getClassId(),
                                                                                           objectReference.getLn(),
                                                                                           DLMSUtils.attrSN2LN(attribute),
                                                                                           (from == null ? null : getBufferRangeDescriptor(from, to))));
            }
            else if (objectReference.isSNReference()) {
//               System.out.println("KV_DEBUG> SN "+objectReference+", attr"+attribute); 
               responseData = protocolLink.getDLMSConnection().sendRequest(buildReadRequest((short)objectReference.getSn(),
                                                                                            attribute,
                                                                                            (from == null ? null : getBufferRangeDescriptor(from, to))));
            }
            return CheckCosemPDUResponseHeader(responseData);
        }
        catch(DataAccessResultException e) {
            throw(e);
        }
        catch(IOException e) {
            throw new NestedIOException(e);
        }
    }
    
    
    private byte[] buildReadRequestNext(int blockNr) {
        // KV 06052009
        byte[] readRequestArray = new byte[READREQUEST_DATA_SIZE];
        readRequestArray[0] = (byte)0xE6; // Destination_LSAP
        readRequestArray[1] = (byte)0xE6; // Source_LSAP
        readRequestArray[2] = 0x00; // LLC_Quality
        readRequestArray[DL_COSEMPDU_OFFSET] = COSEM_READREQUEST;
        readRequestArray[DL_COSEMPDU_LENGTH_OFFSET] = 0x01; // length of the variable length SEQUENCE OF
        readRequestArray[DL_COSEMPDU_TAG_OFFSET] = 0x05; // block-number-access
        readRequestArray[READREQUEST_BLOCKNR_MSB] = (byte)(((blockNr)>>8)&0x00FF);
        readRequestArray[READREQUEST_BLOCKNR_LSB] = (byte)((blockNr)&0x00FF);
        return readRequestArray;
    } // protected byte[] buildReadRequest(int iObj, int iAttr, byte[] byteSelectiveBuffer)    
    
    private byte[] buildReadRequest(int iObj, int iAttr, byte[] byteSelectiveBuffer) {
        // Simple request data Array
        
        // KV_DEBUG
        // System.out.println("****************************** object SN: 0x"+Integer.toHexString(iObj+iAttr));
        
        byte[] readRequestArray = new byte[READREQUEST_DATA_SIZE];
        int i;
        
        readRequestArray[0] = (byte)0xE6; // Destination_LSAP
        readRequestArray[1] = (byte)0xE6; // Source_LSAP
        readRequestArray[2] = 0x00; // LLC_Quality
        readRequestArray[DL_COSEMPDU_OFFSET] = COSEM_READREQUEST;
        readRequestArray[DL_COSEMPDU_LENGTH_OFFSET] = 0x01; // length of the variable length SEQUENCE OF
        if (byteSelectiveBuffer == null)
            readRequestArray[DL_COSEMPDU_TAG_OFFSET] = 0x02; // implicit objectname
        else
            readRequestArray[DL_COSEMPDU_TAG_OFFSET] = 0x04; // object name integer data
        
        readRequestArray[READREQUEST_SN_MSB] = (byte)(((iObj+iAttr)>>8)&0x00FF);
        readRequestArray[READREQUEST_SN_LSB] = (byte)((iObj+iAttr)&0x00FF);
        if (byteSelectiveBuffer != null) {
            // Concatenate 2 byte arrays into requestData.
            byte[] requestData = new byte[readRequestArray.length+byteSelectiveBuffer.length];
            for (i=0;i<READREQUEST_DATA_SIZE;i++)
                requestData[i] = readRequestArray[i];
            for (i=READREQUEST_DATA_SIZE;i<requestData.length;i++)
                requestData[i] = byteSelectiveBuffer[i-READREQUEST_DATA_SIZE];
            return requestData;
        }
        else {
            return readRequestArray;
        }
    } // protected byte[] buildReadRequest(int iObj, int iAttr, byte[] byteSelectiveBuffer)
    
    private byte[] buildWriteRequest(int iObj, int iAttr, byte[] byteSelectiveBuffer) throws IOException {
        // Simple request data Array
        byte[] readRequestArray = new byte[READREQUEST_DATA_SIZE];
        int i;
        
        //if (byteSelectiveBuffer == null) throw new IOException("No data to write!");
        
        readRequestArray[0] = (byte)0xE6; // Destination_LSAP
        readRequestArray[1] = (byte)0xE6; // Source_LSAP
        readRequestArray[2] = 0x00; // LLC_Quality
        readRequestArray[DL_COSEMPDU_OFFSET] = COSEM_WRITEREQUEST;
        readRequestArray[DL_COSEMPDU_LENGTH_OFFSET] = 0x01; // length of the variable length SEQUENCE OF
        readRequestArray[DL_COSEMPDU_TAG_OFFSET] = 0x02; // implicit objectname
        
        readRequestArray[READREQUEST_SN_MSB] = (byte)(((iObj+iAttr)>>8)&0x00FF);
        readRequestArray[READREQUEST_SN_LSB] = (byte)((iObj+iAttr)&0x00FF);
        if (byteSelectiveBuffer != null) {
            // Concatenate 2 byte arrays into requestData.
            byte[] requestData = new byte[readRequestArray.length+byteSelectiveBuffer.length];
            for (i=0;i<READREQUEST_DATA_SIZE;i++)
                requestData[i] = readRequestArray[i];
            for (i=READREQUEST_DATA_SIZE;i<requestData.length;i++)
                requestData[i] = byteSelectiveBuffer[i-READREQUEST_DATA_SIZE];
            return requestData;
        }
        else {
            return readRequestArray;
        }
    } // private byte[] buildWriteRequest(short sObj, short sAttr, byte[] byteSelectiveBuffer)
    
    
    private byte[] buildGetRequest(int classId,byte[] LN,byte bAttr, byte[] byteSelectiveBuffer) {
        // Simple request data Array
        byte[] readRequestArray = new byte[GETREQUEST_DATA_SIZE];
        int i;
        
        readRequestArray[0] = (byte)0xE6; // Destination_LSAP
        readRequestArray[1] = (byte)0xE6; // Source_LSAP
        readRequestArray[2] = 0x00; // LLC_Quality
        readRequestArray[DL_COSEMPDU_OFFSET] = COSEM_GETREQUEST;
        readRequestArray[DL_COSEMPDU_OFFSET+1] = COSEM_GETREQUEST_NORMAL; // get request normal
        
        readRequestArray[DL_COSEMPDU_OFFSET+2] = INVOKE_ID_AND_PRIORITY; //invoke id and priority
        
        readRequestArray[DL_COSEMPDU_OFFSET_CID] = (byte)(classId>>8);
        readRequestArray[DL_COSEMPDU_OFFSET_CID+1] = (byte)classId;
        
        for (i=0;i<6;i++) readRequestArray[DL_COSEMPDU_OFFSET_LN+i] = LN[i];
        
        readRequestArray[DL_COSEMPDU_OFFSET_ATTR] = bAttr;
        
        if (byteSelectiveBuffer == null) {
            readRequestArray[DL_COSEMPDU_OFFSET_ACCESS_SELECTOR]=0; // Selective access descriptor NOT present
            return readRequestArray;
        }
        else {
            readRequestArray[DL_COSEMPDU_OFFSET_ACCESS_SELECTOR]=1; // Selective access descriptor present
            
            
            // Concatenate 2 byte arrays into requestData.
            byte[] requestData = new byte[readRequestArray.length+byteSelectiveBuffer.length];
            for (i=0;i<GETREQUEST_DATA_SIZE;i++)
                requestData[i] = readRequestArray[i];
            for (i=GETREQUEST_DATA_SIZE;i<requestData.length;i++)
                requestData[i] = byteSelectiveBuffer[i-(GETREQUEST_DATA_SIZE)];
            return requestData;
        }
        
    } // private byte[] buildGetRequest(short sIC,byte[] LN,byte bAttr, byte[] byteSelectiveBuffer)
    
    private byte[] buildGetRequestNext(int iBlockNumber) {
        byte[] readRequestArray = new byte[GETREQUESTNEXT_DATA_SIZE];
        
        readRequestArray[0] = (byte)0xE6; // Destination_LSAP
        readRequestArray[1] = (byte)0xE6; // Source_LSAP
        readRequestArray[2] = 0x00; // LLC_Quality
        readRequestArray[DL_COSEMPDU_OFFSET] = COSEM_GETREQUEST;
        readRequestArray[DL_COSEMPDU_OFFSET+1] = COSEM_GETREQUEST_NEXT; // get request next
        readRequestArray[DL_COSEMPDU_OFFSET+2] = INVOKE_ID_AND_PRIORITY; //invoke id and priority
        readRequestArray[DL_COSEMPDU_OFFSET+3] = (byte)(iBlockNumber>>24);
        readRequestArray[DL_COSEMPDU_OFFSET+4] = (byte)(iBlockNumber>>16);
        readRequestArray[DL_COSEMPDU_OFFSET+5] = (byte)(iBlockNumber>>8);
        readRequestArray[DL_COSEMPDU_OFFSET+6] = (byte)iBlockNumber;
        return readRequestArray;
    }
    
    private byte[] buildSetRequest(int classId,byte[] LN,byte bAttr, byte[] byteSelectiveBuffer) {
        // Simple request data Array
        byte[] writeRequestArray = new byte[SETREQUEST_DATA_SIZE];
        int i;
        
        writeRequestArray[0] = (byte)0xE6; // Destination_LSAP
        writeRequestArray[1] = (byte)0xE6; // Source_LSAP
        writeRequestArray[2] = 0x00; // LLC_Quality
        writeRequestArray[DL_COSEMPDU_OFFSET] = COSEM_SETREQUEST;
        writeRequestArray[DL_COSEMPDU_OFFSET+1] = COSEM_SETREQUEST_NORMAL; // get request normal
        writeRequestArray[DL_COSEMPDU_OFFSET+2] = INVOKE_ID_AND_PRIORITY; //invoke id and priority
        
        writeRequestArray[DL_COSEMPDU_OFFSET_CID] = (byte)(classId>>8);
        writeRequestArray[DL_COSEMPDU_OFFSET_CID+1] = (byte)classId;
        for (i=0;i<6;i++) writeRequestArray[DL_COSEMPDU_OFFSET_LN+i] = LN[i];
        writeRequestArray[DL_COSEMPDU_OFFSET_ATTR] = bAttr;
        
        if (byteSelectiveBuffer == null) {
            writeRequestArray[DL_COSEMPDU_OFFSET_ACCESS_SELECTOR]=0;
            return writeRequestArray;
        }
        else {
            writeRequestArray[DL_COSEMPDU_OFFSET_ACCESS_SELECTOR]=0;
            // Concatenate 2 byte arrays into requestData.
            byte[] requestData = new byte[writeRequestArray.length+byteSelectiveBuffer.length];
            for (i=0;i<GETREQUEST_DATA_SIZE;i++)
                requestData[i] = writeRequestArray[i];
            for (i=GETREQUEST_DATA_SIZE;i<requestData.length;i++)
                requestData[i] = byteSelectiveBuffer[i-(GETREQUEST_DATA_SIZE)];
            return requestData;
        }
        
    } // private byte[] buildSetRequest(short sIC,byte[] LN,byte bAttr, byte[] byteSelectiveBuffer)

    private byte[] buildActionRequest(int classId,byte[] LN,int methodId, byte[] data) {
        // Simple request data Array
        byte[] writeRequestArray = new byte[ACTIONREQUEST_DATA_SIZE];
        int i;
        
        writeRequestArray[0] = (byte)0xE6; // Destination_LSAP
        writeRequestArray[1] = (byte)0xE6; // Source_LSAP
        writeRequestArray[2] = 0x00; // LLC_Quality
        writeRequestArray[DL_COSEMPDU_OFFSET] = COSEM_ACTIONREQUEST;
        writeRequestArray[DL_COSEMPDU_OFFSET+1] = COSEM_ACTIONREQUEST_NORMAL; // get request normal
        writeRequestArray[DL_COSEMPDU_OFFSET+2] = INVOKE_ID_AND_PRIORITY; //invoke id and priority
        
        writeRequestArray[DL_COSEMPDU_OFFSET_CID] = (byte)(classId>>8);
        writeRequestArray[DL_COSEMPDU_OFFSET_CID+1] = (byte)classId;
        for (i=0;i<6;i++) writeRequestArray[DL_COSEMPDU_OFFSET_LN+i] = LN[i];
        writeRequestArray[DL_COSEMPDU_OFFSET_ATTR] = (byte)methodId;
        
        if (data == null) {
            writeRequestArray[DL_COSEMPDU_OFFSET_ACCESS_SELECTOR]=0;
            return writeRequestArray;
        }
        else {
            writeRequestArray[DL_COSEMPDU_OFFSET_ACCESS_SELECTOR]=1;
            // Concatenate 2 byte arrays into requestData.
            byte[] requestData = new byte[writeRequestArray.length+data.length];
            for (i=0;i<ACTIONREQUEST_DATA_SIZE;i++)
                requestData[i] = writeRequestArray[i];
            for (i=ACTIONREQUEST_DATA_SIZE;i<requestData.length;i++)
                requestData[i] = data[i-(ACTIONREQUEST_DATA_SIZE)];
            return requestData;
        }
        
    } // private byte[] buildActionRequest(int classId,byte[] LN,int methodId, byte[] data)
    
    
    private void evalDataAccessResult(byte bVal) throws IOException {
        if (bVal != 0)
        throw new DataAccessResultException((int)bVal&0xFF);
    } // private void evalDataAccessResult(byte bVal) throws IOException

    
    private static final byte SERVICEERROR_ACCESS_TAG=5;
    private static final byte ACCESS_AUTHORIZATION=1;
    
    private static final byte READRESPONSE_DATA_TAG=0;
    private static final byte READRESPONSE_DATAACCESSERROR_TAG=1;
    private static final byte READRESPONSE_DATABLOCK_RESULT_TAG=2;
    
    private byte[] CheckCosemPDUResponseHeader(byte[] responseData) throws IOException {
        int i;
        
        boolean boolLastBlock=true;
        int iBlockNumber;
        int iBlockSize;
        int iArrayNROfEntries;
        ReceiveBuffer receiveBuffer=new ReceiveBuffer();
        
        do {
            i=DL_COSEMPDU_OFFSET;
            
            switch(responseData[i]) {
                case COSEM_READRESPONSE: {
                	switch(responseData[DL_COSEMPDU_OFFSET+2]) {
                	
                		case READRESPONSE_DATA_TAG:
                        	receiveBuffer.addArray(responseData,DL_COSEMPDU_OFFSET+3);
                        	return receiveBuffer.getArray();
                		
                		case READRESPONSE_DATAACCESSERROR_TAG:
                            evalDataAccessResult(responseData[DL_COSEMPDU_OFFSET+3]);
                            receiveBuffer.addArray(responseData,DL_COSEMPDU_OFFSET+3);
                            return receiveBuffer.getArray();
                		
                		case READRESPONSE_DATABLOCK_RESULT_TAG: {
                			i=DL_COSEMPDU_OFFSET+3; // to point to the block last
                			
                            boolLastBlock = (responseData[i] != 0x00);
                            i++; // skip lastblock
                            iBlockNumber = ProtocolUtils.getInt(responseData,i,2);
                            i+=2; // skip iBlockNumber

                            iBlockSize = (int)DLMSUtils.getAXDRLength(responseData,i);
                            
                            i += DLMSUtils.getAXDRLengthOffset(responseData,i);
                            
                            if (iBlockNumber==1)
                                i+=2; // skip the tricky read response sequence of choice and data encoding 0100

                            if (DEBUG>=1)
                            	System.out.println("last block="+boolLastBlock+", blockNumber="+iBlockNumber+", blockSize="+iBlockSize+", offset="+i);
                            
                            receiveBuffer.addArray(responseData,i);
                            
                            if (!boolLastBlock) {
                                try {
                                    if (DEBUG>=1)
                                    	System.out.println("Acknowledge block "+iBlockNumber);
                                    responseData = protocolLink.getDLMSConnection().sendRequest(buildReadRequestNext(iBlockNumber));
                                    if (DEBUG>=1)
                                    	System.out.println("next response data = "+ProtocolUtils.outputHexString(responseData));
                                }
                                catch(IOException e) {
                                    throw new NestedIOException(e,"Error in COSEM_GETRESPONSE_WITH_DATABLOCK");
                                }
                            }
                            else {
                                return (receiveBuffer.getArray());
                            }
                                    
                			
                		} break; // READRESPONSE_DATABLOCK_RESULT_TAG
                    		
                	} // switch(responseData[DL_COSEMPDU_OFFSET+2])
                    
                } break; //COSEM_READRESPONSE
                
                case COSEM_WRITERESPONSE: {
                    if (responseData[DL_COSEMPDU_OFFSET+2] == READRESPONSE_DATAACCESSERROR_TAG) {
                        evalDataAccessResult(responseData[DL_COSEMPDU_OFFSET+3]);
                    }
                    receiveBuffer.addArray(responseData,DL_COSEMPDU_OFFSET+3);
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
                    
                    switch(responseData[DL_COSEMPDU_OFFSET+1]){
	                    case CONFIRMEDSERVICEERROR_INITIATEERROR_TAG:{
	                    	throw new IOException("Confirmed Service Error - 'Initiate error' - Reason: " + getServiceError(responseData[DL_COSEMPDU_OFFSET+2],responseData[DL_COSEMPDU_OFFSET+3]));
	                    }
	                    case CONFIRMEDSERVICEERROR_READ_TAG:{
	                    	throw new IOException("Confirmed Service Error - 'Read error' - Reason: " + getServiceError(responseData[DL_COSEMPDU_OFFSET+2],responseData[DL_COSEMPDU_OFFSET+3]));
	                    }
	                    case CONFIRMEDSERVICEERROR_WRITE_TAG:{
	                    	throw new IOException("Confirmed Service Error - 'Write error' - Reason: " + getServiceError(responseData[DL_COSEMPDU_OFFSET+2],responseData[DL_COSEMPDU_OFFSET+3]));
	                    }
	                    default:{
	                      throw new IOException("Unknown service error, "+responseData[DL_COSEMPDU_OFFSET+1]+
	                      responseData[DL_COSEMPDU_OFFSET+2]+
	                      responseData[DL_COSEMPDU_OFFSET+3]);
	                    }
                    }
                } // !!! break !!! COSEM_CONFIRMEDSERVICEERROR
                
                case COSEM_GETRESPONSE: {
                    i++; // skip tag
                    switch(responseData[i]) {
                        case COSEM_GETRESPONSE_NORMAL: {
                            i++; // skip tag
                            i++; // skip invoke id & priority
                            switch(responseData[i]) {
                                case 0: // data
                                    i++;
                                    receiveBuffer.addArray(responseData,i);
                                    return receiveBuffer.getArray();
                                    
                                case 1: // data-access-result
                                {
                                    i++;
                                    evalDataAccessResult(responseData[i]);
                                    //System.out.println("Data access result OK");
                                    
                                } break;  // data-access-result
                                
                                default:
                                    throw new IOException("unknown COSEM_GETRESPONSE_NORMAL,  "+responseData[i]);
                                    
                            } // switch(responseData[i])
                            
                        } break; // case COSEM_GETRESPONSE_NORMAL:
                        
                        case COSEM_GETRESPONSE_WITH_DATABLOCK: {
                            i++; // skip tag
                            i++; // skip invoke id & priority
                            
                            boolLastBlock = (responseData[i] != 0x00);
                            i++; // skip lastblock
                            iBlockNumber = ProtocolUtils.getInt(responseData,i);
                            i+=4; // skip iBlockNumber
                            switch(responseData[i]) {
                                case 0: // data
                                {
                                    i++; // skip tag
                                    
                                    if (iBlockNumber==1) {
                                        iBlockSize = (int)DLMSUtils.getAXDRLength(responseData,i);
                                        i += DLMSUtils.getAXDRLengthOffset(responseData,i);
                                        receiveBuffer.addArray(responseData,i);
                                        i++; /// skip array tag
                                        iArrayNROfEntries = (int)DLMSUtils.getAXDRLength(responseData,i);
                                        i += DLMSUtils.getAXDRLengthOffset(responseData,i);
                                    }
                                    else {
                                        iBlockSize = (int)DLMSUtils.getAXDRLength(responseData,i);
                                        i += DLMSUtils.getAXDRLengthOffset(responseData,i);
                                        receiveBuffer.addArray(responseData,i);
                                    }
                                    
                                    if (!(boolLastBlock)) {
                                        try {
                                            responseData = protocolLink.getDLMSConnection().sendRequest(buildGetRequestNext(iBlockNumber));
                                        }
                                        catch(IOException e) {
                                            throw new NestedIOException(e,"Error in COSEM_GETRESPONSE_WITH_DATABLOCK");
                                        }
                                    }
                                    else {
                                        return (receiveBuffer.getArray());
                                    }
                                    
                                } break; // data
                                
                                case 1: // data-access-result
                                {
                                    i++;
                                    evalDataAccessResult(responseData[i]);
                                    //System.out.println("Data access result OK");
                                    
                                } break;  // data-access-result
                                
                                default:
                                    throw new IOException("unknown COSEM_GETRESPONSE_WITH_DATABLOCK,  "+responseData[i]);
                            }
                            
                        } break; // case COSEM_GETRESPONSE_WITH_DATABLOCK:
                        
                        default:
                            throw new IOException("Unknown/unimplemented COSEM_GETRESPONSE, "+responseData[i]);
                            
                    } // switch(responseData[i])
                    
                } break; // case COSEM_GETRESPONSE:

                case COSEM_ACTIONRESPONSE: {
                    i++; // skip tag
                    switch(responseData[i]) {
                        case COSEM_ACTIONRESPONSE_NORMAL: {
                            i++; // skip tag
                            i++; // skip invoke id & priority
//                            evalDataAccessResult(responseData[i]);
                            switch(responseData[i]) {
                            case 0: // data
                                i++;
                                receiveBuffer.addArray(responseData,i);
                                return receiveBuffer.getArray();
                                
                            case 1: // data-access-result
                            {
                                i++;
                                evalDataAccessResult(responseData[i]);
                                //System.out.println("Data access result OK");
                                
                            } break;  // data-access-result
                            
                            default:
                                throw new IOException("unknown COSEM_ACTIONRESPONSE_NORMAL,  "+responseData[i]);
                                
                        } // switch(responseData[i])
                        } break; // case COSEM_ACTIONRESPONSE_NORMAL:
                        
                        default:
                            throw new IOException("Unknown/unimplemented COSEM_ACTIONRESPONSE, "+responseData[i]);
                            
                    } // switch(responseData[i])
                    
                } break; // case COSEM_ACTIONRESPONSE:
                
                
                case COSEM_SETRESPONSE: {
                    i++; // skip tag
                    switch(responseData[i]) {
                        case COSEM_SETRESPONSE_NORMAL: {
                            i++; // skip tag
                            i++; // skip invoke id & priority
//                            evalDataAccessResult(responseData[i]);
                            switch(responseData[i]) {
                            case 0: // data
                                i++;
                                receiveBuffer.addArray(responseData,i);
                                return receiveBuffer.getArray();
                                
                            case 1: // data-access-result
                            {
                                i++;
                                evalDataAccessResult(responseData[i]);
                                //System.out.println("Data access result OK");
                                
                            } break;  // data-access-result
                            
                            default:
                                throw new IOException("unknown COSEM_SETRESPONSE_NORMAL,  "+responseData[i]);
                                
                        } // switch(responseData[i])
                        } break; // case COSEM_SETRESPONSE_NORMAL:
                        
                        default:
                            throw new IOException("Unknown/unimplemented COSEM_SETRESPONSE, "+responseData[i]);
                            
                    } // switch(responseData[i])
                    
                } break; // case COSEM_SETRESPONSE:
                
                default: {
                    throw new IOException("Unknown COSEM PDU, "+" 0x"+Integer.toHexString(ProtocolUtils.byte2int(responseData[DL_COSEMPDU_OFFSET]))+
                    " 0x"+Integer.toHexString(ProtocolUtils.byte2int(responseData[DL_COSEMPDU_OFFSET+1]))+
                    " 0x"+Integer.toHexString(ProtocolUtils.byte2int(responseData[DL_COSEMPDU_OFFSET+2]))+
                    " 0x"+Integer.toHexString(ProtocolUtils.byte2int(responseData[DL_COSEMPDU_OFFSET+3])));
                } // !!! break !!! default
                
            } // switch(responseData[i])
            
        } while (!(boolLastBlock));
        
        return null;
        
    } // byte[] CheckCosemPDUResponseHeader(byte[] responseData) throws IOException
    
    private String getServiceError(byte b, byte c) {
    	switch(b){
    	case 0:{ // Application-reference
    		switch(c){
    		case 0 : return "Application-reference - Other";
    		case 1 : return "Application-reference - Time out since request sent";
    		case 2 : return "Application-reference - Peer AEi not reachable";
    		case 3 : return "Application-reference - Addressing trouble";
    		case 4 : return "Application-reference - Application-context incompatibility";
    		case 5 : return "Application-reference - Error at the local or distant equipment";
    		case 6 : return "Application-reference - Error detected by the deciphering function";
    		}; break ;
    	}
    	case 1:{ // Hardware-resource
    		switch(c){
    		case 0 : return "Hardware-resource - Other";
    		case 1 : return "Hardware-resource - Memory unavailable";
    		case 2 : return "Hardware-resource - Processor-resource unavailable";
    		case 3 : return "Hardware-resource - Mass-storage unavailable";
    		case 4 : return "Hardware-resource - Other resource unavailable";
    		}; break ;
    	}
    	case 2:{ // VDE-State-error
    		switch(c){
    		case 0 : return "VDE-State-error - Other";
    		case 1 : return "VDE-State-error - No DLMS context";
    		case 2 : return "VDE-State-error - Loading data-set";
    		case 3 : return "VDE-State-error - Status nochange";
    		case 4 : return "VDE-State-error - Status inoperable";
    		}; break ;
    	}
    	case 3:{ // Service
    		switch(c){
    		case 0: return "Service - Other";
    		case 1: return "Service - PDU size to long";
    		case 2: return "Service - Service unsupported";
    		}; break ;
    	}
    	case 4:{ // Definition
    		switch(c){
    		case 0: return "Definition - Other";
    		case 1: return "Definition - Object undefined";
    		case 2: return "Definition - Object class inconsistent";
    		case 3: return "Definition - Object attribute inconsistent";
    		}; break ;
    	}
    	case 5:{ // Access
    		switch(c){
    		case 0: return "Access - Other";
    		case 1: return "Access - Scope of access violated";
    		case 2: return "Access - Object access violated";
    		case 3: return "Access - Hardware-fault";
    		case 4: return "Access - Object unavailable";
    		}; break ;
    	}
    	case 6:{ // Initiate
    		switch(c){
    		case 0: return "Initiate - Other";
    		case 1: return "Initiate - DLMS version too low";
    		case 2: return "Initiate - Incompatible conformance";
    		case 3: return "Initiate - PDU size too short";
    		case 4: return "Initiate - Refused by the VDE Handler";
    		}; break ;
    	}
    	case 7:{ // Load-Data-Set
    		switch(c){
    		case 0: return "Load-Data-Set - Other";
    		case 1: return "Load-Data-Set - Primitive out of sequence";
    		case 2: return "Load-Data-Set - Not loadable";
    		case 3: return "Load-Data-Set - Evaluated data set size too large";
    		case 4: return "Load-Data-Set - Proposed segment not awaited";
    		case 5: return "Load-Data-Set - Segment interpretation error";
    		case 6: return "Load-Data-Set - Segment storage error";
    		case 7: return "Load-Data-Set - Data set not in correct state for uploading";
    		}; break ;
    	}
    	case 8:{ // Change scope
    		return "Change Scope";
    	}
    	case 9:{ // Task
    		switch(c){
    		case 0: return "Task - Other";
    		case 1: return "Task - Remote control parameter set to FALSE";
    		case 2: return "Task - TI in stopped state";
    		case 3: return "Task - TI in running state";
    		case 4: return "Task - TI in unusable state";
    		}; break ;
    	}
    	case 10:{ // Other
    		return "Other";
    	}
    	default:{
    		return "Other";
    	}
    	}
		return "";
	}

	private byte[] getBufferRangeDescriptor(Calendar fromCalendar, Calendar toCalendar) {
        if (toCalendar == null)
           return getBufferRangeDescriptorSL7000(fromCalendar);
        else if (protocolLink.getMeterConfig().isActarisPLCC())
           return getBufferRangeDescriptorActarisPLCC(fromCalendar,toCalendar);
        else
           return getBufferRangeDescriptorDefault(fromCalendar,toCalendar);
    }
    
    private byte[] getBufferRangeDescriptorActarisPLCC(Calendar fromCalendar,Calendar toCalendar) {
        
         byte[] intreq={(byte)0x01, // range descriptor
                       (byte)0x02, // structure
                       (byte)0x04, // 4 items in structure
         // capture object definition
         (byte)0x0F,(byte)0x00,
         // from value
         (byte)0x09,(byte)0x0C,(byte)0x07,(byte)0xD2,(byte)0x05,(byte)23,(byte)0xFF,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xFF,(byte)0x80,(byte)0x00,(byte)0x00,
         // to value
         (byte)0x09,(byte)0x0C,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xFF,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xFF,(byte)0xff,(byte)0xff,(byte)0xff,
         // selected values
         }; //(byte)0x00};

         final int CAPTURE_FROM_OFFSET=5; // was 4
         final int CAPTURE_TO_OFFSET=19; // was 18
         
         intreq[CAPTURE_FROM_OFFSET]=TYPEDESC_OCTET_STRING;
         intreq[CAPTURE_FROM_OFFSET+1]=12; // length
         intreq[CAPTURE_FROM_OFFSET+2]=(byte)(fromCalendar.get(Calendar.YEAR) >> 8);
         intreq[CAPTURE_FROM_OFFSET+3]=(byte)fromCalendar.get(Calendar.YEAR);
         intreq[CAPTURE_FROM_OFFSET+4]=(byte)(fromCalendar.get(Calendar.MONTH)+1);
         intreq[CAPTURE_FROM_OFFSET+5]=(byte)fromCalendar.get(Calendar.DAY_OF_MONTH);
         intreq[CAPTURE_FROM_OFFSET+6]=(byte)0xff;
         intreq[CAPTURE_FROM_OFFSET+7]=(byte)fromCalendar.get(Calendar.HOUR_OF_DAY);
         intreq[CAPTURE_FROM_OFFSET+8]=(byte)fromCalendar.get(Calendar.MINUTE);
         intreq[CAPTURE_FROM_OFFSET+9]=(byte)0x00;
         intreq[CAPTURE_FROM_OFFSET+10]=(byte)0xFF;
         intreq[CAPTURE_FROM_OFFSET+11]=(byte)0x80;
         intreq[CAPTURE_FROM_OFFSET+12]=0x00;
         intreq[CAPTURE_FROM_OFFSET+13]=0x00;

         intreq[CAPTURE_TO_OFFSET]=TYPEDESC_OCTET_STRING;
         intreq[CAPTURE_TO_OFFSET+1]=12; // length
         intreq[CAPTURE_TO_OFFSET+2]=(byte)(toCalendar.get(Calendar.YEAR) >> 8);
         intreq[CAPTURE_TO_OFFSET+3]=(byte)(toCalendar.get(Calendar.YEAR));
         intreq[CAPTURE_TO_OFFSET+4]=(byte)(toCalendar.get(Calendar.MONTH)+1);
         intreq[CAPTURE_TO_OFFSET+5]=(byte)(toCalendar.get(Calendar.DAY_OF_MONTH));
         intreq[CAPTURE_TO_OFFSET+6]=(byte)0xFF;
         intreq[CAPTURE_TO_OFFSET+7]=(byte)toCalendar.get(Calendar.HOUR_OF_DAY);
         intreq[CAPTURE_TO_OFFSET+8]=(byte)toCalendar.get(Calendar.MINUTE);
         intreq[CAPTURE_TO_OFFSET+9]=(byte)0x00;
         intreq[CAPTURE_TO_OFFSET+10]=(byte)0xFF;
         intreq[CAPTURE_TO_OFFSET+11]=(byte)0x80;
         intreq[CAPTURE_TO_OFFSET+12]=(byte)0x00;
         intreq[CAPTURE_TO_OFFSET+13]=(byte)0x00;
        
         return intreq;
        
    }
    
    private byte[] getBufferRangeDescriptorSL7000(Calendar fromCalendar) {
        
         byte[] intreq={(byte)0x01, // range descriptor
                       (byte)0x02, // structure
                       (byte)0x04, // 4 items in structure
         // capture object definition
         (byte)0x00,
         // from value
         (byte)0x09,(byte)0x0C,(byte)0x07,(byte)0xD2,(byte)0x05,(byte)23,(byte)0xFF,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xFF,(byte)0x80,(byte)0x00,(byte)0x00,
         // to value
         (byte)0x09,(byte)0x0C,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xFF,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xFF,(byte)0xff,(byte)0xff,(byte)0xff,
         // selected values
         (byte)0x00};

         final int CAPTURE_FROM_OFFSET=4;
         final int CAPTURE_TO_OFFSET=18;
         
         
         
         intreq[CAPTURE_FROM_OFFSET]=TYPEDESC_OCTET_STRING;
         intreq[CAPTURE_FROM_OFFSET+1]=12; // length
         intreq[CAPTURE_FROM_OFFSET+2]=(byte)(fromCalendar.get(Calendar.YEAR) >> 8);
         intreq[CAPTURE_FROM_OFFSET+3]=(byte)fromCalendar.get(Calendar.YEAR);
         intreq[CAPTURE_FROM_OFFSET+4]=(byte)(fromCalendar.get(Calendar.MONTH)+1);
         intreq[CAPTURE_FROM_OFFSET+5]=(byte)fromCalendar.get(Calendar.DAY_OF_MONTH);
         intreq[CAPTURE_FROM_OFFSET+6]=(byte)0xff;
         intreq[CAPTURE_FROM_OFFSET+7]=(byte)0xff; //fromCalendar.get(Calendar.HOUR_OF_DAY);
         intreq[CAPTURE_FROM_OFFSET+8]=(byte)0xff; //fromCalendar.get(Calendar.MINUTE);
         intreq[CAPTURE_FROM_OFFSET+9]=(byte)0xFF;
         intreq[CAPTURE_FROM_OFFSET+10]=(byte)0xFF;
         intreq[CAPTURE_FROM_OFFSET+11]=(byte)0x80;
         intreq[CAPTURE_FROM_OFFSET+12]=0x00;
         intreq[CAPTURE_FROM_OFFSET+13]=0x00;

         intreq[CAPTURE_TO_OFFSET]=TYPEDESC_OCTET_STRING;
         intreq[CAPTURE_TO_OFFSET+1]=12; // length
         intreq[CAPTURE_TO_OFFSET+2]=(byte)0xff; //(toCalendar.get(Calendar.YEAR) >> 8);
         intreq[CAPTURE_TO_OFFSET+3]=(byte)0xff; //toCalendar.get(Calendar.YEAR);
         intreq[CAPTURE_TO_OFFSET+4]=(byte)0xff; //(toCalendar.get(Calendar.MONTH)+1);
         intreq[CAPTURE_TO_OFFSET+5]=(byte)0xff; //toCalendar.get(Calendar.DAY_OF_MONTH);
         intreq[CAPTURE_TO_OFFSET+6]=(byte)0xFF;
         intreq[CAPTURE_TO_OFFSET+7]=(byte)0xff; //toCalendar.get(Calendar.HOUR_OF_DAY);
         intreq[CAPTURE_TO_OFFSET+8]=(byte)0xff; //toCalendar.get(Calendar.MINUTE);
         intreq[CAPTURE_TO_OFFSET+9]=(byte)0xff; //0x00;
         intreq[CAPTURE_TO_OFFSET+10]=(byte)0xFF;
         intreq[CAPTURE_TO_OFFSET+11]=(byte)0xff; //0x80;
         intreq[CAPTURE_TO_OFFSET+12]=(byte)0xff; //0x00;
         intreq[CAPTURE_TO_OFFSET+13]=(byte)0xff; //0x00;
        
         return intreq;
    }
    
    private byte[] getBufferRangeDescriptorDefault(Calendar fromCalendar, Calendar toCalendar) {
        
        byte[] intreq={(byte)0x01, // range descriptor
        (byte)0x02, // structure
        (byte)0x04, // 4 items in structure
        // capture object definition
        (byte)0x02,(byte)0x04,
        (byte)0x12,(byte)0x00,(byte)0x08,
        (byte)0x09,(byte)0x06,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0xFF,
        (byte)0x0F,(byte)0x02,
        (byte)0x12,(byte)0x00,(byte)0x00,
        // from value
        (byte)0x09,(byte)0x0C,(byte)0x07,(byte)0xD2,(byte)0x05,(byte)23,(byte)0xFF,(byte)11,(byte)0x00,(byte)0x00,(byte)0xFF,(byte)0x80,(byte)0x00,(byte)0x00,
        // to value
        (byte)0x09,(byte)0x0C,(byte)0x07,(byte)0xD2,(byte)0x05,(byte)23,(byte)0xFF,(byte)13,(byte)0x00,(byte)0x00,(byte)0xFF,(byte)0x80,(byte)0x00,(byte)0x00,
        // selected values
        (byte)0x01,(byte)0x00};
        
        int CAPTURE_FROM_OFFSET=21;
        int CAPTURE_TO_OFFSET=35;
        
        
        intreq[CAPTURE_FROM_OFFSET]=TYPEDESC_OCTET_STRING;
        intreq[CAPTURE_FROM_OFFSET+1]=12; // length
        intreq[CAPTURE_FROM_OFFSET+2]=(byte)(fromCalendar.get(Calendar.YEAR) >> 8);
        intreq[CAPTURE_FROM_OFFSET+3]=(byte)fromCalendar.get(Calendar.YEAR);
        intreq[CAPTURE_FROM_OFFSET+4]=(byte)(fromCalendar.get(Calendar.MONTH)+1);
        intreq[CAPTURE_FROM_OFFSET+5]=(byte)fromCalendar.get(Calendar.DAY_OF_MONTH);
        //             bDOW = (byte)fromCalendar.get(Calendar.DAY_OF_WEEK);
        //             intreq[CAPTURE_FROM_OFFSET+6]=bDOW--==1?(byte)7:bDOW;
        intreq[CAPTURE_FROM_OFFSET+6]=(byte)0xff;
        intreq[CAPTURE_FROM_OFFSET+7]=(byte)fromCalendar.get(Calendar.HOUR_OF_DAY);
        intreq[CAPTURE_FROM_OFFSET+8]=(byte)fromCalendar.get(Calendar.MINUTE);
        //             intreq[CAPTURE_FROM_OFFSET+9]=(byte)fromCalendar.get(Calendar.SECOND);
        
        if (protocolLink.getMeterConfig().isIskra())
            intreq[CAPTURE_FROM_OFFSET+9]=0;
        else    
            intreq[CAPTURE_FROM_OFFSET+9]=0x01;
        
        intreq[CAPTURE_FROM_OFFSET+10]=(byte)0xFF;
        intreq[CAPTURE_FROM_OFFSET+11]=(byte)0x80;
        intreq[CAPTURE_FROM_OFFSET+12]=0x00;
        if (protocolLink.getTimeZone().inDaylightTime(fromCalendar.getTime()))
            intreq[CAPTURE_FROM_OFFSET+13]=(byte)0x80;
        else
            intreq[CAPTURE_FROM_OFFSET+13]=0x00;

        
        
        intreq[CAPTURE_TO_OFFSET]=TYPEDESC_OCTET_STRING;
        intreq[CAPTURE_TO_OFFSET+1]=12; // length
        intreq[CAPTURE_TO_OFFSET+2]=(byte)(toCalendar.get(Calendar.YEAR) >> 8);
        intreq[CAPTURE_TO_OFFSET+3]=(byte)toCalendar.get(Calendar.YEAR);
        intreq[CAPTURE_TO_OFFSET+4]=(byte)(toCalendar.get(Calendar.MONTH)+1);
        intreq[CAPTURE_TO_OFFSET+5]=(byte)toCalendar.get(Calendar.DAY_OF_MONTH);
        //             bDOW = (byte)toCalendar.get(Calendar.DAY_OF_WEEK);
        //             intreq[CAPTURE_TO_OFFSET+6]=bDOW--==1?(byte)7:bDOW;
        intreq[CAPTURE_TO_OFFSET+6]=(byte)0xFF;
        intreq[CAPTURE_TO_OFFSET+7]=(byte)toCalendar.get(Calendar.HOUR_OF_DAY);
        intreq[CAPTURE_TO_OFFSET+8]=(byte)toCalendar.get(Calendar.MINUTE);
        //             intreq[CAPTURE_TO_OFFSET+9]=(byte)toCalendar.get(Calendar.SECOND);
        intreq[CAPTURE_TO_OFFSET+9]=0x00;
        intreq[CAPTURE_TO_OFFSET+10]=(byte)0xFF;
        intreq[CAPTURE_TO_OFFSET+11]=(byte)0x80;
        intreq[CAPTURE_TO_OFFSET+12]=0x00;
        if (protocolLink.getTimeZone().inDaylightTime(toCalendar.getTime()))
            intreq[CAPTURE_TO_OFFSET+13]=(byte)0x80;
        else
            intreq[CAPTURE_TO_OFFSET+13]=0x00;
        
        
        return intreq;
    }
    
    protected UniversalObject[] data2UOL(byte[] responseData) throws IOException {
        long lNrOfItemsInArray=0;
        int itemInArray;
        byte bOffset=0;
        short sBaseName,sClassID;
        byte A,B,C,D,E,F;
        int t=0,iFieldIndex;
        UniversalObject[] universalObject=null;
        int level=0;
        if (DEBUG>=1) System.out.println("KV_DEBUG> responseData="+ProtocolUtils.outputHexString(responseData));
        List values = new ArrayList();
        try {
            
            if (responseData[0] == TYPEDESC_ARRAY) {
                if ((responseData[1] & 0x80) != 0) {
                    bOffset = (byte)(responseData[1]&(byte)0x7F);
                    for (int i=0;i<bOffset;i++) {
                        lNrOfItemsInArray = lNrOfItemsInArray << 8;
                        lNrOfItemsInArray |= ((long)responseData[2+(int)i]& 0x000000ff);
                    }
                }
                else lNrOfItemsInArray = (long)responseData[1] & 0x000000FF;
                
                if (lNrOfItemsInArray == 0) protocolLink.getLogger().warning("DLMSZMD: No new profile data.");
                universalObject = new UniversalObject[(int)lNrOfItemsInArray];
                
                t = 2+bOffset;
                for (itemInArray=0; itemInArray<lNrOfItemsInArray;itemInArray++) {
                    
                    if (DEBUG>=1) System.out.println("KV_DEBUG> itemInArray="+itemInArray);
    
                    if (responseData[t] == TYPEDESC_STRUCTURE) {
                        if (DEBUG>=1) System.out.println("KV_DEBUG> TYPEDESC_STRUCTURE");
                        int iNROfItems;
                        int iIndex=0;
                        
                        t++; // skip structure tag
                        iNROfItems = responseData[t];
                        t++; // skip nr of items in structure
                        
                        values.clear();
                        
                        for (iFieldIndex=0;iFieldIndex<iNROfItems;iFieldIndex++) {
                            
                            if (DEBUG>=1) System.out.println("KV_DEBUG> iFieldIndex="+iFieldIndex);
                            
                            if ((responseData[t] == TYPEDESC_LONG) ||
                            (responseData[t] == TYPEDESC_LONG_UNSIGNED)) {
                                if (DEBUG>=1) System.out.println("KV_DEBUG> TYPEDESC_LONG | TYPEDESC_LONG_UNSIGNED");
                                t++; // skip tag
                                values.add(new Long((long)ProtocolUtils.getShort(responseData,t) & 0x0000FFFF));
                                t+=2; // skip (unsigned) long (2byte) value
                            }
                            else if ((responseData[t] == TYPEDESC_OCTET_STRING) ||
                            (responseData[t] == TYPEDESC_VISIBLE_STRING)) {
                                if (DEBUG>=1) System.out.println("KV_DEBUG> TYPEDESC_OCTET_STRING | TYPEDESC_VISIBLE_STRING");
                                t++; // skip tag
                                int iLength = responseData[t];
                                t++; // skip length byte
                                int temp;
                                for (temp=0;temp<iLength;temp++) {
                                    values.add(new Long((long)responseData[t+temp]&0x000000FF));
                                }
                                t+=iLength; // skip string, iLength bytes
                            }
                            else if ((responseData[t] == TYPEDESC_DOUBLE_LONG_UNSIGNED) ||
                            (responseData[t] == TYPEDESC_DOUBLE_LONG)) {
                                if (DEBUG>=1) System.out.println("KV_DEBUG> TYPEDESC_DOUBLE_LONG_UNSIGNED | TYPEDESC_DOUBLE_LONG");
                                t++; // skip tag
                                values.add(new Long((long)ProtocolUtils.getInt(responseData,t)));
                                t+=4; // skip double unsigned long (4byte) value
                            }
                            else if ((responseData[t] == TYPEDESC_BOOLEAN) ||
                            (responseData[t] == TYPEDESC_INTEGER) ||
                            (responseData[t] == TYPEDESC_UNSIGNED)) {
                                if (DEBUG>=1) System.out.println("KV_DEBUG> TYPEDESC_BOOLEAN | TYPEDESC_INTEGER | TYPEDESC_UNSIGNED");
                                t++; // skip tag
                                values.add(new Long((long)responseData[t]&0x000000FF));
                                t++; // skip (1byte) value
                            }
                            // KV 29072004
                            else if (responseData[t] == TYPEDESC_LONG64) {
                                if (DEBUG>=1) System.out.println("KV_DEBUG> TYPEDESC_LONG64");
                                t++; // skip tag
                                values.add(new Long((long)ProtocolUtils.getLong(responseData,t))); // KV 09/10/2006
                                t+=8; // skip double unsigned long (8byte) value
                            }
                            else if (responseData[t] == TYPEDESC_STRUCTURE) {
                                if (DEBUG>=1) System.out.println("KV_DEBUG> TYPEDESC_STRUCTURE");
                                t=skipStructure(responseData,t);
                            }
                            else throw new IOException("Error parsing objectlistdata, unknown type.");
                            
                        } // for (iFieldIndex=0;iFieldIndex<universalObject[(int)i].lFields.length;iFieldIndex++)
                        
                        universalObject[itemInArray] = new UniversalObject(values,protocolLink.getReference());
                        
                    } // if (responseData[t] == TYPEDESC_STRUCTURE)  // structure
                    else throw new IOException("Error parsing objectlistdata, no structure found.");
                    
                } // for (i=0; i<lNrOfItemsInArray;i++)
                
            } // if (responseData[0] == TYPEDESC_ARRAY)
            else throw new IOException("Error parsing objectlistdata, no array found.");
        }
        catch(ArrayIndexOutOfBoundsException e) {
            if (DEBUG>=1) 
                e.printStackTrace();
            
            throw new IOException("Error bad data received, index out of bounds, datalength="+responseData.length+", lNrOfItemsInArray="+lNrOfItemsInArray+", t="+t+", bOffset="+bOffset);
        }
        return universalObject;
        
    } // private UniversalObject[] Data2UOL(byte[] responseData) throws IOException
    
    private int skipStructure(byte[] responseData,int t) throws IOException {
        
        
        int level=0;
        long elementsInArray=0; 
        int[] membersInStructure = new int[10]; // max structure depth = 10!!!!
        t++; //skip structure tag
        membersInStructure[level] = responseData[t];
        t++; // skip structure nr of members
        while((level>0) || ((level==0) && (membersInStructure[level]>0))) {        
            if ((responseData[t] == TYPEDESC_LONG) ||
                (responseData[t] == TYPEDESC_LONG_UNSIGNED)) {
                t++; // skip tag
                t+=2; // skip (unsigned) long (2byte) value
                membersInStructure[level]--;
                if (DEBUG>=1) System.out.println("KV_DEBUG> skipStructure (t="+t+"), TYPEDESC_LONG | TYPEDESC_LONG_UNSIGNED, level="+level);
            }
            else if ((responseData[t] == TYPEDESC_OCTET_STRING) ||
                     (responseData[t] == TYPEDESC_VISIBLE_STRING)) {
                t++; // skip tag
                t+=(responseData[t]+1); // skip string, iLength bytes
                membersInStructure[level]--;
                if (DEBUG>=1) System.out.println("KV_DEBUG> skipStructure (t="+t+"), TYPEDESC_OCTET_STRING | TYPEDESC_VISIBLE_STRING, level="+level);
            }
            else if ((responseData[t] == TYPEDESC_DOUBLE_LONG_UNSIGNED) ||
                     (responseData[t] == TYPEDESC_DOUBLE_LONG)) {
                t++; // skip tag
                t+=4; // skip double unsigned long (4byte) value
                membersInStructure[level]--;
                if (DEBUG>=1) System.out.println("KV_DEBUG> skipStructure (t="+t+"), TYPEDESC_DOUBLE_LONG_UNSIGNED | TYPEDESC_DOUBLE_LONG, level="+level);
            }
            else if ((responseData[t] == TYPEDESC_BOOLEAN) ||
                     (responseData[t] == TYPEDESC_INTEGER) ||
                     (responseData[t] == TYPEDESC_UNSIGNED)) {
                t++; // skip tag
                t++; // skip (1byte) value
                membersInStructure[level]--;
                if (DEBUG>=1) System.out.println("KV_DEBUG> skipStructure (t="+t+"), TYPEDESC_BOOLEAN | TYPEDESC_INTEGER | TYPEDESC_UNSIGNED, level="+level);
            }
            // KV 28072004
            else if (responseData[t] == TYPEDESC_LONG64) {
                t++; // skip tag
                t+=8; // skip (8byte) value
                membersInStructure[level]--;
                if (DEBUG>=1) System.out.println("KV_DEBUG> skipStructure (t="+t+"), TYPEDESC_LONG64, level="+level);
            }
            // Skip the access rights structure in case of long name referencing...
            else if (responseData[t] == TYPEDESC_STRUCTURE) {
                t++; // skip structure tag
                membersInStructure[level]--;
                level++;
                membersInStructure[level] = responseData[t];
                t++; // skip nr of members
                if (DEBUG>=1) System.out.println("KV_DEBUG> skipStructure (t="+t+"), TYPEDESC_STRUCTURE, level="+level);
            }
            else if (responseData[t] == TYPEDESC_ARRAY) {
                t++; // skip array tag
                int offset=0;
                if ((responseData[t] & 0x80) != 0) {
                    offset = (int)(responseData[t+1]&(byte)0x7F);
                    for (int i=0;i<offset;i++) {
                        elementsInArray = elementsInArray << 8;
                        elementsInArray |= ((long)responseData[t+2+(int)i]& 0x000000ff);
                    }
                }
                else elementsInArray = (long)responseData[t] & 0x000000FF;
                t += (offset+1); // skip nr of elements
                
                membersInStructure[level]--;
                level++;
                membersInStructure[level] = (int)elementsInArray;
                
                if (DEBUG>=1) System.out.println("KV_DEBUG> skipStructure (t="+t+"), TYPEDESC_ARRAY, level="+level+", elementsInArray="+elementsInArray);
            }
            else if (responseData[t] == TYPEDESC_NULL) {
                t++; // skip tag
                membersInStructure[level]--;
                if (DEBUG>=1) System.out.println("KV_DEBUG> skipStructure (t="+t+"), TYPEDESC_NULL, level="+level);
            }
            // KV 05042007
            else if (responseData[t] ==TYPEDESC_ENUM) {
                t++; // skip tag
                t++; // skip (1byte) value
                membersInStructure[level]--;
                if (DEBUG>=1) System.out.println("KV_DEBUG> skipStructure (t="+t+"), TYPEDESC_ENUM, level="+level);
            }
            else throw new IOException("AbsrtactCosemObject, skipStructure, Error parsing objectlistdata, unknown response tag "+responseData[t]);
            
            // if all members of a structure are handled, decrement level...
            while(level>0) {
               if (membersInStructure[level] == 0) level--;
               else break;
            }

        } // while((level>0) && (membersInStructure[level]>0)) 
        
        return t;
        
    } // private int skipStructure(byte[] responseData,int t) throws IOException
    
    protected ObjectReference getObjectReference(byte[] ln,int sn) throws IOException {
        if (protocolLink.getReference() == ProtocolLink.LN_REFERENCE)
            return new ObjectReference(ln);
        else if (protocolLink.getReference() == ProtocolLink.SN_REFERENCE)
            return new ObjectReference(sn);
        throw new IOException("AbstractCosemObject, getObjectReference, invalid reference type "+protocolLink.getReference());
    }    
    
    /**
     * Getter for property objectReference.
     * @return Value of property objectReference.
     */
    public com.energyict.dlms.cosem.ObjectReference getObjectReference() {
        return objectReference;
    }
}
