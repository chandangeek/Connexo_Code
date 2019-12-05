/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class19/ResponseTimeEnum.java $
 * Version:     
 * $Id: ResponseTimeEnum.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 3:32:47 PM
 */
package com.elster.dlms.cosem.classes.class19;

import com.elster.dlms.cosem.classes.common.CosemEnumFactory;

import com.elster.dlms.cosem.classes.common.ICosemEnum;

/**
 * Enumeration for the default baud rate of the IEC local port setup<P>
 * COSEM class id 19, attribute 4.<P>
 * See BB ed.10 p.95
 *
 * @author osse
 */
public enum ResponseTimeEnum implements ICosemEnum
{
  RESPONSE_TIME_20_MS(0, 20),
  RESPONSE_TIME_200_MS(1, 200);
  private final int responseTime;
  private final int id;
  private final String name;

  private ResponseTimeEnum(final int id, final int reponsetime)
  {
    this.id = id;
    this.responseTime = reponsetime;
    this.name = Integer.toBinaryString(reponsetime)+" ms";
  }

  public static CosemEnumFactory<ResponseTimeEnum> getFactory()
  {
    return new CosemEnumFactory<ResponseTimeEnum>(values());
  }

  /**
   * Returns the response time.<P>
   * @return The response time in ms.
   */
  public int getResponseTime()
  {
    return responseTime;
  }

  public int getId()
  {
    return id;
  }

  public String getName()
  {
    return name;
  }
  
  
  
  
  

}
