/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderGetRequest.java $
 * Version:     
 * $Id: CoderGetRequest.java 5022 2012-08-17 13:20:21Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:22:02
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.get.CosemGetRequest;
import com.elster.dlms.cosem.application.services.get.CosemGetRequestNext;
import com.elster.dlms.cosem.application.services.get.CosemGetRequestNormal;
import com.elster.dlms.cosem.application.services.get.CosemGetRequestWithList;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS get request".
 *
 * @author osse
 */
public class CoderGetRequest extends AbstractAXdrCoder<CosemGetRequest>
{
  private final CoderGetRequestNormal coderGetRequestNormal = new CoderGetRequestNormal();
  private final CoderGetRequestNext coderGetRequestNext = new CoderGetRequestNext();
  private final CoderGetRequestWithList coderGetRequestWithList = new CoderGetRequestWithList();
  
  @Override
  public void encodeObject(final CosemGetRequest object, final AXdrOutputStream out) throws IOException
  {
    switch (object.getRequestType())
    {
      case NORMAL:
        out.write(1);
        coderGetRequestNormal.encodeObject((CosemGetRequestNormal)object, out);
        break;
      case NEXT:
        out.write(2);
        coderGetRequestNext.encodeObject((CosemGetRequestNext)object, out);
        break;
      case WITH_LIST:
        out.write(3);
        coderGetRequestWithList.encodeObject((CosemGetRequestWithList)object, out);
        break;
      default:
        throw new IOException("Unexpected request type: " + object.getRequestType());
    }
  }
  
  @Override
  public CosemGetRequest decodeObject(final AXdrInputStream in) throws IOException
  {
    final int tag = in.readTag();
    
    switch (tag)
    {
      case 1:
        return coderGetRequestNormal.decodeObject(in);
      case 2:
        return coderGetRequestNext.decodeObject(in);
      case 3:
        return coderGetRequestWithList.decodeObject(in);
      default:
        throw new IOException("Unexpected selection number: " + tag);
    }
  }
  
}
