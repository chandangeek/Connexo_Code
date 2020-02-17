/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderReleaseResponse.java $
 * Version:     
 * $Id: CoderReleaseResponse.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2010 16:32:57
 */
package com.elster.dlms.apdu.coding;

import com.elster.ber.coding.BerDecoderCollection;
import com.elster.ber.coding.BerDecoderInt;
import com.elster.ber.coding.BerDecoderMappedCollection;
import com.elster.ber.coding.BerEncoder;
import com.elster.ber.coding.BerIds;
import com.elster.ber.types.BerCollection;
import com.elster.ber.types.BerId;
import com.elster.ber.types.BerValueInt;
import com.elster.ber.types.BerValueOctetString;
import com.elster.coding.AbstractCoder;
import com.elster.dlms.cosem.application.services.open.NegotiatedXDlmsContext;
import com.elster.dlms.cosem.application.services.release.ReleaseResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * En-/decoder for the Release request PDU.
 *
 * @author osse
 */
public class CoderReleaseResponse extends AbstractCoder<ReleaseResponse>
{
  private final BerEncoder berEncoder = new BerEncoder();
  private final CoderInitiateResponse coderInitiateResponse = new CoderInitiateResponse(true);

  public CoderReleaseResponse()
  {
  }

  /**
   * Encodes the open response to the output stream.<P>
   * If the user info of the open response is not {@code null} the negotiatedXDlmsContext and the 
   * xDlmsError properties are ignored.
   * 
   * @param releaseRequest The open response to encode.
   * @param out The output stream to encode to.
   * @throws IOException
   */
  @Override
  public void encodeObject(ReleaseResponse releaseRequest, OutputStream out) throws IOException
  {
    BerCollection pdu = new BerCollection(BerCodingReleaseResponse.ID_RELEASE_RESPONSE);

    int reasonId;
    switch (releaseRequest.getReason())
    {
      case NORMAL:
        reasonId = 0;
        break;
      case NOT_FINISHED:
        reasonId = 1;
        break;
      case USER_DEFINED:
        reasonId = 30;
        break;
      default:
        throw new IOException("Unknown reason to encode: " + releaseRequest.getReason());
    }

    pdu.add(new BerValueInt(BerCodingReleaseResponse.ID_REASON, reasonId));

    //--- user info (optional) ---
    if (releaseRequest.getUserInfo() != null)
    {
      BerCollection userInfo = new BerCollection(BerCodingReleaseResponse.ID_USER_INFO);
      userInfo.add(new BerValueOctetString(BerIds.ID_OCTETSTRING, releaseRequest.getUserInfo()));
      pdu.add(userInfo);
    }
    else if (releaseRequest.getNegotiatedXDlmsContext() != null)
    {
      byte[] userInfoBytes =
              coderInitiateResponse.encodeObjectToBytes(releaseRequest.getNegotiatedXDlmsContext());
      BerCollection userInfo = new BerCollection(BerCodingAarq.ID_USER_INFO);
      userInfo.add(new BerValueOctetString(BerIds.ID_OCTETSTRING, userInfoBytes));
      pdu.add(userInfo);
    }

    berEncoder.encode(out, pdu);
  }

  @Override
  public ReleaseResponse decodeObject(InputStream in) throws IOException
  {
    BerDecoderCollection releaseRequestDecoder = BerCodingReleaseResponse.buildDecoderReleaseRequest();
    BerCollection decoded = releaseRequestDecoder.decode(in);
    
    
    if (!decoded.getIdentifier().equals(BerCodingReleaseResponse.ID_RELEASE_RESPONSE))
    {
      throw new IOException("Wrong identifier");
    }

    ReleaseResponse releaseResponse = new ReleaseResponse();


    //--- Reason ---
    Integer reasonId = decoded.findValue(Integer.class, BerCodingReleaseResponse.ID_REASON);

    if (reasonId == null)
    {
      throw new IOException("No reason id provided");
    }

    switch (reasonId)
    {
      case 0:
        releaseResponse.setReason(ReleaseResponse.Reason.NORMAL);
        break;
      case 1:
        releaseResponse.setReason(ReleaseResponse.Reason.NOT_FINISHED);
        break;
      case 30:
        releaseResponse.setReason(ReleaseResponse.Reason.USER_DEFINED);
        break;
      default:
        throw new IOException("Unknown reason ID: " + reasonId);
    }

    //--- user info ---
    NegotiatedXDlmsContext negotiatedXDlmsContext = null;
    byte[] userInfo = decoded.findValue(byte[].class, BerCodingReleaseResponse.ID_USER_INFO,
                                        BerIds.ID_OCTETSTRING);
    releaseResponse.setUserInfo(userInfo);

    if (userInfo != null && userInfo.length > 0)
    {
      switch (userInfo[0])
      {
        case 0x08: //Normally the user info should be encrypted
          negotiatedXDlmsContext = coderInitiateResponse.decodeObject(new ByteArrayInputStream(userInfo));
          break;
      }
    }
    releaseResponse.setNegotiatedXDlmsContext(negotiatedXDlmsContext);

    return releaseResponse;
  }

  private static final class BerCodingReleaseResponse
  {

    private BerCodingReleaseResponse()
    {
      //no instances allowed
    }
    
    
    public static final BerId ID_RELEASE_RESPONSE = new BerId(BerId.Tag.APPLICATION, true, 3);
    public static final BerId ID_REASON = new BerId(BerId.Tag.CONTEXT_SPECIFIC, false, 0);
    public static final BerId ID_USER_INFO = new BerId(BerId.Tag.CONTEXT_SPECIFIC, true, 30);

    /**
     * Builds an decoder for decoding AARE APDUs (BER).<P>
     *
     * @return The decoder.
     */
    public static BerDecoderCollection buildDecoderReleaseRequest()
    {
      BerDecoderMappedCollection result = new BerDecoderMappedCollection();

      result.addMapping(ID_REASON, new BerDecoderInt());
      return result;
    }

  }

}
