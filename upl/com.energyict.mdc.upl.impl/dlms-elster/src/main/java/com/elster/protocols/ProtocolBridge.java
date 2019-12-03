/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/ProtocolBridge.java $
 * Version:     
 * $Id: ProtocolBridge.java 2194 2010-10-20 07:04:59Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.05.2010 10:51:42
 */
package com.elster.protocols;

import com.elster.protocols.streams.TimeoutableInputStreamPipe;
import com.elster.protocols.streams.TimeoutableOutputStreamPipe;
import java.io.IOException;

/**
 * This class provides 2 protocols which are connected with pipes.
 * <P>
 * Everything which is written to "sideA" can be read from "sideB" and the other way around.
 *
 * @author osse
 */
public class ProtocolBridge
{
  IStreamProtocol sideA;
  IStreamProtocol sideB;
  TimeoutableInputStreamPipe inA;
  TimeoutableOutputStreamPipe outB;
  TimeoutableInputStreamPipe inB;
  TimeoutableOutputStreamPipe outA;

  /**
   * Sole constructor
   *
   * @throws IOException
   */

  public ProtocolBridge() throws IOException
  {
    inA = new TimeoutableInputStreamPipe();
    outB = new TimeoutableOutputStreamPipe(inA);

    inB = new TimeoutableInputStreamPipe();
    outA = new TimeoutableOutputStreamPipe(inB);

    sideA= new StreamProtocol(inA, outA, true);
    sideB= new StreamProtocol(inB, outB, true);

    sideA.open();
    sideB.open();
  }
  
  /**
   * Side A.
   * <P>
   * (see class description)
   *
   * @return side b.
   */
  public IStreamProtocol getSideA()
  {
    return sideA;
  }

  /**
   * Side B.
   * <P>
   * (see class description)
   *
   * @return side a.
   */
  public IStreamProtocol getSideB()
  {
    return sideB;
  }

}
