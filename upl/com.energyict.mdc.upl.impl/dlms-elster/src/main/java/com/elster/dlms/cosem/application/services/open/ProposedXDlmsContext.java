/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/open/ProposedXDlmsContext.java $
 * Version:     
 * $Id: ProposedXDlmsContext.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.07.2010 12:26:04
 */
package com.elster.dlms.cosem.application.services.open;

import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation;
import java.util.EnumSet;

/**
 * Proposed XDlms context uses in the user data field of the open request <P>
 * The Proposed XDlms context will be encoded as initiate request.
 *
 * @author osse
 */
public class ProposedXDlmsContext extends AbstractCosemServiceInvocation
{
  private byte[] dedicatedKey = null;
  private int proposedDlmsVersionNumber = 6;
  private EnumSet<DlmsConformance> proposedDlmsConformance;
  private int clientMaxReceivePduSize = 0;
  private boolean responseAllowed = true;

  public int getClientMaxReceivePduSize()
  {
    return clientMaxReceivePduSize;
  }

  public void setClientMaxReceivePduSize(int clientMaxReceivePduSize)
  {
    this.clientMaxReceivePduSize = clientMaxReceivePduSize;
  }

  public byte[] getDedicatedKey()
  {
    return dedicatedKey == null ? null : dedicatedKey.clone();
  }

  public void setDedicatedKey(final byte[] dedicatedKey)
  {
    this.dedicatedKey = dedicatedKey == null ? null : dedicatedKey.clone();
  }

  public EnumSet<DlmsConformance> getProposedDlmsConformance()
  {
    return proposedDlmsConformance;
  }

  public void setProposedDlmsConformance(EnumSet<DlmsConformance> proposedDlmsConformance)
  {
    this.proposedDlmsConformance = proposedDlmsConformance;
  }

  public int getProposedDlmsVersionNumber()
  {
    return proposedDlmsVersionNumber;
  }

  public void setProposedDlmsVersionNumber(int proposedDlmsVersionNumber)
  {
    this.proposedDlmsVersionNumber = proposedDlmsVersionNumber;
  }

  public boolean isResponseAllowed()
  {
    return responseAllowed;
  }

  public void setResponseAllowed(boolean responseAllowed)
  {
    this.responseAllowed = responseAllowed;
  }

  @Override
  public String toString()
  {
    return "ProposedXDlmsContext{" + "dedicatedKey=" + CodingUtils.byteArrayToString(dedicatedKey, "")
           + ", proposedDlmsVersionNumber=" + proposedDlmsVersionNumber + ", proposedDlmsConformance="
           + proposedDlmsConformance + ", clientMaxReceivePduSize=" + clientMaxReceivePduSize
           + ", responseAllowed=" + responseAllowed + '}';
  }

  @Override
  public ServiceType getServiceType()
  {
    return ServiceType.INITIATE;
  }

  @Override
  public ServiceInvocationType getServiceInvocationType()
  {
    return ServiceInvocationType.REQUEST;
  }

  @Override
  public ServiceInvocation getServiceInvocation()
  {
    return ServiceInvocation.INITIATE_REQUEST;
  }

}
