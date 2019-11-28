/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/common/TimeWindow.java $
 * Version:     
 * $Id: TimeWindow.java 3669 2011-10-07 15:26:31Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  03.08.2011 13:31:52
 */
package com.elster.dlms.cosem.classes.common;

import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.AbstractValidator;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorArray;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorOctetString;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidatorStructure;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;
import com.elster.dlms.types.data.IDlmsDataProvider;

/**
 * Time Window, used by "Auto answer" and "Auto connect"
 *
 * @author osse
 */
public class TimeWindow implements IDlmsDataProvider
{
  
  private final DlmsDateTime startTime;
  private final DlmsDateTime endTime;
  public final static AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorOctetString(12, 12),
          new ValidatorOctetString(12, 12));
  public final static AbstractValidator LIST_VALIDATOR = new ValidatorArray(VALIDATOR);
  public final static TimeWindow[] EMPTY_TIME_WINDOWS = new TimeWindow[0];
  

  public static TimeWindow[] fromDlmsDataArray(final DlmsData data) throws ValidationExecption
  {
    LIST_VALIDATOR.validate(data);
    final DlmsDataArray array = (DlmsDataArray)data;
    TimeWindow[] result = new TimeWindow[array.size()];
    for (int i = 0; i < array.size(); i++)
    {
      result[i] = new TimeWindow(array.get(i));
    }
    return result;
  }

  public TimeWindow(final DlmsDateTime startTime, final DlmsDateTime endTime)
  {
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public TimeWindow(DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);

    final DlmsDataStructure structure = (DlmsDataStructure)data;
    final DlmsDataOctetString startData = (DlmsDataOctetString)structure.get(0);
    final DlmsDataOctetString endData = (DlmsDataOctetString)structure.get(1);
    this.startTime = new DlmsDateTime(startData.getValue());
    this.endTime = new DlmsDateTime(endData.getValue());
  }

  public DlmsDateTime getEndTime()
  {
    return endTime;
  }

  public DlmsDateTime getStartTime()
  {
    return startTime;
  }

  // @Override
  public DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataOctetString(startTime.toBytes()),
            new DlmsDataOctetString(endTime.toBytes()));
  }

  @Override
  public String toString()
  {
    return "TimeWindow{" + "startTime=" + startTime + ", endTime=" + endTime + '}';
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
    final TimeWindow other = (TimeWindow)obj;
    if (this.startTime != other.startTime && (this.startTime == null
                                              || !this.startTime.equals(other.startTime)))
    {
      return false;
    }
    if (this.endTime != other.endTime && (this.endTime == null || !this.endTime.equals(other.endTime)))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 7;
    hash = 29 * hash + (this.startTime != null ? this.startTime.hashCode() : 0);
    hash = 29 * hash + (this.endTime != null ? this.endTime.hashCode() : 0);
    return hash;
  }

}
