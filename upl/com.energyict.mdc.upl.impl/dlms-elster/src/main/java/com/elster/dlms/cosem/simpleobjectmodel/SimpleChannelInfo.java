/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/simpleobjectmodel/SimpleChannelInfo.java $
 * Version:     
 * $Id: SimpleChannelInfo.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Mar 17, 2011 2:58:54 PM
 */

package com.elster.dlms.cosem.simpleobjectmodel;

import com.elster.dlms.cosem.classes.class03.ScalerUnit;
import com.elster.dlms.cosem.classes.class07.CaptureObjectDefinition;

/**
 * Information of one channel of a profile.
 *
 * @author osse
 */
public class SimpleChannelInfo
{
  private final CaptureObjectDefinition captureObjectDefinition;
  private final ScalerUnit scalerUnit;

  public SimpleChannelInfo(CaptureObjectDefinition captureObjectDefinition, ScalerUnit scalerUnit)
  {
    this.captureObjectDefinition = captureObjectDefinition;
    this.scalerUnit = scalerUnit;
  }

  public CaptureObjectDefinition getCaptureObjectDefinition()
  {
    return captureObjectDefinition;
  }

  public ScalerUnit getScalerUnit()
  {
    return scalerUnit;
  }

}
