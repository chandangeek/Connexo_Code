/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/release/ReleaseResponse.java $
 * Version:     
 * $Id: ReleaseResponse.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  11.07.2011 09:41:00
 */
package com.elster.dlms.cosem.application.services.release;

import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation;
import com.elster.dlms.cosem.application.services.open.NegotiatedXDlmsContext;

/**
 * COSEM release response.<P>
 * see GB ed.7 p. 142
 *
 * @author osse
 */
public class ReleaseResponse extends AbstractCosemServiceInvocation
{
  public enum Reason
  {
    NORMAL, NOT_FINISHED, USER_DEFINED
  };

  private NegotiatedXDlmsContext negotiatedXDlmsContext;
  private byte[] userInfo;
  private Reason reason;

  @Override
  public ServiceType getServiceType()
  {
    return ServiceType.RELEASE;
  }

  @Override
  public ServiceInvocationType getServiceInvocationType()
  {
    return ServiceInvocationType.RESPONSE;
  }

  @Override
  public ServiceInvocation getServiceInvocation()
  {
    return ServiceInvocation.RELEASE_RESPONSE;
  }

  public NegotiatedXDlmsContext getNegotiatedXDlmsContext()
  {
    return negotiatedXDlmsContext;
  }

  public void setNegotiatedXDlmsContext(NegotiatedXDlmsContext negotiatedXDlmsContext)
  {
    this.negotiatedXDlmsContext = negotiatedXDlmsContext;
  }


  public Reason getReason()
  {
    return reason;
  }

  public void setReason(Reason reason)
  {
    this.reason = reason;
  }

  public byte[] getUserInfo()
  {
    return userInfo == null ? null : userInfo.clone();
  }

  public void setUserInfo(final byte[] userInfo)
  {
    this.userInfo = userInfo == null ? null : userInfo.clone();
  }

}
