/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/HdlcFrameLayer.java $
 * Version:     
 * $Id: HdlcFrameLayer.java 4279 2012-04-02 14:37:29Z HaasRollenbJ $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  20.07.2011 13:04:46
 */
package com.elster.protocols.hdlc;

import com.elster.protocols.IStreamProtocol;
import com.elster.protocols.streams.TimeoutInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Preparation to spilt the HDLC Protocol handling in smaller parts.
 * 
 * Currently not used.
 * 
 * @author osse
 */
public class HdlcFrameLayer implements IHdlcFrameLayer
{
  private static final Logger LOGGER = Logger.getLogger(HdlcFrameLayer.class.getName());
  public static final int FLAG = 0x7E;
  private final IStreamProtocol sublayer;
  private final TimeoutInputStream in;

  public HdlcFrameLayer(IStreamProtocol sublayer)
  {
    this.sublayer = sublayer;
    in = new TimeoutInputStream(sublayer.getInputStream(), 2000);
  }

  public void sendFrame(HdlcFrame frame) throws IOException
  {
    LOGGER.log(Level.FINE, "sendFrame: sending {0}-Frame",
               frame.getControllField().getCommandAndResponseType());
    sublayer.getOutputStream().write(FLAG);
    frame.encode(sublayer.getOutputStream());
    sublayer.getOutputStream().write(FLAG);
    sublayer.getOutputStream().flush();
  }

  public HdlcFrame receiveFrame(int timeout, int interOctetTimeout) throws IOException
  {
    LOGGER.log(Level.FINEST, "Receiving frame");

    if (timeout != in.getTimeout())
    {
      in.setTimeout(timeout);
    }

    HdlcFrame frame = new HdlcFrame();
    frame.decode(in);

    int endingFlag = in.read();
    if (endingFlag != FLAG)
    {
      throw new HdlcDecodingIOException("No ending flag received. Expected: " + FLAG + ", Received: "
                                        + endingFlag);
    }

    LOGGER.log(Level.FINE, "Received {0}-Frame", frame.getControllField().getCommandAndResponseType());

    return frame;
  }

}
