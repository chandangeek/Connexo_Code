/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/basic/CosemMethodDescriptorWithData.java $
 * Version:     
 * $Id: CosemMethodDescriptorWithData.java 5120 2012-09-07 15:57:36Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 11:11:27
 */
package com.elster.dlms.types.basic;

import com.elster.dlms.types.data.DlmsData;

/**
 * This class holds the COSEM method descriptor. <P>
 * See GB ed. 7 p. 151-154. Valid values are described in the BB.
 *
 * @author osse
 */
public class CosemMethodDescriptorWithData
{
  private final CosemMethodDescriptor methodDescriptor;
  private final DlmsData data;

  public CosemMethodDescriptorWithData(CosemMethodDescriptor methodDescriptor, DlmsData data)
  {
    this.methodDescriptor = methodDescriptor;
    this.data = data;
  }
  
  
  

  public CosemMethodDescriptorWithData(final ObisCode instanceId,final int classId,final int methodId, DlmsData data)
  {
    this.methodDescriptor = new CosemMethodDescriptor(instanceId, classId, methodId);
    this.data = data;
  }

  public CosemMethodDescriptor getMethodDescriptor()
  {
    return methodDescriptor;
  }

  public DlmsData getData()
  {
    return data;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 89 * hash + (this.methodDescriptor != null ? this.methodDescriptor.hashCode() : 0);
    hash = 89 * hash + (this.data != null ? this.data.hashCode() : 0);
    return hash;
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
    final CosemMethodDescriptorWithData other = (CosemMethodDescriptorWithData)obj;
    if (this.methodDescriptor != other.methodDescriptor &&
        (this.methodDescriptor == null || !this.methodDescriptor.equals(other.methodDescriptor)))
    {
      return false;
    }
    if (this.data != other.data && (this.data == null || !this.data.equals(other.data)))
    {
      return false;
    }
    return true;
  }

  @Override
  public String toString()
  {
    return "CosemMethodDescriptorWithData{" + "methodDescriptor=" + methodDescriptor + ", data=" + data + '}';
  }
  
  



}
