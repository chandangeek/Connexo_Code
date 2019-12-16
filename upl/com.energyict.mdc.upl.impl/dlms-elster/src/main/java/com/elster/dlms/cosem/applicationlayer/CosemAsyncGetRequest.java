/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/CosemAsyncGetRequest.java $
 * Version:     
 * $Id: CosemAsyncGetRequest.java 4279 2012-04-02 14:37:29Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Oct 28, 2010 4:36:41 PM
 */
package com.elster.dlms.cosem.applicationlayer;

import com.elster.dlms.cosem.application.services.action.ActionResult;
import com.elster.dlms.cosem.application.services.common.DataAccessResult;
import com.elster.dlms.cosem.application.services.get.GetDataResult;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import java.io.IOException;
import java.util.Date;

/**
 * Get request to run background.
 *
 * @author osse
 */
public class CosemAsyncGetRequest extends CosemAsyncServiceInvocation
{
  private final CosemAttributeDescriptor attributeDescriptor;
  private GetDataResult getDataResult;
  private Date timestamp;


  public CosemAsyncGetRequest(CosemAttributeDescriptor attributeDescriptor)
  {
    this.attributeDescriptor = attributeDescriptor;
  }

  @Override
  public ServiceType getServiceType()
  {
    return ServiceType.GET;
  }

  public CosemAttributeDescriptor getAttributeDescriptor()
  {
    return attributeDescriptor;
  }

  public GetDataResult getGetDataResult()
  {
    return getDataResult;
  }

  public Date getTimestamp()
  {
    return (Date)timestamp.clone();
  }
  
  

  void setGetDataResult(final GetDataResult getDataResult,final Date timestamp)
  {
    this.getDataResult = getDataResult;
    this.timestamp= (Date) timestamp.clone();
  }

    /**
   * Checks the result of the action service invocation.<P>
   * Throws an exception if an exception occurred during the execution of the invocation or if
   * the result is not {@link ActionResult#SUCCESS }.
   *
   * @throws IOException
   */
  public void checkResult() throws IOException
  {
    switch (getState())
    {
      case FINISHED:
        if (getDataResult.getAccessResult() != DataAccessResult.SUCCESS)
        {
          throw new IOException("Get service invocation not successful.  Result: \"" + getDataResult.getAccessResult()+ "\"");
        }
        break;
      case ERROR:
        if (getErrorReason() != null)
        {
          throw getErrorReason();
        }
        else
        {
          throw new IOException("Unknown error in get service invocation");
        }
      default:
        throw new IOException("Unexpected state: " + getState());
    }
  }

  
}
