/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class8193/ParameterListElement.java $
 * Version:     
 * $Id: ParameterListElement.java 3583 2011-09-28 15:44:22Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  08.08.2011 11:20:00
 */
package com.elster.dlms.cosem.classes.class8193;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.IDlmsDataValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorOctetString;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.basic.BitString;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataBitString;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.IDlmsDataProvider;

/**
 * Parameter List Element for IC 8193 (proposal for IC 65)
 *
 * @author osse
 */
public class ParameterListElement implements IDlmsDataProvider
{
  private final int classId;
  private final ObisCode logicalName;
  private final BitString attributeList;
  public final static IDlmsDataValidator VALIDATOR =
          CosemAttributeValidators.immutableValidator(new ValidatorStructure(
          new ValidatorSimpleType(DlmsData.DataType.LONG_UNSIGNED),
          new ValidatorOctetString(6, 6),
          new ValidatorSimpleType(DlmsData.DataType.BIT_STRING)));
  public final static IDlmsDataValidator LIST_VALIDATOR =
          CosemAttributeValidators.immutableValidator(new ValidatorArray(VALIDATOR));

  public static ParameterListElement[] fromDlmsDataArray(DlmsData data) throws ValidationExecption
  {
    LIST_VALIDATOR.validate(data);
    DlmsDataArray array = (DlmsDataArray)data;
    ParameterListElement[] result = new ParameterListElement[array.size()];
    for (int i = 0; i < array.size(); i++)
    {
      result[i] = new ParameterListElement(array.get(i));
    }
    return result;
  }

  public ParameterListElement(int classId, ObisCode logicalName, BitString attributeList)
  {
    this.classId = classId;
    this.logicalName = logicalName;
    this.attributeList = attributeList;
  }

  private ParameterListElement(DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);

    DlmsDataStructure structure = (DlmsDataStructure)data;
    DlmsDataLongUnsigned classIdData = (DlmsDataLongUnsigned)structure.get(0);
    DlmsDataOctetString logicalNameData = (DlmsDataOctetString)structure.get(1);
    DlmsDataBitString attributeListData = (DlmsDataBitString)structure.get(2);

    this.classId = classIdData.getValue();
    this.logicalName = new ObisCode(logicalNameData.getValue());
    this.attributeList = attributeListData.getValue();
  }

  //@Override
  public DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataLongUnsigned(classId),
            new DlmsDataOctetString(logicalName.toByteArray()),
            new DlmsDataBitString(attributeList));
  }

  public BitString getAttributeList()
  {
    return attributeList;
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
    return "ParameterListElement{" + "classId=" + classId + ", logicalName=" + logicalName + ", attributeList=" +
           attributeList + '}';
  }

}
