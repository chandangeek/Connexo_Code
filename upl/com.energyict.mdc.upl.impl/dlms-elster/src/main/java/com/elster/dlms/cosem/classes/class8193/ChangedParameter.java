/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class8193/ChangedParameter.java $
 * Version:     
 * $Id: ChangedParameter.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  08.08.2011 11:20:00
 */
package com.elster.dlms.cosem.classes.class8193;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.IDlmsDataValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorOctetString;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.DlmsDataUnsigned;
import com.elster.dlms.types.data.IDlmsDataProvider;

/**
 * Changed Parameter for IC 8193 (proposal for IC 65)
 *
 * @author osse
 */
public class ChangedParameter implements IDlmsDataProvider
{
  private final int classId;
  private final ObisCode logicalName;
  private final int attributeId;
  private final DlmsData attributeValue;
  public final static IDlmsDataValidator VALIDATOR =
          CosemAttributeValidators.immutableValidator(
          new ValidatorStructure(
          new ValidatorSimpleType(DlmsData.DataType.LONG_UNSIGNED),
          new ValidatorOctetString(6, 6),
          new ValidatorStructure(
          new ValidatorSimpleType(DlmsData.DataType.UNSIGNED),
          new CosemAttributeValidators.ValidatorAny())));
  
//  public final static IDlmsDataValidator LIST_VALIDATOR =
//          CosemAttributeValidators.immutableValidator(new ValidatorArray(VALIDATOR));
//
//  public static ChangedParameter[] fromDlmsDataArray(DlmsData data) throws ValidationExecption
//  {
//    LIST_VALIDATOR.validate(data);
//    DlmsDataArray array = (DlmsDataArray)data;
//    ChangedParameter[] result = new ChangedParameter[array.size()];
//    for (int i = 0; i < array.size(); i++)
//    {
//      result[i] = new ChangedParameter(array.get(i));
//    }
//    return result;
//  }

  public ChangedParameter(int classId, ObisCode logicalName, int attributeId, DlmsData attributeValue)
  {
    this.classId = classId;
    this.logicalName = logicalName;
    this.attributeId = attributeId;
    this.attributeValue = attributeValue;
  }


  private ChangedParameter(DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);

    DlmsDataStructure structure = (DlmsDataStructure)data;
    DlmsDataLongUnsigned classIdData = (DlmsDataLongUnsigned)structure.get(0);
    DlmsDataOctetString logicalNameData = (DlmsDataOctetString)structure.get(1);
    
    DlmsDataStructure attributesStructure = (DlmsDataStructure)structure.get(2);
    
    DlmsDataUnsigned attributeIdData= (DlmsDataUnsigned)attributesStructure.get(0);
    
    this.classId = classIdData.getValue();
    this.logicalName = new ObisCode(logicalNameData.getValue());
    this.attributeId= attributeIdData.getValue();
    this.attributeValue = attributesStructure.get(1); //can be directly assigned.
  }

  //@Override
  public DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataLongUnsigned(classId),
            new DlmsDataOctetString(logicalName.toByteArray()),
            new DlmsDataStructure(
                    new DlmsDataUnsigned(classId),
                    attributeValue
                    )
            );
  }

  public int getAttributeId()
  {
    return attributeId;
  }

  public DlmsData getAttributeValue()
  {
    return attributeValue;
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
  public String toString()
  {
    return "ChangedParameter{" + "classId=" + classId + ", logicalName=" + logicalName + ", attributeId=" +
           attributeId + ", attributeValue=" + attributeValue + '}';
  }
  
  
  
}
