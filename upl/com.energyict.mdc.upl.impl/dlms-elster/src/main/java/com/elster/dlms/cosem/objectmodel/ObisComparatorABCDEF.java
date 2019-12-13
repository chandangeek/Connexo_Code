/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/ObisComparatorABCDEF.java $
 * Version:     
 * $Id: ObisComparatorABCDEF.java 4017 2012-02-15 15:31:00Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  30.09.2011 19:30:53
 */
package com.elster.dlms.cosem.objectmodel;

import java.util.Comparator;

/**
 * Compares {@code CosemObject}s by their OBIS codes.<P>
 * The OBIS codes will be compared by their value groups starting by value group A.
 *
 * @author osse
 */
public class ObisComparatorABCDEF implements Comparator<CosemObject>//, Serializable
{
  public int compare(final CosemObject o1, final CosemObject o2)
  {
    return o1.getLogicalName().compareTo(o2.getLogicalName());
  }

}
