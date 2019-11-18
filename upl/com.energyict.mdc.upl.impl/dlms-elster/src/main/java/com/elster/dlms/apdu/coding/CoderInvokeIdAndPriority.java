/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderInvokeIdAndPriority.java $
 * Version:     
 * $Id: CoderInvokeIdAndPriority.java 2430 2010-12-06 13:56:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.04.2010 11:05:30
 */

package com.elster.dlms.apdu.coding;


import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.types.basic.ServiceInvocationId;
import java.io.IOException;

/**
  * En-/decoder for the "COSEM invoke id and priority" PDU part.
 *
 * @author osse
 */
public class CoderInvokeIdAndPriority extends AbstractAXdrCoder<ServiceInvocationId>
{

  @Override
  public void encodeObject(final ServiceInvocationId object,final AXdrOutputStream out) throws IOException
  {
    out.writeUnsigned8(object.toInteger());
  }

  @Override
  public ServiceInvocationId decodeObject(final AXdrInputStream in) throws IOException
  {
    return new ServiceInvocationId(in.readUnsigned8());
  }
}
