/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderReleaseRequest.java $
 * Version:     
 * $Id: CoderReleaseRequest.java 3665 2011-10-04 17:34:41Z osse $
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
import com.elster.dlms.cosem.application.services.open.ProposedXDlmsContext;
import com.elster.dlms.cosem.application.services.release.ReleaseRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * En-/decoder for the Release request PDU.
 *
 * @author osse
 */
public class CoderReleaseRequest extends AbstractCoder<ReleaseRequest>
{
  private final BerEncoder berEncoder = new BerEncoder();
  private final CoderInitiateRequest coderInitiateRequest = new CoderInitiateRequest(true);

  public CoderReleaseRequest()
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
  public void encodeObject(ReleaseRequest releaseRequest, OutputStream out) throws IOException
  {
    BerCollection pdu = new BerCollection(BerCodingReleaseRequest.ID_RELEASE_REQUEST);

    int reasonId;
    switch (releaseRequest.getReason())
    {
      case NORMAL:
        reasonId = 0;
        break;
      case USER_DEFINED:
        reasonId = 30;
        break;
      default:
        throw new IOException("Unknown reason to encode: " + releaseRequest.getReason());
    }

    pdu.add(new BerValueInt(BerCodingReleaseRequest.ID_REASON, reasonId));

    //--- user info (optional) ---
    if (releaseRequest.getUserInfo() != null)
    {
      BerCollection userInfo = new BerCollection(BerCodingReleaseRequest.ID_USER_INFO);
      userInfo.add(new BerValueOctetString(BerIds.ID_OCTETSTRING, releaseRequest.getUserInfo()));
      pdu.add(userInfo);
    }
    else if (releaseRequest.getProposedXDlmsContext() != null)
    {
      byte[] userInfoBytes =
              coderInitiateRequest.encodeObjectToBytes(releaseRequest.getProposedXDlmsContext());
      BerCollection userInfo = new BerCollection(BerCodingAarq.ID_USER_INFO);
      userInfo.add(new BerValueOctetString(BerIds.ID_OCTETSTRING, userInfoBytes));
      pdu.add(userInfo);
    }

    berEncoder.encode(out, pdu);
  }

  @Override
  public ReleaseRequest decodeObject(InputStream in) throws IOException
  {
    BerDecoderCollection releaseRequestDecoder = BerCodingReleaseRequest.buildDecoderReleaseRequest();
    BerCollection decoded = releaseRequestDecoder.decode(in);

    ReleaseRequest releaseRequest = new ReleaseRequest();
    
    if (!decoded.getIdentifier().equals(BerCodingReleaseRequest.ID_RELEASE_REQUEST))
    {
      throw new IOException("Wrong identifier");
    }


    //--- Reason ---
    int reasonId = decoded.findValue(Integer.class, BerCodingReleaseRequest.ID_REASON);

    switch (reasonId)
    {
      case 0:
        releaseRequest.setReason(ReleaseRequest.Reason.NORMAL);
        break;
      case 30:
        releaseRequest.setReason(ReleaseRequest.Reason.USER_DEFINED);
        break;
      default:
        throw new IOException("Unknown reason ID: " + reasonId);
    }

    //--- user info ---
    ProposedXDlmsContext propeXDlmsContext = null;
    byte[] userInfo = decoded.findValue(byte[].class, BerCodingReleaseRequest.ID_USER_INFO,
                                        BerIds.ID_OCTETSTRING);
    releaseRequest.setUserInfo(userInfo);

    if (userInfo != null && userInfo.length > 0)
    {
      switch (userInfo[0])
      {
        case 0x01: //Normally the user info should be encrypted
          propeXDlmsContext = coderInitiateRequest.decodeObject(new ByteArrayInputStream(userInfo));
          break;
      }
    }
    releaseRequest.setProposedXDlmsContext(propeXDlmsContext);

    return releaseRequest;
  }

  private final static class BerCodingReleaseRequest
  {

    private BerCodingReleaseRequest()
    {
      //no instances allowed
    }
    
    
    
    public static final BerId ID_RELEASE_REQUEST = new BerId(BerId.Tag.APPLICATION, true, 2);
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
