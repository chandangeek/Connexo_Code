/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderSetRequestNormal.java $
 * Version:     
 * $Id: CoderSetRequestNormal.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:08:06
 */

package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.set.CosemSetRequestNormal;
import java.io.IOException;

/**
 * En-/decoder for the "set request normal" APDU.
 *
 * @author osse
 */
public class CoderSetRequestNormal extends AbstractAXdrCoder<CosemSetRequestNormal>
{
  private final CoderInvokeIdAndPriority coderInvokeIdAndPriority= new CoderInvokeIdAndPriority();
  private final CoderCosemAttributeDescriptor coderCosemAttributeDescriptor= new CoderCosemAttributeDescriptor();
  private final CoderDlmsData coderValue= new CoderDlmsData();

  @Override
  public void encodeObject(final CosemSetRequestNormal object,final AXdrOutputStream out) throws IOException
  {
    coderInvokeIdAndPriority.encodeObject(object.getInvocationId(), out);
    coderCosemAttributeDescriptor.encodeObject(object.getAttributeDescriptor(), out);
    coderValue.encodeObject(object.getData(), out);
  }

  @Override
  public CosemSetRequestNormal decodeObject(final AXdrInputStream in) throws IOException
  {
    CosemSetRequestNormal setRequestNormal= new CosemSetRequestNormal();

    setRequestNormal.setInvocationId(coderInvokeIdAndPriority.decodeObject(in));
    setRequestNormal.setAttributeDescriptor(coderCosemAttributeDescriptor.decodeObject(in));
    setRequestNormal.setData(coderValue.decodeObject(in));

    return setRequestNormal;
  }



}
