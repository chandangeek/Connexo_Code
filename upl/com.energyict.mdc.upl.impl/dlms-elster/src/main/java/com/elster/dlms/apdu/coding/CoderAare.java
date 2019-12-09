/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderAare.java $
 * Version:     
 * $Id: CoderAare.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 16:32:57
 */
package com.elster.dlms.apdu.coding;

import com.elster.ber.coding.BerDecoderCollection;
import com.elster.ber.coding.BerEncoder;
import com.elster.ber.coding.BerIds;
import com.elster.ber.types.BerCollection;
import com.elster.ber.types.BerId;
import com.elster.ber.types.BerValueBitString;
import com.elster.ber.types.BerValueInt;
import com.elster.ber.types.BerValueObjectIdentifer;
import com.elster.ber.types.BerValueOctetString;
import com.elster.coding.AbstractCoder;
import com.elster.dlms.cosem.application.services.open.AuthenticationValue;
import com.elster.dlms.cosem.application.services.open.ConfirmedServiceError;
import com.elster.dlms.cosem.application.services.open.FailureType;
import com.elster.dlms.cosem.application.services.open.NegotiatedXDlmsContext;
import com.elster.dlms.cosem.application.services.open.OpenResponse;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.ObjectIdentifier;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * En-/decoder for the AARE PDU.
 *
 * @author osse
 */
public class CoderAare extends AbstractCoder<OpenResponse>
{
  private final static BitString DEFAULT_PROTOCOL_VERSION = new BitString(1, new byte[]
          {
            1
          });
  private final CoderInitiateResponse coderInitiateResponse;//= new CoderInitiateResponse(true, true);
  private final CoderConfirmedServiceError coderConfirmedServiceError;// = new CoderConfirmedServiceError(true);
  private final BerAuthValueConverter authValueConverter = new BerAuthValueConverter();
  private final BerEncoder berEncoder = new BerEncoder();
  private final boolean decodeUserinfo;

//  public CoderAare(boolean decodeUserinfo)
//  {
//    this(decodeUserinfo,true);
//  }

  /**
   * Constructor with the possibility to skip the decoding of the VAA name.<P>
   * This decoding must be skipped to decode the AARE from an EK240.<P>
   * The VAA Name will be set to -1 instead.
   *
   * @param decodeVaaName {@code true} the VAA name will be decoded (normal mode).  {@code false} the VAA name
   * will not be decoded.
   */
  public CoderAare(boolean decodeUserinfo, boolean decodeVaaName)
  {
    this.decodeUserinfo= decodeUserinfo;

    if (decodeUserinfo)
    {
      coderInitiateResponse= new CoderInitiateResponse(true, decodeVaaName);
      coderConfirmedServiceError = new CoderConfirmedServiceError(true);
    }
    else
    {
      coderInitiateResponse= null;
      coderConfirmedServiceError = null;
    }
  }

  /**
   * Encodes the open response to the output stream.<P>
   * If the user info of the open response is not {@code null} the negotiatedXDlmsContext and the 
   * xDlmsError properties are ignored.
   * 
   * @param openResponse The open response to encode.
   * @param out The output stream to encode to.
   * @throws IOException
   */
  @Override
  public void encodeObject(OpenResponse openResponse, OutputStream out) throws IOException
  {
    BerCollection aare = new BerCollection(BerCodingAare.ID_AARE);

    //--- protocol version (only if not default value) ---
    if (openResponse.getProtocolVersion() != null && !openResponse.getProtocolVersion().equals(
            DEFAULT_PROTOCOL_VERSION))
    {
      aare.add(new BerValueBitString(BerCodingAare.ID_PROTOCOL_VERSION, openResponse.getProtocolVersion()));
    }

    //--- Application context name ---
    if (openResponse.getApplicationContextName() != null)
    {
      BerCollection applicationContextName = new BerCollection(BerCodingAare.ID_APPLICATION_CONTEXT_NAME);
      applicationContextName.add(new BerValueObjectIdentifer(BerIds.ID_OID, openResponse.
              getApplicationContextName()));
      aare.add(applicationContextName);
    }
    else
    {
      throw new IOException("An application context name must be provided");
    }

    //--- result ---
    BerCollection result = new BerCollection(BerCodingAare.ID_RESULT);
    result.add(new BerValueInt(BerIds.ID_INT, openResponse.getResult()));
    aare.add(result);

    //--- result source diagnostic ---

    if (openResponse.getFailureType() != null)
    {
      BerCollection resultSourceDiagnostic = new BerCollection(BerCodingAare.ID_RESULT_SOURCE_DIAGNOSTIC);
      BerCollection type = new BerCollection(new BerId(BerId.Tag.CONTEXT_SPECIFIC, true, openResponse.
              getFailureType().getType()));
      resultSourceDiagnostic.add(type);
      type.add(new BerValueInt(BerIds.ID_INT, openResponse.getFailureType().getReason()));

      aare.add(resultSourceDiagnostic);
    }
    else
    {
      throw new IOException("An failure type must be provided");
    }



    //--- Responding Ap Title (optional)---
    if (openResponse.getRespondingApTitle() != null)
    {
      BerCollection respondingApTitle = new BerCollection(BerCodingAare.ID_RESPONDING_AP_TITLE);
      respondingApTitle.add(new BerValueOctetString(BerIds.ID_OCTETSTRING,
                                                    openResponse.getRespondingApTitle()));
      aare.add(respondingApTitle);
    }

    //--- ACSE Requirements ("optional") ---
    if (openResponse.getAcseRequirements() != null)
    {
      aare.add(new BerValueBitString(BerCodingAare.ID_RESPONDER_ACSE_REQUIREMENTS, openResponse.
              getAcseRequirements()));
    }

    //--- Security mechanism name (optional) ---
    if (openResponse.getSecurityMechanismName() != null)
    {
      aare.add(new BerValueObjectIdentifer(BerCodingAare.ID_MECHANISM_NAME,
                                           openResponse.getSecurityMechanismName()));
    }


    //--- Responding authentication value (optional) ---
    if (openResponse.getRespondingAuthenticationValue() != null)
    {
      aare.add(authValueConverter.buildBerCollection(BerCodingAare.ID_RESPONDING_AUTHENTICATION_VALUE,
                                                     openResponse.getRespondingAuthenticationValue()));
    }

    //--- user info (optional) ---

    if (openResponse.getUserInfo() != null)
    {
      BerCollection userInfo = new BerCollection(BerCodingAare.ID_USER_INFO);
      userInfo.add(new BerValueOctetString(BerIds.ID_OCTETSTRING, openResponse.getUserInfo()));
      aare.add(userInfo);
    }
    else if(openResponse.getNegotiatedXDlmsContext() != null)
    {
      ByteArrayOutputStream userInfoOut = new ByteArrayOutputStream();
      coderInitiateResponse.encodeObject(openResponse.getNegotiatedXDlmsContext(), userInfoOut);

      BerCollection userInfo = new BerCollection(BerCodingAare.ID_USER_INFO);
      userInfo.add(new BerValueOctetString(BerIds.ID_OCTETSTRING, userInfoOut.toByteArray()));

      aare.add(userInfo);
    }

    berEncoder.encode(out, aare);
  }

  @Override
  public OpenResponse decodeObject(InputStream in) throws IOException
  {
    BerDecoderCollection coderAarq = BerCodingAare.buildDecoderAare();
    BerCollection decoded = coderAarq.decode(in);
    
    if (!decoded.getIdentifier().equals(BerCodingAare.ID_AARE))
    {
      throw new IOException("Wrong ID");
    }

    OpenResponse openResponse = new OpenResponse();

    //--- protocol Version (optional with default value)---
    BitString protocolVersion = decoded.findValue(BitString.class, BerCodingAare.ID_PROTOCOL_VERSION);
    if (protocolVersion == null) //Default for protocol version
    {
      protocolVersion = DEFAULT_PROTOCOL_VERSION;
    }
    openResponse.setProtocolVersion(protocolVersion);

    //--- Application context name ---
    ObjectIdentifier applicationContextName = decoded.findValue(ObjectIdentifier.class,
                                                                BerCodingAare.ID_APPLICATION_CONTEXT_NAME,
                                                                BerIds.ID_OID);
    if (applicationContextName == null)
    {
      throw new IOException("An application context name must be provided");
    }
    openResponse.setApplicationContextName(applicationContextName);


    //--- result ---
    Integer result = decoded.findValue(Integer.class, BerCodingAare.ID_RESULT, BerIds.ID_INT);

    if (result == null)
    {
      throw new IOException("An result must be provided");
    }
    openResponse.setResult(result);


    //--- result source diagnostic ---

    Integer rdServiceUser = decoded.findValue(Integer.class, BerCodingAare.ID_RESULT_SOURCE_DIAGNOSTIC,
                                              BerCodingAare.ID_RESULT_SOURCE_DIAGNOSTIC_ACSE_SERVICE_USER,
                                              BerIds.ID_INT);

    Integer rdServiceProvider = decoded.findValue(Integer.class, BerCodingAare.ID_RESULT_SOURCE_DIAGNOSTIC,
                                                  BerCodingAare.ID_RESULT_SOURCE_DIAGNOSTIC_ACSE_SERVICE_PROVIDER,
                                                  BerIds.ID_INT);

    if ((rdServiceUser == null && rdServiceProvider == null) || (rdServiceUser != null && rdServiceProvider
                                                                                          != null))
    {
      throw new IOException("An result source diagnostic must be provided");
    }

    FailureType failureType = new FailureType();

    if (rdServiceUser != null)
    {
      failureType.setType(1);
      failureType.setReason(rdServiceUser);
    }

    if (rdServiceProvider != null)
    {
      failureType.setType(2);
      failureType.setReason(rdServiceProvider);
    }

    openResponse.setFailureType(failureType);

    //--- Responding Ap Title (optional)---
    byte[] respondingApTitle = decoded.findValue(byte[].class, BerCodingAare.ID_RESPONDING_AP_TITLE,
                                                 BerIds.ID_OCTETSTRING);
    openResponse.setRespondingApTitle(respondingApTitle);

    //--- ACSE Requirements ("optional") ---
    BitString acseRequirements = decoded.findValue(BitString.class,
                                                   BerCodingAare.ID_RESPONDER_ACSE_REQUIREMENTS);
    openResponse.setAcseRequirements(acseRequirements);

    //--- Security mechanism name (optional) ---
    ObjectIdentifier securityMechanismName = decoded.findValue(ObjectIdentifier.class,
                                                               BerCodingAare.ID_MECHANISM_NAME);
    openResponse.setSecurityMechanismName(securityMechanismName);

    //--- Responding authentication value (optional) ---
    AuthenticationValue respondingAuthenticationValue = null;
    if (decoded.valueExists(BerCodingAare.ID_RESPONDING_AUTHENTICATION_VALUE))
    {
      respondingAuthenticationValue = authValueConverter.buildAuthenticationValue(decoded.findBerValue(
              BerCollection.class, BerCodingAare.ID_RESPONDING_AUTHENTICATION_VALUE));
    }
    openResponse.setRespondingAuthenticationValue(respondingAuthenticationValue);

    //--- user info ---
    NegotiatedXDlmsContext negotiatedXDlmsContext = null;
    ConfirmedServiceError xDlmsInitiateError = null;
    byte[] userInfo = decoded.findValue(byte[].class, BerCodingAare.ID_USER_INFO, BerIds.ID_OCTETSTRING);
    openResponse.setUserInfo(userInfo);

    if (userInfo != null && userInfo.length > 0)
    {
      switch (userInfo[0])
      {
        case 0x08:
          negotiatedXDlmsContext = coderInitiateResponse.decodeObject(new ByteArrayInputStream(userInfo));
          break;
        case 0x0E:
          xDlmsInitiateError = coderConfirmedServiceError.decodeObject(new ByteArrayInputStream(userInfo));
          break;
      }
    }
    openResponse.setNegotiatedXDlmsContext(negotiatedXDlmsContext);
    openResponse.setxDlmsInitiateError(xDlmsInitiateError);

    return openResponse;
  }

  public boolean isDecodeUserinfo()
  {
    return decodeUserinfo;
  }
  
  

}
