/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class20/WeekProfile.java $
 * Version:     
 * $Id: WeekProfile.java 3649 2011-09-30 14:11:10Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  May 5, 2011 10:10:24 AM
 */
package com.elster.dlms.cosem.classes.class20;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorOctetString;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.DlmsDataUnsigned;
import com.elster.dlms.types.data.IDlmsDataProvider;

/**
 * Week profile for the activity calendar<P>
 * See BB ed.10 p.81
 *
 * @author osse
 */
public class WeekProfile implements IDlmsDataProvider
{
  private final String weekProfileName;
  private final int monday;
  private final int tuesday;
  private final int wednesday;
  private final int thursday;
  private final int friday;
  private final int saturday;
  private final int sunday;
  public static final AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorOctetString(-1, -1),
          new ValidatorSimpleType(DataType.UNSIGNED),
          new ValidatorSimpleType(DataType.UNSIGNED),
          new ValidatorSimpleType(DataType.UNSIGNED),
          new ValidatorSimpleType(DataType.UNSIGNED),
          new ValidatorSimpleType(DataType.UNSIGNED),
          new ValidatorSimpleType(DataType.UNSIGNED),
          new ValidatorSimpleType(DataType.UNSIGNED));
  
    
  public final static AbstractValidator LIST_VALIDATOR = new ValidatorArray(VALIDATOR);

  public static WeekProfile[] fromDlmsDataArray(DlmsData data) throws ValidationExecption
  {
    LIST_VALIDATOR.validate(data);
    DlmsDataArray array = (DlmsDataArray)data;
    WeekProfile[] result = new WeekProfile[array.size()];
    for (int i = 0; i < array.size(); i++)
    {
      result[i] = new WeekProfile(array.get(i));
    }
    return result;
  }
  

  public WeekProfile(final String weekProfileName, final int monday, final int tuesday, final int wednesday,
                     final int thursday, final int friday,
                     final int saturday, final int sunday)
  {
    this.weekProfileName = weekProfileName;
    this.monday = monday;
    this.tuesday = tuesday;
    this.wednesday = wednesday;
    this.thursday = thursday;
    this.friday = friday;
    this.saturday = saturday;
    this.sunday = sunday;
  }

  public WeekProfile(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);
    final DlmsDataStructure structure = (DlmsDataStructure)data;
    this.weekProfileName = new String(((DlmsDataOctetString)(structure.get(0))).getValue());
    this.monday = ((DlmsDataUnsigned)(structure.get(1))).getValue();
    this.tuesday = ((DlmsDataUnsigned)(structure.get(2))).getValue();
    this.wednesday = ((DlmsDataUnsigned)(structure.get(3))).getValue();
    this.thursday = ((DlmsDataUnsigned)(structure.get(4))).getValue();
    this.friday = ((DlmsDataUnsigned)(structure.get(5))).getValue();
    this.saturday = ((DlmsDataUnsigned)(structure.get(6))).getValue();
    this.sunday = ((DlmsDataUnsigned)(structure.get(7))).getValue();
  }

  public DlmsData toDlmsData()
  {

    return new DlmsDataStructure(
            new DlmsDataOctetString(weekProfileName.getBytes()),
            new DlmsDataUnsigned(monday),
            new DlmsDataUnsigned(tuesday),
            new DlmsDataUnsigned(wednesday),
            new DlmsDataUnsigned(thursday),
            new DlmsDataUnsigned(friday),
            new DlmsDataUnsigned(saturday),
            new DlmsDataUnsigned(sunday));
  }

  public int getFriday()
  {
    return friday;
  }

  public int getMonday()
  {
    return monday;
  }

  public int getSaturday()
  {
    return saturday;
  }

  public int getSunday()
  {
    return sunday;
  }

  public int getThursday()
  {
    return thursday;
  }

  public int getTuesday()
  {
    return tuesday;
  }

  public int getWednesday()
  {
    return wednesday;
  }

  public String getWeekProfileName()
  {
    return weekProfileName;
  }

  @Override
  public String toString()
  {
    return "WeekProfile{" + "weekProfileName=" + weekProfileName + ", monday=" + monday + ", tuesday=" + tuesday +
           ", wednesday=" + wednesday + ", thursday=" + thursday + ", friday=" + friday + ", saturday=" + saturday +
           ", sunday=" + sunday + '}';
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
    final WeekProfile other = (WeekProfile)obj;
    if ((this.weekProfileName == null) ? (other.weekProfileName != null)
            : !this.weekProfileName.equals(other.weekProfileName))
    {
      return false;
    }
    if (this.monday != other.monday)
    {
      return false;
    }
    if (this.tuesday != other.tuesday)
    {
      return false;
    }
    if (this.wednesday != other.wednesday)
    {
      return false;
    }
    if (this.thursday != other.thursday)
    {
      return false;
    }
    if (this.friday != other.friday)
    {
      return false;
    }
    if (this.saturday != other.saturday)
    {
      return false;
    }
    if (this.sunday != other.sunday)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 23 * hash + (this.weekProfileName != null ? this.weekProfileName.hashCode() : 0);
    hash = 23 * hash + this.monday;
    hash = 23 * hash + this.tuesday;
    hash = 23 * hash + this.wednesday;
    hash = 23 * hash + this.thursday;
    hash = 23 * hash + this.friday;
    hash = 23 * hash + this.saturday;
    hash = 23 * hash + this.sunday;
    return hash;
  }
  
  



}
