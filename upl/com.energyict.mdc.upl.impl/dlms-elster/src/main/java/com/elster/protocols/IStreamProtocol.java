/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/IStreamProtocol.java $
 * Version:
 * $Id: IStreamProtocol.java 4874 2012-07-19 15:23:43Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Oct 19, 2010 3:05:00 PM
 */

package com.elster.protocols;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for stream providing protocols.
 *
 * @author osse
 */
public interface IStreamProtocol extends IProtocol
{

    /**
   * Returns the input stream for this protocol
   *
   * @return the input stream.
   */
  InputStream getInputStream();

  /**
   * Returns the output stream for this protocol
   *
   * @return the output stream.
   */
  OutputStream getOutputStream();


}
