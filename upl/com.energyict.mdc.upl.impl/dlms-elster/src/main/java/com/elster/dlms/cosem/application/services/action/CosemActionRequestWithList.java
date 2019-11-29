/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/action/CosemActionRequestWithList.java $
 * Version:     
 * $Id: CosemActionRequestWithList.java 5120 2012-09-07 15:57:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 10:50:23
 */

package com.elster.dlms.cosem.application.services.action;

import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.data.DlmsData;
import java.util.ArrayList;
import java.util.List;

/**
 * COSEM ACTION request "with list"
 *
 * @author osse
 */
public class CosemActionRequestWithList extends CosemActionRequest
{

  private final List<CosemMethodDescriptor> methodDescriptors= new ArrayList<CosemMethodDescriptor>();
  private final List<DlmsData> methodInvocationParamters= new ArrayList<DlmsData>();

  public RequestType getRequestType()
  {
    return RequestType.WITH_LIST;
  }

  public List<CosemMethodDescriptor> getMethodDescriptors()
  {
    return methodDescriptors;
  }

  public List<DlmsData> getMethodInvocationParamters()
  {
    return methodInvocationParamters;
  }

  @Override
  public String toString()
  {
    return "CosemActionRequestWithList{" + "methodDescriptor=" + methodDescriptors +
           ", methodInvocationParamers=" + methodInvocationParamters + '}';
  }

}
