/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class15/AccessSelectorElsObjectIdList.java $
 * Version:     
 * $Id: AccessSelectorElsObjectIdList.java 4476 2012-05-09 09:31:33Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2011 08:51:25
 */
package com.elster.dlms.cosem.classes.class15;

import com.elster.dlms.cosem.objectmodel.CosemAccessSelector;
import com.elster.dlms.types.basic.AccessSelectionParameters;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataOctetString;

/**
 * Elster version of access selector 3 for Class ID 15 Attribute 2.
 *
 * Same as described in BB ed.10 p. 61 but with an array of logical names (octet stings) instead of an array 
 * with object ids.
 * 
 * 
 *
 * @author osse
 */
public class AccessSelectorElsObjectIdList implements CosemAccessSelector
{
  private final ObisCode[] logicalNames;

  public AccessSelectorElsObjectIdList(final ObisCode first, final ObisCode... more)
  {
    logicalNames = new ObisCode[1 + more.length];
    logicalNames[0] = first;
    System.arraycopy(more, 0, logicalNames, 1, more.length);
  }

  public AccessSelectorElsObjectIdList(final ObisCode[] logicalNames)
  {
    this.logicalNames = logicalNames.clone();
  }

  public ObisCode[] getObjectIds()
  {
    return logicalNames.clone();
  }

  public AccessSelectionParameters getAccessSelectionParameters()
  {
    DlmsDataOctetString ids[] = new DlmsDataOctetString[logicalNames.length];

    for (int i = 0; i < ids.length; i++)
    {
      ids[i] = new DlmsDataOctetString(logicalNames[i].toByteArray());
    }
    return new AccessSelectionParameters(3, new DlmsDataArray(ids));
  }

  //@Override //for Java 1.5
  public int getId()
  {
    return 3;
  }

  //@Override //for Java 1.5
  public DlmsData toDlmsData()
  {
    return getAccessSelectionParameters().getAccessParameters();
  }

}
