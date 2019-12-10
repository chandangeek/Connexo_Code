/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class19/DefaultModeEnum.java $
 * Version:     
 * $Id: DefaultModeEnum.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 3:32:47 PM
 */
package com.elster.dlms.cosem.classes.class19;

import com.elster.dlms.cosem.classes.common.CosemEnumFactory;
import com.elster.dlms.cosem.classes.common.ICosemEnum;

/**
 * Enumeration for the default mode of the IEC local port setup<P>
 * COSEM class id 19, attribute 2.<P>
 * See BB ed.10 p.94
 *
 * @author osse
 */
public enum DefaultModeEnum implements ICosemEnum
{
  DEFAULT_MODE_IEC_62056_21(0, "IEC 62056-21"),
  DEFAULT_MODE_HDLC(1, "HDLC"),
  DEFAULT_MODE_NOT_SPECIFIED(2, "not specified");
  
  
  private final int id;
  private final String name;
  
  private DefaultModeEnum(final int id, final String name)
  {
    this.id=id;
    this.name=name;
  }
  
  public static CosemEnumFactory<DefaultModeEnum> getFactory()
  {
     return new CosemEnumFactory<DefaultModeEnum>(values()) {
      @Override
      public ICosemEnum createDefault(final int id, final String text)
      {
        return new ICosemEnum() {

          public int getId()
          {
            return id;
          }

          public String getName()
          {
            return "Unknown id "+id;
          }
        };
      }
    };
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
