/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/IHdlcChannel.java $
 * Version:
 * $Id: IHdlcChannel.java 3747 2011-11-09 11:06:07Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  12.05.2010 11:20:22
 */

package com.elster.protocols.hdlc;

import java.io.IOException;

/**
 * This interface describes an HDLC channel.
 *
 * @author osse
 */
public interface IHdlcChannel
{

  /**
   * Closes the connection.
   * <P>
   * This is done by sending an DISC Frame and waiting for an UA-Frame.
   *
   * @throws IOException
   */
  void close() throws IOException;

  /**
   * Returns the destination address for this connection.
   *
   * @return the destination address.
   */
  HdlcAddress getDestAddress();
  
  /**
   * Returns the source address for this connection.
   *
   * @return the source address.
   */
  HdlcAddress getSourceAddress();

  
  int getMaxInformationFieldLengthReceive();
  int getMaxInformationFieldLengthTransmit();

  /**
   * Returns the current receive sequence number.
   *
   * @return The current receive sequence number.
   */
  //int getReceiveSequenceNo();

  /**
   * Returns the current send sequence number.
   *
   * @return The current send sequence number.
   */
  //int getSendSequenceNo();



  int getWindowSizeReceive();

  int getWindowSizeTransmit();

}
