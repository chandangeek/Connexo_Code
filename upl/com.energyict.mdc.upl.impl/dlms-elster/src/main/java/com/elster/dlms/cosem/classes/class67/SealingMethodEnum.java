/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class67/SealingMethodEnum.java $
 * Version:     
 * $Id: SealingMethodEnum.java 6704 2013-06-07 13:49:37Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 3:32:47 PM
 */
package com.elster.dlms.cosem.classes.class67;

import com.elster.dlms.cosem.classes.common.CosemEnumFactory;
import com.elster.dlms.cosem.classes.common.CosemEnum;

/**
 * Enumeration for the output type of the sensor manager class<P>
 * COSEM class id 67, attribute 4.<P>
 * See BB ed.10 p.91
 *
 * @author osse
 */
public final class SealingMethodEnum extends CosemEnum
{
  public static final SealingMethodEnum OT_NOT_SPECIFIED =
          new SealingMethodEnum(0, "not specified");
  public static final SealingMethodEnum OT_4_20_MA =
          new SealingMethodEnum(1, "4–20 mA,");
  public static final SealingMethodEnum OT_0_20_MA =
          new SealingMethodEnum(2, "0–20 mA");
  public static final SealingMethodEnum OT_0_5_V =
          new SealingMethodEnum(3, "0–5 V");
  public static final SealingMethodEnum OT_0_10_V =
          new SealingMethodEnum(4, "0–10 V");
  public static final SealingMethodEnum OT_PT100 =
          new SealingMethodEnum(5, "Pt100");
  public static final SealingMethodEnum OT_PT500 =
          new SealingMethodEnum(6, "Pt500");
  public static final SealingMethodEnum OT_PT1000 =
          new SealingMethodEnum(7, "Pt1000");
  public static final SealingMethodEnum OT_HART =
          new SealingMethodEnum(8, "HART");
  public static final SealingMethodEnum OT_MANUFACTURER_SPECIFIC =
          new SealingMethodEnum(128, "manufacturer specific");
  private static final SealingMethodEnum[] VALUES =
  {
    OT_NOT_SPECIFIED,
    OT_4_20_MA,
    OT_0_20_MA,
    OT_0_5_V,
    OT_0_10_V,
    OT_PT100,
    OT_PT500,
    OT_PT1000,
    OT_HART,
    OT_MANUFACTURER_SPECIFIC
  };
  private static final CosemEnumFactory<SealingMethodEnum> FACTORY = new CosemEnumFactory<SealingMethodEnum>(
          VALUES)
  {
    @Override
    public SealingMethodEnum createDefault(int id, String text)
    {
      throw new UnsupportedOperationException("Output type id " + id);
    }

  };

  private SealingMethodEnum(final int id, final String name)
  {
    super(id, name);
  }

  public static CosemEnumFactory<SealingMethodEnum> getFactory()
  {
    return FACTORY;
  }

  public static SealingMethodEnum[] getValues()
  {
    return VALUES.clone();
  }

}
