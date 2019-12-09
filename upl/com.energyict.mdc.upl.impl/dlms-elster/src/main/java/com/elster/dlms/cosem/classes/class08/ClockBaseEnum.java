/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class08/ClockBaseEnum.java $
 * Version:     
 * $Id: ClockBaseEnum.java 3601 2011-09-29 11:44:03Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 3:32:47 PM
 */
package com.elster.dlms.cosem.classes.class08;


import com.elster.dlms.cosem.classes.common.CosemEnumFactory;
import com.elster.dlms.cosem.classes.common.CosemEnum;

/**
 * Enumeration for the clock base of clock objects<P>
 * COSEM class id 8, attribute 9.<P>
 * See BB ed.10 p.73
 *
 * @author osse
 */
public final class ClockBaseEnum extends CosemEnum
{
  public static final ClockBaseEnum CLOCK_BASE_NOT_DEFINED =
          new ClockBaseEnum(0, "not defined"); 
  public static final ClockBaseEnum CLOCK_BASE_INTERNAL_CRYSTAL =
          new ClockBaseEnum(1, "internal crystal");
  public static final ClockBaseEnum CLOCK_BASE_MAINS_FREQUENCY_50HZ =
          new ClockBaseEnum(2, "mains frequency 50 Hz");
  public static final ClockBaseEnum CLOCK_BASE_MAINS_FREQUENCY_60HZ =
          new ClockBaseEnum(3, "mains frequency 60 Hz");
  public static final ClockBaseEnum CLOCK_BASE_GPS =
          new ClockBaseEnum(4, "GPS (global positioning system)");
  public static final ClockBaseEnum CLOCK_BASE_RADIO_CONTROLLED =
          new ClockBaseEnum(5, "radio controlled");

  private static final ClockBaseEnum[] VALUES =
  {
    CLOCK_BASE_NOT_DEFINED,
    CLOCK_BASE_INTERNAL_CRYSTAL,
    CLOCK_BASE_MAINS_FREQUENCY_50HZ,
    CLOCK_BASE_MAINS_FREQUENCY_60HZ,
    CLOCK_BASE_GPS,
    CLOCK_BASE_RADIO_CONTROLLED
  };

  private static final CosemEnumFactory<ClockBaseEnum> FACTORY= new CosemEnumFactory<ClockBaseEnum>(VALUES) {
    @Override
    public ClockBaseEnum createDefault(final int id,final String text)
    {
      return new ClockBaseEnum(id,text);
    }
  };

  private ClockBaseEnum(final int id,final String name)
  {
    super(id, name);
  }

  public static CosemEnumFactory<ClockBaseEnum> getFactory()
  {
    return FACTORY;
  }

  public static ClockBaseEnum[] getValues()
  {
    return VALUES.clone();
  }
}
