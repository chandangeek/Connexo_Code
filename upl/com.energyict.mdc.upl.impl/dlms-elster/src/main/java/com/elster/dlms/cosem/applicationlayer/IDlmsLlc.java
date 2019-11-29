/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/dlms/cosem/applicationlayer/IDlmsLlc.java $
 * Version:     
 * $Id: IDlmsLlc.java 3585 2011-09-28 15:49:20Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  12.05.2010 08:25:55
 */
package com.elster.dlms.cosem.applicationlayer;

import com.elster.protocols.IProtocolStateObservable;
import com.elster.protocols.ProtocolState;
import java.io.IOException;

/**
 * LLC for DLMS.
 *
 * @author osse
 */
public interface IDlmsLlc extends IProtocolStateObservable
{
  /**
   * Opens the connection to an logical device
   *
   * @param clientId The ID of the client (For example 0x10 for the public client.)
   * @param logicalDeviceId The ID of the logical device (0x01 for the management device).
   * @param wait {@code true}: this method blocks until the connection is open or an error occurred.
   * @throws IOException
   */
  void openLogicalDeviceLink(int clientId, int logicalDeviceId, boolean wait) throws IOException;

  void closeLogicalDeviceLink(boolean wait) throws IOException;

  int getLogicalDeviceId();

  void writePdu(ProtocolDataUnitOut pdu, boolean wait) throws IOException;

  ProtocolState getProtocolState();

  boolean isOpen();

  ProtocolDataUnitIn readPdu(boolean wait, int timeoutMs) throws IOException;
}
