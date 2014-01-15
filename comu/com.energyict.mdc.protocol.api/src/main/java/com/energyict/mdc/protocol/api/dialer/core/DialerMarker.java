/*
 * DialerMarker.java
 *
 * Created on 20 april 2004, 14:45
 */

package com.energyict.mdc.protocol.api.dialer.core;

import com.energyict.mdc.protocol.api.dialer.core.Dialer;
import com.energyict.mdc.protocol.api.dialer.core.Direct;
import com.energyict.mdc.protocol.api.dialer.core.DriverManaged;
import com.energyict.mdc.protocol.api.dialer.core.IPDial;
import com.energyict.mdc.protocol.api.dialer.core.Link;
import com.energyict.mdc.protocol.api.dialer.core.Modem;
import com.energyict.mdc.protocol.api.dialer.core.Optical;
import com.energyict.mdc.protocol.api.dialer.core.PostSelect;

/**
 * @author Koen
 */
public final class DialerMarker {

    /**
     * Creates a new instance of DialerMarker
     */
    private DialerMarker() {
    }

    public static boolean hasOpticalMarker(Link link) {
        return isMarkedWith(Optical.class, link);
    }

    public static boolean hasDirectMarker(Dialer dialer) {
        return isMarkedWith(Direct.class, dialer);
    }

    public static boolean hasModemMarker(Dialer dialer) {
        return isMarkedWith(Modem.class, dialer);
    }

    public static boolean hasPostSelectMarker(Dialer dialer) {
        return isMarkedWith(PostSelect.class, dialer);
    }

    public static boolean hasIPDialMarker(Link link) {
        return isMarkedWith(IPDial.class, link);
    }

    /**
     * Returns true if the given link is marked as being driver managed, false if not. The marker interface used in this case is
     * the {@link DriverManaged} interface.
     *
     * @param link The link to check.
     * @return <code>true</code> if the link is marked as being driver managed, <code>false</code> if not.
     */
    public static boolean isDriverManaged(final Link link) {
        return isMarkedWith(DriverManaged.class, link);
    }

    /**
     * Checks if the given link is marked with the given marker interface.
     *
     * @param marker The marker interface.
     * @param link   The link.
     * @return True if the link has been marker with the given marker, false if not.
     */
    private static boolean isMarkedWith(final Class<?> marker, final Link link) {
        return isMarkedWith(marker, link.getClass());
    }

    /**
     * Checks if the given class <strong>marked</strong> has been marked with the marker interface <strong>marker</strong>.
     *
     * @param marker The marker interface.
     * @param marked The marked class.
     * @return True if the class has been marked with the marker interface, false if not.
     */
    private static boolean isMarkedWith(final Class<?> marker, final Class<?> marked) {
        final Class<?>[] implementedInterfaces = marked.getInterfaces();

        for (final Class<?> implementedInterface : implementedInterfaces) {
            if (implementedInterface.isAssignableFrom(marker)) {
                return true;
            }
        }

        return false;
    }
}
