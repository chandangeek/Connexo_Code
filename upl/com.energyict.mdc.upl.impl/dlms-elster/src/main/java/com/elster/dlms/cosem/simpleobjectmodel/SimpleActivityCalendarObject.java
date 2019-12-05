/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleActivityCalendarObject.java $
 * Version:     
 * $Id: SimpleActivityCalendarObject.java 3823 2011-12-07 09:28:12Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  12.07.2011 09:49:14
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.data.DlmsDataInteger;
import com.elster.dlms.types.data.DlmsDataOctetString;
import java.io.IOException;

/**
 * SimpleCosemObject for the activity calendar (IC 20).
 *
 * @author osse
 */
public class SimpleActivityCalendarObject extends SimpleCosemObject
{
  //private DlmsDateTime activatePassiveCalendarTime= DlmsDateTime.NOT_SPECIFIED_DATE_TIME;
  private final SimpleActivityCalendarProfiles profilesPassive;
  private final SimpleActivityCalendarProfiles profilesActive;

  /*package private*/
  SimpleActivityCalendarObject(final SimpleCosemObjectDefinition definition,
                               final SimpleCosemObjectManager objectManager)
  {
    super(definition, objectManager);
    profilesActive = new SimpleActivityCalendarProfiles(definition, objectManager, true);
    profilesPassive = new SimpleActivityCalendarProfiles(definition, objectManager, false);
  }

  public DlmsDateTime getActivatePassiveCalendarTime() throws IOException
  {
    return new DlmsDateTime(executeGet(10, DlmsDataOctetString.class, false).getValue());
  }

  public void setActivatePassiveCalendarTime(final DlmsDateTime activatePassiveCalendarTime) throws
          IOException
  {
    executeSet(10, new DlmsDataOctetString(activatePassiveCalendarTime.toBytes()));
  }

  public SimpleActivityCalendarProfiles getProfilesActive()
  {
    return profilesActive;
  }

  public SimpleActivityCalendarProfiles getProfilesPassive()
  {
    return profilesPassive;
  }
  
  public void activatePassiveCalendar() throws IOException
  {
    executeMethod(1, new DlmsDataInteger(0));
  }
          

}
