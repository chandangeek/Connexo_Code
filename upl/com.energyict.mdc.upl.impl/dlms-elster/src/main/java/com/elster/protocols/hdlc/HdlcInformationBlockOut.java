/* File:        
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/hdlc/HdlcInformationBlockOut.java $
 * Version:     
 * $Id: HdlcInformationBlockOut.java 3665 2011-10-04 17:34:41Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  12.05.2010 11:26:02
 */

package com.elster.protocols.hdlc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class represents one HDLC information block for sending.
 *
 * @author osse
 */
public class HdlcInformationBlockOut
{
  public enum State{NONE,SENDING,SENT,ERROR};

  private State state= State.NONE;

  private final InputStream informationInputStream;

  private IOException errorReason;


  /**
   * Creates an HdlcInformationBlockOut with using an input stream for providing the data.
   *
   * @param informationInputStream The data will be read from this stream.
   */
  public HdlcInformationBlockOut(InputStream informationInputStream)
  {
    //TODO: evtl. InputStream entfernen und einen outputstream bereitstellen.
    super();
    this.informationInputStream = informationInputStream;
  }

   /**
   * Creates an HdlcInformationBlockOut with using an byte array for providing the data.
   *
   * @param information The data to send
   */
  public HdlcInformationBlockOut(final byte[] information)
  {
    super();
    this.informationInputStream = new ByteArrayInputStream(information);
  }

  InputStream getInformationInputStream()
  {
    return informationInputStream;
  }
  
  /**
   * Returns the state of this block.
   * 
   * @return The state of this block.
   */
  public synchronized State getState()
  {
    return state;
  }

  void setState(State state)
  {
    setState(state,null);

  }

  synchronized void setState(State state, IOException reason)
  {
    this.state = state;
    this.errorReason= reason;
    notifyAll();
  }


  /**
   * Waits until the information was sent (the confirmation was received) or an error occurred.
   * 
   * @throws InterruptedException
   */
  public synchronized void waitFor() throws InterruptedException
  {
    while (getState()!= State.ERROR && getState()!=State.SENT)
    {
      wait();
    }
  }

  /**
   * Returns the exception which caused an error.
   *
   * @return The exception which caused an error or null.
   */
  public synchronized IOException getErrorReason()
  {
    return errorReason;
  }



}
