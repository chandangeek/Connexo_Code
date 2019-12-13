/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderSetRequestWithList.java $
 * Version:     
 * $Id: CoderSetRequestWithList.java 5122 2012-09-10 09:54:11Z osse $
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
import com.elster.dlms.cosem.application.services.set.CosemSetRequestWithList;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.data.DlmsData;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS set request with list".
 *
 * @author osse
 */
public class CoderSetRequestWithList extends AbstractAXdrCoder<CosemSetRequestWithList>
{
  private final CoderInvokeIdAndPriority coderInvokeIdAndPriority = new CoderInvokeIdAndPriority();
  private final AXdrCoderSequenceOfWrapper<CosemAttributeDescriptor> coderCosemAttributeDescriptors =
          new AXdrCoderSequenceOfWrapper<CosemAttributeDescriptor>(new CoderCosemAttributeDescriptor());
  private final AXdrCoderSequenceOfWrapper<DlmsData> coderValueList =
          new AXdrCoderSequenceOfWrapper<DlmsData>(new CoderDlmsData());

  @Override
  public void encodeObject(final CosemSetRequestWithList object, final AXdrOutputStream out) throws IOException
  {
    coderInvokeIdAndPriority.encodeObject(object.getInvocationId(), out);
    coderCosemAttributeDescriptors.encodeObject(object.getAttributeDescriptors(), out);
    coderValueList.encodeObject(object.getValues(), out);
  }

  @Override
  public CosemSetRequestWithList decodeObject(final AXdrInputStream in) throws IOException
  {
    final CosemSetRequestWithList result = new CosemSetRequestWithList();

    result.setInvocationId(coderInvokeIdAndPriority.decodeObject(in));
    result.getAttributeDescriptors().addAll(coderCosemAttributeDescriptors.decodeObject(in));
    result.getValues().addAll(coderValueList.decodeObject(in));

    return result;
  }

}
