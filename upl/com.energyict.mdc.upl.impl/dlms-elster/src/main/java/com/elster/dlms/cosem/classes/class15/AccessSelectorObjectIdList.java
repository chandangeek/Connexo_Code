/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/classes/class15/AccessSelectorObjectIdList.java $
 * Version:     
 * $Id: AccessSelectorObjectIdList.java 4469 2012-05-08 13:19:45Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  28.07.2011 08:51:25
 */
package com.elster.dlms.cosem.classes.class15;

import com.elster.dlms.cosem.objectmodel.CosemAccessSelector;
import com.elster.dlms.types.basic.AccessSelectionParameters;
import com.elster.dlms.types.basic.CosemObjectId;
import com.elster.dlms.types.data.*;

/**
 * Access selector 3 for Class ID 15 Attribute 2.<P>
 * See BB ed.10 p. 61
 *
 * @author osse
 */
public class AccessSelectorObjectIdList implements CosemAccessSelector
{
  private final CosemObjectId[] objectIds;

  public AccessSelectorObjectIdList(final CosemObjectId first,final CosemObjectId... more)
  {
    objectIds = new CosemObjectId[1 + more.length];
    objectIds[0] = first;
    System.arraycopy(more, 0, objectIds, 1, more.length);
  }
  
  public AccessSelectorObjectIdList(final CosemObjectId[] ids)
  {
    objectIds = ids.clone();
  }  

  public CosemObjectId[] getObjectIds()
  {
    return objectIds.clone();
  }

  public AccessSelectionParameters getAccessSelectionParameters()
  {
    DlmsDataStructure ids[] = new DlmsDataStructure[objectIds.length];

    for (int i = 0; i < ids.length; i++)
    {
      ids[i] = new DlmsDataStructure(
              new DlmsDataLongUnsigned(objectIds[i].getClassId()),
              new DlmsDataOctetString(objectIds[i].getLogicalName().toByteArray()));
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
