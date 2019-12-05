/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/types/basic/CosemAttributeDescriptorWithData.java $
 * Version:     
 * $Id: CosemAttributeDescriptorWithData.java 5118 2012-09-07 12:58:12Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Sep 7, 2012 2:10:57 PM
 */
package com.elster.dlms.types.basic;

import com.elster.dlms.types.data.DlmsData;

/**
 * Combination of a CosemAttributeDescriptor and DlmsData
 *
 * @author osse
 */
public class CosemAttributeDescriptorWithData
{
  private final CosemAttributeDescriptor cosemAttributeDescriptor;
  private final DlmsData data;

  public CosemAttributeDescriptorWithData(final CosemAttributeDescriptor cosemAttributeDescriptor,
                                          final DlmsData data)
  {
    this.cosemAttributeDescriptor = cosemAttributeDescriptor;
    this.data = data;
  }

  public CosemAttributeDescriptorWithData(final ObisCode instanceId, final int classId, final int attributeId,
                                          final DlmsData data)
  {
    this(instanceId, classId, attributeId, null, data);
  }

  public CosemAttributeDescriptorWithData(final ObisCode instanceId, final int classId, final int attributeId,
                                          final AccessSelectionParameters accessSelectionParameters,
                                          final DlmsData data)
  {
    this.cosemAttributeDescriptor = new CosemAttributeDescriptor(instanceId, classId, attributeId,
                                                                 accessSelectionParameters);
    this.data = data;
  }

  public CosemAttributeDescriptor getCosemAttributeDescriptor()
  {
    return cosemAttributeDescriptor;
  }

  public DlmsData getData()
  {
    return data;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 53 * hash + (this.cosemAttributeDescriptor != null ? this.cosemAttributeDescriptor.hashCode() : 0);
    hash = 53 * hash + (this.data != null ? this.data.hashCode() : 0);
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
    final CosemAttributeDescriptorWithData other = (CosemAttributeDescriptorWithData)obj;
    if (this.cosemAttributeDescriptor != other.cosemAttributeDescriptor &&
        (this.cosemAttributeDescriptor == null ||
         !this.cosemAttributeDescriptor.equals(other.cosemAttributeDescriptor)))
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
    return "CosemAttributeDescriptorWithData{" + "cosemAttributeDescriptor=" + cosemAttributeDescriptor +
           ", data=" + data + '}';
  }
  
  

}
