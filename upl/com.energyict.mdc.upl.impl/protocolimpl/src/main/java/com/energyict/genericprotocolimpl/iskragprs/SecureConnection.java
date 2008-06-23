/**
 * 
 */
package com.energyict.genericprotocolimpl.iskragprs;

import java.io.IOException;

import com.energyict.dlms.DLMSConnection;
import com.energyict.protocol.ProtocolUtils;

/**
 * @author gna
 *
 */
public class SecureConnection {
	
	private int DEBUG = 0;
	private String firmwareVersion = "";
	private String strPassword = "";
	
	DLMSConnection dlmsConnection;
	
    byte[] aarqlowestlevel={
		    (byte)0xE6,(byte)0xE6,(byte)0x00,
		    (byte)0x60, // AARQ
		    (byte)0x1D, // bytes to follow
		    (byte)0xA1,(byte)0x09,(byte)0x06,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x01,(byte)0x01, //application context name , LN no ciphering
		    (byte)0xBE,(byte)0x10,(byte)0x04,(byte)0x0E,
		    (byte)0x01, // initiate request
		    (byte)0x00,(byte)0x00,(byte)0x00, // unused parameters
		    (byte)0x06,  // dlms version nr
		    (byte)0x5F,(byte)0x1F,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x7E,(byte)0x1F, // proposed conformance
		    (byte)0xFF,(byte)0xFF};
    
	byte[] aarqlowlevel17={
		    (byte)0xE6,(byte)0xE6,(byte)0x00,
		    (byte)0x60, // AARQ
		    (byte)0x37, // bytes to follow
		    (byte)0xA1,(byte)0x09,(byte)0x06,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x01,(byte)0x01, //application context name , LN no ciphering
		    (byte)0xAA,(byte)0x02,(byte)0x07,(byte)0x80, // ACSE requirements
		    (byte)0xAB,(byte)0x09,(byte)0x06,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x02,(byte)0x01};
		    //(byte)0xAC,(byte)0x0A,(byte)0x04}; //,(byte)0x08,(byte)0x41,(byte)0x42,(byte)0x43,(byte)0x44,(byte)0x45,(byte)0x46,(byte)0x47,(byte)0x48,

    byte[] aarqlowlevel17_2={
		    (byte)0xBE,(byte)0x0F,(byte)0x04,(byte)0x0D,
		    (byte)0x01, // initiate request
		    (byte)0x00,(byte)0x00,(byte)0x00, // unused parameters
		    (byte)0x06,  // dlms version nr
		    (byte)0x5F,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x10,(byte)0x1D, // proposed conformance
		    (byte)0x21,(byte)0x34};
    
    byte[] aarqlowlevelOLD={
		    (byte)0xE6,(byte)0xE6,(byte)0x00,
		    (byte)0x60, // AARQ
		    (byte)0x35, // bytes to follow
		    (byte)0xA1,(byte)0x09,(byte)0x06,(byte)0x07,
		    (byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x01,(byte)0x01, //application context name , LN no ciphering
		    (byte)0x8A,(byte)0x02,(byte)0x07,(byte)0x80, // ACSE requirements
		    (byte)0x8B,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x02,(byte)0x01};
		    //(byte)0xAC}; //,(byte)0x0A,(byte)0x80}; //,(byte)0x08,(byte)0x41,(byte)0x42,(byte)0x43,(byte)0x44,(byte)0x45,(byte)0x46,(byte)0x47,(byte)0x48,

    byte[] aarqlowlevelOLD_2={(byte)0xBE,(byte)0x0F,(byte)0x04,(byte)0x0D,
		    (byte)0x01, // initiate request
		    (byte)0x00,(byte)0x00,(byte)0x00, // unused parameters
		    (byte)0x06,  // dlms version nr
		    (byte)0x5F,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x10,(byte)0x1D, // proposed conformance
		    (byte)0x21,(byte)0x34};
    
    byte[] aarqlowlevelANY={
		    (byte)0xE6,(byte)0xE6,(byte)0x00,
		    (byte)0x60, // AARQ
		    (byte)0x36, // bytes to follow
		    (byte)0xA1,(byte)0x09,(byte)0x06,(byte)0x07,
		    (byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x01,(byte)0x01, //application context name , LN no ciphering
		    (byte)0x8A,(byte)0x02,(byte)0x07,(byte)0x80, // ACSE requirements
		    (byte)0x8B,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x02,(byte)0x01};
		    
    byte[] aarqlowlevelANY_2={(byte)0xBE,(byte)0x10,(byte)0x04,(byte)0x0E,
		    (byte)0x01, // initiate request
		    (byte)0x00,(byte)0x00,(byte)0x00, // unused parameters
		    (byte)0x06,  // dlms version nr
		    (byte)0x5F,(byte)0x1F,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x10,(byte)0x1D, // proposed conformance
		    (byte)0x21,(byte)0x34};
    
	byte[] rlrq_APDU={
			(byte)0xE6, (byte)0xE6, (byte)0x00,
			(byte)0x62, (byte)0x03, (byte)0x80, (byte)0x00, (byte)0x00};
    
    private static final byte AARE_TAG							=	0x61;
    private static final byte AARE_APPLICATION_CONTEXT_NAME 	= 	(byte)0xA1;
    private static final byte AARE_RESULT 						= 	(byte)0xA2;
    private static final byte AARE_RESULT_SOURCE_DIAGNOSTIC 	= 	(byte)0xA3;
    private static final byte ACSE_SERVICE_USER 				= 	(byte)0xA1;
    private static final byte ACSE_SERVICE_PROVIDER 			= 	(byte)0xA2;
    private static final byte AARE_USER_INFORMATION 			= 	(byte)0xBE;
    private static final byte DLMS_PDU_INITIATE_RESPONSE 		= 	(byte)0x08;
    private static final byte DLMS_PDU_CONFIRMED_SERVICE_ERROR	=	(byte)0x0E;

	/**
	 * 
	 */
	public SecureConnection() {
	}

	public SecureConnection(int securityLevelProperty, String firmwareVersion, String strPassword, DLMSConnection connection) throws IOException {
		this.strPassword = strPassword;
		this.firmwareVersion = firmwareVersion;
		this.dlmsConnection = connection;
		
		requestApplAssoc(securityLevelProperty);
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	private void requestApplAssoc(int securityLevelProperty) throws IOException {
		
		byte[] aarq;
		if (securityLevelProperty == 0) {
			aarq = aarqlowestlevel;
		}
		else if (securityLevelProperty == 1) {
			aarq = getLowLevelSecurity();
		}
		else {
			aarq = getLowLevelSecurity();
		}
		doRequestApplAssoc(aarq);
	}
	
    private byte[] getLowLevelSecurity() {
        if ("1.7".compareTo(firmwareVersion) == 0) {
             return buildaarq(aarqlowlevel17,aarqlowlevel17_2);
        }
        else  if ("OLD".compareTo(firmwareVersion) == 0) {
            return buildaarq(aarqlowlevelOLD,aarqlowlevelOLD_2);
        }
        else {
            return buildaarq(aarqlowlevelANY,aarqlowlevelANY_2);
        }
     }
    
    private byte[] buildaarq(byte[] aarq1,byte[] aarq2) {
        byte[] aarq=null; 
        int i,t=0;
        // prepare aarq buffer
        aarq = new byte[3+aarq1.length+1+(strPassword==null?0:strPassword.length())+aarq2.length];
        // copy aarq1 to aarq buffer
        for (i=0;i<aarq1.length;i++)
            aarq[t++] = aarq1[i];
        
        // calling authentification
        aarq[t++] = (byte)0xAC; // calling authentification tag
        aarq[t++] = (byte)((strPassword==null?0:strPassword.length())+2); // length to follow
        aarq[t++] = (byte)0x80; // tag representation
        // copy password to aarq buffer
        aarq[t++] = (byte)(strPassword==null?0:strPassword.length());
        for (i=0;i<(strPassword==null?0:strPassword.length());i++)
            aarq[t++] = (byte)strPassword.charAt(i);
        
        
        // copy in aarq2 to aarq buffer
        for (i=0;i<aarq2.length;i++)
            aarq[t++] = aarq2[i];
        
        aarq[4] = (byte)(((int)aarq.length&0xFF)-5); // Total length of frame - headerlength
        
        return aarq;
     }
    
    private void doRequestApplAssoc(byte[] aarq) throws IOException  {
        byte[] responseData;
        responseData = getDlmsConnection().sendRequest(aarq);
        CheckAARE(responseData);
        if (DEBUG >= 2) ProtocolUtils.printResponseData(responseData);
     } // public void doRequestApplAssoc(int iLevel) throws IOException
    
    private void CheckAARE(byte[] responseData) throws IOException
    {
       int i;
//       int iLength;
       String strResultSourceDiagnostics="";
       InitiateResponse initiateResponse=new InitiateResponse(); 
       
       i=0;
       while(true)
       {
          if (responseData[i] == AARE_TAG)
          {
             i+=2; // skip tag & length
             while(true)
             {
                if (responseData[i] == AARE_APPLICATION_CONTEXT_NAME)
                {
                   i++; // skip tag
                   i += responseData[i]; // skip length + data
                } // if (responseData[i] == AARE_APPLICATION_CONTEXT_NAME)
                
                else if (responseData[i] == AARE_RESULT)
                {
                   i++; // skip tag
                   if ((responseData[i] == 3) &&
                       (responseData[i+1] == 2) &&
                       (responseData[i+2] == 1) &&
                       (responseData[i+3] == 0))
                   {
                      // Result OK
                      return;
                   }
                   i += responseData[i]; // skip length + data
                } // else if (responseData[i] == AARE_RESULT)
                
                else if (responseData[i] == AARE_RESULT_SOURCE_DIAGNOSTIC)
                {
                     i++; // skip tag
                     if (responseData[i] == 5) // check length
                     {
                         if (responseData[i+1] == ACSE_SERVICE_USER)
                         {
                             if ((responseData[i+2] == 3) &&
                                 (responseData[i+3] == 2) &&
                                 (responseData[i+4] == 1))
                             {
                                 if (responseData[i+5] == 0x00)
                                    strResultSourceDiagnostics += ", ACSE_SERVICE_USER";
                                 else if (responseData[i+5] == 0x01)
                                     strResultSourceDiagnostics += ", ACSE_SERVICE_USER, no reason given";
                                 else if (responseData[i+5] == 0x02)
                                    strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Application Context Name Not Supported";
                                 else if (responseData[i+5] == 0x0B)
                                    strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Not Recognised";
                                 else if (responseData[i+5] == 0x0C)
                                    strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Mechanism Name Required";
                                 else if (responseData[i+5] == 0x0D)
                                    strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Failure";
                                 else if (responseData[i+5] == 0x0E)
                                    strResultSourceDiagnostics += ", ACSE_SERVICE_USER, Authentication Required";
                                 else throw new IOException("Application Association Establishment failed, ACSE_SERVICE_USER, unknown result!");
                             }
                             else
                             {
                                 throw new IOException("Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_USER,  wrong tag");
                             }
                         } // if (responseData[i+1] == ACSE_SERVICE_USER)
                         else if (responseData[i+1] == ACSE_SERVICE_PROVIDER)
                         {
                             if ((responseData[i+2] == 3) &&
                                 (responseData[i+3] == 2) &&
                                 (responseData[i+4] == 1))
                             {
                                 if (responseData[i+5] == 0x00)
                                    strResultSourceDiagnostics +=", ACSE_SERVICE_PROVIDER!";
                                 else if (responseData[i+5] == 0x01)
                                    strResultSourceDiagnostics +=", ACSE_SERVICE_PROVIDER, No Reason Given!";
                                 else if (responseData[i+5] == 0x02)
                                    strResultSourceDiagnostics += ", ACSE_SERVICE_PROVIDER, No Common ACSE Version!";
                                 else throw new IOException("Application Association Establishment Failed, ACSE_SERVICE_PROVIDER, unknown result");
                             }
                             else throw new IOException("Application Association Establishment Failed, result_source_diagnostic, ACSE_SERVICE_PROVIDER,  wrong tag");
                         } // else if (responseData[i+1] == ACSE_SERVICE_PROVIDER)
                         else throw new IOException("Application Association Establishment Failed, result_source_diagnostic,  wrong tag");
                     }
                     else
                     {
                         throw new IOException("Application Association Establishment Failed, result_source_diagnostic, wrong length");
                     }

                     i += responseData[i]; // skip length + data
                } // else if (responseData[i] == AARE_RESULT_SOURCE_DIAGNOSTIC)
                
                else if (responseData[i] == AARE_USER_INFORMATION)
                {
                   i++; // skip tag
                   if (responseData[i+2] > 0) { // length of octet string
                       if (DLMS_PDU_INITIATE_RESPONSE == responseData[i+3]) {
                           initiateResponse.bNegotiatedQualityOfService=responseData[i+4];
                           initiateResponse.bNegotiatedDLMSVersionNR=responseData[i+5];
                           initiateResponse.lNegotiatedConformance=(ProtocolUtils.getInt(responseData,i+8)&0x00FFFFFF); // conformance has only 3 bytes, 24 bit
                           initiateResponse.sServerMaxReceivePduSize=ProtocolUtils.getShort(responseData,i+12);
                           initiateResponse.sVAAName=ProtocolUtils.getShort(responseData,i+14);
                           /*
                           System.out.println(initiateResponse.bNegotiatedDLMSVersionNR + " "+
                                              initiateResponse.bNegotiatedQualityOfService + " "+
                                              initiateResponse.lNegotiatedConformance + " "+
                                              initiateResponse.sServerMaxReceivePduSize + " " +
                                              initiateResponse.sVAAName);
                           */

                       }
                       else if (DLMS_PDU_CONFIRMED_SERVICE_ERROR == responseData[i+3]) 
                       {
                           if (0x01 == responseData[i+4])
                               strResultSourceDiagnostics += ", InitiateError";
                           else if (0x02 == responseData[i+4])
                               strResultSourceDiagnostics += ", getStatus";
                           else if (0x03 == responseData[i+4])
                               strResultSourceDiagnostics += ", getNameList";
                           else if (0x13 == responseData[i+4])
                               strResultSourceDiagnostics += ", terminateUpload";
                           else throw new IOException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown ConfirmedServiceError choice");

                           if (0x06 != responseData[i+5])
                               strResultSourceDiagnostics += ", No ServiceError tag";

                           if (0x00 == responseData[i+6])
                               strResultSourceDiagnostics += "";
                           else if (0x01 == responseData[i+6])
                               strResultSourceDiagnostics += ", DLMS version too low";
                           else if (0x02 == responseData[i+6])
                               strResultSourceDiagnostics += ", Incompatible conformance";
                           else if (0x03 == responseData[i+6])
                               strResultSourceDiagnostics = ", pdu size too short";
                           else if (0x04 == responseData[i+6])
                               strResultSourceDiagnostics = ", refused by the VDE handler";
                           else throw new IOException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown respons ");
                       }
                       else
                       {
                           throw new IOException("Application Association Establishment Failed, AARE_USER_INFORMATION, unknown respons!");
                       }
                       
                   } // if (responseData[i+2] > 0) --> length of the octet string
                   
                   i += responseData[i]; // skip length + data
                } // else if (responseData[i] == AARE_USER_INFORMATION)
                else
                {
                   i++; // skip tag
                   // Very tricky, suppose we receive a length > 128 because of corrupted data,
                   // then if we keep byte, it is signed and we can enter a LOOP because length will
                   // be subtracted from i!!!
                   i += (((int)responseData[i])&0x000000FF); // skip length + data
                }

                if (i++ >= (responseData.length-1))
                {
                    i=(responseData.length-1);
                    break;
                }
             } // while(true)
          
          } // if (responseData[i] == AARE_TAG)
          
          if (i++ >= (responseData.length-1))
          {
              i=(responseData.length-1);
              break;
          }
       } // while(true)

       throw new IOException("Application Association Establishment Failed"+strResultSourceDiagnostics);

    } // void CheckAARE(byte[] responseData) throws IOException

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	public String getStrPassword() {
		return strPassword;
	}

	public void setStrPassword(String strPassword) {
		this.strPassword = strPassword;
	}

	public DLMSConnection getDlmsConnection() {
		return dlmsConnection;
	}

	public void setDlmsConnection(DLMSConnection dlmsConnection) {
		this.dlmsConnection = dlmsConnection;
	}
	
	public void disConnect() throws IOException{
		byte[] responseData = getDlmsConnection().sendRequest(rlrq_APDU);
	    if (DEBUG >= 1) ProtocolUtils.printResponseData(responseData);
	}

    class InitiateResponse
    {
       protected byte bNegotiatedQualityOfService;
       protected byte bNegotiatedDLMSVersionNR;
       protected long lNegotiatedConformance;
       protected short sServerMaxReceivePduSize;
       protected short sVAAName;
        
       InitiateResponse()
       {
           bNegotiatedQualityOfService=0;
           bNegotiatedDLMSVersionNR=0;
           lNegotiatedConformance=0;
           sServerMaxReceivePduSize=0;
           sVAAName=0;
       }
    }
	
}
