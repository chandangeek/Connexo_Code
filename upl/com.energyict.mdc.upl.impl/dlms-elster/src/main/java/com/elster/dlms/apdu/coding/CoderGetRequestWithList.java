/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderGetRequestWithList.java $
 * Version:     
 * $Id: CoderGetRequestWithList.java 5122 2012-09-10 09:54:11Z osse $
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
import com.elster.dlms.cosem.application.services.get.CosemGetRequestWithList;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS get request with list".
 *
 * @author osse
 */
public class CoderGetRequestWithList extends AbstractAXdrCoder<CosemGetRequestWithList>
{
  private final CoderInvokeIdAndPriority coderInvokeIdAndPriority = new CoderInvokeIdAndPriority();
  private final AXdrCoderSequenceOfWrapper<CosemAttributeDescriptor> coderCosemAttributeDescriptors =
          new AXdrCoderSequenceOfWrapper<CosemAttributeDescriptor>(new CoderCosemAttributeDescriptor());

  @Override
  public void encodeObject(final CosemGetRequestWithList object, final AXdrOutputStream out) throws IOException
  {
    coderInvokeIdAndPriority.encodeObject(object.getInvocationId(), out);
    coderCosemAttributeDescriptors.encodeObject(object.getAttributeDescriptors(), out);
  }

  @Override
  public CosemGetRequestWithList decodeObject(final AXdrInputStream in) throws IOException
  {
    final CosemGetRequestWithList result = new CosemGetRequestWithList();

    result.setInvocationId(coderInvokeIdAndPriority.decodeObject(in));
    result.getAttributeDescriptors().addAll(coderCosemAttributeDescriptors.decodeObject(in));

    return result;
  }

}
