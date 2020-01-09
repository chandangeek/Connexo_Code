/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleActivityCalendarProfiles.java $
 * Version:     
 * $Id: SimpleActivityCalendarProfiles.java 3643 2011-09-30 12:15:42Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  12.07.2011 09:49:14
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.cosem.classes.class20.DayProfile;
import com.elster.dlms.cosem.classes.class20.Season;
import com.elster.dlms.cosem.classes.class20.WeekProfile;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataOctetString;
import java.io.IOException;
import java.util.Collection;

/**
 * SimpleCosemObject for a set of profiles (active or passive) of the activity calendar
 *
 * @author osse
 */
public class SimpleActivityCalendarProfiles extends SimpleCosemObject
{
  private final boolean active;
  private static final DayProfile[] EMPTY_DAY_PROFILES= new DayProfile[0];
  private static final WeekProfile[] EMPTY_WEEK_PROFILES= new WeekProfile[0];
  private static final Season[] EMPTY_SEASONS= new Season[0];

  public SimpleActivityCalendarProfiles(final SimpleCosemObjectDefinition definition,
                                        final SimpleCosemObjectManager manager,final boolean active)
  {
    super(definition, manager);
    this.active = active;
  }

  public boolean isActive()
  {
    return active;
  }

  public DayProfile[] getDayProfiles() throws IOException
  {
    try
    {
      final int offset = active ? 0 : 4;
      final DlmsDataArray dayProfileData = executeGet(5 + offset, DlmsDataArray.class, false);
      return DayProfile.fromDlmsDataArray(dayProfileData);
    }
    catch (ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException(ex);
    }
  }

  public void setDayProfiles(final DayProfile[] dayProfiles) throws IOException
  {
    final int offset = active ? 0 : 4;
    executeSet(5+offset, new DlmsDataArray(dayProfiles));
  }
  
  public void setDayProfiles(Collection<DayProfile> dayProfiles) throws IOException
  {
    setDayProfiles(dayProfiles.toArray(EMPTY_DAY_PROFILES));
  }
  
  
  public Season[] getSeasons() throws IOException
  {
    try
    {
      final int offset = active ? 0 : 4;
      final DlmsDataArray seasonProfileData = executeGet(3 + offset, DlmsDataArray.class, false);
      return Season.fromDlmsDataArray(seasonProfileData);
    }
    catch (ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException(ex);
    }
  }
  
   public void setSeasons(final Season[] seasons) throws IOException
  {
    final int offset = active ? 0 : 4;
    executeSet(3+offset, new DlmsDataArray(seasons));
  }
   
     
  public void setSeasons(final Collection<Season> seasons) throws IOException
  {
    setSeasons(seasons.toArray(EMPTY_SEASONS));
  }

  public WeekProfile[] getWeekProfiles() throws IOException
  {
    try
    {
      final int offset = active ? 0 : 4;
      final DlmsDataArray weekProfileData = executeGet(4 + offset, DlmsDataArray.class, false);
      return WeekProfile.fromDlmsDataArray(weekProfileData);
    }
    catch (ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException(ex);
    }
  }
  
  public void setWeekProfiles(final WeekProfile[] weekProfiles) throws IOException
  {
    final int offset = active ? 0 : 4;
    executeSet(4+offset, new DlmsDataArray(weekProfiles));
  }

  public void setWeekProfiles(final Collection<WeekProfile> weekProfiles) throws IOException
  {
    setWeekProfiles(weekProfiles.toArray(EMPTY_WEEK_PROFILES));
  }

  public String getCalendarName() throws IOException
  {
    final int offset = active ? 0 : 4;
    final DlmsDataOctetString calendarNameData = executeGet(2 + offset, DlmsDataOctetString.class, false);
    return new String(calendarNameData.getValue());
  }

  public void setCalendarName(final String calendarName) throws IOException
  {
    final int offset = active ? 0 : 4;
    executeSet(2 + offset, new DlmsDataOctetString(calendarName.getBytes()));
  }

}
