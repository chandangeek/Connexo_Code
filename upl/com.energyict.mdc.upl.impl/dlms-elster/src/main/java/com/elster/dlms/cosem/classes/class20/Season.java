/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class20/Season.java $
 * Version:     
 * $Id: Season.java 3649 2011-09-30 14:11:10Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  May 5, 2011 9:06:57 AM
 */
package com.elster.dlms.cosem.classes.class20;

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
 * Season for the activity calendar<P>
 * See BB ed.10 p.80f
 *
 * @author osse
 */
public class Season implements IDlmsDataProvider
{
  private final String seasonProfileName;
  private final DlmsDateTime seasonStart;
  private final String weekName;
  public static final AbstractValidator VALIDATOR = new ValidatorStructure(
          new ValidatorOctetString(-1, -1),
          new ValidatorOctetString(12, 12),
          new ValidatorOctetString(-1, -1));
  
  public final static AbstractValidator LIST_VALIDATOR = new ValidatorArray(VALIDATOR);

  public static Season[] fromDlmsDataArray(DlmsData data) throws ValidationExecption
  {
    LIST_VALIDATOR.validate(data);
    DlmsDataArray array = (DlmsDataArray)data;
    Season[] result = new Season[array.size()];
    for (int i = 0; i < array.size(); i++)
    {
      result[i] = new Season(array.get(i));
    }
    return result;
  }
  

  public Season(final String seasonProfileName, final DlmsDateTime seasonStart, final String weekName)
  {
    this.seasonProfileName = seasonProfileName;
    this.seasonStart = seasonStart;
    this.weekName = weekName;
  }

  public Season(final DlmsData data) throws ValidationExecption
  {
    VALIDATOR.validate(data);
    final DlmsDataStructure structure = (DlmsDataStructure)data;
    this.seasonProfileName = new String(((DlmsDataOctetString)(structure.get(0))).getValue());
    this.seasonStart = new DlmsDateTime(((DlmsDataOctetString)(structure.get(1))).getValue());
    this.weekName = new String(((DlmsDataOctetString)(structure.get(2))).getValue());
  }

  public DlmsData toDlmsData()
  {
    return new DlmsDataStructure(
            new DlmsDataOctetString(seasonProfileName.getBytes()),
            new DlmsDataOctetString(seasonStart.toBytes()),
            new DlmsDataOctetString(weekName.getBytes()));
  }

  public String getSeasonProfileName()
  {
    return seasonProfileName;
  }

  public DlmsDateTime getSeasonStart()
  {
    return seasonStart;
  }

  public String getWeekName()
  {
    return weekName;
  }

  @Override
  public String toString()
  {
    return "Season{" + "seasonProfileName=" + seasonProfileName + ", seasonStart=" + seasonStart + ", weekName=" +
           weekName + '}';
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
    final Season other = (Season)obj;
    if ((this.seasonProfileName == null) ? (other.seasonProfileName != null)
            : !this.seasonProfileName.equals(other.seasonProfileName))
    {
      return false;
    }
    if (this.seasonStart != other.seasonStart &&
        (this.seasonStart == null || !this.seasonStart.equals(other.seasonStart)))
    {
      return false;
    }
    if ((this.weekName == null) ? (other.weekName != null) : !this.weekName.equals(other.weekName))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    int hash = 5;
    hash = 89 * hash + (this.seasonProfileName != null ? this.seasonProfileName.hashCode() : 0);
    hash = 89 * hash + (this.seasonStart != null ? this.seasonStart.hashCode() : 0);
    hash = 89 * hash + (this.weekName != null ? this.weekName.hashCode() : 0);
    return hash;
  }
  
  



}
