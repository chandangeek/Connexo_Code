/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class21/ValueDefinition.java $
 * Version:     
 * $Id: ValueDefinition.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  05.08.2011 09:00:41
 */
package com.elster.dlms.cosem.classes.class21;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.IDlmsDataValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorOctetString;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataInteger;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.IDlmsDataProvider;

/**
 * Value Definition for the Register monitor (IC 21 Attr. 3)<P>
 * See BB ed.10 p.82
 *
 * @author osse
 */
public class ValueDefinition implements IDlmsDataProvider
{ 
  private final int classId;
  private final ObisCode logicalName;
  private final int attributeIndex;
  public final static IDlmsDataValidator VALIDATOR = CosemAttributeValidators.immutableValidator(
          new ValidatorStructure(
          new ValidatorSimpleType(DataType.LONG_UNSIGNED),
          new ValidatorOctetString(6, 6),
          new ValidatorSimpleType(DataType.INTEGER)));

  public ValueDefinition(int classId, ObisCode logicalName, int attributeIndex)
  {
    this.classId = classId;
    this.logicalName = logicalName;
    this.attributeIndex = attributeIndex;
  }

  public ValueDefinition(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);

    final DlmsDataStructure structure = (DlmsDataStructure)data;
    final DlmsDataLongUnsigned classIdData = (DlmsDataLongUnsigned)structure.get(0);
    final DlmsDataOctetString logicalNameData = (DlmsDataOctetString)structure.get(1);
    final DlmsDataInteger attributeIndexData = (DlmsDataInteger)structure.get(2);

    this.classId = classIdData.getValue();
    this.logicalName = new ObisCode(logicalNameData.getValue());
    this.attributeIndex = attributeIndexData.getValue();
  }

  //@Override
  public DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataLongUnsigned(classId),
            new DlmsDataOctetString(logicalName.toByteArray()),
            new DlmsDataInteger(attributeIndex));
  }

  public int getAttributeIndex()
  {
    return attributeIndex;
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
    return "ValueDefinition{" + "classId=" + classId + ", logicalName=" + logicalName + ", attributeIndex=" +
           attributeIndex + '}';
  }

  
  
}
