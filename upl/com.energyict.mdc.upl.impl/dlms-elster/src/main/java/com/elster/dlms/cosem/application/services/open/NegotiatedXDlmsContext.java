/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/open/NegotiatedXDlmsContext.java $
 * Version:     
 * $Id: NegotiatedXDlmsContext.java 4279 2012-04-02 14:37:29Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  09.08.2010 11:04:28
 */

package com.elster.dlms.cosem.application.services.open;

import com.elster.dlms.cosem.application.services.common.AbstractCosemServiceInvocation;
import java.util.EnumSet;

/**
 * Class to hold the Negotiated XDlms Context.
 * 
 * See GB ed.7 p.139
 *
 * @author osse
 */
public class NegotiatedXDlmsContext extends AbstractCosemServiceInvocation
{

  private int negotiatedDlmsVersionNumber;
  private EnumSet<DlmsConformance> negotiatedDlmsConformance;
  private int serverMaxReceivePduSize;
  private int vaaName;

  public EnumSet<DlmsConformance> getNegotiatedDlmsConformance()
  {
    return negotiatedDlmsConformance;
  }

  public void setNegotiatedDlmsConformance(EnumSet<DlmsConformance> negotiatedDlmsConformance)
  {
    this.negotiatedDlmsConformance = negotiatedDlmsConformance;
  }

  public int getNegotiatedDlmsVersionNumber()
  {
    return negotiatedDlmsVersionNumber;
  }

  public void setNegotiatedDlmsVersionNumber(int negotiatedDlmsVersionNumber)
  {
    this.negotiatedDlmsVersionNumber = negotiatedDlmsVersionNumber;
  }

  public int getServerMaxReceivePduSize()
  {
    return serverMaxReceivePduSize;
  }

  public void setServerMaxReceivePduSize(int serverMaxReceivePduSize)
  {
    this.serverMaxReceivePduSize = serverMaxReceivePduSize;
  }

  public int getVaaName()
  {
    return vaaName;
  }

  public void setVaaName(int vaaName)
  {
    this.vaaName = vaaName;
  }

  @Override
  public String toString()
  {
    return "NegotiatedXDlmsContext{" + "negotiatedDlmsVersionNumber=" + negotiatedDlmsVersionNumber +
           ", negotiatedDlmsConformance=" + negotiatedDlmsConformance + ", serverMaxReceivePduSize=" +
           serverMaxReceivePduSize + ", vaaName=" + vaaName + '}';
  }

  @Override
  public ServiceType getServiceType()
  {
    return ServiceType.INITIATE;
  }

  @Override
  public ServiceInvocationType getServiceInvocationType()
  {
    return ServiceInvocationType.RESPONSE;
  }

  public ServiceInvocation getServiceInvocation()
  {
    return ServiceInvocation.INITIATE_RESPONSE;
  }


}
