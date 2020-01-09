/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleClockObject.java $
 * Version:     
 * $Id: SimpleClockObject.java 4010 2012-02-13 15:52:14Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Mar 9, 2011 3:55:47 PM
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.data.DlmsDataBoolean;
import com.elster.dlms.types.data.DlmsDataLong;
import com.elster.dlms.types.data.DlmsDataOctetString;
import java.io.IOException;
import java.util.Date;

/**
 * Class for Objects of COSEM class "Clock".
 *
 * @author osse
 */
public class SimpleClockObject extends SimpleCosemObject
{

  /*package private*/
  SimpleClockObject(final SimpleCosemObjectDefinition definition, final SimpleCosemObjectManager manager)
  {
    super(definition, manager);
  }

  /**
   * Attribute 2 of  COSEM class "Clock".
   */
  public DlmsDateTime getTime() throws IOException
  {
    final DlmsDataOctetString octetString = executeGet(2, DlmsDataOctetString.class, true);

    return new DlmsDateTime(octetString.getValue());
  }

  /**
   * Attribute 2 of  COSEM class "Clock".
   */
  public void setTime(final DlmsDateTime dateTime) throws IOException
  {
    executeSet(2, new DlmsDataOctetString(dateTime.toBytes()));
  }
  

  
  /**
   * Attribute 8 of  COSEM class "Clock".
   */
  public void setDaylightSavingsEnabled(final boolean dstEnabled) throws IOException
  {
    executeSet(8, new DlmsDataBoolean(dstEnabled));
  }
  
  /**
   * Attribute 8 of  COSEM class "Clock".
   */  
  public boolean isDaylightSavingsEnabled() throws IOException
  {
    return executeGet(8, DlmsDataBoolean.class, false).getValue();
  }
  
  

  /**
   * Calls the shift time method (id 6) of the clock object.
   * 
   * @param seconds The amount of seconds to shift.
   * @throws IOException 
   */
  public void shiftTime(final int seconds) throws IOException
  {
    if (seconds < DlmsDataLong.MIN_VALUE || seconds > DlmsDataLong.MAX_VALUE)
    {
      throw new IOException("Time difference to big");
    }
    executeMethod(6, new DlmsDataLong(seconds));
  }

  /**
   * Reads the time in the device and calculates the difference to the system time.
   * 
   * @return The difference to the system time. 
   * @throws IOException 
   */
  public int getDifferenceToSystemTimeSec() throws IOException
  {
    final long start = System.currentTimeMillis();
    final DlmsDateTime deviceTimeDlms = getTime();
    final long end = System.currentTimeMillis();
    final long systemTimeStamp = start + ((end - start) / 2); //same as "(start+end)/2" but without the risk of an overflow

    if (deviceTimeDlms.isRegularUtc())
    {
      final Date deviceTime = deviceTimeDlms.getUtcDate();
      return (int)((deviceTime.getTime() - systemTimeStamp) / 1000);
    }
    else
    {
      throw new IOException("No deviation in device time");
    }
  }

  /**
   * Shifts the time in the device to the system time.
   * 
   * @throws IOException 
   */
  public void shiftTimeToSystemTime() throws IOException
  {
    shiftTime( -getDifferenceToSystemTimeSec());
  }
  
  
  /**
   * Sets the time in the device to the system time.
   * 
   * @throws IOException 
   */
  public void setTimeToSystemTime() throws IOException
  {
    setTime(new DlmsDateTime(new Date()));
  }

}
