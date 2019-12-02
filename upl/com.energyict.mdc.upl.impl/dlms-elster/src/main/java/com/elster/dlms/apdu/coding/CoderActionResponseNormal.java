/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderActionResponseNormal.java $
 * Version:     
 * $Id: CoderActionResponseNormal.java 2430 2010-12-06 13:56:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:08:06
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.action.ActionResponse;
import com.elster.dlms.cosem.application.services.action.CosemActionResponseNormal;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS action response normal".
 *
 * @author osse
 */
public class CoderActionResponseNormal extends AbstractAXdrCoder<CosemActionResponseNormal>
{
  private final CoderInvokeIdAndPriority coderInvokeIdAndPriority = new CoderInvokeIdAndPriority();
  private final CoderActionResponseWithOptionalData coderActionResponseWithOptionalData =
          new CoderActionResponseWithOptionalData();

  @Override
  public void encodeObject(final CosemActionResponseNormal object,final AXdrOutputStream out) throws IOException
  {
    coderInvokeIdAndPriority.encodeObject(object.getInvocationId(), out);
    ActionResponse actionResponseWithOptionalData = object.getActionResponseWithOptionalData();
    coderActionResponseWithOptionalData.encodeObject(actionResponseWithOptionalData, out);
  }

  @Override
  public CosemActionResponseNormal decodeObject(final AXdrInputStream in) throws IOException
  {
    CosemActionResponseNormal actionResponseNormal = new CosemActionResponseNormal();

    actionResponseNormal.setInvocationId(coderInvokeIdAndPriority.decodeObject(in));
    ActionResponse actionResponseWithOptionalData = coderActionResponseWithOptionalData.
            decodeObject(in);
    actionResponseNormal.setActionResponseWithOptionalData(actionResponseWithOptionalData);
    return actionResponseNormal;
  }

}
