package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.PersistenceTest;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;

import java.sql.SQLException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ServletBasedInboundComPortImpl} component.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since 2012-10-11 (13:38)
 */
public class ServletBasedInboundComPortImplTest extends PersistenceTest {

    private static final String COMPORT_NAME = "ServletBasedComPort";
    private static final String DESCRIPTION = "Description for new Servlet Based Inbound ComPort";
    private static final boolean ACTIVE = true;
    private static final int NUMBER_OF_SIMULTANEOUS_CONNECTIONS = 11;
    private static final int PORT_NUMBER = 8080;
    private static final String CONTEXT_PATH = "/eiweb";
    private static final String KEY_STORE_FILE_PATH = "/tmp/key_store";
    private static final String TRUST_STORE_FILE_PATH = "/tmp/trust_store";
    private static final String STORE_PASSWORD = "couldnotkeepthisasecretanylonger";
    private static final String KEY_STORE_PASSWORD = STORE_PASSWORD;
    private static final String TRUST_STORE_PASSWORD = STORE_PASSWORD;

    @Override
    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    @Transactional
    public void testInbound() {
        ServletBasedInboundComPort comPort = createSimpleComPort();
        assertTrue("Servlet based inbound com ports are expected to be INBOUND", comPort.isInbound());
    }

    @Test
    @Transactional
    public void testIsServletBased() {
        ServletBasedInboundComPort comPort = createSimpleComPort();
        assertThat(comPort.isServletBased()).isTrue();
    }

    @Test
    @Transactional
    public void testIsNotTCP() {
        ServletBasedInboundComPort comPort = createSimpleComPort();
        assertThat(comPort.isTCPBased()).isFalse();
    }

    @Test
    @Transactional
    public void testIsNotUDP() {
        ServletBasedInboundComPort comPort = createSimpleComPort();
        assertThat(comPort.isUDPBased()).isFalse();
    }

    @Test
    @Transactional
    public void testIsNotModem() {
        ServletBasedInboundComPort comPort = createSimpleComPort();
        assertThat(comPort.isModemBased()).isFalse();
    }

    @Test
    @Transactional
    public void testCreateWithoutViolations() throws SQLException {
        ServletBasedInboundComPort comPort = this.createSimpleComPort();

        // Asserts
        assertEquals("Name does not match", COMPORT_NAME, comPort.getName());
        assertEquals("Description does not match", DESCRIPTION, comPort.getDescription());
        assertTrue("Was expecting the new com port to be active", comPort.isActive());
        assertEquals("Incorrect number of simultaneous connections", NUMBER_OF_SIMULTANEOUS_CONNECTIONS, comPort.getNumberOfSimultaneousConnections());
        assertThat(comPort.getPortNumber()).describedAs("Incorrect PortNumber").isEqualTo(PORT_NUMBER);
        assertEquals("Incorrect context path", CONTEXT_PATH, comPort.getContextPath());
        assertTrue("Incorrect https flag", comPort.isHttps());
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    @Transactional
    public void testCreateWithoutName() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(null, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // See expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_ACTIVE_INBOUND_COMPORT_MUST_HAVE_POOL+"}", property = "comPortPool")
    @Transactional
    public void testCreateWithoutComPortPool() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expecting an TranslatableApplicationException to be thrown because the ComPortPool is not set
    }

    @Test
    @Transactional
    public void testCreateInActivePortWithoutComPortPool() throws SQLException {
        ServletBasedInboundComPort servletBasedInboundComPort = createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .https(true)
                .description(DESCRIPTION)
                .active(false)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();
        assertThat(servletBasedInboundComPort.isActive()).isFalse();
        assertThat(servletBasedInboundComPort.getComPortPool()).isNull();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_ACTIVE_INBOUND_COMPORT_MUST_HAVE_POOL+"}")
    public void setPortActiveWithoutComPortPoolTest() {
        ServletBasedInboundComPort servletBasedInboundComPort = createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(false)
                .add();

        servletBasedInboundComPort.setActive(true);
        servletBasedInboundComPort.update();
    }

    @Test
    @Transactional
    public void setPortActiveWithComPortPoolTest() {
        ServletBasedInboundComPort servletBasedInboundComPort = createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(false)
                .add();

        InboundComPortPool comPortPool = createComPortPool();
        servletBasedInboundComPort.setComPortPool(comPortPool);
        servletBasedInboundComPort.setActive(true);
        servletBasedInboundComPort.update();

        assertThat(servletBasedInboundComPort.isActive()).isTrue();
        assertThat(servletBasedInboundComPort.getComPortPool()).isEqualTo(comPortPool);
    }
    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_PORT+"}", property = "name")
    @Transactional
    public void testCreateWithExistingName() throws SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        ServletBasedInboundComPort comPort = this.createSimpleComPort(onlineComServer);

        onlineComServer.newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER+1)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // See expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "numberOfSimultaneousConnections")
    @Transactional
    public void testCreateWithZeroSimultaneousConnections() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, 0, PORT_NUMBER)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // See expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_VALUE_TOO_SMALL+"}", property = "portNumber")
    @Transactional
    public void testCreateWithZeroPortNumber() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, 0)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // See expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "contextPath")
    @Transactional
    public void testCreateWithoutContextPath() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, null, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // See expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "contextPath")
    @Transactional
    public void testCreateWithEmptyContextPath() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, "", NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // See expected constraint violation rule
    }

    /**
     * Tests that any KeyStore that is specified is ignored
     * when the https flag is NOT set.
     *
     * @throws SQLException      Indicates failure
     */
    @Test
    @Transactional
    public void testCreateWithKeyStoreSpecsButNoHTTPS() throws SQLException {
        ServletBasedInboundComPort comPort = createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .https(false)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();


        // Asserts
        assertThat(comPort.getKeyStoreSpecsFilePath()).isNull();
        assertThat(comPort.getTrustStoreSpecsFilePath()).isNull();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "trustStoreSpecsFilePath", strict = false)
    @Transactional
    public void testCreateWithOnlyKeyStore() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(null).trustStoreSpecsPassword(null).add();

        // Expected TranslatableApplicationException because trust store is not specified
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "keyStoreSpecsFilePath")
    @Transactional
    public void testCreateWithoutKeyStoreFilePath() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .https(true)
                .keyStoreSpecsFilePath(null).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expected TranslatableApplicationException because key store file path is not specified
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "keyStoreSpecsFilePath")
    @Transactional
    public void testCreateWithEmptyKeyStoreFilePath() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .https(true)
                .keyStoreSpecsFilePath("").keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expected TranslatableApplicationException because key store file path is empty
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "keyStoreSpecsPassword")
    @Transactional
    public void testCreateWithoutKeyStorePassword() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(null)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expected TranslatableApplicationException because key store password is not specified
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "keyStoreSpecsPassword")
    @Transactional
    public void testCreateWithEmptyKeyStorePassword() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword("")
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expected TranslatableApplicationException because key store password is empty
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "trustStoreSpecsFilePath")
    @Transactional
    public void testCreateWithoutTrustStoreFilePath() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(null).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expected TranslatableApplicationException because trust store file path is not specified
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "trustStoreSpecsFilePath")
    @Transactional
    public void testCreateWithEmptyTrustStoreFilePath() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath("").trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expected TranslatableApplicationException because trust store file path is empty
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "trustStoreSpecsPassword")
    @Transactional
    public void testCreateWithoutTrustStorePassword() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(null).add();

        // Expected TranslatableApplicationException because the trust store password is not specified
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "trustStoreSpecsPassword")
    @Transactional
    public void testCreateWithEmptyTrustStorePassword() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword("").add();

        // Expected TranslatableApplicationException because the trust store password is empty
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "keyStoreSpecsFilePath", strict = false)
    @Transactional
    public void testCreateWithOnlyTrustStore() throws SQLException {
        createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .https(true)
                .keyStoreSpecsFilePath(null).keyStoreSpecsPassword(null)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expected TranslatableApplicationException because key store is not specified
    }

    @Test
    @Transactional
    public void testLoad() throws SQLException {
        ServletBasedInboundComPort comPort = createSimpleComPort();

        ServletBasedInboundComPort reloaded = (ServletBasedInboundComPort) getEngineModelService().findComPort(comPort.getId()).get();

        // Asserts
        assertThat(reloaded).isNotNull();
        assertEquals("Name does not match", comPort.getName(), reloaded.getName());
        assertEquals("Description does not match", comPort.getDescription(), reloaded.getDescription());
        assertEquals("Was NOT expecting the new com port to be active", comPort.isActive(), reloaded.isActive());
        assertEquals("Incorrect number of simultaneous connections", comPort.getNumberOfSimultaneousConnections(), reloaded.getNumberOfSimultaneousConnections());
        assertEquals("Incorrect PortNumber", comPort.getPortNumber(), reloaded.getPortNumber());
        assertThat(reloaded.isHttps()).isTrue();
        assertThat(reloaded.getKeyStoreSpecsFilePath()).isEqualTo(KEY_STORE_FILE_PATH);
        assertThat(reloaded.getKeyStoreSpecsPassword()).isEqualTo(KEY_STORE_PASSWORD);
        assertThat(reloaded.getTrustStoreSpecsFilePath()).isEqualTo(TRUST_STORE_FILE_PATH);
        assertThat(reloaded.getTrustStoreSpecsPassword()).isEqualTo(TRUST_STORE_PASSWORD);
        assertEquals("Incorrect context path", comPort.getContextPath(), reloaded.getContextPath());
    }

    @Test
    @Transactional
    public void updateWithoutViolations() throws SQLException {
        final int newNumberOfSimultaneousConnections = 99;
        final int newPortNumber = 512;
        final String newName = "NewComPortName";
        final String newDescription = "NewDescriptionForUpdatedModemBasedInboundComPortImpl";
        final boolean newActive = false;
        ServletBasedInboundComPortImpl comPort = (ServletBasedInboundComPortImpl) this.createSimpleComPort();
        comPort.setName(newName);
        comPort.setDescription(newDescription);
        comPort.setActive(newActive);
        comPort.setPortNumber(newPortNumber);
        String updatedContextPath = "/eiweb/updated";
        comPort.setContextPath(updatedContextPath);
        comPort.setHttps(false);
        comPort.setNumberOfSimultaneousConnections(newNumberOfSimultaneousConnections);

        // Business method
        comPort.update();

        // Asserts
        assertEquals("Name does not match", newName, comPort.getName());
        assertEquals("Description does not match", newDescription, comPort.getDescription());
        assertEquals("Was NOT expecting the new com port to be active", newActive, comPort.isActive());
        assertEquals("Incorrect number of simultaneous connections", newNumberOfSimultaneousConnections, comPort.getNumberOfSimultaneousConnections());
        assertEquals("Incorrect PortNumber", newPortNumber, comPort.getPortNumber());
        assertEquals("Incorrect context path", updatedContextPath, comPort.getContextPath());
        assertFalse("Incorrect https flag", comPort.isHttps());
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}", property = "name")
    @Transactional
    public void updateWithEmptyName() throws SQLException {
        ServletBasedInboundComPortImpl comPort = (ServletBasedInboundComPortImpl) this.createSimpleComPort();

        comPort.setName(null);

        // Business method
        comPort.update();

        // See expected constraint violation rule
    }

    @Test
    @Transactional
    public void testKeyStoreSpecsAreRemovedWhenHttpsIsSwitchedOff() throws SQLException {
        ServletBasedInboundComPort comPort = createSimpleComPort();
        comPort.setHttps(false);
        // Business method
        comPort.update();

        // Asserts
        assertThat(comPort.isHttps()).isFalse();
        assertThat(comPort.getKeyStoreSpecsFilePath()).isNull();
        assertThat(comPort.getTrustStoreSpecsFilePath()).isNull();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "keyStoreSpecsFilePath", strict=false)
    @Transactional
    public void testUpdateWithoutKeyStore() throws SQLException {
        ServletBasedInboundComPort comPort = createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();
        comPort.setKeyStoreSpecsFilePath(null);
        comPort.setKeyStoreSpecsPassword(null);

        // Business method
        comPort.update();

        // Expected TranslatableApplicationException because the key store is null
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "keyStoreSpecsFilePath")
    @Transactional
    public void testUpdateWithoutKeyStoreFilePath() throws SQLException {
        ServletBasedInboundComPort comPort = createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();
        comPort.setKeyStoreSpecsFilePath(null);

        // Business method
        comPort.update();

        // Expected TranslatableApplicationException because the key store file path is null
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "keyStoreSpecsFilePath")
    @Transactional
    public void testUpdateWithEmptyKeyStoreFilePath() throws SQLException {
        ServletBasedInboundComPort comPort = createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();
        comPort.setKeyStoreSpecsFilePath("");

        // Business method
        comPort.update();

        // Expected TranslatableApplicationException because the key store file path is empty
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "keyStoreSpecsPassword")
    @Transactional
    public void testUpdateWithoutKeyStorePassword() throws SQLException {
        ServletBasedInboundComPort comPort = createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();
        comPort.setKeyStoreSpecsPassword(null);

        // Business method
        comPort.update();

        // Expected TranslatableApplicationException because the key store password is null
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "keyStoreSpecsPassword")
    @Transactional
    public void testUpdateWithEmptyKeyStorePassword() throws SQLException {
        ServletBasedInboundComPort comPort = createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();
        comPort.setKeyStoreSpecsPassword("");

        // Business method
        comPort.update();

        // Expected TranslatableApplicationException because the key store password is empty
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY_IF_HTTPS+"}", property = "trustStoreSpecsFilePath", strict = false)
    @Transactional
    public void testUpdateWithoutTrustStore() throws SQLException {
        ServletBasedInboundComPort comPort = createOnlineComServer().newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();
        comPort.setTrustStoreSpecsFilePath(null);
        comPort.setTrustStoreSpecsPassword(null);

        // Business method
        comPort.update();

        // Expected TranslatableApplicationException because the trust store is null
    }

    private int comPortPoolIndex=1;
    private InboundComPortPool createComPortPool() {
        return getEngineModelService().newInboundComPortPool("comPortPool"+comPortPoolIndex++, ComPortType.SERVLET, inboundDeviceProtocolPluggableClass, Collections.emptyMap());
    }

    private ServletBasedInboundComPort createSimpleComPort() {
        return createSimpleComPort(createOnlineComServer());
    }

    private ServletBasedInboundComPort createSimpleComPort(ComServer comServer) {
        return comServer.newServletBasedInboundComPort(COMPORT_NAME, CONTEXT_PATH, NUMBER_OF_SIMULTANEOUS_CONNECTIONS, PORT_NUMBER)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD)
                .add();
    }

    private int onlineNameNumber = 1;

    private OnlineComServer createOnlineComServer() {
        OnlineComServer.OnlineComServerBuilder<? extends OnlineComServer> onlineComServerBuilder = getEngineModelService().newOnlineComServerBuilder();
        String name = "Online-" + onlineNameNumber++;
        onlineComServerBuilder.name(name);
        onlineComServerBuilder.active(true);
        onlineComServerBuilder.serverLogLevel(ComServer.LogLevel.ERROR);
        onlineComServerBuilder.communicationLogLevel(ComServer.LogLevel.TRACE);
        onlineComServerBuilder.changesInterPollDelay(new TimeDuration(60));
        onlineComServerBuilder.schedulingInterPollDelay(new TimeDuration(90));
        onlineComServerBuilder.storeTaskQueueSize(1);
        onlineComServerBuilder.storeTaskThreadPriority(1);
        onlineComServerBuilder.numberOfStoreTaskThreads(1);
        onlineComServerBuilder.serverName(name);
        onlineComServerBuilder.eventRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
        return onlineComServerBuilder.create();
    }
}