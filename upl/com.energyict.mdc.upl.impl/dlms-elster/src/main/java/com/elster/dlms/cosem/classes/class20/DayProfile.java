/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class20/DayProfile.java $
 * Version:     
 * $Id: DayProfile.java 4279 2012-04-02 14:37:29Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  May 5, 2011 10:20:21 AM
 */
package com.elster.dlms.cosem.classes.class20;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorChoice;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorOctetString;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.basic.DlmsTime;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.*;
import com.elster.dlms.types.data.DlmsData.DataType;
import java.util.Arrays;
import java.util.Collection;

/**
 * Day profile for the activity calendar<P>
 * See BB ed.10 p.81
 *
 * @author osse
 */
public class DayProfile implements IDlmsDataProvider
{
  private final int dayId;
  private final DayProfileAction[] dayProfileActions;
  public final static AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DataType.UNSIGNED),
          new ValidatorArray(DayProfileAction.VALIDATOR));
  
  public final static AbstractValidator LIST_VALIDATOR = new ValidatorArray(VALIDATOR);
  
  public static final DayProfile[] EMPTY_DAY_PROFILES= new DayProfile[0];
  public static final DayProfileAction[] EMPTY_DAY_PROFILE_ACTIONS= new DayProfileAction[0];

  public static DayProfile[] fromDlmsDataArray(DlmsData data) throws ValidationExecption
  {
    LIST_VALIDATOR.validate(data);
    DlmsDataArray array = (DlmsDataArray)data;
    DayProfile[] result = new DayProfile[array.size()];
    for (int i = 0; i < array.size(); i++)
    {
      result[i] = new DayProfile(array.get(i));
    }
    return result;
  }
  

  public DayProfile(final int dayId, final DayProfileAction[] dayProfileActions)
  {
    this.dayId = dayId;
    this.dayProfileActions = dayProfileActions.clone();
  }

  public DayProfile(final int dayId, Collection<DayProfileAction> dayProfileActions)
  {
    this(dayId,dayProfileActions.toArray(EMPTY_DAY_PROFILE_ACTIONS));
  }

  public DayProfile(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);
    final DlmsDataStructure structure = (DlmsDataStructure)data;
    this.dayId = ((DlmsDataUnsigned)(structure.get(0))).getValue();
    final DlmsDataArray array = (DlmsDataArray)structure.get(1);

    dayProfileActions = new DayProfileAction[array.size()];

    for (int i = 0; i < dayProfileActions.length; i++)
    {
      dayProfileActions[i] = new DayProfileAction(array.get(i));
    }
  }

  public DlmsData toDlmsData()
  {
    DlmsData[] actions = new DlmsData[dayProfileActions.length];

    for (int i = 0; i < dayProfileActions.length; i++)
    {
      actions[i] = dayProfileActions[i].toDlmsData();
    }

    return new DlmsDataStructure(
            new DlmsDataUnsigned(dayId),
            new DlmsDataArray(actions));
  }

  public int getDayId()
  {
    return dayId;
  }

  public DayProfileAction[] getDayProfileActions()
  {
    return dayProfileActions.clone();
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("DayProfile{" + "dayId=");
    sb.append(dayId);
    sb.append(", dayProfileActions={");

    boolean first = true;
    for (DayProfileAction dpa : dayProfileActions)
    {
      if (!first)
      {
        sb.append(", ");
      }
      sb.append(dpa.toString());
      first=false;
    }
    sb.append("}}");
    return sb.toString();
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
    final DayProfile other = (DayProfile)obj;
    if (this.dayId != other.dayId)
    {
      return false;
    }
    if (!Arrays.deepEquals(this.dayProfileActions, other.dayProfileActions))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 97 * hash + this.dayId;
    hash = 97 * hash + Arrays.deepHashCode(this.dayProfileActions);
    return hash;
  }
  

  public static class DayProfileAction
  {
    private final DlmsTime startTime;
    private final ObisCode scriptLogicalName;
    private final int scriptSelector;
    public static final AbstractValidator VALIDATOR = new ValidatorStructure(
            new ValidatorChoice(new ValidatorOctetString(4, 4),new ValidatorSimpleType(DataType.TIME)), new ValidatorOctetString(6, 6), new ValidatorSimpleType(
            DataType.LONG_UNSIGNED));

    public DayProfileAction(final DlmsTime startTime, final ObisCode scriptLogicalName,
                            final int scriptSelector)
    {
      this.startTime = startTime;
      this.scriptLogicalName = scriptLogicalName;
      this.scriptSelector = scriptSelector;
    }

    public DayProfileAction(final DlmsData data) throws ValidationExecption
    {
      VALIDATOR.validate(data);
      final DlmsDataStructure structure = (DlmsDataStructure)data;

      DlmsData startTimeData = structure.get(0);
      
      if (startTimeData instanceof DlmsDataTime)
      {
         this.startTime=((DlmsDataTime) startTimeData).getValue();
      }
      else
      {
        this.startTime = new DlmsTime(((DlmsDataOctetString)(structure.get(0))).getValue());
      }
      
      this.scriptLogicalName = new ObisCode(((DlmsDataOctetString)(structure.get(1))).getValue());
      this.scriptSelector = ((DlmsDataLongUnsigned)(structure.get(2))).getValue();
    }

    public DlmsData toDlmsData()
    {
      return new DlmsDataStructure(
              new DlmsDataOctetString(startTime.toBytes()),
              new DlmsDataOctetString(scriptLogicalName.toByteArray()),
              new DlmsDataLongUnsigned(scriptSelector));
    }

    public ObisCode getScriptLogicalName()
    {
      return scriptLogicalName;
    }

    public int getScriptSelector()
    {
      return scriptSelector;
    }

    public DlmsTime getStartTime()
    {
      return startTime;
    }

    @Override
    public String toString()
    {
      return "DayProfileAction{" + "startTime=" + startTime + ", scriptLogicalName=" + scriptLogicalName
             + ", scriptSelector=" + scriptSelector + '}';
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
      final DayProfileAction other = (DayProfileAction)obj;
      if (this.startTime != other.startTime &&
          (this.startTime == null || !this.startTime.equals(other.startTime)))
      {
        return false;
      }
      if (this.scriptLogicalName != other.scriptLogicalName &&
          (this.scriptLogicalName == null || !this.scriptLogicalName.equals(other.scriptLogicalName)))
      {
        return false;
      }
      if (this.scriptSelector != other.scriptSelector)
      {
        return false;
      }
      return true;
    }

    @Override
    public int hashCode()
    {
      int hash = 7;
      hash = 41 * hash + (this.startTime != null ? this.startTime.hashCode() : 0);
      hash = 41 * hash + (this.scriptLogicalName != null ? this.scriptLogicalName.hashCode() : 0);
      hash = 41 * hash + this.scriptSelector;
      return hash;
    }
    
    

  }

}
