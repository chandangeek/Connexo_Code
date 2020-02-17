/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderAarq.java $
 * Version:     
 * $Id: CoderAarq.java 3665 2011-10-04 17:34:41Z osse $
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
import com.elster.ber.types.BerValueBitString;
import com.elster.ber.types.BerValueObjectIdentifer;
import com.elster.ber.types.BerValueOctetString;
import com.elster.coding.AbstractCoder;
import com.elster.dlms.cosem.application.services.open.AuthenticationValue;
import com.elster.dlms.cosem.application.services.open.OpenRequest;
import com.elster.dlms.cosem.application.services.open.ProposedXDlmsContext;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.ObjectIdentifier;
import com.elster.dlms.types.basic.ServiceClass;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * En-/decoder for the AARQ PDU.
 *
 * @author osse
 */
public class CoderAarq extends AbstractCoder<OpenRequest>
{
  private final static BitString DEFAULT_PROTOCOL_VERSION = new BitString(1, new byte[]
          {
            1
          });
  private final CoderInitiateRequest coderInitiateRequest = new CoderInitiateRequest(true);
  private final BerAuthValueConverter authValueConverter = new BerAuthValueConverter();
  private final BerEncoder berEncoder = new BerEncoder();

  /**
   * Encodes the open request.<P>
   * If the user info of the open request is not null the proposed XDlms context will be ignored.
   * Otherwise the proposed XDlms context (if not null) will be encoded as user info.
   * 
   * @param openRequest The open request to encode.
   * @param out The output stream to encode to.
   * @throws IOException
   */
  @Override
  public void encodeObject(OpenRequest openRequest, OutputStream out) throws IOException
  {
    BerCollection aarq = new BerCollection(BerCodingAarq.ID_AARQ);

    //--- protocol version (only if not default value) ---
    if (openRequest.getProtocolVersion() != null && !openRequest.getProtocolVersion().equals(
            DEFAULT_PROTOCOL_VERSION))
    {
      aarq.add(new BerValueBitString(BerCodingAarq.ID_PROTOCOL_VERSION, openRequest.getProtocolVersion()));
    }

    //--- Application context name ---
    if (openRequest.getApplicationContextName() != null)
    {
      BerCollection applicationContextName = new BerCollection(BerCodingAarq.ID_APPLICATION_CONTEXT_NAME);
      applicationContextName.add(new BerValueObjectIdentifer(BerIds.ID_OID, openRequest.
              getApplicationContextName()));
      aarq.add(applicationContextName);
    }
    else
    {
      throw new IOException("An application context name must be provided");
    }

    //--- Calling Ap Title (optional)---
    if (openRequest.getCallingApTitle() != null)
    {
      BerCollection callingApTitle = new BerCollection(BerCodingAarq.ID_CALLING_AP_TITLE);
      callingApTitle.add(new BerValueOctetString(BerIds.ID_OCTETSTRING, openRequest.getCallingApTitle()));
      aarq.add(callingApTitle);
    }

    //--- ACSE Requirements ("optional") ---
    if (openRequest.getAcseRequirements() != null)
    {
      aarq.add(new BerValueBitString(BerCodingAarq.ID_SENDER_ACSE_REQUIREMENTS, openRequest.
              getAcseRequirements()));
    }

    //--- Security mechanism name (optional) ---
    if (openRequest.getSecurityMechanismName() != null)
    {
      aarq.add(new BerValueObjectIdentifer(BerCodingAarq.ID_MECHANISM_NAME,
                                           openRequest.getSecurityMechanismName()));
    }


    //--- Calling authentication value (optional) ---
    if (openRequest.getCallingAuthenticationValue() != null)
    {
      aarq.add(authValueConverter.buildBerCollection(BerCodingAarq.ID_CALLING_AUTHENTICATION_VALUE,
                                                     openRequest.getCallingAuthenticationValue()));
    }

    if (openRequest.getUserInfo() != null)
    {
      BerCollection userInfo = new BerCollection(BerCodingAarq.ID_USER_INFO);
      userInfo.add(new BerValueOctetString(BerIds.ID_OCTETSTRING, openRequest.getUserInfo()));
      aarq.add(userInfo);
    }
    else if (openRequest.getProposedXDlmsContext() != null)  //--- user info (optional) ---
    {
      ByteArrayOutputStream userInfoOut = new ByteArrayOutputStream();
      coderInitiateRequest.encodeObject(openRequest.getProposedXDlmsContext(), userInfoOut);

      BerCollection userInfo = new BerCollection(BerCodingAarq.ID_USER_INFO);
      userInfo.add(new BerValueOctetString(BerIds.ID_OCTETSTRING, userInfoOut.toByteArray()));

      aarq.add(userInfo);
    }

    berEncoder.encode(out, aarq);
  }

  @Override
  public OpenRequest decodeObject(InputStream in) throws IOException
  {
    BerDecoderCollection coderAarq = BerCodingAarq.buildDecoderAarq();
    BerCollection decoded = coderAarq.decode(in);

    OpenRequest result = new OpenRequest();

    //--- protocol Version (optional with default value)---
    BitString protocolVersion = decoded.findValue(BitString.class, BerCodingAarq.ID_PROTOCOL_VERSION);
    if (protocolVersion == null) //Default for protocol version
    {
      protocolVersion = DEFAULT_PROTOCOL_VERSION;
    }
    result.setProtocolVersion(protocolVersion);

    //--- Application context name
    ObjectIdentifier applicationContextName = decoded.findValue(ObjectIdentifier.class,
                                                                BerCodingAarq.ID_APPLICATION_CONTEXT_NAME,
                                                                BerIds.ID_OID);
    if (applicationContextName == null)
    {
      throw new IOException("An application context name must be provided");
    }
    result.setApplicationContextName(applicationContextName);

    //--- Calling Ap Title (optional)---
    byte[] callingApTitle = decoded.findValue(byte[].class, BerCodingAarq.ID_CALLING_AP_TITLE,
                                              BerIds.ID_OCTETSTRING);
    result.setCallingApTitle(callingApTitle);

    //--- ACSE Requirements ("optional") ---
    BitString acseRequirements = decoded.findValue(BitString.class, BerCodingAarq.ID_SENDER_ACSE_REQUIREMENTS);
    result.setAcseRequirements(acseRequirements);

    //--- Security mechanism name (optional) ---
    ObjectIdentifier securityMechanismName = decoded.findValue(ObjectIdentifier.class,
                                                               BerCodingAarq.ID_MECHANISM_NAME);
    result.setSecurityMechanismName(securityMechanismName);

    //--- Calling authentication value (optional) ---
    AuthenticationValue callingAuthenticationValue = null;
    if (decoded.valueExists(BerCodingAarq.ID_CALLING_AUTHENTICATION_VALUE))
    {
      callingAuthenticationValue = authValueConverter.buildAuthenticationValue(decoded.findBerValue(
              BerCollection.class, BerCodingAarq.ID_CALLING_AUTHENTICATION_VALUE));
    }
    result.setCallingAuthenticationValue(callingAuthenticationValue);

    //--- user info ---
    ProposedXDlmsContext proposedXDlmsContext = null;
    byte[] userInfo = decoded.findValue(byte[].class, BerCodingAarq.ID_USER_INFO, BerIds.ID_OCTETSTRING);
    result.setUserInfo(userInfo);

    if (userInfo != null && userInfo.length > 0 && userInfo[0] == 0x01) //  userInfo[0] == 0x01 is the tag for the initiate request.
    {
      proposedXDlmsContext = coderInitiateRequest.decodeObject(new ByteArrayInputStream(userInfo));
      result.setServiceClass(proposedXDlmsContext.isResponseAllowed() ? ServiceClass.CONFIRMED
              : ServiceClass.UNCONFIRMED);
    }
    result.setProposedXDlmsContext(proposedXDlmsContext);

    return result;
  }

}
