/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/basic/ServiceInvocationId.java $
 * Version:     
 * $Id: ServiceInvocationId.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 10:54:23
 */
package com.elster.dlms.types.basic;

/**
 * This class is the DLMS service invocation ID and priority.<P>
 * See GB ed.7 p.117, encoding see GB ed.7 p.212 "Invoke-Id-And-Priority".
 *
 * @author osse
 */
public class ServiceInvocationId
{
  private final int id; //Value from security examples

  /**
   * Creates a service invocation using the specified id.
   *
   * @param id The complete service invocation "byte".
   */
  public ServiceInvocationId(int id)
  {
    this.id = id;
  }

  /**
   * Creates a service invocation id with specified parameters.
   *
   * @param invokeId The invocation id (Only the 4 LSBs are used)
   * @param priority
   * @param serviceClass
   */
  public ServiceInvocationId(int invokeId, Priority priority, ServiceClass serviceClass)
  {
    int localId = 0;

    //--- invoke id ---
    localId = (0x0F & localId) | (0xF0 & (invokeId << 4));

    //--- priority ---
    if (priority == Priority.HIGH)
    {
      localId = localId | 0x01;
    }
    else
    {
      localId = localId & (0x01 ^ 0xFF);
    }

    //--- serviceClass ---
    if (serviceClass == ServiceClass.CONFIRMED)
    {
      localId = localId | 0x02;
    }
    else
    {
      localId = localId & (0x02 ^ 0xFF);
    }

    this.id = localId;
  }

  public enum Priority
  {
    NORMAL, HIGH
  };

  ;

  public int getInvokeID()
  {
    return 0x0F & (id >> 4);
  }

  public Priority getPriority()
  {
    if (0 != (0x01 & id))
    {
      return Priority.HIGH;
    }
    else
    {
      return Priority.NORMAL;
    }
  }

  public ServiceClass getServiceClass()
  {
    if (0 != (0x02 & id))
    {
      return ServiceClass.CONFIRMED;
    }
    else
    {
      return ServiceClass.UNCONFIRMED;
    }
  }

  public int toInteger()
  {
    return id;
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
    final ServiceInvocationId other = (ServiceInvocationId)obj;
    if (this.id != other.id)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 47 * hash + this.id;
    return hash;
  }

  @Override
  public String toString()
  {
    return "ServiceInvocationId{" + "id=" + id + ", invokeId=" + getInvokeID() + ", priority=" + getPriority()
           + ", serviceClass=" + getServiceClass() + '}';
  }

}
