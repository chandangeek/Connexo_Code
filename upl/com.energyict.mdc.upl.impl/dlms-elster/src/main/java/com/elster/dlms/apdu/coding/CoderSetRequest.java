/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderSetRequest.java $
 * Version:     
 * $Id: CoderSetRequest.java 5118 2012-09-07 12:58:12Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:22:02
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.set.CosemSetRequest;
import com.elster.dlms.cosem.application.services.set.CosemSetRequestNormal;
import com.elster.dlms.cosem.application.services.set.CosemSetRequestWithList;
import java.io.IOException;

/**
 * En-/decoder for the "set request" APDU.
 *
 * @author osse
 */
public class CoderSetRequest extends AbstractAXdrCoder<CosemSetRequest>
{
  private final CoderSetRequestNormal coderSetRequestNormal = new CoderSetRequestNormal();
  private final CoderSetRequestWithList coderSetRequestWithList = new CoderSetRequestWithList();

  @Override
  public void encodeObject(final CosemSetRequest object, final AXdrOutputStream out) throws IOException
  {
    switch (object.getRequestType())
    {
      case NORMAL:
        out.write(1);
        coderSetRequestNormal.encodeObject((CosemSetRequestNormal)object, out);
        break;
      case FIRST_BLOCK:
        out.write(2);
        throw new UnsupportedOperationException("Not supported yet.");
      case ONE_BLOCK:
        out.write(3);
        throw new UnsupportedOperationException("Not supported yet.");
      case LAST_BLOCK:
        out.write(3);
        throw new UnsupportedOperationException("Not supported yet.");
      case WITH_LIST:
        out.write(4);
        coderSetRequestWithList.encodeObject((CosemSetRequestWithList)object, out);
        break;
      case FIRST_BLOCK_WITH_LIST:
        out.write(5);
        throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  @Override
  public CosemSetRequest decodeObject(final AXdrInputStream in) throws IOException
  {
    int selection = in.readUnsigned8();

    switch (selection)
    {
      case 1:
        return coderSetRequestNormal.decodeObject(in);
      case 2:
        throw new UnsupportedOperationException("Not supported yet.");
      case 3:
        throw new UnsupportedOperationException("Not supported yet.");
      case 4:
        return coderSetRequestWithList.decodeObject(in);
      case 5:
        throw new UnsupportedOperationException("Not supported yet.");
      default:
        throw new IOException("Unexpected selection number");
    }
  }

}
