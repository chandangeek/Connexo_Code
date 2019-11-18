/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/objectmodel/CosemMethod.java $
 * Version:     
 * $Id: CosemMethod.java 3913 2012-01-16 12:54:54Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  21.05.2010 13:34:22
 */
package com.elster.dlms.cosem.objectmodel;

import com.elster.dlms.cosem.application.services.action.ActionResponse;
import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.common.MethodAccessMode;
import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.data.DlmsData;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Generic COSEM method, used by the COSEM object.<P>
 *
 * @author osse
 */
public class CosemMethod extends CosemExecutor
{
  private final int id;
  private final MethodAccessMode accessMode;
  private final CosemObject parent;
  public static final Set<MethodAccessMode> EXECUTABLE_ACCESS_MODES =
          Collections.unmodifiableSet(
          EnumSet.of(MethodAccessMode.ACCESS));
  public static final Set<MethodAccessMode> EXECUTABLE_AUTHENTICATED_ACCESS_MODES =
          Collections.unmodifiableSet(
          EnumSet.of(MethodAccessMode.ACCESS, MethodAccessMode.AUTHENTICATED_ACCESS));

  public CosemMethod(final CosemObject parent, final int id, final MethodAccessMode accessMode)
  {
    super();
    this.parent = parent;
    this.id = id;
    this.accessMode = accessMode;
  }

  public MethodAccessMode getAccessMode()
  {
    return accessMode;
  }

  public int getId()
  {
    return id;
  }

  public CosemObject getParent()
  {
    return parent;
  }

  public CosemMethodDescriptor getMethodDescriptor()
  {
    return new CosemMethodDescriptor(getParent().getLogicalName(), getParent().getCosemClassId(), getId());
  }

  /*
   * Maybe it could be better to separate the execution part into a derived class.
   */
  private DlmsData parameters = null;
  private ActionResponse actionResponse;

  public synchronized ActionResponse getActionResult()
  {
    return actionResponse;
  }

  public void setActionResponse(final ExecutionState executionState, final ActionResponse actionResponse)
  {
    synchronized (this)
    {
      this.actionResponse = actionResponse;
    }
    setExecutionState(executionState);
  }

  public synchronized DlmsData getParameters()
  {
    return parameters;
  }

  public synchronized void setParameters(final DlmsData parameters)
  {
    this.parameters = parameters;
  }

  public synchronized ActionResponse getActionResponse()
  {
    return actionResponse;
  }

  @Override
  public boolean isExecutable(final CosemApplicationLayer applicationLayer)
  {
    if (!super.isExecutable(applicationLayer))
    {
      return false;
    }

    if (applicationLayer.isCipheredContext())
    {
      return EXECUTABLE_AUTHENTICATED_ACCESS_MODES.contains(getAccessMode());
    }
    else
    {
      return EXECUTABLE_ACCESS_MODES.contains(getAccessMode());
    }
  }

}
