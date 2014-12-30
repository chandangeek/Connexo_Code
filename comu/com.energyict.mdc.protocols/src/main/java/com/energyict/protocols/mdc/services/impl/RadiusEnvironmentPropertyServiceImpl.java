package com.energyict.protocols.mdc.services.impl;

import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.csd.IpUpdater;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

/**
 * Provides an implementation for the {@link EnvironmentPropertyService} interface
 * that uses the OSGi framework of setting/getting properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-30 (08:32)
 */
@Component(name="com.energyict.mdc.service.deviceprotocols.properties.radius.ipfinder", service = EnvironmentPropertyService.class)
public class RadiusEnvironmentPropertyServiceImpl implements RadiusEnvironmentPropertyService {

    private volatile String driverClass;
    private volatile String connectionUrl;
    private volatile String databaseUserName;
    private volatile String databasePassword;

    @Activate
    public void activate(Map<String, Object> props) {
        if (props != null) {
            driverClass = stringFromObject(props.get(IpUpdater.RADIUS_IPFINDER_DRIVER_CLASS_ENVIRONMENT_PROPERTY_NAME), "oracle.jdbc.OracleDriver");
            connectionUrl = stringFromObject(props.get(IpUpdater.RADIUS_IPFINDER_CONNECTION_URL_ENVIRONMENT_PROPERTY_NAME), "jdbc:oracle:thin:@localhost:1521:eiserver");
            databaseUserName = stringFromObject(props.get(IpUpdater.RADIUS_IPFINDER_DB_USER_NAME_ENVIRONMENT_PROPERTY_NAME), null);
            databasePassword = stringFromObject(props.get(IpUpdater.RADIUS_IPFINDER_DB_PASSWORD_ENVIRONMENT_PROPERTY_NAME), null);
        }
    }

    @Override
    public String getDriverClass() {
        return driverClass;
    }

    @Override
    public String getConnectionUrl() {
        return connectionUrl;
    }

    @Override
    public String getDatabaseUserName() {
        return databaseUserName;
    }

    @Override
    public String getDatabasePassword() {
        return databasePassword;
    }

    private String stringFromObject(Object property, String defaultValue) {
        if (property == null) {
            return defaultValue;
        }
        else if (property instanceof String) {
            return (String) property;
        }
        else {
            return defaultValue;
        }
    }

}