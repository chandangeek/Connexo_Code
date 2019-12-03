/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/action/ActionResponse.java $
 * Version:     
 * $Id: ActionResponse.java 2553 2011-01-18 17:59:43Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Nov 12, 2010 10:06:09 AM
 */

package com.elster.dlms.cosem.application.services.action;

import com.elster.dlms.cosem.application.services.get.GetDataResult;

/**
 * The DLMS action response (part of the action service).<P>
 * See GB ed.7 p.151
 *
 * @author osse
 */
public class ActionResponse
{
  private ActionResult actionResult;
  private GetDataResult getDataResult;

  public ActionResponse(ActionResult actionResult, GetDataResult getDataResult)
  {
    this.actionResult = actionResult;
    this.getDataResult = getDataResult;
  }

  public ActionResponse()
  {
  }

  public ActionResult getActionResult()
  {
    return actionResult;
  }

  public void setActionResult(ActionResult actionResult)
  {
    this.actionResult = actionResult;
  }

  public GetDataResult getGetDataResult()
  {
    return getDataResult;
  }

  public void setGetDataResult(GetDataResult getDataResult)
  {
    this.getDataResult = getDataResult;
  }

  @Override
  public String toString()
  {
    return "ActionResponseWithOptionalData{" + "actionResult=" + actionResult + ", getDataResult=" +
           getDataResult + '}';
  }




}
