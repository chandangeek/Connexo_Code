/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleCosemObject.java $
 * Version:     
 * $Id: SimpleCosemObject.java 3823 2011-12-07 09:28:12Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Mar 9, 2011 1:51:09 PM
 */
package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import java.io.IOException;

/**
 * Base class for COSEM object in the Simple COSEM Object Model
 *
 * @author osse
 */
public class SimpleCosemObject
{
  private final SimpleCosemObjectDefinition definition;
  private final SimpleCosemObjectManager manager;

  protected SimpleCosemObject(final SimpleCosemObjectDefinition definition,
                              final SimpleCosemObjectManager manager)
  {
    this.definition = definition;
    this.manager = manager;
  }

  public ObisCode getLogicalName()
  {
    return definition.getLogicalName();
  }

  public SimpleCosemObjectDefinition getDefinition()
  {
    return definition;
  }

  protected SimpleCosemObjectManager getManager()
  {
    return manager;
  }

  protected <T extends DlmsData> T executeGet(final int attributeId, final Class<T> expectedDataType,
                                              final boolean forceRead) throws IOException
  {
    return manager.executeGetData(definition, attributeId, expectedDataType, forceRead);
  }

  protected void executeSet(final int attributeId, final DlmsData data) throws IOException
  {
    manager.executeSetData(definition, attributeId, data);
  }
  
  protected DlmsData executeMethod(final int methodId, final DlmsData parameters) throws IOException
  {
    return manager.executeMethod(definition, methodId, parameters);
  }
  

}
