/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/ProtocolDataUnitIn.java $
 * Version:     
 * $Id: ProtocolDataUnitIn.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  18.05.2010 10:30:02
 */

package com.elster.dlms.cosem.applicationlayer;

import java.io.InputStream;

/**
 * Single received PDU.
 *
 * @author osse
 */
public class ProtocolDataUnitIn
{
  private final InputStream in;

  public ProtocolDataUnitIn(InputStream in)
  {
    this.in = in;
  }

  public InputStream getInputStream()
  {
    return in;
  }
}
