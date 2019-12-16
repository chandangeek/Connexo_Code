/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class07/CaptureObjectDefinition.java $
 * Version:     
 * $Id: CaptureObjectDefinition.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  31.05.2010 14:19:05
 */

package com.elster.dlms.cosem.classes.class07;

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
import com.elster.dlms.types.data.DlmsDataInteger;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;

/**
 * Class for the "capture_object_definition"<P>
 * Part of the capture_objects of the COSEM class "Profile Generic".<P>
 * See BB ed.10 p. 47<P>
 * (COSEM class id 7, Attribute 3)
 *
 * @author osse
 */
public class CaptureObjectDefinition
{
  private final int classId;
  private final ObisCode logicalName;
  private final int attributeIndex;
  private final int dataIndex;
  
  
  public final static AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DataType.LONG_UNSIGNED),
          new ValidatorOctetString(6,6),
          new ValidatorSimpleType(DataType.INTEGER),
          new ValidatorSimpleType(DataType.LONG_UNSIGNED)
);

  
    public final static AbstractValidator LIST_VALIDATOR = new ValidatorArray(VALIDATOR);

  public static CaptureObjectDefinition[] fromDlmsDataArray(DlmsData data) throws ValidationExecption
  {
    LIST_VALIDATOR.validate(data);
    DlmsDataArray array = (DlmsDataArray)data;
    CaptureObjectDefinition[] result = new CaptureObjectDefinition[array.size()];
    for (int i = 0; i < array.size(); i++)
    {
      result[i] = new CaptureObjectDefinition(array.get(i));
    }
    return result;
  }


  public CaptureObjectDefinition(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);
    final DlmsDataStructure structure= (DlmsDataStructure)data;

    final DlmsDataLongUnsigned dataClassId = (DlmsDataLongUnsigned)structure.get(0);
    final DlmsDataOctetString dataLogicalName = (DlmsDataOctetString)structure.get(1);
    final DlmsDataInteger dataAttributeIndex = (DlmsDataInteger)structure.get(2);
    final DlmsDataLongUnsigned dataDataIndex = (DlmsDataLongUnsigned)structure.get(3);

    classId = dataClassId.getValue();
    logicalName=new ObisCode(dataLogicalName.getValue());
    attributeIndex= dataAttributeIndex.getValue();
    dataIndex= dataDataIndex.getValue();
  }

  public CaptureObjectDefinition(int classId, ObisCode logicalName, int attributeIndex, int dataIndex)
  {
    this.classId = classId;
    this.logicalName = logicalName;
    this.attributeIndex = attributeIndex;
    this.dataIndex = dataIndex;
  }

  
  public DlmsData toDlmsData()
  {
    DlmsData[] elements= new DlmsData[4];
    
    elements[0]= new DlmsDataLongUnsigned(classId);
    elements[1]= new DlmsDataOctetString(logicalName.toByteArray());
    elements[2]= new DlmsDataInteger(attributeIndex);
    elements[3]= new DlmsDataLongUnsigned(dataIndex);
    
    return new DlmsDataStructure(elements);
  }



  public int getAttributeIndex()
  {
    return attributeIndex;
  }

  public int getClassId()
  {
    return classId;
  }

  public int getDataIndex()
  {
    return dataIndex;
  }

  public ObisCode getLogicalName()
  {
    return logicalName;
  }

  @Override
  public String toString()
  {
    return "CaptureObjectDefinition{" + "classId=" + classId + ", logicalName=" + logicalName + ", attributeIndex=" +
           attributeIndex + ", dataIndex=" + dataIndex + '}';
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
    final CaptureObjectDefinition other = (CaptureObjectDefinition)obj;
    if (this.classId != other.classId)
    {
      return false;
    }
    if (this.logicalName != other.logicalName &&
        (this.logicalName == null || !this.logicalName.equals(other.logicalName)))
    {
      return false;
    }
    if (this.attributeIndex != other.attributeIndex)
    {
      return false;
    }
    if (this.dataIndex != other.dataIndex)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 37 * hash + this.classId;
    hash = 37 * hash + (this.logicalName != null ? this.logicalName.hashCode() : 0);
    hash = 37 * hash + this.attributeIndex;
    hash = 37 * hash + this.dataIndex;
    return hash;
  }







  







}
