/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/get/CosemGetRequestNext.java $
 * Version:     
 * $Id: CosemGetRequestNext.java 2684 2011-02-18 11:31:27Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 11:44:37
 */

package com.elster.dlms.cosem.application.services.get;

/**
 * This COSEM GET request "next"
 *
 * @author osse
 */
public class CosemGetRequestNext extends CosemGetRequest
{
  private long blockNo;

  @Override
  public RequestType getRequestType()
  {
    return RequestType.NEXT;
  }

  public long getBlockNo()
  {
    return blockNo;
  }

  public void setBlockNo(long blockNo)
  {
    this.blockNo = blockNo;
  }

  @Override
  public String toString()
  {
    return "CosemGetRequestNext{ invocationId=" + getInvocationId() + ", blockNo=" + blockNo + '}';
  }
}
