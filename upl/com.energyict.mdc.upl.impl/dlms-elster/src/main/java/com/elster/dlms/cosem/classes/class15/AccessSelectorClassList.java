/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class15/AccessSelectorClassList.java $
 * Version:     
 * $Id: AccessSelectorClassList.java 4469 2012-05-08 13:19:45Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2011 08:51:25
 */
package com.elster.dlms.cosem.classes.class15;

import com.elster.dlms.cosem.objectmodel.CosemAccessSelector;
import com.elster.dlms.types.basic.AccessSelectionParameters;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;

/**
 * Access selector 2 for Class ID 15 Attribute 2.<P>
 * See BB ed.10 p. 61
 *
 * @author osse
 */
public class AccessSelectorClassList implements CosemAccessSelector
{
  final int[] classIds;

  public AccessSelectorClassList(final int first, final int... more)
  {
    classIds = new int[1 + more.length];
    classIds[0] = first;
    System.arraycopy(more, 0, classIds, 1, more.length);
  }

  public int[] getClassIds()
  {
    return classIds.clone();
  }

  public AccessSelectionParameters getAccessSelectionParameters()
  {
    DlmsDataLongUnsigned ids[] = new DlmsDataLongUnsigned[classIds.length];
    for (int i = 0; i < ids.length; i++)
    {
      ids[i] = new DlmsDataLongUnsigned(classIds[i]);
    }
    return new AccessSelectionParameters(2, new DlmsDataArray(ids));
  }

  //@Override //for Java 1.5
  public int getId()
  {
    return 2;
  }

  //@Override //for Java 1.5
  public DlmsData toDlmsData()
  {
    return getAccessSelectionParameters().getAccessParameters();
  }

}
