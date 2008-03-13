/*
 * AbstractResponse.java
 *
 * Created on 16 oktober 2005, 17:12
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import java.io.*;
import com.energyict.protocol.*;

/**
 *
 * @author Koen
 */
abstract public class AbstractResponse {
    
    abstract protected void parse(ResponseData responseData) throws IOException;

    
    // General response codes
    static public final int OK=0x00; // No problems, request accepted
    
    // <nok> all possible codes below
    static public final int ERR=0x01; // Rejection of the received service request. Reason is not provided
    // application level errors response codes
    static public final int SNS=0x02; // Service not supported. Message was valid but could not be honored
    static public final int ISC=0x03; // Insufficient Security Clearance. Authorization level insufficient to complete request
    static public final int ONP=0x04; // Operation not possible. Message was valid but could not be processed. E.g. invalid length, invalid offset
    static public final int IAR=0x05; // Inappropriate Action Requested. Action requested was not appropriate. E.g. write to a read only table or an invalid table id
    static public final int BSY=0x06; // Device Busy.
    static public final int DNR=0x07; // Data not Ready. requested data is not ready to be accessed.
    static public final int DLK=0x08; // Data Locked. Data can not be accessed.
    static public final int RNO=0x09; // Renegotiate Request. Responding device wishes to return to the id or base state and re-negotiate the communicationparameters
    static public final int ISSS=0x0A; // Invalid Service Sequence State. Request is not accepted at the current service sequence state.
    
    static public final int SNAPSHOT_ERROR=0xFF; // KV added 11052007  It appears that some KV2 meters are not happy with the use of the snapshot command...
                                                 // The meter seems to return all rubbish data after issuing that command!!
                                                 // Therefor a custom property has been created UseSnapshotProcedure
    
    PSEMServiceFactory psemServiceFactory;
    
    private int responseCode;
       
    
    /** Creates a new instance of AbstractResponse */
    public AbstractResponse(PSEMServiceFactory psemServiceFactory) {
        this.psemServiceFactory=psemServiceFactory;
    }
    
    public void build(ResponseData responseData) throws IOException {
        
        // send and get response
        byte[] response=responseData.getData();
        
        // op één of andere manier moeten we hier de response data terug krijgen van de requset...
        
        setResponseCode(C12ParseUtils.getInt(response,0));
        if (getResponseCode() == OK)
           parse(responseData);
        else {
            String additionalInfo = getPSEMServiceFactory().getTableId()==-1?"":"Table "+getPSEMServiceFactory().getTableId()+", ";
            switch(getResponseCode()) {
                case ERR: {
                    throw new ResponseIOException(additionalInfo+"Rejection of the received service request.",getResponseCode());
                }  // ERR
                case SNS: {
                    throw new ResponseIOException(additionalInfo+"Service not supported. Message was valid but could not be honored.",getResponseCode());
                }  // SNS
                case ISC: {
                    throw new ResponseIOException(additionalInfo+"Insufficient Security Clearance. Authorization level insufficient to complete request.",getResponseCode());
                }  // ISC
                case ONP: {
                    throw new ResponseIOException(additionalInfo+"Operation not possible. Message was valid but could not be processed.",getResponseCode());
                }  // ONP
                case IAR: {
                    throw new ResponseIOException(additionalInfo+"Inappropriate Action Requested. Action requested was not appropriate.",getResponseCode());
                }  // IAR
                case BSY: {
                    throw new ResponseIOException(additionalInfo+"Device Busy.",getResponseCode());
                }  // BSY
                case DNR: {
                    throw new ResponseIOException(additionalInfo+"Data not Ready. requested data is not ready to be accessed.",getResponseCode());
                }  // DNR
                case DLK: {
                    throw new ResponseIOException(additionalInfo+"Data Locked. Data can not be accessed.",getResponseCode());
                }  // DLK
                case RNO: {
                    throw new ResponseIOException(additionalInfo+"Renegotiate Request. Responding device wishes to return to the id or base state and re-negotiate the communicationparameters.",getResponseCode());
                }  // RNO
                case ISSS: {
                    throw new ResponseIOException(additionalInfo+"Invalid Service Sequence State. Request is not accepted at the current service sequence state.",getResponseCode());
                }  // ISSS
            }
        }
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
    
    public PSEMServiceFactory getPSEMServiceFactory() {
        return psemServiceFactory;
    }
    
}
