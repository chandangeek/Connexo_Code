/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class67/OutputTypeEnum.java $
 * Version:     
 * $Id: OutputTypeEnum.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 3:32:47 PM
 */
package com.elster.dlms.cosem.classes.class67;

import com.elster.dlms.cosem.classes.common.CosemEnumFactory;
import com.elster.dlms.cosem.classes.common.CosemEnum;

/**
 * Enumeration for the sealing method of the sensor manager class<P>
 * COSEM class id 67, attribute 6.<P>
 * See BB ed.10 p.91
 *
 * @author osse
 */
public final class OutputTypeEnum extends CosemEnum
{
  public static final OutputTypeEnum NONE =
          new OutputTypeEnum(0, "none");
  public static final OutputTypeEnum MECHANICAL =
          new OutputTypeEnum(1, "mechanical");
  public static final OutputTypeEnum ELECTRONIC =
          new OutputTypeEnum(2, "electronic");
  public static final OutputTypeEnum SOFTWARE =
          new OutputTypeEnum(3, "software");
  
  private static final OutputTypeEnum[] VALUES =
  {
    NONE,
    MECHANICAL,
    ELECTRONIC,
    SOFTWARE
  };
  private static final CosemEnumFactory<OutputTypeEnum> FACTORY = new CosemEnumFactory<OutputTypeEnum>(
          VALUES)
  {
    @Override
    public OutputTypeEnum createDefault(int id, String text)
    {
      throw new UnsupportedOperationException("Output type id " + id);
    }

  };

  private OutputTypeEnum(final int id, final String name)
  {
    super(id, name);
  }

  public static CosemEnumFactory<OutputTypeEnum> getFactory()
  {
    return FACTORY;
  }

  public static OutputTypeEnum[] getValues()
  {
    return VALUES.clone();
  }

}
