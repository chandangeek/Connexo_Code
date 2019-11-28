/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderActionRequestWithList.java $
 * Version:     
 * $Id: CoderActionRequestWithList.java 5120 2012-09-07 15:57:36Z osse $
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
import com.elster.dlms.cosem.application.services.action.CosemActionRequestWithList;
import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.data.DlmsData;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS action request normal".
 *
 * @author osse
 */
public class CoderActionRequestWithList extends AbstractAXdrCoder<CosemActionRequestWithList>
{
  private final CoderInvokeIdAndPriority coderInvokeIdAndPriority = new CoderInvokeIdAndPriority();
  private final AXdrCoderSequenceOfWrapper<CosemMethodDescriptor> coderCosemMethodDescriptors =
          new AXdrCoderSequenceOfWrapper<CosemMethodDescriptor>(new CoderCosemMethodDescriptor());
  private final AXdrCoderSequenceOfWrapper<DlmsData> coderMethodInvocationParameters =
          new AXdrCoderSequenceOfWrapper<DlmsData>(
          new CoderDlmsData());

  @Override
  public void encodeObject(final CosemActionRequestWithList object, final AXdrOutputStream out) throws
          IOException
  {
    coderInvokeIdAndPriority.encodeObject(object.getInvocationId(), out);
    coderCosemMethodDescriptors.encodeObject(object.getMethodDescriptors(), out);
    coderMethodInvocationParameters.encodeObject(object.getMethodInvocationParamters(), out);
  }

  @Override
  public CosemActionRequestWithList decodeObject(final AXdrInputStream in) throws IOException
  {
    CosemActionRequestWithList result = new CosemActionRequestWithList();

    result.setInvocationId(coderInvokeIdAndPriority.decodeObject(in));
    result.getMethodDescriptors().addAll(coderCosemMethodDescriptors.decodeObject(in));
    result.getMethodInvocationParamters().addAll(coderMethodInvocationParameters.decodeObject(in));
 
    return result;
  }

}
