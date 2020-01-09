/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderInitiateRequest.java $
 * Version:     
 * $Id: CoderInitiateRequest.java 5022 2012-08-17 13:20:21Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  06.08.2010 11:57:56
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrCoderBoolean;
import com.elster.axdr.coding.AXdrCoderDefaultValueWrapper;
import com.elster.axdr.coding.AXdrCoderInteger8;
import com.elster.axdr.coding.AXdrCoderOctetStringVariableLength;
import com.elster.axdr.coding.AXdrCoderOptionalValueWrapper;
import com.elster.axdr.coding.AXdrCoderUnsigned16;
import com.elster.axdr.coding.AXdrCoderUnsigned8;
import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.open.ProposedXDlmsContext;
import java.io.IOException;

/**
 * En-/decoder for the initiate request PDU.
 *
 * @author osse
 */
public class CoderInitiateRequest extends AbstractAXdrCoder<ProposedXDlmsContext>
{
  private final boolean handleLeadingTag;
  private final AXdrCoderOptionalValueWrapper<byte[]> coderDedicatedKey = new AXdrCoderOptionalValueWrapper<byte[]>(
          new AXdrCoderOctetStringVariableLength());
  private final AXdrCoderDefaultValueWrapper<Boolean> coderResponseAllowed = new AXdrCoderDefaultValueWrapper<Boolean>(
          new AXdrCoderBoolean(), Boolean.TRUE);
  private final AXdrCoderOptionalValueWrapper<Integer> coderProposedQualityOfService = new AXdrCoderOptionalValueWrapper<Integer>(
          new AXdrCoderInteger8());
  private final AXdrCoderUnsigned8 coderProposedDlmsVersionNumber = new AXdrCoderUnsigned8();
  private final CoderDlmsConformance coderProposedDlmsConformance = new CoderDlmsConformance();
  private final AXdrCoderUnsigned16 coderclientMaxReceivePduSize = new AXdrCoderUnsigned16();

  /**
   * Constructor.
   *
   * @param handleLeadingTag if {@code true} the first byte from the input stream in c decodeObject must be the tag (value = 1).
   * In {@code encodeObject} the tag will be always written.
   */
  public CoderInitiateRequest(boolean handleLeadingTag)
  {
    this.handleLeadingTag = handleLeadingTag;
  }

  @Override
  public void encodeObject(final ProposedXDlmsContext context, final AXdrOutputStream out) throws IOException
  {
    //--- Tag ---
    if (handleLeadingTag)
    {
      out.write(1);
    }

    //--- Body ---
    coderDedicatedKey.encodeObject(context.getDedicatedKey(), out);
    coderResponseAllowed.encodeObject(context.isResponseAllowed(), out);
    coderProposedQualityOfService.encodeObject(null, out);
    coderProposedDlmsVersionNumber.encodeObject(context.getProposedDlmsVersionNumber(), out);
    coderProposedDlmsConformance.encodeObject(context.getProposedDlmsConformance(), out);
    coderclientMaxReceivePduSize.encodeObject(context.getClientMaxReceivePduSize(), out);
  }

  @Override
  public ProposedXDlmsContext decodeObject(final AXdrInputStream in) throws IOException
  {
    //--- Tag ---
    if (handleLeadingTag)
    {
      int tag = in.read();
      if (tag != 1)
      {
        throw new IOException("Unexpected tag. Expected:1 Actual:" + tag);
      }
    }

    //--- Body ---

    ProposedXDlmsContext result = new ProposedXDlmsContext();

    result.setDedicatedKey(coderDedicatedKey.decodeObject(in));
    result.setResponseAllowed(coderResponseAllowed.decodeObject(in));
    coderProposedQualityOfService.decodeObject(in);
    result.setProposedDlmsVersionNumber(coderProposedDlmsVersionNumber.decodeObject(in));
    result.setProposedDlmsConformance(coderProposedDlmsConformance.decodeObject(in));
    result.setClientMaxReceivePduSize(coderclientMaxReceivePduSize.decodeObject(in));

    return result;
  }

}
