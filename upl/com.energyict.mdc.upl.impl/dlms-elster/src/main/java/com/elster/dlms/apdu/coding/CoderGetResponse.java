/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderGetResponse.java $
 * Version:     
 * $Id: CoderGetResponse.java 5022 2012-08-17 13:20:21Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:22:02
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.get.CosemGetResponse;
import com.elster.dlms.cosem.application.services.get.CosemGetResponseNormal;
import com.elster.dlms.cosem.application.services.get.CosemGetResponseWithList;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS get response".
 *
 * @author osse
 */
public class CoderGetResponse extends AbstractAXdrCoder<CosemGetResponse>
{
  private final CoderGetResponseNormal coderGetResponseNormal = new CoderGetResponseNormal();
  private final CoderGetResponseWithDataBlock coderGetResponseWithDataBlock =
          new CoderGetResponseWithDataBlock();
  private final CoderGetResponseWithList coderGetResponseWithList = new CoderGetResponseWithList();

  @Override
  public void encodeObject(final CosemGetResponse object, final AXdrOutputStream out) throws IOException
  {
    switch (object.getResponseType())
    {
      case NORMAL:
        out.write(1);
        coderGetResponseNormal.encodeObject((CosemGetResponseNormal)object, out);
        break;
      case ONE_BLOCK:
      case LAST_BLOCK:
        out.write(2);
        throw new UnsupportedOperationException("Not supported yet.");
      case WITH_LIST:
        out.write(3);
        coderGetResponseWithList.encodeObject((CosemGetResponseWithList)object, out);
        break;
      default:
        throw new UnsupportedOperationException("Unsupported response type:" + object.getResponseType());
    }
  }

  @Override
  public CosemGetResponse decodeObject(final AXdrInputStream in) throws IOException
  {
    final int selection = in.readUnsigned8();

    switch (selection)
    {
      case 1:
        return coderGetResponseNormal.decodeObject(in);
      case 2:
        return coderGetResponseWithDataBlock.decodeObject(in);
      case 3:
        return coderGetResponseWithList.decodeObject(in);
      default:
        throw new IOException("Unexpected selection number: " + selection);
    }
  }

}
