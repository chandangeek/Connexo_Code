/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderSetResponse.java $
 * Version:     
 * $Id: CoderSetResponse.java 5118 2012-09-07 12:58:12Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:22:02
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.set.CosemSetResponse;
import com.elster.dlms.cosem.application.services.set.CosemSetResponseNormal;
import com.elster.dlms.cosem.application.services.set.CosemSetResponseWithList;
import java.io.IOException;

/**
 * En-/decoder for the "set response" APDU.
 *
 * @author osse
 */
public class CoderSetResponse extends AbstractAXdrCoder<CosemSetResponse>
{
  private final CoderSetResponseNormal coderSetResponseNormal = new CoderSetResponseNormal();
  private final CoderSetResponseWithList coderSetResponseWithList = new CoderSetResponseWithList();

  @Override
  public void encodeObject(final CosemSetResponse object, final AXdrOutputStream out) throws IOException
  {
    switch (object.getResponseType())
    {
      case NORMAL:
        out.writeTag(1);
        coderSetResponseNormal.encodeObject((CosemSetResponseNormal)object, out);
        break;
      case WITH_LIST:
        out.writeTag(5);
        coderSetResponseWithList.encodeObject((CosemSetResponseWithList)object, out);
        break;
      default:
        throw new UnsupportedOperationException("Not supported yet. Set response type: " + object.
                getResponseType());
    }
  }

  @Override
  public CosemSetResponse decodeObject(final AXdrInputStream in) throws IOException
  {
    final int selection = in.readTag();

    switch (selection)
    {
      case 1:
        return coderSetResponseNormal.decodeObject(in);
      case 5:
        return coderSetResponseWithList.decodeObject(in);
      default:
        throw new UnsupportedOperationException("Not supported. Set response tag: "+ selection);
    }
  }

}
