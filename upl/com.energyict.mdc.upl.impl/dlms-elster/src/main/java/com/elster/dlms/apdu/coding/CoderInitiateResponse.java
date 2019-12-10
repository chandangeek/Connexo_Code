/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderInitiateResponse.java $
 * Version:     
 * $Id: CoderInitiateResponse.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  06.08.2010 11:57:56
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrCoderInteger8;
import com.elster.axdr.coding.AXdrCoderOptionalValueWrapper;
import com.elster.axdr.coding.AXdrCoderUnsigned16;
import com.elster.axdr.coding.AXdrCoderUnsigned8;
import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.open.NegotiatedXDlmsContext;
import java.io.IOException;

/**
 * En-/decoder for the initiate request PDU.
 *
 * @author osse
 */
public class CoderInitiateResponse extends AbstractAXdrCoder<NegotiatedXDlmsContext>
{
  private final boolean decodeLeadingTag;
  private final boolean decodeVaaName; //Option for disabling the decoding for the EK240
  private final AXdrCoderOptionalValueWrapper<Integer> coderNegotiatedQualityOfService = new AXdrCoderOptionalValueWrapper<Integer>(
          new AXdrCoderInteger8());
  private final AXdrCoderUnsigned8 coderNegotiatedDlmsVersionNumber = new AXdrCoderUnsigned8();
  private final CoderDlmsConformance coderNegotiatedDlmsConformance = new CoderDlmsConformance();
  private final AXdrCoderUnsigned16 coderServerMaxReceivePduSize = new AXdrCoderUnsigned16();
  private final AXdrCoderUnsigned16 coderVaaName = new AXdrCoderUnsigned16();

  /**
   * Constructor.
   *
   * @param decodeLeadingTag if {@code true} the first byte from the input stream in c decodeObject must be the tag (value = 8).
   * In {@code encodeObject} the tag will be always written.
   */
  public CoderInitiateResponse(final boolean decodeLeadingTag)
  {
    super();
    this.decodeLeadingTag = decodeLeadingTag;
    this.decodeVaaName = true;
  }

  /**
   * Constructor.
   *
   * @param decodeLeadingTag if {@code true} the first byte from the input stream in {@code decodeObject} must be the tag (value = 8).
   * In {@code encodeObject} the tag will be always written.
   *@param decodeVaaName if {@code true} the VAA Name will read from the input stream in {@code decodeObject}. If false the VAA Name will be read from
   * the input stream and be set to -1. (Disabling the reading of the VAA name is necessary due to a bug in the EK240)
   */
  public CoderInitiateResponse(final boolean decodeLeadingTag, final boolean decodeVaaName)
  {
    super();
    this.decodeLeadingTag = decodeLeadingTag;
    this.decodeVaaName = decodeVaaName;
  }

  @Override
  public void encodeObject(final NegotiatedXDlmsContext context,final AXdrOutputStream out) throws IOException
  {
    //--- Tag ---
    out.write(8);

    //--- Body ---
    coderNegotiatedQualityOfService.encodeObject(null, out);
    coderNegotiatedDlmsVersionNumber.encodeObject(context.getNegotiatedDlmsVersionNumber(), out);
    coderNegotiatedDlmsConformance.encodeObject(context.getNegotiatedDlmsConformance(), out);
    coderServerMaxReceivePduSize.encodeObject(context.getServerMaxReceivePduSize(), out);
    coderVaaName.encodeObject(context.getVaaName(), out);

  }

  @Override
  public NegotiatedXDlmsContext decodeObject(final AXdrInputStream in) throws IOException
  {
    //--- Tag ---
    if (decodeLeadingTag)
    {
      final int tag = in.read();
      if (tag != 8)
      {
        throw new IOException("Unexpected tag of initiate response. Expected:8 Actual:" + tag);
      }
    }

    //--- Body ---

    final NegotiatedXDlmsContext result = new NegotiatedXDlmsContext();

    coderNegotiatedQualityOfService.decodeObject(in);
    result.setNegotiatedDlmsVersionNumber(coderNegotiatedDlmsVersionNumber.decodeObject(in));
    result.setNegotiatedDlmsConformance(coderNegotiatedDlmsConformance.decodeObject(in));
    result.setServerMaxReceivePduSize(coderServerMaxReceivePduSize.decodeObject(in));

    if (decodeVaaName)
    {
      result.setVaaName(coderVaaName.decodeObject(in));
    }
    else
    {
      result.setVaaName(-1);
      in.skip(in.available()); //skip the remaining bytes.
    }

    return result;
  }

}
