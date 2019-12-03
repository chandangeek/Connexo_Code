/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class07/SortMethodEnum.java $
 * Version:     
 * $Id: SortMethodEnum.java 3598 2011-09-29 09:10:44Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Dec 10, 2010 3:32:47 PM
 */
package com.elster.dlms.cosem.classes.class07;

import com.elster.dlms.cosem.classes.common.CosemEnumFactory;
import com.elster.dlms.cosem.classes.common.ICosemEnum;

/**
 * Enumeration for the sort method for profiles<P>
 * COSEM class id 18, attribute 5.<P>
 * See BB ed.10 p.48
 *
 * @author osse
 */
public enum SortMethodEnum implements ICosemEnum
{
 SORT_METHOD_FIFO(1, "fifo (first in first out)"),
  SORT_METHOD_LIFO(2, "lifo (last in first out)"),
  SORT_METHOD_LARGEST(3, "largest"),
  SORT_METHOD_SMALLEST(4, "smallest"),
  SORT_METHOD_NEAREST_TO_ZERO(5, "nearest_to_zero"),
  SORT_METHOD_FAREST_FROM_ZERO(6, "farest_from_zero"); 
  
  private final int id;
  private final String name;

  private SortMethodEnum(int id, String name)
  {
    this.id = id;
    this.name = name;
  }

  public static CosemEnumFactory<SortMethodEnum> getFactory()
  {
    return new CosemEnumFactory<SortMethodEnum>(values());
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
