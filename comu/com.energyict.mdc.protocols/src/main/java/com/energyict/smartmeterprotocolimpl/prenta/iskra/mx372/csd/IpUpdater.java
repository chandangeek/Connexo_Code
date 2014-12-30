package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.csd;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.SqlBuilder;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import com.energyict.protocols.mdc.services.impl.RadiusEnvironmentPropertyService;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 31/01/12
 * Time: 16:31
 */

public class IpUpdater {

    public static final String RADIUS_IPFINDER_DRIVER_CLASS_ENVIRONMENT_PROPERTY_NAME = "driverClass";
    public static final String RADIUS_IPFINDER_CONNECTION_URL_ENVIRONMENT_PROPERTY_NAME = "connectionUrl";
    public static final String RADIUS_IPFINDER_DB_USER_NAME_ENVIRONMENT_PROPERTY_NAME = "dbUserName";
    public static final String RADIUS_IPFINDER_DB_PASSWORD_ENVIRONMENT_PROPERTY_NAME = "dbPassword";

    private static final String request = "select FRAMED_IP_ADDRESS from RADIUSACCOUNTING where CALLING_STATION_ID like ? and ? < LOG_DATE";
    private static final String IPADDRESS = "FRAMED_IP_ADDRESS";

    private final RadiusEnvironmentPropertyService propertyService;
    private int pollTimeout;
    private int pollFreq;

    public IpUpdater(RadiusEnvironmentPropertyService propertyService, int pollTimeOut, int pollFreq) {
        this.propertyService = propertyService;
        this.pollTimeout = pollTimeOut;
        this.pollFreq = pollFreq;
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.connection.driver_class", this.propertyService.getDriverClass());
        properties.put("hibernate.connection.url", this.propertyService.getConnectionUrl());
        properties.put("hibernate.connection.username", this.propertyService.getDatabaseUserName());
        properties.put("hibernate.connection.password", this.propertyService.getDatabasePassword());
        return properties;
    }


    /**
     * Polls the IP address in the radius server.
     *
     * @param date2
     * @throws InterruptedException
     * @throws BusinessException
     * @throws SQLException
     * @throws IOException
     */
    public String poll(String phone, Date date2) throws InterruptedException, BusinessException, SQLException, IOException {
        long protocolTimeout = System.currentTimeMillis() + this.pollTimeout;
        String ipAddress = null;
        Date lastDate = null;
        while (ipAddress == null) {
            Connection connection = null;
            PreparedStatement statement = null;
            try {
                connection = getConnection();
                SqlBuilder builder = new SqlBuilder(request);
                builder.bindString(getSQLLikePhoneNumber(phone));
                builder.bindTimestamp(date2);
                statement = builder.getStatement(connection);
                ResultSet rs = null;
                try {
                    rs = statement.executeQuery();
                    while (rs.next()) {
                        if (rs.isFirst()) {
                            ipAddress = rs.getString(IPADDRESS);
                        }
                        else {
                            if (ipAddress != null) {
                                if (!ipAddress.equalsIgnoreCase(rs.getString(IPADDRESS))) {
                                    throw new ConnectionException("Multiple records were found after CSD call, will not update ipaddress.");
                                }
                            }
                        }
                    }
                }
                finally {
                    if (rs != null) {
                        rs.close();
                    }
                }
                if (ipAddress == null) {
                    Thread.sleep(this.pollFreq);
                }

            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            catch (SQLException e) {
                e.printStackTrace();
                if (e.getMessage().contains("Connections could not be acquired from the underlying database!")) {
                    throw new BusinessException("Connections could not be acquired from the underlying database! It is possible that the radius properties are not correct.");
                }
            }
            finally {
                if (connection != null) {
                    connection.close();
                }
                if (statement != null) {
                    statement.close();
                }
            }

            if (System.currentTimeMillis() - protocolTimeout > 0) {
                throw new BusinessException("Could not update the meters IP-address");
            }
        }
        return ipAddress;
    }

    /**
     * Get a connection from Hibernate, just hope he (or she) does this in a proper way
     *
     * @return a {@link Connection}
     */
    private Connection getConnection() {
        return null;
    }

    private String getSQLLikePhoneNumber(String phoneNumber) {
        StringBuilder strBuffer = new StringBuilder();
        while (phoneNumber.startsWith("0") || phoneNumber.startsWith("+")) {
            phoneNumber = phoneNumber.substring(1);
        }
        strBuffer.append("%");    // if 00 or + is added to the phone ...
        strBuffer.append(phoneNumber);
        return strBuffer.toString();
    }

}