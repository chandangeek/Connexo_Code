/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/CosemTask.java $
 * Version:     
 * $Id: CosemTask.java 6772 2013-06-14 15:12:55Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  02.08.2011 17:39:08
 */
package com.elster.dlms.cosem.objectmodel;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.applicationlayer.ICosemApplicationLayerTask;
import java.io.IOException;

/**
 * Abstract base class for {@link ICosemApplicationLayerTask}
 *
 * @author osse
 */
public abstract class CosemTask extends CosemExecutor implements ICosemApplicationLayerTask
{
  
  @Override
  public void setExecutionState(ExecutionState executionState) //makes the method public.
  {
    super.setExecutionState(executionState);
  }
  

}
