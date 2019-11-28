/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/get/CosemGetResponse.java $
 * Version:     
 * $Id: CosemGetResponse.java 2684 2011-02-18 11:31:27Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 11:39:25
 */
package com.elster.dlms.cosem.application.services.get;

import com.elster.dlms.cosem.application.services.common.AbstractCosemDataServiceInvocation;
import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation.ServiceInvocationType;

/**
 * Base class for the COSEM GET response.
 *
 * @author osse
 */
public abstract class CosemGetResponse extends AbstractCosemDataServiceInvocation
{
  public enum ResponseType
  {
    NORMAL, ONE_BLOCK, LAST_BLOCK, WITH_LIST
  };

  public CosemGetResponse()
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
    return ServiceType.GET;
  }

  @Override
  public ServiceInvocation getServiceInvocation()
  {
    return ServiceInvocation.GET_RESPONSE;
  }




}
