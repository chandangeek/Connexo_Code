package com.energyict.protocolimpl.dlms.Z3;

import com.energyict.dlms.DLMSConnection;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

public class AARQ {

	/** The default APDU size. */
	private static final int DEFAULT_APDU_SIZE = 1200;

	/** The password used. */
	private String password;

	/** The DLMS connection to write the AARQ on. */
	private DLMSConnection dlmsConnection;

	/**
	 * Examples of AARQ APDU's are available in GreenBook "11. AARQ and AARE encoding examples"
	 */
    byte[] aarqNoAuthentication={
		    (byte)0xE6,(byte)0xE6,(byte)0x00,
		    (byte)0x60, // AARQ
		    (byte)0x1D, // bytes to follow
		    AARE_APPLICATION_CONTEXT_NAME,(byte)0x09,(byte)0x06,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x01,(byte)0x01, //application context name , LN no ciphering
		    AARE_USER_INFORMATION,(byte)0x10,(byte)0x04,(byte)0x0E, // user information field context
		    (byte)0x01, // initiate request
		    (byte)0x00,(byte)0x00,(byte)0x00, // unused parameters
		    (byte)0x06,  // dlms version nr (xDLMS)
		    (byte)0x5F,(byte)0x1F,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x7E,(byte)0x1F, // proposed conformance
		    (byte)0x04,(byte)0xB0}; //client-max-received-pdu-size

    byte[] aarqlowlevel={
		    (byte)0xE6,(byte)0xE6,(byte)0x00,
		    (byte)0x60, // AARQ
		    (byte)0x36, // bytes to follow
		    AARE_APPLICATION_CONTEXT_NAME,(byte)0x09,(byte)0x06,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x01,(byte)0x01, //application context name , LN no ciphering
		    (byte)0x8A,(byte)0x02,(byte)0x07,(byte)0x80, // ACSE requirements
		    (byte)0x8B,(byte)0x07,(byte)0x60,(byte)0x85,(byte)0x74,(byte)0x05,(byte)0x08,(byte)0x02,(byte)0x01}; // mechanism-name component

    byte[] aarqlowlevel2={AARE_USER_INFORMATION,(byte)0x10,(byte)0x04,(byte)0x0E,
		    (byte)0x01, // initiate request
		    (byte)0x00,(byte)0x00,(byte)0x00, // unused parameters
		    (byte)0x06,  // dlms version nr
		    (byte)0x5F,(byte)0x1F,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x7E,(byte)0x1F, // proposed conformance
		    (byte)0x04,(byte)0xB0};
		    //client-max-received-pdu-size

	byte[] rlrq_APDU={
			(byte)0xE6, (byte)0xE6, (byte)0x00,
			(byte)0x62, (byte)0x03, (byte)0x80, (byte)0x00, (byte)0x00};

    private static final byte AARE_TAG							=	0x61;
    private static final byte AARE_APPLICATION_CONTEXT_NAME 	= 	(byte)0xA1;
    private static final byte AARE_RESULT 						= 	(byte)0xA2;
    private static final byte AARE_RESULT_SOURCE_DIAGNOSTIC 	= 	(byte)0xA3;
    private static final byte AARQ_CALLING_AUTHENTICATION_VALUE = 	(byte)0xAC;
    private static final byte ACSE_SERVICE_USER 				= 	(byte)0xA1;
    private static final byte ACSE_SERVICE_PROVIDER 			= 	(byte)0xA2;
    private static final byte AARE_USER_INFORMATION 			= 	(byte)0xBE;
    private static final byte DLMS_PDU_INITIATE_RESPONSE 		= 	(byte)0x08;
    private static final byte DLMS_PDU_CONFIRMED_SERVICE_ERROR	=	(byte)0x0E;

    public AARQ(){

    }

    /**
     * Creates and executes an AARQ, using the default APDU size {@link #DEFAULT_APDU_SIZE}.
     *
     * @param 	securityLevel		The security level.
     * @param 	password			The password.
     * @param 	dlmsConnection		The DLMS connection.
     *
     * @throws	IOException			If an error occurs during the AARQ execution.
     */
    public AARQ(int securityLevel, String password, DLMSConnection dlmsConnection) throws IOException{
    	this(securityLevel, password, dlmsConnection, DEFAULT_APDU_SIZE);
    }

    /**
     * Create a new association request. Allows for the setting of the maximum APDU size.
     *
     * @param 	securityLevel		The security level.
     * @param 	password			The password.
     * @param 	connection			The connection to use.
     * @param	maximumAPDUSize		The maximum size of the APDU to negotiate.
     *
     * @throws 	IOException			If the AARQ request fails.
     */
    public AARQ(final int securityLevel, final String password, final DLMSConnection connection, final int maximumAPDUSize) throws IOException {
    	this.password = password;
    	this.dlmsConnection = connection;

    	this.updateAPDUSize(maximumAPDUSize);

    	// FIXME: Constructors should never do anything but initialize the object.
    	this.requestApplicationAssociation(securityLevel);
    }

    /**
     * Updates the APDU size for this AARQ. It actually updates the static structures. We should probably just create the AARQ
     * instead of having this static data.
     *
     * @param 	apduSize		The APDU size.
     */
    private final void updateAPDUSize(final int apduSize) {
    	final byte apduSizeLSB = (byte)(apduSize & 0xFF);
    	final byte apduSizeMSB = (byte)(apduSize >> 8);

    	this.aarqNoAuthentication[this.aarqNoAuthentication.length - 1] = apduSizeLSB;
    	this.aarqNoAuthentication[this.aarqNoAuthentication.length - 2] = apduSizeMSB;
    	this.aarqlowlevel2[this.aarqlowlevel2.length - 1] = apduSizeLSB;
    	this.aarqlowlevel2[this.aarqlowlevel2.length - 2] = apduSizeMSB;
    }

	private void requestApplicationAssociation(int securityLevel) throws IOException {
		byte[] aarq = null;

		if(securityLevel == 0){		// no authentication
			aarq = aarqNoAuthentication;
		} else if(securityLevel == 1){		// low level authentication
			aarq = getLowLevelAuthentication();
		} else if(securityLevel == 2){
			aarq = getLowLevelAuthentication();	//TODO should be highLevel authentication, but not sure how
		}

		doRequestApplicationAssociation(aarq);
	}

	private void doRequestApplicationAssociation(byte[] aarq) throws IOException {
		byte[] responseData;
		responseData = this.dlmsConnection.sendRequest(aarq);
		CheckAARE(responseData);

	}

	private byte[] getLowLevelAuthentication() {
		return buildAarq(aarqlowlevel, aarqlowlevel2);
	}

	private byte[] buildAarq(byte[] aarq1, byte[] aarq2) {
		byte[] aarq = null;
		int t = 0;

		// prepare aarq buffer
		aarq = new byte[3+aarq1.length + 1 + (password == null ? 0:password.length()) + aarq2.length];

		// copy aarq1 to aarq buffer
		for(int i = 0; i < aarq1.length; i++){
			aarq[t++] = aarq1[i];
		}

		// calling authentication
		aarq[t++] = AARQ_CALLING_AUTHENTICATION_VALUE;
		aarq[t++] = (byte)((password==null?0:password.length())+2); // length to follow
		aarq[t++] = (byte)0x80; // tag representation

		// copy password to aarq buffer
        aarq[t++] = (byte)(password==null?0:password.length());
        for (int i=0;i<(password==null?0:password.length());i++)
            aarq[t++] = (byte)password.charAt(i);

        // copy aarq2 to aarq buffer
        for (int i=0;i<aarq2.length;i++){
            aarq[t++] = aarq2[i];
        }

        aarq[4] = (byte)(((int)aarq.length&0xFF)-5); // Total length of frame - headerlength
		return aarq;
	}

    private void CheckAARE(byte[] responseData) throws IOException
    {
    	//System.out.println(responseData.length);
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
                       (responseData[i+3] == 0)){
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
                                 (responseData[i+4] == 1)){
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
                             else{
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
                     else{
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

                       } else if (DLMS_PDU_CONFIRMED_SERVICE_ERROR == responseData[i+3]){
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
                       } else {
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

	public void disConnect() throws IOException{
		try {
			byte[] responseData = dlmsConnection.sendRequest(rlrq_APDU);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Failed to successfully disconnect.");
		}
	}

	public static void main(String[] args){
//		000100010010002b6129a1090607
//		60857405080101a203020100a305a103
//		020100be10040e0800065f1f04000010
//		1802000007

		/*byte[] response = new byte[]{0x00, 0x01, 0x00, 0x01, 0x00, 0x10, 0x00, 0x2b, 0x61, 0x29, (byte)0xa1, 0x09, 0x06, 0x07, 0x60, (byte)0x85, 0x74,
				0x05, 0x08, 0x01, 0x01, (byte)0xa2, 0x03, 0x02, 0x01, 0x00, (byte)0xa3, 0x05, (byte)0xa1, 0x03, 0x02, 0x01, 0x00, (byte)0xbe, 0x10, 0x04, 0x0e,
				0x08, 0x00, 0x06, 0x5f, 0x1f, 0x04, 0x00, 0x00, 0x10, 0x18, 0x02, 0x00, 0x00, 0x07};*/
		final byte[] resp = new byte[] {
				0x00, 0x01, 0x00, 0x01, 0x00, 0x10, 0x00, 0x2b, 0x61, 0x29, (byte)0xa1, 0x09, 0x06, 0x07, 0x60, (byte)0x85, 0x74, 0x05, 0x08, 0x01, 0x01, (byte)0xa2, 0x03, 0x02, 0x01, 0x00, (byte)0xa3, 0x05, (byte)0xa1, 0x03, 0x02, 0x01, 0x00, (byte)0xbe, 0x10, 0x04, 0x0e, 0x08, 0x00, 0x06, 0x5f, 0x1f, 0x04, 0x00, 0x00, 0x10, 0x19, 0x02, 0x00, 0x00, 0x07
		};


		AARQ aarq = new AARQ();
		try {
			aarq.CheckAARE(resp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
