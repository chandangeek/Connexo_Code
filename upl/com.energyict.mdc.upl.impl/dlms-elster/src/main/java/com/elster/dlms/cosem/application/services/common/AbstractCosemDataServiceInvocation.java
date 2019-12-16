/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/common/AbstractCosemDataServiceInvocation.java $
 * Version:     
 * $Id: AbstractCosemDataServiceInvocation.java 2553 2011-01-18 17:59:43Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Jan 18, 2011 6:32:44 PM
 */

package com.elster.dlms.cosem.application.services.common;

import com.elster.dlms.types.basic.ServiceInvocationId;

/**
 * Base class for COSEM data services (GET, SET, ACTION).
 *
 * @author osse
 */
public abstract class AbstractCosemDataServiceInvocation extends AbstractCosemServiceInvocation
{
  private ServiceInvocationId invocationId;

  public ServiceInvocationId getInvocationId()
  {
    return invocationId;
  }

  public void setInvocationId(ServiceInvocationId invocationId)
  {
    this.invocationId = invocationId;
  }



}
