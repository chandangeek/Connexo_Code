/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import java.util.Set;

/**
 * Models the behavior of a component that represents
 * a protocol that can be covered by the licensing mechanism.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-18 (15:11)
 */
public interface LicensedProtocol {

    /**
     * Gets the code that should be used in the license file
     * to indicate that the protocol is covered by the license.
     *
     * @return The code
     */
    public int getCode ();

    /**
     * Gets the name of the class that implements this LicensedProtocol.
     *
     * @return The name of the class that implements this LicensedProtocol
     */
    public String getClassName ();

    /**
     * Gets the set of {@link ProtocolFamily families}
     * to which this LicensedProtocol belongs.
     *
     * @return The set of protocol families
     */
    public Set<ProtocolFamily> getFamilies ();

    /**
     * Gets the name fo the Protocol
     *
     * @return the name of the Protocol
     */
    public String getName();

}