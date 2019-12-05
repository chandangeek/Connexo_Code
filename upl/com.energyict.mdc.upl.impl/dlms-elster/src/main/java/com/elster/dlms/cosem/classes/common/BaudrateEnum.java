/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/common/BaudrateEnum.java $
 * Version:     
 * $Id: BaudrateEnum.java 3657 2011-09-30 17:25:40Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 3:32:47 PM
 */
package com.elster.dlms.cosem.classes.common;

/**
 * Enumeration for the default baud rate of the IEC local port setup<P>
 * COSEM class id 19, attribute 3.<P>
 * See BB ed.10 p.94
 *
 * @author osse
 */
public enum BaudrateEnum implements ICosemEnum
{
  BAUDRATE_300(0, 300),
  BAUDRATE_600(1, 600),
  BAUDRATE_1200(2, 1200),
  BAUDRATE_2400(3, 2400),
  BAUDRATE_4800(4, 4800),
  BAUDRATE_9600(5, 9600),
  BAUDRATE_19200(6, 19200),
  BAUDRATE_38400(7, 38400),
  BAUDRATE_57600(8, 57600),
  BAUDRATE_115200(9, 115200);

  private final int id;
  private final int baudrate;

  private BaudrateEnum(final int id, final int baudrate)
  {
    this.id = id;
    this.baudrate = baudrate;
  }

  public static CosemEnumFactory<BaudrateEnum> getFactory()
  {
    return new CosemEnumFactory<BaudrateEnum>(values());
  }

  /**
   * Returns the baud rate.<P>
   *
   * @return The baud rate
   */
  public int getBaudrate()
  {
    return baudrate;
  }

  public int getId()
  {
    return id;
  }

  public String getName()
  {
    return baudrate + " baud";
  }

}
