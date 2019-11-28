/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/IBaudrateSupport.java $
 * Version:
 * $Id: IBaudrateSupport.java 2430 2010-12-06 13:56:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  27.04.2010 13:27:41
 */

package com.elster.protocols;

import java.io.IOException;

/**
 * This interface should be implemented by protocols which supporting
 * different baudrates.
 *
 * @author osse
 */
public interface IBaudrateSupport
{
  /**
   * Returns the current baud rate.
   *
   * @return The current baud rate.
   */
  int getBaudrate();

  /**
   * Sets or changes the baudrate.
   *
   * @param newBaudrate The new baudrate
   * @throws IOException
   */
  void setBaudrate(int newBaudrate) throws IOException;
}
