/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderGetRequestNext.java $
 * Version:     
 * $Id: CoderGetRequestNext.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.08.2010 15:24:57
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.get.CosemGetRequestNext;
import com.elster.dlms.types.basic.ServiceInvocationId;
import java.io.IOException;

/**
 *  En-/decoder for the "DLMS get request next".
 *
 * @author osse
 */
class CoderGetRequestNext extends AbstractAXdrCoder<CosemGetRequestNext>
{
  private final CoderInvokeIdAndPriority coderInvokeIdAndPriority = new CoderInvokeIdAndPriority();

  @Override
  public void encodeObject(final CosemGetRequestNext object, final AXdrOutputStream out) throws IOException
  {
    coderInvokeIdAndPriority.encodeObject(object.getInvocationId(), out);
    out.writeUnsigned32(object.getBlockNo());
  }

  @Override
  public CosemGetRequestNext decodeObject(final AXdrInputStream in) throws IOException
  {
    final ServiceInvocationId serviceInvocationId = coderInvokeIdAndPriority.decodeObject(in);
    final long blockNo = in.readUnsigned32();
    final CosemGetRequestNext cosemGetRequestNext =
            new CosemGetRequestNext();
    cosemGetRequestNext.setInvocationId(serviceInvocationId);
    cosemGetRequestNext.setBlockNo(blockNo);
    return cosemGetRequestNext;
  }

}
