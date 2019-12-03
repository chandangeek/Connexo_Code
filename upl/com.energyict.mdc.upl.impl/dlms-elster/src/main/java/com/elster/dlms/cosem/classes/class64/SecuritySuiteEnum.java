/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class64/SecuritySuiteEnum.java $
 * Version:     
 * $Id: SecuritySuiteEnum.java 3601 2011-09-29 11:44:03Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 3:32:47 PM
 */
package com.elster.dlms.cosem.classes.class64;

import com.elster.dlms.cosem.classes.common.CosemEnumFactory;
import com.elster.dlms.cosem.classes.common.CosemEnum;

/**
 * Enumeration for the security policy of the Security setup<P>
 * COSEM class id 64, attribute 2.<P>
 * See BB ed.10 p.71
 *
 * @author osse
 */
public final class SecuritySuiteEnum extends CosemEnum
{
  public static final SecuritySuiteEnum SECURITY_SUITE_AES_GCM_128 =
          new SecuritySuiteEnum(0, "AES-GCM-128 for authenticated encryption and AES-128 for key wrapping");

  private static final SecuritySuiteEnum[] VALUES =
  {
    SECURITY_SUITE_AES_GCM_128
  };

  private static final CosemEnumFactory<SecuritySuiteEnum> FACTORY= new CosemEnumFactory<SecuritySuiteEnum>(VALUES) {
    @Override
    public SecuritySuiteEnum createDefault(final int id,final String text)
    {
      return new SecuritySuiteEnum(id,text);
    }
  };

  private SecuritySuiteEnum(final int id,final String name)
  {
    super(id, name);
  }

  public static CosemEnumFactory<SecuritySuiteEnum> getFactory()
  {
    return FACTORY;
  }

  public static SecuritySuiteEnum[] getValues()
  {
    return VALUES.clone();
  }
}
