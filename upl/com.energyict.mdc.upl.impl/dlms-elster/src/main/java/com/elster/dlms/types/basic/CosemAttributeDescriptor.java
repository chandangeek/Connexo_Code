/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/basic/CosemAttributeDescriptor.java $
 * Version:     
 * $Id: CosemAttributeDescriptor.java 3685 2011-10-12 11:39:18Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  19.05.2010 11:11:27
 */
package com.elster.dlms.types.basic;

/**
 * This class holds the COSEM attribute descriptor. <P>
 * See GB ed. 7 p. 146-149. Valid values are described in the BB.
 *
 * @author osse
 */
public class CosemAttributeDescriptor
{
  private final ObisCode instanceId;
  private final int classId;
  private final int attributeId;
  private final AccessSelectionParameters accessSelectionParameters;

  public CosemAttributeDescriptor(final ObisCode instanceId, final int classId, final int attributeId)
  {
    this(instanceId, classId, attributeId, null);
  }

  public CosemAttributeDescriptor(final ObisCode instanceId, final int classId, final int attributeId,
                                  final AccessSelectionParameters accessSelectionParameters)
  {
    this.classId = classId;
    this.instanceId = instanceId;
    this.attributeId = attributeId;
    this.accessSelectionParameters = accessSelectionParameters;
  }

  public int getAttributeId()
  {
    return attributeId;
  }

  public int getClassId()
  {
    return classId;
  }

  public ObisCode getInstanceId()
  {
    return instanceId;
  }

  public AccessSelectionParameters getAccessSelectionParameters()
  {
    return accessSelectionParameters;
  }

  @Override
  public String toString()
  {
    return "CosemAttributeDescriptor{" + "instanceId=" + instanceId + ", classId=" + classId
           + ", attributeId=" + attributeId + ", accessSelectionParameters=" + accessSelectionParameters + '}';
  }

  /**
   * Creates an attribute descriptor without access selection parameters.<P>
   * (If this object has no access selection parameters, the method will simply return {@code this}.)
   * 
   * @return The {@code CosemAttributeDescriptor} without access selection parameters.
   */
  public CosemAttributeDescriptor stripAccessSelectionParameters()
  {
    if (accessSelectionParameters == null)
    {
      return this;
    }
    else
    {
      return new CosemAttributeDescriptor(instanceId, classId, attributeId);
    }
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
    final CosemAttributeDescriptor other = (CosemAttributeDescriptor)obj;
    if (this.instanceId != other.instanceId && (this.instanceId == null || !this.instanceId.equals(
            other.instanceId)))
    {
      return false;
    }
    if (this.classId != other.classId)
    {
      return false;
    }
    if (this.attributeId != other.attributeId)
    {
      return false;
    }
    if (this.accessSelectionParameters != other.accessSelectionParameters && (this.accessSelectionParameters
                                                                              == null
                                                                              || !this.accessSelectionParameters.
            equals(other.accessSelectionParameters)))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 37 * hash + (this.instanceId != null ? this.instanceId.hashCode() : 0);
    hash = 37 * hash + this.classId;
    hash = 37 * hash + this.attributeId;
    return hash;
  }

}
