/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/CosemActionResponseException.java $
 * Version:     
 * $Id: CosemActionResponseException.java 3159 2011-06-30 15:56:56Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.06.2011 11:23:18
 */
package com.elster.dlms.cosem.applicationlayer;

import com.elster.dlms.cosem.application.services.action.ActionResponse;

/**
 * Exception for an exception in an Action Response.
 *
 * @author osse
 */
public class CosemActionResponseException extends CosemApplicationLayerException
{
  private final ActionResponse actionResponse;

  public CosemActionResponseException(ActionResponse actionResponse)
  {
    this.actionResponse = actionResponse;
  }

  public CosemActionResponseException(String message, ActionResponse actionResponse)
  {
    super(message);
    this.actionResponse = actionResponse;
  }

  public ActionResponse getActionResponse()
  {
    return actionResponse;
  }
  
}
