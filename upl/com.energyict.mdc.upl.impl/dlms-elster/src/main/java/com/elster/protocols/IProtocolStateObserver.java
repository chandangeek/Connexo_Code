/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/IProtocolStateObserver.java $
 * Version:
 * $Id: IProtocolStateObserver.java 2430 2010-12-06 13:56:06Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  25.05.2010 12:24:25
 */

package com.elster.protocols;

/**
 * This interface must be implemented by observers of the open state of protocols.
 * <P>
 * See {@link com.elster.protocols.IProtocolStateObservable}
 *
 * @author osse
 */
public interface IProtocolStateObserver
{
  /**
   * Called if the open state of the sender has changed.
   *
   * @param sender The observed object.
   * @param oldState The old state.
   * @param newState The new state.
   */
  public void openStateChanged(Object sender, ProtocolState oldState, ProtocolState newState);


  /**
   * Called if connection was broken.
   *
   * @param sender The observed Object
   * @param orign The protocol layer which caused (/detected) the error.
   * @param reason The exception which indicated that the connection was broken.
   */
  public void connectionBroken(Object sender,Object orign, Exception reason);

}
