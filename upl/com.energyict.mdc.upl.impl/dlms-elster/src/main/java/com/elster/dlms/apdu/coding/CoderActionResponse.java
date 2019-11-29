/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderActionResponse.java $
 * Version:     
 * $Id: CoderActionResponse.java 5120 2012-09-07 15:57:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:22:02
 */

package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.action.CosemActionResponse;
import com.elster.dlms.cosem.application.services.action.CosemActionResponseNormal;
import com.elster.dlms.cosem.application.services.action.CosemActionResponseWithList;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS action response".
 *
 * @author osse
 */
public class CoderActionResponse extends AbstractAXdrCoder<CosemActionResponse>
{
  private final CoderActionResponseNormal coderActionResponseNormal= new CoderActionResponseNormal();
  private final CoderActionResponseWithList coderActionResponseWithList= new CoderActionResponseWithList();

  @Override
  public void encodeObject(final CosemActionResponse object,final AXdrOutputStream out) throws IOException
  {
    switch (object.getResponseType())
    {
      case NORMAL:
        out.write(1);
        coderActionResponseNormal.encodeObject((CosemActionResponseNormal)object,out);
        break;
      case ONE_BLOCK:
      case LAST_BLOCK:
        out.write(2);
        throw new UnsupportedOperationException("Not supported yet.");
      case WITH_LIST:
        out.write(3);
        coderActionResponseWithList.encodeObject((CosemActionResponseWithList) object, out);
        break;
      case NEXT:
        out.write(4);
        throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  @Override
  public CosemActionResponse decodeObject(final AXdrInputStream in) throws IOException
  {
    int selection= in.readUnsigned8();

    switch (selection)
    {
      case 1:
        return coderActionResponseNormal.decodeObject(in);
      case 2:
        throw new UnsupportedOperationException("Not supported yet.");
      case 3:
        return coderActionResponseWithList.decodeObject(in);
      case 4:
        throw new UnsupportedOperationException("Not supported yet.");
      default:
        throw new IOException("Unexpected selection number");
    }
  }




}
