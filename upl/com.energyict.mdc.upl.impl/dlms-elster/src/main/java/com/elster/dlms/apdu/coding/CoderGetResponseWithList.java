/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderGetResponseWithList.java $
 * Version:     
 * $Id: CoderGetResponseWithList.java 5122 2012-09-10 09:54:11Z osse $
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
import com.elster.dlms.cosem.application.services.get.CosemGetResponseWithList;
import com.elster.dlms.cosem.application.services.get.GetDataResult;
import java.io.IOException;

/**
 * En-/decoder for the "DLMS get response with list".
 *
 * @author osse
 */
public class CoderGetResponseWithList extends AbstractAXdrCoder<CosemGetResponseWithList>
{
  private final CoderInvokeIdAndPriority coderInvokeIdAndPriority= new CoderInvokeIdAndPriority();
  private final AXdrCoderSequenceOfWrapper<GetDataResult> coderGetDataResults=new AXdrCoderSequenceOfWrapper<GetDataResult>(new CoderGetDataResult());


  @Override
  public void encodeObject(final CosemGetResponseWithList object,final AXdrOutputStream out) throws IOException
  {
    coderInvokeIdAndPriority.encodeObject(object.getInvocationId(), out);
    coderGetDataResults.encodeObject(object.getGetDataResults(), out);
  }

  @Override
  public CosemGetResponseWithList decodeObject(final AXdrInputStream in) throws IOException
  {
    final CosemGetResponseWithList result= new CosemGetResponseWithList();

    result.setInvocationId(coderInvokeIdAndPriority.decodeObject(in));

    result.getGetDataResults().addAll(coderGetDataResults.decodeObject(in));

    return result;
  }



}
