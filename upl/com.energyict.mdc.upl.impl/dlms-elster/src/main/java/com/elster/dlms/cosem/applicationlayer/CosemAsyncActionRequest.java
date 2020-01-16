/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/CosemAsyncActionRequest.java $
 * Version:     
 * $Id: CosemAsyncActionRequest.java 4279 2012-04-02 14:37:29Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Oct 28, 2010 4:36:41 PM
 */
package com.elster.dlms.cosem.applicationlayer;

import com.elster.dlms.cosem.application.services.action.ActionResponse;
import com.elster.dlms.cosem.application.services.action.ActionResult;
import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.data.DlmsData;
import java.io.IOException;

/**
 * Action request for executing in background.
 *
 * @author osse
 */
public class CosemAsyncActionRequest extends CosemAsyncServiceInvocation
{
  private final CosemMethodDescriptor methodDecriptor;
  private final DlmsData parameters;
  private ActionResponse actionResponseWithOptionalData;

  public CosemAsyncActionRequest(CosemMethodDescriptor methodDecriptor, DlmsData parameters)
  {
    this.methodDecriptor = methodDecriptor;
    this.parameters = parameters;
  }

  @Override
  public ServiceType getServiceType()
  {
    return ServiceType.ACTION;
  }

  public CosemMethodDescriptor getMethodDecriptor()
  {
    return methodDecriptor;
  }

  public DlmsData getParameters()
  {
    return parameters;
  }

  public ActionResponse getActionResponseWithOptionalData()
  {
    return actionResponseWithOptionalData;
  }

  public void setActionResponseWithOptionalData(ActionResponse actionResponseWithOptionalData)
  {
    this.actionResponseWithOptionalData = actionResponseWithOptionalData;
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
        if (getActionResponseWithOptionalData().getActionResult() != ActionResult.SUCCESS)
        {
          throw new IOException("Action service invocation not successful. Object:"+getMethodDecriptor().getInstanceId().toString()+", Method ID:"+getMethodDecriptor().getMethodId()+", Result: \"" + getActionResponseWithOptionalData().
                  getActionResult() + "\"");
        }
        break;
      case ERROR:
        if (getErrorReason() != null)
        {
          throw getErrorReason();
        }
        else
        {
          throw new IOException("Unknown error in action service invocation");
        }
      default:
        throw new IOException("Unexpected state: " + getState());
    }
  }

}
