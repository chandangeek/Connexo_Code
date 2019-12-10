/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/set/CosemSetResponse.java $
 * Version:     
 * $Id: CosemSetResponse.java 2579 2011-01-25 17:47:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 11:39:25
 */
package com.elster.dlms.cosem.application.services.set;

import com.elster.dlms.cosem.application.services.common.AbstractCosemDataServiceInvocation;
import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation.ServiceInvocationType;

/**
 * Base class for the COSEM SET response.
 *
 * @author osse
 */
public abstract class CosemSetResponse extends AbstractCosemDataServiceInvocation
{
  public enum ResponseType
  {
    NORMAL, ACK_BLOCK, LAST_BLOCK, LAST_BLOCK_WITH_LIST, WITH_LIST
  };

  public CosemSetResponse()
  {
  }

  public abstract ResponseType getResponseType();

  @Override
  public ServiceInvocationType getServiceInvocationType()
  {
    return ServiceInvocationType.RESPONSE;
  }

  @Override
  public ServiceType getServiceType()
  {
    return ServiceType.SET;
  }

  public ServiceInvocation getServiceInvocation()
  {
    return ServiceInvocation.SET_RESPONSE;
  }

}
