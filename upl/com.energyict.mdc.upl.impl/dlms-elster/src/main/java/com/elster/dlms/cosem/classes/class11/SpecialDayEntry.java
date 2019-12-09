/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class11/SpecialDayEntry.java $
 * Version:     
 * $Id: SpecialDayEntry.java 3643 2011-09-30 12:15:42Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Apr 27, 2011 3:51:52 PM
 */
package com.elster.dlms.cosem.classes.class11;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorOctetString;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorSimpleType;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.basic.DlmsDate;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsData.DataType;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.DlmsDataUnsigned;
import com.elster.dlms.types.data.IDlmsDataProvider;

/**
 * Special day entry for COSEM class id 11 attribute 2 (Special days table - entries)<P>
 * See BB ed.10 p.79
 *
 * @author osse
 */
public class SpecialDayEntry implements IDlmsDataProvider
{
  private final int index;
  private final DlmsDate date;
  private final int dayId;
  
  public final static SpecialDayEntry[] EMPTY_SPECIAL_DAY_ENTRIES= new SpecialDayEntry[0];
  
  public final static AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorSimpleType(DataType.LONG_UNSIGNED),
          new ValidatorOctetString(5,5),
          new ValidatorSimpleType(DataType.UNSIGNED));
  
  public final static AbstractValidator LIST_VALIDATOR = new ValidatorArray(VALIDATOR);

  public static SpecialDayEntry[] fromDlmsDataArray(DlmsData data) throws ValidationExecption
  {
    LIST_VALIDATOR.validate(data);
    DlmsDataArray array = (DlmsDataArray)data;
    SpecialDayEntry[] result = new SpecialDayEntry[array.size()];
    for (int i = 0; i < array.size(); i++)
    {
      result[i] = new SpecialDayEntry(array.get(i));
    }
    return result;
  }
  

  public SpecialDayEntry(final int index, final DlmsDate date, final int dayId)
  {
    this.index = index;
    this.date = date;
    this.dayId = dayId;
  }

  public SpecialDayEntry(DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);
    
    final DlmsDataStructure structure = (DlmsDataStructure)data;
    
    final DlmsDataLongUnsigned indexData = (DlmsDataLongUnsigned)structure.get(0);
    final DlmsDataOctetString specialDayDateData = (DlmsDataOctetString)structure.get(1);
    final DlmsDataUnsigned dayIdData = (DlmsDataUnsigned)structure.get(2);

    this.index = indexData.getValue();
    this.date = new DlmsDate(specialDayDateData.getValue());
    this.dayId = dayIdData.getValue();
  }

  public DlmsDate getDate()
  {
    return date;
  }

  public int getDayId()
  {
    return dayId;
  }

  public int getIndex()
  {
    return index;
  }

  @Override
  public String toString()
  {
    return "SpecialDayEntry{" + "index=" + index + ", date=" + date + ", dayId=" + dayId + '}';
  }

  public DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataLongUnsigned(getIndex()),
            new DlmsDataOctetString(getDate().toBytes()),
            new DlmsDataUnsigned(getDayId()));
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
    final SpecialDayEntry other = (SpecialDayEntry)obj;
    if (this.index != other.index)
    {
      return false;
    }
    if (this.date != other.date && (this.date == null || !this.date.equals(other.date)))
    {
      return false;
    }
    if (this.dayId != other.dayId)
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 37 * hash + this.index;
    hash = 37 * hash + (this.date != null ? this.date.hashCode() : 0);
    hash = 37 * hash + this.dayId;
    return hash;
  }

}
