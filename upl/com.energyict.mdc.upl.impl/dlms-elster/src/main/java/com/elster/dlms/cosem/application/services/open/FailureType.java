/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/application/services/open/FailureType.java $
 * Version:     
 * $Id: FailureType.java 4279 2012-04-02 14:37:29Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.07.2010 12:17:05
 */

package com.elster.dlms.cosem.application.services.open;

/**
 * The Failure Type of COSEM OPEN service.
 * 
 * see GB p.204 "ServiceError"
 *
 * @author osse
 */
public class FailureType
{
  private int type;
  private int reason;

  public FailureType()
  {
  }

  public FailureType(int type, int reason)
  {
    this.type = type;
    this.reason = reason;
  }
 
  public int getReason()
  {
    return reason;
  }

  public void setReason(int reason)
  {
    this.reason = reason;
  }

  public int getType()
  {
    return type;
  }

  public void setType(int type)
  {
    this.type = type;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final FailureType other = (FailureType)obj;
    if (this.type != other.type)
    {
      return false;
    }
    if (this.reason != other.reason)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 67 * hash + this.type;
    hash = 67 * hash + this.reason;
    return hash;
  }

  @Override
  public String toString()
  {
    return "FailureType{" + "type=" + type + ", reason=" + reason + '}';
  }



}
