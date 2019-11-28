/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class06/ObjectDefinition.java $
 * Version:     
 * $Id: ObjectDefinition.java 3040 2011-06-06 16:53:17Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Apr 27, 2011 10:33:40 AM
 */
package com.elster.dlms.cosem.classes.class06;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorOctetString;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;

/**
 * Object definition for COSEM class id 6 attribute 2 (Register activation - register_assignment)<P>
 * See BB ed.10 p. 44
 *
 * @author osse
 */
public class ObjectDefinition
{
  private final int classId;
  private final ObisCode logicalName;
  public final static AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DataType.LONG_UNSIGNED),
          new ValidatorOctetString(6, 6));

  public ObjectDefinition(final int classId, final ObisCode logicalName)
  {
    this.classId = classId;
    this.logicalName = logicalName;
  }

  public ObjectDefinition(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);
    final DlmsDataStructure structure = (DlmsDataStructure)data;
    this.classId = ((DlmsDataLongUnsigned)structure.get(0)).getValue();
    this.logicalName = new ObisCode(((DlmsDataOctetString)structure.get(1)).getValue());
  }

  DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataLongUnsigned(classId),
            new DlmsDataOctetString(logicalName.toByteArray()));
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
    final ObjectDefinition other = (ObjectDefinition)obj;
    if (this.classId != other.classId)
    {
      return false;
    }
    if (this.logicalName != other.logicalName && (this.logicalName == null || !this.logicalName.equals(
            other.logicalName)))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 61 * hash + this.classId;
    hash = 61 * hash + (this.logicalName != null ? this.logicalName.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString()
  {
    return "ObjectDefinition{" + "classId=" + classId + ", logicalName=" + logicalName + '}';
  }

}
