/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderGetRequestNormal.java $
 * Version:     
 * $Id: CoderGetRequestNormal.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:08:06
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.get.CosemGetRequestNormal;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS get request normal".
 *
 * @author osse
 */
public class CoderGetRequestNormal extends AbstractAXdrCoder<CosemGetRequestNormal>
{
  private final CoderInvokeIdAndPriority coderInvokeIdAndPriority = new CoderInvokeIdAndPriority();
  private final CoderCosemAttributeDescriptor coderCosemAttributeDescriptor =
          new CoderCosemAttributeDescriptor();
//  private final AXdrCoderOptionalValueWrapper<AccessSelectionParameters> coderAccessSelection = new AXdrCoderOptionalValueWrapper<AccessSelectionParameters>(
//          new CoderSelectiveAccessSelector());

  @Override
  public void encodeObject(final CosemGetRequestNormal object, final AXdrOutputStream out) throws IOException
  {
    coderInvokeIdAndPriority.encodeObject(object.getInvocationId(), out);
    coderCosemAttributeDescriptor.encodeObject(object.getAttributeDescriptor(), out);
  }

  @Override
  public CosemGetRequestNormal decodeObject(final AXdrInputStream in) throws IOException
  {
    CosemGetRequestNormal getRequestNormal = new CosemGetRequestNormal();

    getRequestNormal.setInvocationId(coderInvokeIdAndPriority.decodeObject(in));
    getRequestNormal.setAttributeDescriptor(coderCosemAttributeDescriptor.decodeObject(in));

    return getRequestNormal;
  }

}
