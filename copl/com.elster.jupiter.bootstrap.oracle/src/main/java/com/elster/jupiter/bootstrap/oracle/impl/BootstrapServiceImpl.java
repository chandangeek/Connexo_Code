/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.oracle.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.DataSourceSetupException;
import com.elster.jupiter.bootstrap.InvalidPasswordException;
import com.elster.jupiter.bootstrap.PropertyNotFoundException;
import com.elster.jupiter.util.Holder;
import com.elster.jupiter.util.HolderBuilder;

import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This Component is responsible for creating a DataSource on demand. It does so by getting the needed properties from the BundleContext.
 * <br/>
 * Required properties :
 * <ul>
 * <li><code>com.elster.jupiter.datasource.jdbcurl</code> : the database url.</li>
 * <li><code>com.elster.jupiter.datasource.jdbcuser</code> : the database user.</li>
 * <li><code>com.elster.jupiter.datasource.jdbcpassword</code> : the database user's password.</li>
 * <li><code>com.elster.jupiter.datasource.keyfile</code> : the database user's password encryption key.</li>
 * </ul>
 * Optional properties :
 * <ul>
 * <li><code>com.elster.jupiter.datasource.pool.maxlimit</code> : max limit, will default to 50.</li>
 * <li><code>com.elster.jupiter.datasource.pool.maxstatements</code> : max statements, will default to 50.</li>
 * <li><code>com.elster.jupiter.datasource.pool.oracle.ons.nodes</code> : ons nodes information. </li>
 * </ul>
 */
@Component(name = "com.elster.jupiter.bootstrap.oracle",
        property = {"osgi.command.scope=orm", "osgi.command.function=dbConnection"})
public final class BootstrapServiceImpl implements BootstrapService {

    public static final String ORACLE_ONS_NODES = "com.elster.jupiter.datasource.pool.oracle.ons.nodes";

    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;
    private String keyFile;
    private int maxLimit;
    private int maxStatementsLimit;
    private int inactivityTimeout;
    private int abandonedConnectionsTimeout;
    private int timeToLive;
    private int maxConnectionReuseTime;
    private String onsNodes;

    private ConnectionProperties connectionProperties = new ConnectionProperties();

    private Holder<DataSource> cache = HolderBuilder.lazyInitialize(this::doCreateDataSource);
    private boolean initialized = false;

    public BootstrapServiceImpl() {
    }

    @Inject
    public BootstrapServiceImpl(BundleContext bundleContext) {
        activate(bundleContext);
    }

    @Activate
    public void activate(BundleContext context) {
        connectionProperties.poolProvider = getOptionalProperty(context, CONNECTION_POOL_PROVIDER, ORACLE_CP);
        connectionProperties.jdbcUrl = getRequiredProperty(context, JDBC_DRIVER_URL);
        connectionProperties.jdbcUser = getRequiredProperty(context, JDBC_USER);
        connectionProperties.jdbcPassword = getRequiredProperty(context, JDBC_PASSWORD);
        connectionProperties.keyFile = getRequiredProperty(context, KEY_FILE);
        connectionProperties.maxLimit = getOptionalIntProperty(context, JDBC_POOLMAXLIMIT, Integer.parseInt(JDBC_POOLMAXLIMIT_DEFAULT));
        connectionProperties.maxStatementsLimit = getOptionalIntProperty(context, JDBC_POOLMAXSTATEMENTS, Integer.parseInt(JDBC_POOLMAXSTATEMENTS_DEFAULT));
        connectionProperties.onsNodes = getOptionalProperty(context, ORACLE_ONS_NODES, null);
        connectionProperties.connectionWaitTimeout = getOptionalIntProperty(context, JDBC_CONNECTION_WAIT_TIMEOUT, 30);
        connectionProperties.inactivityTimeout = getOptionalIntProperty(context, JDBC_INACTIVITY_TIMEOUT, 0);
        connectionProperties.abandonedConnectionsTimeout = getOptionalIntProperty(context, JDBC_ABANDONED_CONNECTION_TIMEOUT, 0);
        connectionProperties.timeToLive = getOptionalIntProperty(context, JDBC_TTL_TIMEOUT, 0);
        connectionProperties.maxConnectionReuseTime = getOptionalIntProperty(context, JDBC_MAX_CONNECTION_REUSE_TIME, 0);
    }

    @Deactivate
    public void deactivate() {
        if (initialized) {
            try {
                UniversalConnectionPoolManager manager = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
                manager.destroyConnectionPool(CONNECTION_POOL_NAME);
            } catch (UniversalConnectionPoolException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public DataSource createDataSource() {
        DataSource dataSource = cache.get();
        initialized = true;
        return dataSource;
    }

    private DataSource doCreateDataSource() {
        DataSource dataSource;
        try {
            dataSource = createDataSourceFromProperties();
        } catch (SQLException e) {
            // Basically this should never occur, since we're not accessing the DB in any way, just yet.
            throw new DataSourceSetupException(e);
        }
        return dataSource;
    }

    private DataSource createDataSourceFromProperties() throws SQLException {
        if (HIKARI_CP.equals(connectionProperties.poolProvider.trim().toLowerCase())) {
            return new HikariDataSourceProvider().createDataSource(connectionProperties);
        }
        return new OracleDataSourceProvider().createDataSource(connectionProperties);
    }

    private String getRequiredProperty(BundleContext context, String property) {
        String value = context.getProperty(property);
        if (value == null) {
            throw new PropertyNotFoundException(property);
        }
        return value.trim();
    }

    private String getOptionalProperty(BundleContext context, String property, String defaultValue) {
        String value = context.getProperty(property);
        return value == null ? defaultValue : value.trim();
    }

    private int getOptionalIntProperty(BundleContext context, String propertyName, int defaultValue) {
        String propertyValue = getOptionalProperty(context, propertyName, null);
        return propertyValue == null ? defaultValue : Integer.parseInt(propertyValue);
    }


    public void dbConnection() {
        StringBuilder sb = new StringBuilder("Connection settings :").append("\n");
        sb.append(" jdbcUrl = ").append(jdbcUrl).append("\n");
        sb.append(" dbUser = ").append(jdbcUser).append("\n");
        System.out.println(sb.toString());
    }

    private String getDecryptedPassword(String encryptedPassword, String filePath) {

        String decryptedPassword = "";
        List<String> list = null;
        try {
            list = Files.lines(Paths.get(filePath))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            //Logger.getAnonymousLogger().log(Level.SEVERE, exception, () -> "Bootstrap service initialization: encryption failed");
            Logger.getAnonymousLogger().log(Level.SEVERE, () -> "Cannot establish a connection to the database. Check the connection details.");
            throw new PropertyNotFoundException(KEY_FILE);
        }

        if (list.size() != 2) {
            Logger.getAnonymousLogger().log(Level.SEVERE, () -> "Cannot establish a connection to the database. Check the connection details.");
            throw new PropertyNotFoundException(KEY_FILE);
        } else {
            try {
                byte[] aesEncryptionKey = list.get(0).getBytes("UTF-8");
                String id = list.get(1);

                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(id.getBytes("UTF-8"));
                byte[] encryptedPasswordData = DatatypeConverter.parseBase64Binary(encryptedPassword);


                String initVector = new BigInteger(1, md5.digest()).toString(16).substring(0, 16);
                byte[] iv = initVector.getBytes("UTF-8");
                Cipher decrypt = Cipher.getInstance("AES/CBC/PKCS5Padding");
                decrypt.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesEncryptionKey, "AES"), new IvParameterSpec(iv));
                byte[] decryptedPasswordData = decrypt.doFinal(encryptedPasswordData);
                decryptedPassword = new String(decryptedPasswordData, "UTF-8");
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException |
                    InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException
                    | BadPaddingException | ArrayIndexOutOfBoundsException e) {
                InvalidPasswordException exception = new InvalidPasswordException();
                Logger.getAnonymousLogger().log(Level.SEVERE, () -> "Cannot establish a connection to the database. Check the connection details.");
                throw exception;
            }
        }
        return decryptedPassword;

    }
}