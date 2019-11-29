/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderActionResponseWithOptionalData.java $
 * Version:     
 * $Id: CoderActionResponseWithOptionalData.java 2430 2010-12-06 13:56:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:08:06
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrCoderOptionalValueWrapper;
import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.action.ActionResponse;
import com.elster.dlms.cosem.application.services.action.ActionResult;
import com.elster.dlms.cosem.application.services.get.GetDataResult;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS get response normal".
 *
 * @author osse
 */
public class CoderActionResponseWithOptionalData extends AbstractAXdrCoder<ActionResponse>
{
  private final AXdrCoderOptionalValueWrapper<GetDataResult> coderGetDataResult = new AXdrCoderOptionalValueWrapper<GetDataResult>(
          new CoderGetDataResult());

  @Override
  public void encodeObject(final ActionResponse object,final AXdrOutputStream out) throws IOException
  {
    out.writeUnsigned8(object.getActionResult().getId());
    coderGetDataResult.encodeObject(object.getGetDataResult(), out);
  }

  @Override
  public ActionResponse decodeObject(final AXdrInputStream in) throws IOException
  {
    ActionResponse actionResponseWithOptionalData = new ActionResponse();

    int actionResultId = in.readUnsigned8();
    ActionResult actionResult = ActionResult.findById(actionResultId);

    if (actionResult == null)
    {
      throw new IOException("Unknown Action Result ID: " + actionResultId);
    }

    actionResponseWithOptionalData.setActionResult(actionResult);
    actionResponseWithOptionalData.setGetDataResult(coderGetDataResult.decodeObject(in));
    return actionResponseWithOptionalData;
  }

}
