/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class28/AutoAnswerStatusEnum.java $
 * Version:     
 * $Id: AutoAnswerStatusEnum.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 3:32:47 PM
 */
package com.elster.dlms.cosem.classes.class28;



import com.elster.dlms.cosem.classes.common.CosemEnumFactory;
import com.elster.dlms.cosem.classes.common.CosemEnum;

/**
 * Enumeration for the mode of the Auto answer class<P>
 * COSEM class id 28, attribute 2.<P>
 * See BB ed.10 p.99
 *
 * @author osse
 */
public final class AutoAnswerStatusEnum extends CosemEnum
{
  public static final AutoAnswerStatusEnum INACTIVE =
          new AutoAnswerStatusEnum(0, "Inactive");
  public static final AutoAnswerStatusEnum ACTIVE =
          new AutoAnswerStatusEnum(1, "Active");
  public static final AutoAnswerStatusEnum LOCKED =
          new AutoAnswerStatusEnum(2, "Locked");

  private static final AutoAnswerStatusEnum[] VALUES =
  {
    INACTIVE,
    ACTIVE,
    LOCKED,
  };
  private static final CosemEnumFactory<AutoAnswerStatusEnum> FACTORY = new CosemEnumFactory<AutoAnswerStatusEnum>(
          VALUES) {

    @Override
    public AutoAnswerStatusEnum createDefault(int id, String text)
    {
      throw new UnsupportedOperationException("Status "+id);
    }
  };
  

  private AutoAnswerStatusEnum(final int id,  final String name)
  {
    super(id, name);
  }

  public static CosemEnumFactory<AutoAnswerStatusEnum> getFactory()
  {
    return FACTORY;
  }

  public static AutoAnswerStatusEnum[] getValues()
  {
    return VALUES.clone();
  }


}
