/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/streams/ITimeoutControl.java $
 * Version:
 * $Id: ITimeoutControl.java 2190 2010-10-19 12:51:58Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  Oct 8, 2010 11:17:12 AM
 */

package com.elster.protocols.streams;

/**
 * This interface ...
 *
 * @author osse
 */
public interface ITimeoutControl
{
  int getTimeout();
  void setTimeout(int timeout);

  int getTotalTimeout();
  void setTotalTimeout(int totalTimeout);

}
