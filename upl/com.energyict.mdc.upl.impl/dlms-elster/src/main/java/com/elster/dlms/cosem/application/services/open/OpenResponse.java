/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/open/OpenResponse.java $
 * Version:     
 * $Id: OpenResponse.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.07.2010 12:13:08
 */
package com.elster.dlms.cosem.application.services.open;

import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation;
import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation.ServiceInvocationType;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.ObjectIdentifier;

/**
 * Open response for the COSEM-OPEN service.<P>
 * see GB ed.7 p.139
 *
 * @author osse
 */
public class OpenResponse extends AbstractCosemServiceInvocation
{
  private BitString protocolVersion;
  private ObjectIdentifier applicationContextName;
  private int result;
  private FailureType failureType;
  private byte[] respondingApTitle;
  private BitString acseRequirements;
  private ObjectIdentifier securityMechanismName;
  private AuthenticationValue respondingAuthenticationValue;
  private NegotiatedXDlmsContext negotiatedXDlmsContext;
  private ConfirmedServiceError xDlmsInitiateError;
  private byte[] userInfo;

  public BitString getAcseRequirements()
  {
    return acseRequirements;
  }

  public void setAcseRequirements(BitString acseRequirements)
  {
    this.acseRequirements = acseRequirements;
  }

  public ObjectIdentifier getApplicationContextName()
  {
    return applicationContextName;
  }

  public void setApplicationContextName(ObjectIdentifier applicationContextName)
  {
    this.applicationContextName = applicationContextName;
  }

  public FailureType getFailureType()
  {
    return failureType;
  }

  public void setFailureType(FailureType failureType)
  {
    this.failureType = failureType;
  }

  public NegotiatedXDlmsContext getNegotiatedXDlmsContext()
  {
    return negotiatedXDlmsContext;
  }

  public void setNegotiatedXDlmsContext(NegotiatedXDlmsContext negotiatedXDlmsContext)
  {
    this.negotiatedXDlmsContext = negotiatedXDlmsContext;
  }

  public BitString getProtocolVersion()
  {
    return protocolVersion;
  }

  public void setProtocolVersion(BitString protocolVersion)
  {
    this.protocolVersion = protocolVersion;
  }

  /**
   * Gets the responding AP title (system title)<P>
   * (see {@link OpenRequest#getCallingApTitle()}
   *
   * @return  The responding AP title.
   */
  public byte[] getRespondingApTitle()
  {
    return respondingApTitle == null ? null : respondingApTitle.clone();

  }

  /**
   * Sets the responding AP title (system title)<P>
   * (see {@link OpenRequest#getCallingApTitle()}
   *
   * @param respondingApTitle The responding AP title.
   */
  public void setRespondingApTitle(final byte[] respondingApTitle)
  {
    this.respondingApTitle = respondingApTitle==null? null: respondingApTitle.clone();
  }

  public AuthenticationValue getRespondingAuthenticationValue()
  {
    return respondingAuthenticationValue;
  }

  public void setRespondingAuthenticationValue(AuthenticationValue respondingAuthenticationValue)
  {
    this.respondingAuthenticationValue = respondingAuthenticationValue;
  }

  public int getResult()
  {
    return result;
  }

  public void setResult(int result)
  {
    this.result = result;
  }

  public ObjectIdentifier getSecurityMechanismName()
  {
    return securityMechanismName;
  }

  public void setSecurityMechanismName(ObjectIdentifier securityMechanismName)
  {
    this.securityMechanismName = securityMechanismName;
  }

  public ConfirmedServiceError getxDlmsInitiateError()
  {
    return xDlmsInitiateError;
  }

  public void setxDlmsInitiateError(ConfirmedServiceError xDlmsInitiateError)
  {
    this.xDlmsInitiateError = xDlmsInitiateError;
  }

  public byte[] getUserInfo()
  {
    return userInfo==null? null: userInfo.clone();
  }

  public void setUserInfo(byte[] userInfo)
  {
    this.userInfo = userInfo==null? null: userInfo.clone();
  }

  @Override
  public String toString()
  {
    return "OpenResponse{" + "protocolVersion=" + protocolVersion
           + ", applicationContextName=" + applicationContextName
           + ", result=" + result
           + ", failureType=" + failureType
           + ", respondingApTitle=" + (respondingApTitle == null ? null : CodingUtils.byteArrayToString(
            respondingApTitle))
           + ", acseRequirements="
           + acseRequirements
           + ", securityMechanismName="
           + securityMechanismName
           + ", respondingAuthenticationValue="
           + respondingAuthenticationValue
           + ", negotiatedXDlmsContext="
           + negotiatedXDlmsContext
           + ", xDlmsInitiateError="
           + xDlmsInitiateError + '}';
  }

  @Override
  public ServiceInvocationType getServiceInvocationType()
  {
    return ServiceInvocationType.RESPONSE;
  }

  @Override
  public ServiceType getServiceType()
  {
    return ServiceType.OPEN;
  }

  public ServiceInvocation getServiceInvocation()
  {
    return ServiceInvocation.OPEN_RESPONSE;
  }

}
