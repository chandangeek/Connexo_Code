/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderActionResponseWithList.java $
 * Version:     
 * $Id: CoderActionResponseWithList.java 6704 2013-06-07 13:49:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:08:06
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrCoderSequenceOfWrapper;
import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.action.ActionResponse;
import com.elster.dlms.cosem.application.services.action.CosemActionResponseWithList;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS action response with list".
 *
 * @author osse
 */
public class CoderActionResponseWithList extends AbstractAXdrCoder<CosemActionResponseWithList>
{
  private final CoderInvokeIdAndPriority coderInvokeIdAndPriority = new CoderInvokeIdAndPriority();
  private final AXdrCoderSequenceOfWrapper<ActionResponse> coderActionResponseWithOptionalData =
          new AXdrCoderSequenceOfWrapper<ActionResponse> (new CoderActionResponseWithOptionalData());

  @Override
  public void encodeObject(final CosemActionResponseWithList object,final AXdrOutputStream out) throws IOException
  {
    coderInvokeIdAndPriority.encodeObject(object.getInvocationId(), out);
    coderActionResponseWithOptionalData.encodeObject(object.getActionResponsesWithOptionalData(), out);
  }

  @Override
  public CosemActionResponseWithList decodeObject(final AXdrInputStream in) throws IOException
  {
    CosemActionResponseWithList result = new CosemActionResponseWithList();

    result.setInvocationId(coderInvokeIdAndPriority.decodeObject(in));
    result.getActionResponsesWithOptionalData().addAll(coderActionResponseWithOptionalData.decodeObject(in));
    
    return result;
  }

}
