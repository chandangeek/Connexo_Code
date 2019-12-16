/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/open/ConfirmedServiceError.java $
 * Version:     
 * $Id: ConfirmedServiceError.java 2579 2011-01-25 17:47:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  09.08.2010 16:17:24
 */

package com.elster.dlms.cosem.application.services.open;

import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation;

/**
 * Very simple implementation of the confirmed service error.<P>
 * Saves the 3 levels simply as integers without the capability to get an
 * readable name.
 *
 * @author osse
 */
public class ConfirmedServiceError extends AbstractCosemServiceInvocation
{
  private int confirmedServiceErrorType;
  private int serviceErrorType;
  private int error;

  public ConfirmedServiceError(int confirmedServiceErrorType, int serviceErrorType, int error)
  {
    this.confirmedServiceErrorType = confirmedServiceErrorType;
    this.serviceErrorType = serviceErrorType;
    this.error = error;
  }

  public ConfirmedServiceError()
  {
  }


  public int getConfirmedServiceErrorType()
  {
    return confirmedServiceErrorType;
  }

  public void setConfirmedServiceErrorType(int confirmedServiceErrorType)
  {
    this.confirmedServiceErrorType = confirmedServiceErrorType;
  }

  public int getError()
  {
    return error;
  }

  public void setError(int error)
  {
    this.error = error;
  }

  public int getServiceErrorType()
  {
    return serviceErrorType;
  }

  public void setServiceErrorType(int serviceErrorType)
  {
    this.serviceErrorType = serviceErrorType;
  }

  @Override
  public String toString()
  {
    return "ConfirmedServiceError{" + "confirmedServiceErrorType=" + confirmedServiceErrorType +
           ", serviceErrorType=" + serviceErrorType + ", error=" + error + '}';
  }

  @Override
  public ServiceType getServiceType()
  {
    return ServiceType.SERVICE_ERROR;
  }

  @Override
  public ServiceInvocationType getServiceInvocationType()
  {
    return ServiceInvocationType.RESPONSE;
  }

  
    @Override
  public ServiceInvocation getServiceInvocation()
  {
    return ServiceInvocation.SERVICE_ERROR;
  }




}
