/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/apdu/coding/CoderSetResponseWithList.java $
 * Version:     
 * $Id: CoderSetResponseWithList.java 5122 2012-09-10 09:54:11Z osse $
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
import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.application.services.set.CosemSetResponseWithList;
import java.io.IOException;

/**
 * En-/decoder for the "set response with list" APDU.
 *
 * @author osse
 */
public class CoderSetResponseWithList extends AbstractAXdrCoder<CosemSetResponseWithList>
{
  private final CoderInvokeIdAndPriority coderInvokeIdAndPriority = new CoderInvokeIdAndPriority();
  private final  AXdrCoderSequenceOfWrapper<DataAccessResult> coderDataAccessResults = new AXdrCoderSequenceOfWrapper<DataAccessResult>(new CoderDataAccessResult());

  @Override
  public void encodeObject(final CosemSetResponseWithList object,final AXdrOutputStream out) throws IOException
  {
    coderInvokeIdAndPriority.encodeObject(object.getInvocationId(), out);
    coderDataAccessResults.encodeObject(object.getDataAccessResults(), out);
  }

  @Override
  public CosemSetResponseWithList decodeObject(final AXdrInputStream in) throws IOException
  {
    final CosemSetResponseWithList setResponseNormal = new CosemSetResponseWithList();

    setResponseNormal.setInvocationId(coderInvokeIdAndPriority.decodeObject(in));
    setResponseNormal.getDataAccessResults().addAll(coderDataAccessResults.decodeObject(in));
    return setResponseNormal;
  }

}
