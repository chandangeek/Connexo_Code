/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class29/AutoConnectModeEnum.java $
 * Version:     
 * $Id: AutoConnectModeEnum.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 3:32:47 PM
 */
package com.elster.dlms.cosem.classes.class29;


import com.elster.dlms.cosem.classes.common.CosemEnumFactory;
import com.elster.dlms.cosem.classes.common.CosemEnum;

/**
 * Enumeration for the mode of the Auto connect class<P>
 * COSEM class id 29, attribute 2.<P>
 * See BB ed.10 p.101
 *
 * @author osse
 */
public final class AutoConnectModeEnum extends CosemEnum
{
  public static final AutoConnectModeEnum NO_AUTO_DIALING =
          new AutoConnectModeEnum(0, "no auto dialling");
  public static final AutoConnectModeEnum AUTO_DIALING_ALLOWED_ANYTME =
          new AutoConnectModeEnum(1, "auto dialling allowed anytime");
  public static final AutoConnectModeEnum AUTO_DIALING_ALLOWED_ANYTME_IN_TW =
          new AutoConnectModeEnum(2, "auto dialling allowed in time window");
  public static final AutoConnectModeEnum AUTO_DIALING_ALLOWED_ANYTME_IN_TW_ALARM_ANYTIME =
          new AutoConnectModeEnum(3, "\"regular\" auto dialling allowed in time window; \"alarm\" allowed anytime");
  public static final AutoConnectModeEnum SMS_PLMN =
          new AutoConnectModeEnum(4,"SMS sending via Public Land Mobile Network (PLMN)");
  public static final AutoConnectModeEnum SMS_PSTN =
          new AutoConnectModeEnum(5, "SMS sending via PSTN");
  public static final AutoConnectModeEnum EMAIL =
          new AutoConnectModeEnum(6, "email sending");
 
  private static final AutoConnectModeEnum[] VALUES =
  {
    NO_AUTO_DIALING,
    AUTO_DIALING_ALLOWED_ANYTME,
    AUTO_DIALING_ALLOWED_ANYTME_IN_TW,
    AUTO_DIALING_ALLOWED_ANYTME_IN_TW_ALARM_ANYTIME,
    SMS_PLMN,
    SMS_PSTN,
    EMAIL
  };
  private static final CosemEnumFactory<AutoConnectModeEnum> FACTORY = new CosemEnumFactory<AutoConnectModeEnum>(
          VALUES) {

    @Override
    public AutoConnectModeEnum createDefault(int id, String text)
    {
      throw new UnsupportedOperationException("Mode "+id);
    }
  };
  

  private AutoConnectModeEnum(final int id,  final String name)
  {
    super(id, name);
  }

  public static CosemEnumFactory<AutoConnectModeEnum> getFactory()
  {
    return FACTORY;
  }

  public static AutoConnectModeEnum[] getValues()
  {
    return VALUES.clone();
  }


}
