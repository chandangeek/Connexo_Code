/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class28/AutoAnswerModeEnum.java $
 * Version:     
 * $Id: AutoAnswerModeEnum.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 3:32:47 PM
 */
package com.elster.dlms.cosem.classes.class28;

import com.elster.dlms.cosem.classes.common.CosemEnumFactory;
import com.elster.dlms.cosem.classes.common.ICosemEnum;

/**
 * Enumeration for the mode of the Auto answer class<P>
 * COSEM class id 28, attribute 2.<P>
 * See BB ed.10 p.99
 *
 * @author osse
 */
public enum AutoAnswerModeEnum implements ICosemEnum
{
  LINE_DEDICATED_TO_THE_DEVICE(0, "line dedicated to the device"),
  SHARED_LINE_MANAGEMENT_LIMITED_CALLS(1, "shared line management with a limited number of calls allowed"),
  SHARED_LINE_MANAGEMENT_LIMITED_SUCCESSFUL_CALLS(2,
                                                  "shared line management with a limited number of successful calls allowed"),
  NO_MODEM_CONNECTED(3, "currently no modem connected");
  private final int id;
  private final String name;

  private AutoAnswerModeEnum(final int id, final String name)
  {
    this.id = id;
    this.name = name;
  }

  public static CosemEnumFactory<AutoAnswerModeEnum> getFactory()
  {
    return new CosemEnumFactory<AutoAnswerModeEnum>(values());
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
