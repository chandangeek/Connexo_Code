/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderActionRequest.java $
 * Version:     
 * $Id: CoderActionRequest.java 5120 2012-09-07 15:57:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:22:02
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.action.CosemActionRequest;
import com.elster.dlms.cosem.application.services.action.CosemActionRequestNormal;
import com.elster.dlms.cosem.application.services.action.CosemActionRequestWithList;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS action request".
 *
 * @author osse
 */
public class CoderActionRequest extends AbstractAXdrCoder<CosemActionRequest>
{
  private final CoderActionRequestNormal coderActionRequestNormal = new CoderActionRequestNormal();
  private final CoderActionRequestWithList coderActionRequestWithList = new CoderActionRequestWithList();

  @Override
  public void encodeObject(final CosemActionRequest object, final AXdrOutputStream out) throws IOException
  {
    switch (object.getRequestType())
    {
      case NORMAL:
        out.write(1);
        coderActionRequestNormal.encodeObject((CosemActionRequestNormal)object, out);
        break;
      case NEXT:
        out.write(2);
        throw new UnsupportedOperationException("Not supported yet.");
      case WITH_LIST:
        out.write(3);
        coderActionRequestWithList.encodeObject((CosemActionRequestWithList)object, out);
        break;
      case FIRST_BLOCK:
        out.write(4);
        throw new UnsupportedOperationException("Not supported yet.");
      case WITH_LIST_AND_FIRST_BLOCK:
        out.write(5);
        throw new UnsupportedOperationException("Not supported yet.");
      case ONE_BLOCK:
        out.write(6);
        throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  @Override
  public CosemActionRequest decodeObject(AXdrInputStream in) throws IOException
  {
    int tag = in.readTag();

    switch (tag)
    {
      case 1:
        return coderActionRequestNormal.decodeObject(in);
      case 2:
        throw new UnsupportedOperationException("Not supported yet.");
      case 3:
        throw new UnsupportedOperationException("Not supported yet.");
      case 4:
        throw new UnsupportedOperationException("Not supported yet.");
      case 5:
        throw new UnsupportedOperationException("Not supported yet.");
      case 6:
        throw new UnsupportedOperationException("Not supported yet.");
      default:
        throw new IOException("Unexpected selection number");
    }
  }

}
