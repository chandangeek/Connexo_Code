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

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractResponse {
    
    abstract protected void parse(ResponseData responseData) throws IOException;

    
    // General response codes
    static public final int OK=0x00;   // No problems, request accepted
    
    // <nok> all possible codes below
    static public final int ERR=0x01;  // Rejection of the received service request. Reason is not provided
    // application level errors response codes
    static public final int SNS=0x02;  // Service not supported. Message was valid but could not be honored
    static public final int ISC=0x03;  // Insufficient Security Clearance. Authorization level insufficient to complete request
    static public final int ONP=0x04;  // Operation not possible. Message was valid but could not be processed. E.g. invalid length, invalid offset
    static public final int IAR=0x05;  // Inappropriate Action Requested. Action requested was not appropriate. E.g. write to a read only table or an invalid table id
    static public final int BSY=0x06;  // Device Busy.
    static public final int DNR=0x07;  // Data not Ready. requested data is not ready to be accessed.
    static public final int DLK=0x08;  // Data Locked. Data can not be accessed.
    static public final int RNO=0x09;  // Renegotiate Request. Responding device wishes to return to the id or base state and re-negotiate the communication parameters
    static public final int ISSS=0x0A; // Invalid Service Sequence State. Request is not accepted at the current service sequence state.
    static public final int SME=0x0B;  // Security Mechanism Error. Covers errors 'security mechanism not supported' and 'invalid encryption key'
    static public final int UAT=0x0C;  // Unknown Application Title. Returned when an unknown or invalid <called-AP-title> s received
    static public final int NETT=0x0D; // Network Time-out
    static public final int NETR=0x0E; // Network Not Reachable
    static public final int RQTL=0x0F; // Request Too Large
    static public final int RSTL=0x10; // Response Too Large
    static public final int SGNP=0x11; // Segmentation not possible
    static public final int SGERR=0x12;// Segmentation error

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
                }
                case SNS: {
                    throw new ResponseIOException(additionalInfo+"Service not supported. Message was valid but could not be honored.",getResponseCode());
                }
                case ISC: {
                    throw new ResponseIOException(additionalInfo+"Insufficient Security Clearance. Authorization level insufficient to complete request.",getResponseCode());
                }
                case ONP: {
                    throw new ResponseIOException(additionalInfo+"Operation not possible. Message was valid but could not be processed.",getResponseCode());
                }
                case IAR: {
                    throw new ResponseIOException(additionalInfo+"Inappropriate Action Requested. Action requested was not appropriate.",getResponseCode());
                }
                case BSY: {
                    throw new ResponseIOException(additionalInfo+"Device Busy.",getResponseCode());
                }
                case DNR: {
                    throw new ResponseIOException(additionalInfo+"Data not Ready. requested data is not ready to be accessed.",getResponseCode());
                }
                case DLK: {
                    throw new ResponseIOException(additionalInfo+"Data Locked. Data can not be accessed.",getResponseCode());
                }
                case RNO: {
                    throw new ResponseIOException(additionalInfo+"Renegotiate Request. Responding device wishes to return to the id or base state and re-negotiate the communicationparameters.",getResponseCode());
                }
                case ISSS: {
                    throw new ResponseIOException(additionalInfo+"Invalid Service Sequence State. Request is not accepted at the current service sequence state.",getResponseCode());
                }
                case SME: {
                    throw new ResponseIOException(additionalInfo+"Security Mechanism Error.",getResponseCode());
                }
                case UAT: {
                    throw new ResponseIOException(additionalInfo+"Unknown Application Title. Returned when an unknown or invalid <called-AP-title> s received.",getResponseCode());
                }
                case NETT: {
                    throw new ResponseIOException(additionalInfo+"Network Time-out.",getResponseCode());
                }
                case NETR: {
                    throw new ResponseIOException(additionalInfo+"Network Not Reachable.",getResponseCode());
                }
                case RQTL: {
                    throw new ResponseIOException(additionalInfo+"Request Too Large.",getResponseCode());
                }
                case RSTL: {
                    throw new ResponseIOException(additionalInfo+"Response Too Large.",getResponseCode());
                }
                case SGNP: {
                    throw new ResponseIOException(additionalInfo+"Segmentation not possible.",getResponseCode());
                }
                case SGERR: {
                    throw new ResponseIOException(additionalInfo+"Segmentation error.",getResponseCode());
                }
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
