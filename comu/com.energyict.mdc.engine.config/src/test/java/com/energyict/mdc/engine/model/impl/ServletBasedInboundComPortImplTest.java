package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.Expected;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.PersistenceTest;
import com.energyict.mdc.engine.model.ServletBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import java.sql.SQLException;
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
    public void testCreateWithoutViolations() throws BusinessException, SQLException {
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
    @Expected(expected = TranslatableApplicationException.class, messageId = "XcannotBeEmpty")
    @Transactional
    public void testCreateWithoutName() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .portNumber(PORT_NUMBER)
                .contextPath(CONTEXT_PATH)
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Expected(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithoutComPortPool() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .name(COMPORT_NAME)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .portNumber(PORT_NUMBER)
                .contextPath(CONTEXT_PATH)
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expecting an TranslatableApplicationException to be thrown because the ComPortPool is not set
    }

    @Test
    @Expected(expected = TranslatableApplicationException.class, messageId = "duplicateComPortX")
    @Transactional
    public void testCreateWithExistingName() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        ServletBasedInboundComPort comPort = this.createSimpleComPort(onlineComServer);

        onlineComServer.newServletBasedInboundComPort()
                .name(COMPORT_NAME)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .portNumber(PORT_NUMBER)
                .contextPath(CONTEXT_PATH)
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expecting a BusinessException to be thrown because a ComPort with the same name already exists
    }

    @Test
    @Expected(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithZeroSimultaneousConnections() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .name(COMPORT_NAME)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .portNumber(PORT_NUMBER)
                .contextPath(CONTEXT_PATH)
                .numberOfSimultaneousConnections(0)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Expected(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithZeroPortNumber() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .name(COMPORT_NAME)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .portNumber(0)
                .contextPath(CONTEXT_PATH)
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Expected(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithoutContextPath() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .name(COMPORT_NAME)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .portNumber(PORT_NUMBER)
                .contextPath(null)
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test
    @Expected(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithEmptyContextPath() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .name(COMPORT_NAME)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .portNumber(PORT_NUMBER)
                .contextPath("")
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    /**
     * Tests that any KeyStore that is specified is ignored
     * when the https flag is NOT set.
     *
     * @throws BusinessException Indicates failure
     * @throws SQLException      Indicates failure
     */
    @Test
    @Transactional
    public void testCreateWithKeyStoreSpecsButNoHTTPS() throws BusinessException, SQLException {
        ServletBasedInboundComPort comPort = createOnlineComServer().newServletBasedInboundComPort()
                .https(false)
                .name(COMPORT_NAME)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .portNumber(PORT_NUMBER)
                .contextPath(CONTEXT_PATH)
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();


        // Asserts
        assertThat(comPort.getKeyStoreSpecsFilePath()).isNull();
        assertThat(comPort.getTrustStoreSpecsFilePath()).isNull();
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithOnlyKeyStore() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(null).trustStoreSpecsPassword(null).add();

        // Expected TranslatableApplicationException because trust store is not specified
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithoutKeyStoreFilePath() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(null).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expected TranslatableApplicationException because key store file path is not specified
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithEmptyKeyStoreFilePath() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath("").keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expected TranslatableApplicationException because key store file path is empty
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithoutKeyStorePassword() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(null)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expected TranslatableApplicationException because key store password is not specified
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithEmptyKeyStorePassword() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword("")
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expected TranslatableApplicationException because key store password is empty
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithoutTrustStoreFilePath() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(null).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expected TranslatableApplicationException because trust store file path is not specified
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithEmptyTrustStoreFilePath() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath("").trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expected TranslatableApplicationException because trust store file path is empty
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithoutTrustStorePassword() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(null).add();

        // Expected TranslatableApplicationException because the trust store password is not specified
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithEmptyTrustStorePassword() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword("").add();

        // Expected TranslatableApplicationException because the trust store password is empty
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithOnlyTrustStore() throws BusinessException, SQLException {
        createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(null).keyStoreSpecsPassword(null)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();

        // Expected TranslatableApplicationException because key store is not specified
    }

    @Test
    @Transactional
    public void testLoad() throws SQLException, BusinessException {
        ServletBasedInboundComPort comPort = createSimpleComPort();

        ServletBasedInboundComPort reloaded = (ServletBasedInboundComPort) getEngineModelService().findComPort(comPort.getId());

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
    public void updateWithoutViolations() throws BusinessException, SQLException {
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
        comPort.save();

        // Asserts
        assertEquals("Name does not match", newName, comPort.getName());
        assertEquals("Description does not match", newDescription, comPort.getDescription());
        assertEquals("Was NOT expecting the new com port to be active", newActive, comPort.isActive());
        assertEquals("Incorrect number of simultaneous connections", newNumberOfSimultaneousConnections, comPort.getNumberOfSimultaneousConnections());
        assertEquals("Incorrect PortNumber", newPortNumber, comPort.getPortNumber());
        assertEquals("Incorrect context path", updatedContextPath, comPort.getContextPath());
        assertFalse("Incorrect https flag", comPort.isHttps());
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void updateWithEmptyName() throws BusinessException, SQLException {
        ServletBasedInboundComPortImpl comPort = (ServletBasedInboundComPortImpl) this.createSimpleComPort();

        comPort.setName(null);

        // Business method
        comPort.save();

        // Expected BusinessException because name is null
    }

    @Test
    @Transactional
    public void testKeyStoreSpecsAreRemovedWhenHttpsIsSwitchedOff() throws SQLException, BusinessException {
        ServletBasedInboundComPort comPort = createSimpleComPort();
        comPort.setHttps(false);
        // Business method
        comPort.save();

        // Asserts
        assertThat(comPort.isHttps()).isFalse();
        assertThat(comPort.getKeyStoreSpecsFilePath()).isNull();
        assertThat(comPort.getTrustStoreSpecsFilePath()).isNull();
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testUpdateWithoutKeyStore() throws SQLException, BusinessException {
        ServletBasedInboundComPort comPort = createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();
        comPort.setKeyStoreSpecsFilePath(null);
        comPort.setKeyStoreSpecsPassword(null);

        // Business method
        comPort.save();

        // Expected TranslatableApplicationException because the key store is null
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testUpdateWithoutKeyStoreFilePath() throws SQLException, BusinessException {
        ServletBasedInboundComPort comPort = createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();
        comPort.setKeyStoreSpecsFilePath(null);

        // Business method
        comPort.save();

        // Expected TranslatableApplicationException because the key store file path is null
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testUpdateWithEmptyKeyStoreFilePath() throws SQLException, BusinessException {
        ServletBasedInboundComPort comPort = createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();
        comPort.setKeyStoreSpecsFilePath("");

        // Business method
        comPort.save();

        // Expected TranslatableApplicationException because the key store file path is empty
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testUpdateWithoutKeyStorePassword() throws SQLException, BusinessException {
        ServletBasedInboundComPort comPort = createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();
        comPort.setKeyStoreSpecsPassword(null);

        // Business method
        comPort.save();

        // Expected TranslatableApplicationException because the key store password is null
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testUpdateWithEmptyKeyStorePassword() throws SQLException, BusinessException {
        ServletBasedInboundComPort comPort = createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();
        comPort.setKeyStoreSpecsPassword("");

        // Business method
        comPort.save();

        // Expected TranslatableApplicationException because the key store password is empty
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testUpdateWithoutTrustStore() throws SQLException, BusinessException {
        ServletBasedInboundComPort comPort = createOnlineComServer().newServletBasedInboundComPort()
                .https(true)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH).keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH).trustStoreSpecsPassword(TRUST_STORE_PASSWORD).add();
        comPort.setTrustStoreSpecsFilePath(null);
        comPort.setTrustStoreSpecsPassword(null);

        // Business method
        comPort.save();

        // Expected TranslatableApplicationException because the trust store is null
    }

    private int comPortPoolIndex=1;
    private InboundComPortPool createComPortPool() {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool();
        inboundComPortPool.setComPortType(ComPortType.SERVLET);
        inboundComPortPool.setName("comPortPool"+comPortPoolIndex++);
        inboundComPortPool.setDiscoveryProtocolPluggableClassId(1);
        inboundComPortPool.save();
        return inboundComPortPool;
    }

    private ServletBasedInboundComPort createSimpleComPort() {
        return createSimpleComPort(createOnlineComServer());
    }

    private ServletBasedInboundComPort createSimpleComPort(ComServer comServer) {
        return comServer.newServletBasedInboundComPort()
                .name(COMPORT_NAME)
                .https(true)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .comPortPool(createComPortPool())
                .portNumber(PORT_NUMBER)
                .contextPath(CONTEXT_PATH)
                .numberOfSimultaneousConnections(NUMBER_OF_SIMULTANEOUS_CONNECTIONS)
                .keyStoreSpecsFilePath(KEY_STORE_FILE_PATH)
                .keyStoreSpecsPassword(KEY_STORE_PASSWORD)
                .trustStoreSpecsFilePath(TRUST_STORE_FILE_PATH)
                .trustStoreSpecsPassword(TRUST_STORE_PASSWORD)
                .add();
    }

    private int onlineNameNumber = 1;

    private OnlineComServer createOnlineComServer() {
        OnlineComServer onlineComServer = getEngineModelService().newOnlineComServerInstance();
        String name = "Online-" + onlineNameNumber++;
        onlineComServer.setName(name);
        onlineComServer.setActive(true);
        onlineComServer.setServerLogLevel(ComServer.LogLevel.ERROR);
        onlineComServer.setCommunicationLogLevel(ComServer.LogLevel.TRACE);
        onlineComServer.setChangesInterPollDelay(new TimeDuration(60));
        onlineComServer.setSchedulingInterPollDelay(new TimeDuration(90));
        onlineComServer.setStoreTaskQueueSize(1);
        onlineComServer.setStoreTaskThreadPriority(1);
        onlineComServer.setNumberOfStoreTaskThreads(1);
        onlineComServer.save();
        return onlineComServer;
    }
}