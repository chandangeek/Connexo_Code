/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/action/CosemActionRequestNormal.java $
 * Version:     
 * $Id: CosemActionRequestNormal.java 2684 2011-02-18 11:31:27Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 10:50:23
 */

package com.elster.dlms.cosem.application.services.action;

import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.data.DlmsData;

/**
 * This COSEM ACTION request "normal"
 *
 * @author osse
 */
public class CosemActionRequestNormal extends CosemActionRequest
{

  private CosemMethodDescriptor methodDescriptor;
  private DlmsData methodInvocationParamers;

  public RequestType getRequestType()
  {
    return RequestType.NORMAL;
  }

  public CosemMethodDescriptor getMethodDescriptor()
  {
    return methodDescriptor;
  }

  public void setMethodDescriptor(CosemMethodDescriptor methodDescriptor)
  {
    this.methodDescriptor = methodDescriptor;
  }

  public DlmsData getMethodInvocationParamers()
  {
    return methodInvocationParamers;
  }

  public void setMethodInvocationParamers(DlmsData methodInvocationParamers)
  {
    this.methodInvocationParamers = methodInvocationParamers;
  }

  @Override
  public String toString()
  {
    return "CosemActionRequestNormal{" + "invocationId=" + getInvocationId() + ", methodDescriptor=" + methodDescriptor + ", methodInvocationParamers=" +
           methodInvocationParamers + '}';
  }


  
}
