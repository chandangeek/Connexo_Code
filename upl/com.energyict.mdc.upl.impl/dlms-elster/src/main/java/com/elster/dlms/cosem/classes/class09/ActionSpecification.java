/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class09/ActionSpecification.java $
 * Version:     
 * $Id: ActionSpecification.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  08.07.2011 10:12:25
 */
package com.elster.dlms.cosem.classes.class09;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorAny;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorOctetString;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataEnum;
import com.elster.dlms.types.data.DlmsDataInteger;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;

/**
 * Action specification for script tables. (see BB ed.10 p.75)
 *
 * @author osse
 */
public class ActionSpecification
{
  private final ServiceIdEnum serviceId;
  private final int classId;
  private final ObisCode logicalName;
  private final int index;
  private final DlmsData parameter;
  public static final AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DataType.ENUM),
          new ValidatorSimpleType(DataType.LONG_UNSIGNED),
          new ValidatorOctetString(6, 6),
          new ValidatorSimpleType(DataType.INTEGER),
          new ValidatorAny());
  public final static AbstractValidator LIST_VALIDATOR = new ValidatorArray(VALIDATOR);

  public static ActionSpecification[] fromDlmsDataArray(DlmsData data) throws ValidationExecption
  {
    LIST_VALIDATOR.validate(data);
    DlmsDataArray array = (DlmsDataArray)data;
    ActionSpecification[] result = new ActionSpecification[array.size()];
    for (int i = 0; i < array.size(); i++)
    {
      result[i] = new ActionSpecification(array.get(i));
    }
    return result;
  }

  public static DlmsDataArray toDlmsDataArray(ActionSpecification[] actions)
  {
    DlmsData[] array = new DlmsData[actions.length];
    for (int i = 0; i < array.length; i++)
    {
      array[i] = actions[i].toDlmsData();
    }
    return new DlmsDataArray(array);
  }

  public ActionSpecification(final ServiceIdEnum serviceId, final int classId, final ObisCode logicalName,
                             final int index, final DlmsData parameter)
  {
    this.serviceId = serviceId;
    this.classId = classId;
    this.logicalName = logicalName;
    this.index = index;
    this.parameter = parameter;
  }

  public ActionSpecification(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);
    final DlmsDataStructure structure = (DlmsDataStructure)data;
    serviceId = ServiceIdEnum.getFactory().findValue(((DlmsDataEnum)structure.get(0)).getValue());
    if (serviceId == null)
    {
      throw new ValidationExecption("Unexpected service id: " + data.toString());
    }
    classId = ((DlmsDataLongUnsigned)structure.get(1)).getValue();
    logicalName = new ObisCode(((DlmsDataOctetString)structure.get(2)).getValue());
    index = ((DlmsDataInteger)structure.get(3)).getValue();
    parameter = structure.get(4);
  }

  public DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataEnum(serviceId.getId()),
            new DlmsDataLongUnsigned(classId),
            new DlmsDataOctetString(logicalName.toByteArray()),
            new DlmsDataInteger(index),
            parameter);
  }

  public int getClassId()
  {
    return classId;
  }

  public int getIndex()
  {
    return index;
  }

  public ObisCode getLogicalName()
  {
    return logicalName;
  }

  public DlmsData getParameter()
  {
    return parameter;
  }

  public ServiceIdEnum getServiceId()
  {
    return serviceId;
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
    final ActionSpecification other = (ActionSpecification)obj;
    if (this.serviceId != other.serviceId && (this.serviceId == null
                                              || !this.serviceId.equals(other.serviceId)))
    {
      return false;
    }
    if (this.classId != other.classId)
    {
      return false;
    }
    if (this.logicalName != other.logicalName && (this.logicalName == null || !this.logicalName.equals(
            other.logicalName)))
    {
      return false;
    }
    if (this.index != other.index)
    {
      return false;
    }
    if (this.parameter != other.parameter && (this.parameter == null
                                              || !this.parameter.equals(other.parameter)))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 3;
    hash = 89 * hash + (this.serviceId != null ? this.serviceId.hashCode() : 0);
    hash = 89 * hash + this.classId;
    hash = 89 * hash + (this.logicalName != null ? this.logicalName.hashCode() : 0);
    hash = 89 * hash + this.index;
    hash = 89 * hash + (this.parameter != null ? this.parameter.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString()
  {
    return "ActionSpecification{" + "serviceId=" + serviceId + ", classId=" + classId + ", logicalName="
           + logicalName + ", index=" + index + ", parameter=" + parameter + '}';
  }

}
