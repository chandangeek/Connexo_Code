/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/open/OpenRequest.java $
 * Version:     
 * $Id: OpenRequest.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.07.2010 12:13:08
 */
package com.elster.dlms.cosem.application.services.open;

import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.ObjectIdentifier;
import com.elster.dlms.types.basic.ServiceClass;

/**
 * Open request for the COSEM-OPEN service.<P>
 * see GB ed.7 p.139
 *
 * @author osse
 */
public class OpenRequest extends AbstractCosemServiceInvocation
{
  private BitString protocolVersion;
  private ObjectIdentifier applicationContextName;
  private byte[] callingApTitle; //=System title
  private BitString acseRequirements;
  private ObjectIdentifier securityMechanismName;
  private AuthenticationValue callingAuthenticationValue;
  private ProposedXDlmsContext proposedXDlmsContext;
  private byte[] userInfo;
  private ServiceClass serviceClass;
  public static final BitString ASCE_AUTHENTICATION = new BitString(1, new byte[]
          {
            (byte)0x80
          });

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

  /**
   * The calling_ap_title is used as "system title".<P>
   * See GB ed.7 p.140 and GB ed.7 p.132
   *
   * @return The calling ap title.
   */
  public byte[] getCallingApTitle()
  {
    return callingApTitle == null ? null : callingApTitle.clone();
  }

  /**
   * The calling_ap_title is used as "system title".<P>
   * See GB ed.7 p.140 and GB ed.7 p.132
   *
   * @param callingApTitle The calling ap title
   */
  public void setCallingApTitle(final byte[] callingApTitle)
  {
    this.callingApTitle = callingApTitle == null ? null : callingApTitle.clone();
  }

  public AuthenticationValue getCallingAuthenticationValue()
  {
    return callingAuthenticationValue;
  }

  public void setCallingAuthenticationValue(AuthenticationValue callingAuthenticationValue)
  {
    this.callingAuthenticationValue = callingAuthenticationValue;
  }

  public ProposedXDlmsContext getProposedXDlmsContext()
  {
    if (proposedXDlmsContext == null)
    {
      proposedXDlmsContext = new ProposedXDlmsContext();
    }
    return proposedXDlmsContext;
  }

  public void setProposedXDlmsContext(ProposedXDlmsContext proposedXDlmsContext)
  {
    this.proposedXDlmsContext = proposedXDlmsContext;
  }

  public BitString getProtocolVersion()
  {
    return protocolVersion;
  }

  public void setProtocolVersion(BitString protocolVersion)
  {
    this.protocolVersion = protocolVersion;
  }

  public ObjectIdentifier getSecurityMechanismName()
  {
    return securityMechanismName;
  }

  public void setSecurityMechanismName(ObjectIdentifier securityMechanismName)
  {
    this.securityMechanismName = securityMechanismName;
  }

  public ServiceClass getServiceClass()
  {
    return serviceClass;
  }

  public void setServiceClass(ServiceClass serviceClass)
  {
    this.serviceClass = serviceClass;
  }

  public byte[] getUserInfo()
  {
    return userInfo == null ? null : userInfo.clone();
  }

  public void setUserInfo(final byte[] userInfo)
  {
    this.userInfo = userInfo == null ? null : userInfo.clone();
  }

  @Override
  public String toString()
  {
    return "OpenRequest{" + "protocolVersion=" + protocolVersion + ", applicationContextName="
           + applicationContextName + ", callingApTitle=" + CodingUtils.byteArrayToString(callingApTitle)
           + ", acseRequirements="
           + acseRequirements + ", securityMechanismName=" + securityMechanismName
           + ", callingAuthenticationValue=" + callingAuthenticationValue + ", proposedXDlmsContext="
           + proposedXDlmsContext + ", serviceClass=" + serviceClass + '}';
  }

  @Override
  public ServiceInvocationType getServiceInvocationType()
  {
    return ServiceInvocationType.REQUEST;
  }

  @Override
  public ServiceType getServiceType()
  {
    return ServiceType.OPEN;
  }

  @Override
  public ServiceInvocation getServiceInvocation()
  {
    return ServiceInvocation.OPEN_REQUEST;
  }

}
