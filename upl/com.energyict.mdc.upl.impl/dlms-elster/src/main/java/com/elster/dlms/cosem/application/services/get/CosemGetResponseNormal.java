/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/get/CosemGetResponseNormal.java $
 * Version:     
 * $Id: CosemGetResponseNormal.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 18:39:14
 */
package com.elster.dlms.cosem.application.services.get;

/**
 * This COSEM GET response "normal"
 *
 * @author osse
 */
public class CosemGetResponseNormal extends CosemGetResponse
{
  private GetDataResult getDataResult;

  @Override
  public ResponseType getResponseType()
  {
    return ResponseType.NORMAL;
  }

 

  @Override
  public String toString()
  {
    return "CosemGetResponseNormal{" + "invocationId=" + getInvocationId() + ", getDataResult=" + getDataResult + '}';
  }


  public GetDataResult getGetDataResult()
  {
    return getDataResult;
  }

  public void setGetDataResult(GetDataResult getDataResult)
  {
    this.getDataResult = getDataResult;
  }

}
