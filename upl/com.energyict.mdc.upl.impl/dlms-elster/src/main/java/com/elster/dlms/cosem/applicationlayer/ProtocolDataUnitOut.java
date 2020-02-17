/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/ProtocolDataUnitOut.java $
 * Version:     
 * $Id: ProtocolDataUnitOut.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  18.05.2010 08:09:10
 */

package com.elster.dlms.cosem.applicationlayer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Single PDU to send.
 *
 * @author osse
 */
public class ProtocolDataUnitOut
{
  private final InputStream dataInputStream;

//  public ProtocolDataUnitOut(InputStream dataInputStream)
//  {
//    this.dataInputStream = dataInputStream;
//  }

  /**
   * The data to send.
   * 
   * @param data 
   */
  public ProtocolDataUnitOut(byte[] data)
  {
    this.dataInputStream = new ByteArrayInputStream(data);
  }


  /**
   * This input stream is used to access the provided data.
   * 
   * @return 
   */
  public InputStream getDataInputStream()
  {
    return dataInputStream;
  }

}
