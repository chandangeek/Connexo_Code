package com.energyict.mdc.protocol.api.dialer.core;

/**
 * Marker interface that needs to get applied to dialers that are driver managed. This essentially mean that they are managed
 * by a stack implementation and do not need other software to manage the serial communication to and from the modem. We do not use
 * annotations for this because annotations do not unambiguously define types.
 * <p/>
 * Also see Item 37 in Effective Java 2.
 *
 * @author alex
 */
public interface DriverManaged {

}
