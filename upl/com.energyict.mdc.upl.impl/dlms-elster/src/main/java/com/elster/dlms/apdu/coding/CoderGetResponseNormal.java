/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderGetResponseNormal.java $
 * Version:     
 * $Id: CoderGetResponseNormal.java 5120 2012-09-07 15:57:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 11:08:06
 */

package com.elster.dlms.apdu.coding;

import com.elster.axdr.coding.AXdrInputStream;
import com.elster.axdr.coding.AXdrOutputStream;
import com.elster.axdr.coding.AbstractAXdrCoder;
import com.elster.dlms.cosem.application.services.get.CosemGetResponseNormal;
import com.elster.dlms.cosem.application.services.get.GetDataResult;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS get response normal".
 *
 * @author osse
 */
public class CoderGetResponseNormal extends AbstractAXdrCoder<CosemGetResponseNormal>
{
  private final CoderInvokeIdAndPriority coderInvokeIdAndPriority= new CoderInvokeIdAndPriority();
  private final CoderGetDataResult coderGetDataResult= new CoderGetDataResult();




  @Override
  public void encodeObject(CosemGetResponseNormal object, AXdrOutputStream out) throws IOException
  {
    coderInvokeIdAndPriority.encodeObject(object.getInvocationId(), out);
    coderGetDataResult.encodeObject(object.getGetDataResult(), out);
  }

  @Override
  public CosemGetResponseNormal decodeObject(AXdrInputStream in) throws IOException
  {
    CosemGetResponseNormal getResponseNormal= new CosemGetResponseNormal();

    getResponseNormal.setInvocationId(coderInvokeIdAndPriority.decodeObject(in));

    GetDataResult dataResult=coderGetDataResult.decodeObject(in);
    getResponseNormal.setGetDataResult(dataResult);

    return getResponseNormal;
  }



}
