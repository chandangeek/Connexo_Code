/* File:
 * $HeadURL: https://deosn1-svnvm1.kromschroeder.elster-group.com/svn/eWorkPad/branches/EICT_DLMS_Driver_V2.3/Libraries/ElsterDlmsE2/src/com/elster/protocols/IProtocolStateObservable.java $
 * Version:
 * $Id: IProtocolStateObservable.java 2194 2010-10-20 07:04:59Z osse $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  osse
 * Created on:  25.05.2010 12:26:03
 */

package com.elster.protocols;

/**
 * This interface must be implemented by protocols which have an observable open state.
 *
 * @author osse
 */
public interface IProtocolStateObservable
{
  /**
   * Add the specified observer.
   *
   * @param observer The observer to add.
   */
  void addProtocolStateListener(IProtocolStateObserver observer);

  /**
   * Removes the specified observer.
   *
   * @param observer The observer to remove.
   */
  void removeProtocolStateListener(IProtocolStateObserver observer);

  ProtocolState getProtocolState();

  boolean isOpen();




}

