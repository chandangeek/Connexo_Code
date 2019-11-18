/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/action/CosemActionResponseNormal.java $
 * Version:     
 * $Id: CosemActionResponseNormal.java 2684 2011-02-18 11:31:27Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 18:39:14
 */

package com.elster.dlms.cosem.application.services.action;

/**
 * This COSEM ACTION response "normal"
 *
 * @author osse
 */
public class CosemActionResponseNormal extends CosemActionResponse
{
  ActionResponse actionResponseWithOptionalData;

  public CosemActionResponseNormal(ActionResponse actionResponseWithOptionalData)
  {
    this.actionResponseWithOptionalData = actionResponseWithOptionalData;
  }

  public CosemActionResponseNormal()
  {
  }

  @Override
  public ResponseType getResponseType()
  {
    return ResponseType.NORMAL;
  }

  public ActionResponse getActionResponseWithOptionalData()
  {
    return actionResponseWithOptionalData;
  }

  public void setActionResponseWithOptionalData(ActionResponse actionResponseWithOptionalData)
  {
    this.actionResponseWithOptionalData = actionResponseWithOptionalData;
  }

  @Override
  public String toString()
  {
    return "CosemActionResponseNormal{"+"invocationId=" + getInvocationId() + ", actionResponseWithOptionalData=" + actionResponseWithOptionalData +
           '}';
  }




}
