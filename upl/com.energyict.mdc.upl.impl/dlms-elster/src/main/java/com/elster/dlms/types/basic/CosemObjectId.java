/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/basic/CosemObjectId.java $
 * Version:     
 * $Id: CosemObjectId.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2011 09:05:18
 */
package com.elster.dlms.types.basic;

/**
 * ID for one COSEM object in the device.<P>
 * See GB ed.7 p.149.
 * 
 * @author osse
 */
public class CosemObjectId
{
  private final int classId;
  private final ObisCode logicalName;

  public CosemObjectId(int classId, ObisCode logicalName)
  {
    this.classId = classId;
    this.logicalName = logicalName;
  }

  public int getClassId()
  {
    return classId;
  }

  public ObisCode getLogicalName()
  {
    return logicalName;
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
    final CosemObjectId other = (CosemObjectId)obj;
    if (this.classId != other.classId)
    {
      return false;
    }
    if (this.logicalName != other.logicalName &&
        (this.logicalName == null || !this.logicalName.equals(other.logicalName)))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 73 * hash + this.classId;
    hash = 73 * hash + (this.logicalName != null ? this.logicalName.hashCode() : 0);
    return hash;
  }
  
}
