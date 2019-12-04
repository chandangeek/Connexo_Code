/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elster.protocols;

/**
 * Represents the state of an protocol.
 *
 * @author osse
 */
public enum ProtocolState {
  /**
   * The protocol is closed.
   */
  CLOSE,
  /**
   * The protocol is opening.<P>
   * For example, during dialing a number with an modem and wait for the answer of the remote modem.<br>
   * It is allowed to skip this state (a state change from close directly to open).
   */
  OPENING,
  /**
   * The protocol is open.<P>
   * In the state the streams can be used.
   */
  OPEN,
  /**
   * The protocol is closing.<P>
   * It is allowed to skip this state (a state change from open directly to close).
   */
  CLOSING

}
