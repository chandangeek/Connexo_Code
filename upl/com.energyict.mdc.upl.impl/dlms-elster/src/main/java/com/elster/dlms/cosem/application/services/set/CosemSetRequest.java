/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/set/CosemSetRequest.java $
 * Version:     
 * $Id: CosemSetRequest.java 5118 2012-09-07 12:58:12Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 11:39:25
 */
package com.elster.dlms.cosem.application.services.set;

import com.elster.dlms.cosem.application.services.common.AbstractCosemDataServiceInvocation;
import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation.ServiceInvocationType;

/**
 * The base class for COSEM SET requests.
 *
 * @author osse
 */
public abstract class CosemSetRequest extends AbstractCosemDataServiceInvocation
{
  public enum RequestType
  {
    NORMAL, FIRST_BLOCK, ONE_BLOCK, LAST_BLOCK, WITH_LIST, FIRST_BLOCK_WITH_LIST
  };

  public CosemSetRequest()
  {
  }

  @Override
  public ServiceInvocationType getServiceInvocationType()
  {
    return ServiceInvocationType.REQUEST;
  }

  @Override
  public ServiceType getServiceType()
  {
    return ServiceType.SET;
  }

  @Override
  public ServiceInvocation getServiceInvocation()
  {
    return ServiceInvocation.GET_REQUEST;
  }

  public abstract RequestType getRequestType();

}
