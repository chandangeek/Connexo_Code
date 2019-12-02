/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/action/CosemActionResponseWithList.java $
 * Version:     
 * $Id: CosemActionResponseWithList.java 5120 2012-09-07 15:57:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 18:39:14
 */

package com.elster.dlms.cosem.application.services.action;

import java.util.ArrayList;
import java.util.List;

/**
 * This COSEM ACTION response "with list"
 *
 * @author osse
 */
public class CosemActionResponseWithList extends CosemActionResponse
{
  private final List<ActionResponse> actionResponsesWithOptionalData= new ArrayList<ActionResponse>();



  public CosemActionResponseWithList()
  {
  }

  @Override
  public ResponseType getResponseType()
  {
    return ResponseType.WITH_LIST;
  }

  public List<ActionResponse> getActionResponsesWithOptionalData()
  {
    return actionResponsesWithOptionalData;
  }

  @Override
  public String toString()
  {
    return "CosemActionResponseWithList{" + "actionResponsesWithOptionalData=" +
           actionResponsesWithOptionalData + '}';
  }

}
