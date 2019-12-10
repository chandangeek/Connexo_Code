/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class09/ServiceIdEnum.java $
 * Version:     
 * $Id: ServiceIdEnum.java 3601 2011-09-29 11:44:03Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 3:32:47 PM
 */
package com.elster.dlms.cosem.classes.class09;

import com.elster.dlms.cosem.classes.common.CosemEnumFactory;
import com.elster.dlms.cosem.classes.common.CosemEnum;

/**
 * Enumeration for the service ids for script actions<P>
 * COSEM class id 9<P>
 * See BB ed.10 p.75
 *
 * @author osse
 */
public final class ServiceIdEnum extends CosemEnum
{
  public static final ServiceIdEnum SERVICE_ID_WRITE_ATTRIBUTE =
          new ServiceIdEnum(1, "write attribute");
  public static final ServiceIdEnum SERVICE_ID_EXECUTE_SPECIFIC_METHOD =
          new ServiceIdEnum(2, "execute specific method");
  private static final ServiceIdEnum[] VALUES =
  {
    SERVICE_ID_WRITE_ATTRIBUTE,
    SERVICE_ID_EXECUTE_SPECIFIC_METHOD
  };
  private static final CosemEnumFactory<ServiceIdEnum> FACTORY = new CosemEnumFactory<ServiceIdEnum>(VALUES)
  {
    @Override
    public ServiceIdEnum createDefault(final int id, final String text)
    {
      return new ServiceIdEnum(id, text);
    }

  };

  private ServiceIdEnum(final int id, final String name)
  {
    super(id, name);
  }

  public static CosemEnumFactory<ServiceIdEnum> getFactory()
  {
    return FACTORY;
  }

  public static ServiceIdEnum[] getValues()
  {
    return VALUES.clone();
  }

}
