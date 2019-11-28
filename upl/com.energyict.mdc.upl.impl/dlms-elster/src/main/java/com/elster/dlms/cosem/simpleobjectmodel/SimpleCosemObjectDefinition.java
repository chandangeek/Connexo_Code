/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleCosemObjectDefinition.java $
 * Version:     
 * $Id: SimpleCosemObjectDefinition.java 2776 2011-03-14 15:02:52Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Mar 9, 2011 3:25:25 PM
 */

package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.types.basic.ObisCode;

/**
 * This class describes one COSEM object in the Simple COSEM Object Model
 *
 * @author osse
 */
public class SimpleCosemObjectDefinition
{
  private final int classId;
  private final int classVersion;
  private final ObisCode logicalName;

  public SimpleCosemObjectDefinition(int classId, int classVersion, ObisCode logicalName)
  {
    this.classId = classId;
    this.classVersion = classVersion;
    this.logicalName = logicalName;
  }

  public int getClassId()
  {
    return classId;
  }

  public int getClassVersion()
  {
    return classVersion;
  }

  public ObisCode getLogicalName()
  {
    return logicalName;
  }



}
