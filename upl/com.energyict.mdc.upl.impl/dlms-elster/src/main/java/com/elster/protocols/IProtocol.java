/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/IProtocol.java $
 * Version:
 * $Id: IProtocol.java 6465 2013-04-22 14:45:55Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.04.2010 13:20:33
 */
package com.elster.protocols;

import java.io.IOException;

/**
 * This interface is the interface for protocols which transports their data through
 * streams.
 *
 * @author osse
 */
public interface IProtocol extends IProtocolStateObservable
{

  /**
   * Opens the connection provided by this protocol
   * 
   * @throws IOException
   */
  void open() throws IOException;
  
  
  
  /**
   * Indicates that {@link #open()} should be canceled
   * <P>
   * Called from another thread.<P>
   * {@link #open()} can react to this method by throwing a {@link OpenCanceledException}
   * 
   * @throws IOException
   */
  void cancelOpen();
          

  /**
   * Closes the connection provided by this protocol
   * 
   * @throws IOException
   */
  void close() throws IOException;
}
