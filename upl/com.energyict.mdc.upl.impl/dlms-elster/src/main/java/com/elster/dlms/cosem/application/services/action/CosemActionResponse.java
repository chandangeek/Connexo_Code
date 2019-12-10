/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/action/CosemActionResponse.java $
 * Version:     
 * $Id: CosemActionResponse.java 2579 2011-01-25 17:47:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 11:39:25
 */
package com.elster.dlms.cosem.application.services.action;

import com.elster.dlms.cosem.application.services.common.AbstractCosemDataServiceInvocation;
import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation.ServiceInvocationType;

/**
 * Base class for the COSEM ACTION response.<P>
 * See GB ed.7 p.151
 *
 * @author osse
 */
public abstract class CosemActionResponse extends AbstractCosemDataServiceInvocation
{
  public enum ResponseType
  {
    NORMAL, ONE_BLOCK, LAST_BLOCK, NEXT, WITH_LIST
  };

  public CosemActionResponse()
  {
  }

  @Override
  public ServiceInvocationType getServiceInvocationType()
  {
    return ServiceInvocationType.RESPONSE;
  }

  @Override
  public ServiceType getServiceType()
  {
    return ServiceType.ACTION;
  }

  public ServiceInvocation getServiceInvocation()
  {
    return ServiceInvocation.ACTION_RESPONSE;
  }

  public abstract ResponseType getResponseType();

}
