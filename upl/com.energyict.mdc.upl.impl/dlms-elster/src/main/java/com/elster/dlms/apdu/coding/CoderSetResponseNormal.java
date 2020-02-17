/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderSetResponseNormal.java $
 * Version:     
 * $Id: CoderSetResponseNormal.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:08:06
 */
package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.set.CosemSetResponseNormal;
import java.io.IOException;

/**
 * En-/decoder for the "set response normal" APDU.
 *
 * @author osse
 */
public class CoderSetResponseNormal extends AbstractAXdrCoder<CosemSetResponseNormal>
{
  private final CoderInvokeIdAndPriority coderInvokeIdAndPriority = new CoderInvokeIdAndPriority();
  private final CoderDataAccessResult coderDataAccessResult = new CoderDataAccessResult();

  @Override
  public void encodeObject(CosemSetResponseNormal object, AXdrOutputStream out) throws IOException
  {
    coderInvokeIdAndPriority.encodeObject(object.getInvocationId(), out);
    coderDataAccessResult.encodeObject(object.getDataAccessResult(), out);
  }

  @Override
  public CosemSetResponseNormal decodeObject(AXdrInputStream in) throws IOException
  {
    CosemSetResponseNormal setResponseNormal = new CosemSetResponseNormal();

    setResponseNormal.setInvocationId(coderInvokeIdAndPriority.decodeObject(in));
    setResponseNormal.setDataAccessResult(coderDataAccessResult.decodeObject(in));

    return setResponseNormal;
  }

}
