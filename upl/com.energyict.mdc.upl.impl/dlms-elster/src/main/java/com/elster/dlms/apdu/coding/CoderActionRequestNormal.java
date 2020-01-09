/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderActionRequestNormal.java $
 * Version:     
 * $Id: CoderActionRequestNormal.java 5120 2012-09-07 15:57:36Z osse $
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
import com.elster.dlms.cosem.application.services.action.CosemActionRequestNormal;
import com.elster.dlms.types.data.DlmsData;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS action request normal".
 *
 * @author osse
 */
public class CoderActionRequestNormal extends AbstractAXdrCoder<CosemActionRequestNormal>
{
  private final CoderInvokeIdAndPriority coderInvokeIdAndPriority = new CoderInvokeIdAndPriority();
  private final CoderCosemMethodDescriptor coderCosemMethodDescriptor =
          new CoderCosemMethodDescriptor();
  private final AXdrCoderOptionalValueWrapper<DlmsData> coderMethodInvocationParamers = new AXdrCoderOptionalValueWrapper<DlmsData>(
          new CoderDlmsData());

  @Override
  public void encodeObject(final CosemActionRequestNormal object,final AXdrOutputStream out) throws IOException
  {
    coderInvokeIdAndPriority.encodeObject(object.getInvocationId(), out);
    coderCosemMethodDescriptor.encodeObject(object.getMethodDescriptor(), out);
    coderMethodInvocationParamers.encodeObject(object.getMethodInvocationParamers(), out);
  }

  @Override
  public CosemActionRequestNormal decodeObject(final AXdrInputStream in) throws IOException
  {
    CosemActionRequestNormal actionRequestNormal = new CosemActionRequestNormal();

    actionRequestNormal.setInvocationId(coderInvokeIdAndPriority.decodeObject(in));
    actionRequestNormal.setMethodDescriptor(coderCosemMethodDescriptor.decodeObject(in));
    actionRequestNormal.setMethodInvocationParamers(coderMethodInvocationParamers.decodeObject(in));

    return actionRequestNormal;
  }

}
