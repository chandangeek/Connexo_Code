/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderConfirmedServiceError.java $
 * Version:     
 * $Id: CoderConfirmedServiceError.java 2579 2011-01-25 17:47:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  09.08.2010 16:22:38
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.open.ConfirmedServiceError;
import java.io.IOException;

/**
 * En-/decoder for the "Confirmed service error".
 *
 * @author osse
 */
public class CoderConfirmedServiceError extends AbstractAXdrCoder<ConfirmedServiceError>
{
  private final boolean decodeTag;

  public CoderConfirmedServiceError(boolean decodeTag)
  {
    this.decodeTag = decodeTag;
  }

  @Override
  public void encodeObject(ConfirmedServiceError object, AXdrOutputStream out) throws IOException
  {
    out.write(0x0E);
    out.write(object.getConfirmedServiceErrorType());
    out.write(object.getServiceErrorType());
    out.write(object.getError());
  }

  @Override
  public ConfirmedServiceError decodeObject(AXdrInputStream in) throws IOException
  {

    if (decodeTag)
    {
      int tag = in.readTag();

      if (tag != 0x0E)
      {
        throw new IOException("Unexpected tag for \"Confirmed Service Error\". Expected: 14  Actual: " + tag);
      }
    }
    int a = in.read();
    int b = in.read();
    int c = in.read();

    return new ConfirmedServiceError(a, b, c);
  }

}
