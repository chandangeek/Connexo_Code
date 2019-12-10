/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class64/SecurityPolicyEnum.java $
 * Version:     
 * $Id: SecurityPolicyEnum.java 3601 2011-09-29 11:44:03Z osse $
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
public final class SecurityPolicyEnum extends CosemEnum
{
  public static final SecurityPolicyEnum SECURITY_POLICY_NOTHING =
          new SecurityPolicyEnum(0, "nothing");
  public static final SecurityPolicyEnum SECURITY_POLICY_ALL_MESSAGES_TO_BE_AUTHENTICATED =
          new SecurityPolicyEnum(1, "all messages to be authenticated");
  public static final SecurityPolicyEnum SECURITY_POLICY_ALL_MESSAGES_TO_BE_ENCRYPTED =
          new SecurityPolicyEnum(2, "all messages to be encrypted");
  public static final SecurityPolicyEnum SECURITY_POLICY_LL_MESSAGES_TO_BE_AUTHENTICATED_AND_ENCRYPTED =
          new SecurityPolicyEnum(3, "all messages to be authenticated and encrypted");
  
  

  private static final SecurityPolicyEnum[] VALUES =
  {
    SECURITY_POLICY_NOTHING,
    SECURITY_POLICY_ALL_MESSAGES_TO_BE_AUTHENTICATED,
    SECURITY_POLICY_ALL_MESSAGES_TO_BE_ENCRYPTED,
    SECURITY_POLICY_LL_MESSAGES_TO_BE_AUTHENTICATED_AND_ENCRYPTED
  };

  private static final CosemEnumFactory<SecurityPolicyEnum> FACTORY= new CosemEnumFactory<SecurityPolicyEnum>(VALUES) {
    @Override
    public SecurityPolicyEnum createDefault(final int id,final String text)
    {
      return new SecurityPolicyEnum(id,text);
    }
  };

  private SecurityPolicyEnum(final int id,final String name)
  {
    super(id, name);
  }

  public static CosemEnumFactory<SecurityPolicyEnum> getFactory()
  {
    return FACTORY;
  }

  public static SecurityPolicyEnum[] getValues()
  {
    return VALUES.clone();
  }
}
