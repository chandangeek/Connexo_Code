/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class15/ReducedObjectListElement.java $
 * Version:     
 * $Id: ReducedObjectListElement.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.07.2011 14:53:04
 */
package com.elster.dlms.cosem.classes.class15;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorOctetString;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.DlmsDataUnsigned;

/**
 * Small object list element.<P>
 * Used for selective access (ids 1 & 2).<br>
 * see BB ed. 10 p. 61
 * 
 *
 * @author osse
 */
public class ReducedObjectListElement
{
  private final int classId;
  private final int version;
  private final ObisCode logicalName;
  
  public final static AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DataType.LONG_UNSIGNED),
          new ValidatorSimpleType(DataType.UNSIGNED),
          new ValidatorOctetString(6, 6));
  
  public final static AbstractValidator LIST_VALIDATOR = new ValidatorArray(VALIDATOR);
  
  
  public static ReducedObjectListElement[] fromDlmsDataArray(DlmsData data) throws ValidationExecption
  {
    LIST_VALIDATOR.validate(data);
    DlmsDataArray array = (DlmsDataArray)data;
    ReducedObjectListElement[] result = new ReducedObjectListElement[array.size()];
    for (int i = 0; i < array.size(); i++)
    {
      result[i] = new ReducedObjectListElement(array.get(i));
    }
    return result;
  }

  public ReducedObjectListElement(int classId, int version, ObisCode logicalName)
  {
    this.classId = classId;
    this.version = version;
    this.logicalName = logicalName;
  }
  
  public ReducedObjectListElement(DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);
    
    DlmsDataStructure s= (DlmsDataStructure)data;
    DlmsDataLongUnsigned classIdData= (DlmsDataLongUnsigned)s.get(0);
    DlmsDataUnsigned versionData= (DlmsDataUnsigned)s.get(1);
    DlmsDataOctetString lnData= (DlmsDataOctetString)s.get(2);
    
    this.classId= classIdData.getValue();
    this.version= versionData.getValue();
    this.logicalName= new ObisCode(lnData.getValue());
  }

  public int getClassId()
  {
    return classId;
  }

  public ObisCode getLogicalName()
  {
    return logicalName;
  }

  public int getVersion()
  {
    return version;
  }

  @Override
  public String toString()
  {
    return "ReducedObjectListElement{" + "classId=" + classId + ", version=" + version + ", logicalName=" +
           logicalName + '}';
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
    final ReducedObjectListElement other = (ReducedObjectListElement)obj;
    if (this.classId != other.classId)
    {
      return false;
    }
    if (this.version != other.version)
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
    int hash = 3;
    hash = 41 * hash + this.classId;
    hash = 41 * hash + this.version;
    hash = 41 * hash + (this.logicalName != null ? this.logicalName.hashCode() : 0);
    return hash;
  }
  
  
  
}
