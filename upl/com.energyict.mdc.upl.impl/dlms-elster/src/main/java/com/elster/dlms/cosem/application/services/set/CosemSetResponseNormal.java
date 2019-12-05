/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/set/CosemSetResponseNormal.java $
 * Version:     
 * $Id: CosemSetResponseNormal.java 2684 2011-02-18 11:31:27Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.05.2010 18:39:14
 */

package com.elster.dlms.cosem.application.services.set;

import com.elster.dlms.cosem.application.services.common.DataAccessResult;

/**
 * The COSEM set response "normal".
 *
 * @author osse
 */
public class CosemSetResponseNormal extends CosemSetResponse
{
  DataAccessResult dataAccessResult;

  @Override
  public ResponseType getResponseType()
  {
    return ResponseType.NORMAL;
  }


  public DataAccessResult getDataAccessResult()
  {
    return dataAccessResult;
  }

  public void setDataAccessResult(DataAccessResult dataAccessResult)
  {
    this.dataAccessResult = dataAccessResult;
  }

  @Override
  public String toString()
  {
    return "CosemSetResponseNormal{" + "invocationId=" + getInvocationId() + ", dataAccessResult=" + dataAccessResult + '}';
  }





}
