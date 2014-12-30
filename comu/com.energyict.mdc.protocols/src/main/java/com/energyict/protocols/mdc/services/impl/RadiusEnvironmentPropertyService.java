package com.energyict.protocols.mdc.services.impl;

/**
 * Provides environmental properties that are used by
 * {@link com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.csd.IpUpdater}.
 * These properties used to be provided by the
 * EIServer Environment class through
 * the various get and getProperty methods.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-30 (08:44)
 */
public interface RadiusEnvironmentPropertyService {

    public String getDriverClass();

    public String getConnectionUrl();

    public String getDatabaseUserName();

    public String getDatabasePassword();

}