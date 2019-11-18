/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/get/CosemGetRequest.java $
 * Version:     
 * $Id: CosemGetRequest.java 2579 2011-01-25 17:47:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 11:39:25
 */

package com.elster.dlms.cosem.application.services.get;

import com.elster.dlms.cosem.application.services.common.AbstractCosemDataServiceInvocation;
import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation.ServiceInvocationType;

/**
 * Base class for the COSEM GET request.
 *
 * @author osse
 */
public abstract class CosemGetRequest extends AbstractCosemDataServiceInvocation
{
  public enum RequestType {NORMAL, NEXT, WITH_LIST};

  public CosemGetRequest()
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
    return ServiceType.GET;
  }


  public ServiceInvocation getServiceInvocation()
  {
    return ServiceInvocation.GET_REQUEST;
  }


  public abstract RequestType getRequestType();

}
