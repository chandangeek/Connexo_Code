/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class15/MethodAccessItem.java $
 * Version:     
 * $Id: MethodAccessItem.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  21.05.2010 11:46:30
 */
package com.elster.dlms.cosem.classes.class15;

import com.elster.dlms.cosem.classes.common.MethodAccessMode;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.IDlmsDataValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorChoice;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataBoolean;
import com.elster.dlms.types.data.DlmsDataEnum;
import com.elster.dlms.types.data.DlmsDataInteger;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.IDlmsDataProvider;

/**
 * Method Access Item for one element of the object list.
 *
 * @author osse
 */
public class MethodAccessItem implements IDlmsDataProvider
{
  public static final IDlmsDataValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DlmsData.DataType.INTEGER),
          new ValidatorChoice(
          new ValidatorSimpleType(DlmsData.DataType.BOOLEAN), //IC 15 V0
          new ValidatorSimpleType(DlmsData.DataType.ENUM)) //IC 15 V1
          );
  private final int methodId;
  private final MethodAccessMode accessMode;
  private final int class15Version;

  public MethodAccessItem(int methodId, MethodAccessMode accessMode, int class15Version)
  {
    this.methodId = methodId;
    this.accessMode = accessMode;
    this.class15Version = class15Version;
  }

  public MethodAccessItem(DlmsData data) throws ValidationExecption
  {

    VALIDATOR.validate(data);

    DlmsDataStructure structure = (DlmsDataStructure)data;
    DlmsDataInteger dataMethodId = (DlmsDataInteger)structure.get(0);

    if (structure.get(1).getType() == DlmsData.DataType.BOOLEAN)
    {
      //Class 15 V0
      class15Version = 0;
      DlmsDataBoolean dataAccessMode = (DlmsDataBoolean)structure.get(1);
      if (dataAccessMode.getValue())
      {
        accessMode = MethodAccessMode.ACCESS;
      }
      else
      {
        accessMode = MethodAccessMode.NO_ACCESS;
      }
    }
    else
    {
      //Class 15 V1
      class15Version = 1;

      DlmsDataEnum dataAccessMode = (DlmsDataEnum)structure.get(1);

      switch (dataAccessMode.getValue())
      {
        case 0:
          accessMode = MethodAccessMode.NO_ACCESS;
          break;
        case 1:
          accessMode = MethodAccessMode.ACCESS;
          break;
        case 2:
          accessMode = MethodAccessMode.AUTHENTICATED_ACCESS;
          break;
        default:
          throw new IllegalArgumentException("unknown method access mode: " + dataAccessMode.getValue());
      }

    }
    methodId = dataMethodId.getValue();
  }

  //@Override
  public DlmsData toDlmsData()
  {
    if (class15Version == 0)
    {
      return new DlmsDataStructure(
              new DlmsDataInteger(methodId),
              new DlmsDataBoolean(accessMode != MethodAccessMode.NO_ACCESS));
    }
    else
    {
      return new DlmsDataStructure(
              new DlmsDataInteger(methodId),
              new DlmsDataEnum(accessMode.ordinal()));
    }
  }

  public MethodAccessMode getAccessMode()
  {
    return accessMode;
  }

  public int getMethodId()
  {
    return methodId;
  }

  public int getClass15Version()
  {
    return class15Version;
  }

  @Override
  public String toString()
  {
    return "meth. id=" + methodId + ", access=" + accessMode;
  }

}
