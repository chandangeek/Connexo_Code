/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleAssociationLnObject.java $
 * Version:     
 * $Id: SimpleAssociationLnObject.java 6380 2013-03-28 14:46:45Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Apr 17, 2012 3:02:17 PM
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.cosem.classes.class15.AccessSelectorClassList;
import com.elster.dlms.cosem.classes.class15.CosemObjectListElement;
import com.elster.dlms.cosem.classes.class15.ReducedObjectListElement;
import com.elster.dlms.cosem.classes.info.CosemAttributeValidators.ValidationExecption;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataArray;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Auto connect.<P>
 * COSEM class id 15, See BB. ed.10 p. 57
 *
 * @author osse
 */
public class SimpleAssociationLnObject extends SimpleCosemObject
{
  SimpleAssociationLnObject(final SimpleCosemObjectDefinition definition,
                            final SimpleCosemObjectManager manager)
  {
    super(definition, manager);
  }

  public List<ReducedObjectListElement> getObjectListForClassId(final int classId) throws IOException
  {
    try
    {
      final AccessSelectorClassList accessSelectorClassList = new AccessSelectorClassList(classId);
      final DlmsData data = getManager().executeGetData(getDefinition(), 2, accessSelectorClassList, false);
      final ReducedObjectListElement[] objectListArray = ReducedObjectListElement.fromDlmsDataArray(data);
      return Arrays.asList(objectListArray);
    }
    catch (ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException(ex);
    }
  }

  public List<CosemObjectListElement> getObjectList() throws IOException
  {
    try
    {
      final DlmsDataArray data = executeGet(2, DlmsDataArray.class, false);
      final CosemObjectListElement[] objectListArray = CosemObjectListElement.buildElements(data);
      return Arrays.asList(objectListArray);
    }
    catch (ValidationExecption ex)
    {
      throw new UnexpectedDlmsDataTypeIOException(ex);
    }
  }

}
