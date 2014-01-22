package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.Transactional;
import com.energyict.mdc.TransactionalRule;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.PersistenceTest;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.api.channels.serial.BaudrateValue;
import com.energyict.mdc.protocol.api.channels.serial.FlowControl;
import com.energyict.mdc.protocol.api.channels.serial.NrOfDataBits;
import com.energyict.mdc.protocol.api.channels.serial.NrOfStopBits;
import com.energyict.mdc.protocol.api.channels.serial.Parities;
import com.energyict.protocols.mdc.channels.serial.SerialPortConfiguration;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.assertj.core.api.Fail.fail;

/**
 * Tests the {@link ModemBasedInboundComPortImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (16:01)
 */
public class ModemBasedInboundComPortImplTest extends PersistenceTest {

    private static final String COMPORT_NAME = "ModemBasedComPort";
    private static final String DESCRIPTION = "Description for new ComPort";
    private static final boolean ACTIVE = true;
    private static final int RING_COUNT = 11;
    private static final int MAXIMUM_NUMBER_OF_DIAL_ERRORS = 31;
    private static final BaudrateValue BAUD_RATE = BaudrateValue.BAUDRATE_9600;
    private static final NrOfDataBits NR_OF_DATABITS = NrOfDataBits.EIGHT;
    private static final NrOfStopBits NR_OF_STOPBITS = NrOfStopBits.ONE;
    private static final Parities PARITY = Parities.NONE;
    private static final FlowControl FlOW_CONTROL = FlowControl.NONE;
    private static final TimeDuration CONNECT_TIMEOUT = new TimeDuration(1, TimeDuration.MINUTES);
    private static final TimeDuration DELAY_AFTER_CONNECT = new TimeDuration(20, TimeDuration.SECONDS);
    private static final TimeDuration DELAY_BEFORE_SEND = new TimeDuration(500, TimeDuration.MILLISECONDS);
    private static final TimeDuration AT_COMMAND_TIMEOUT = new TimeDuration(5, TimeDuration.SECONDS);
    private static final BigDecimal AT_COMMAND_TRY = new BigDecimal(5);
    private static final List<String> MODEM_INIT_STRINGS = Arrays.asList("ATM0");
    private static final String ADDRESS_SELECTOR = "Selector";
    private static final String POST_DIAL_COMMANDS = "(D)(F)()W:+++)";

    @Mock
    DataModel dataModel;
    

    private TransactionContext context;

    /**
     * Create a simple {@link ModemBasedInboundComPortImpl}
     *
     * @return a {@link ModemBasedInboundComPortImpl}
     * @throws BusinessException if a logical Business Exception occurred
     * @throws SQLException      if a sql-related exception occurred
     */

    private SerialPortConfiguration getSerialPortConfiguration(String name) {
        return new SerialPortConfiguration(name, BAUD_RATE, NR_OF_DATABITS, NR_OF_STOPBITS, PARITY, FlOW_CONTROL);
    }

    @Test
    @Transactional
    public void testInbound() {
        ModemBasedInboundComPort comPort = createSimpleComPort();
        assertTrue("Modem based inbound com ports are expected to be INBOUND", comPort.isInbound());
    }

    @Test
    @Transactional
    public void testIsModemBased() {
        ModemBasedInboundComPort comPort = createSimpleComPort();
        assertTrue(comPort.isModemBased());
    }

    @Test
    @Transactional
    public void testIsNotTCPBased() {
        ModemBasedInboundComPort comPort = createSimpleComPort();
        assertFalse(comPort.isTCPBased());
    }

    @Test
    @Transactional
    public void testIsNotUDPBased() {
        ModemBasedInboundComPort comPort = createSimpleComPort();
        assertFalse(comPort.isUDPBased());
    }

    @Test
    @Transactional
    public void testIsNotServletBased() {
        ModemBasedInboundComPort comPort = createSimpleComPort();
        assertFalse(comPort.isServletBased());
    }

    @Test
    @Transactional
    public void testCreateWithoutViolations() throws BusinessException, SQLException {
        ModemBasedInboundComPort comPort = this.createSimpleComPort();

        // Asserts
        assertEquals("Name does not match", COMPORT_NAME, comPort.getName());
        assertEquals("Description does not match", DESCRIPTION, comPort.getDescription());
        assertTrue("Was expecting the new com port to be active", comPort.isActive());
        assertEquals("Ringcount does not match", RING_COUNT, comPort.getRingCount());
        assertEquals("Maximum number of dial errors does not match", MAXIMUM_NUMBER_OF_DIAL_ERRORS, comPort.getMaximumDialErrors());
        assertEquals("Connect timeout does not match", CONNECT_TIMEOUT, comPort.getConnectTimeout());
        assertEquals("Delay after connect does not match", DELAY_AFTER_CONNECT, comPort.getDelayAfterConnect());
        assertEquals("Delay before send does not match", DELAY_BEFORE_SEND, comPort.getDelayBeforeSend());
        assertEquals("At command timeout does not match", AT_COMMAND_TIMEOUT, comPort.getAtCommandTimeout());
        assertEquals("At command try does not match", AT_COMMAND_TRY, comPort.getAtCommandTry());
        assertEquals("Modem initialization strings do not match", MODEM_INIT_STRINGS, comPort.getModemInitStrings());
        assertEquals("Address selector does not match", ADDRESS_SELECTOR, comPort.getAddressSelector());
        assertEquals("Post dial commands does not match", POST_DIAL_COMMANDS, comPort.getPostDialCommands());
        assertEquals("Baud rate does not match", BAUD_RATE, comPort.getSerialPortConfiguration().getBaudrate());
        assertEquals("Nr of dataBits does not match", NR_OF_DATABITS, comPort.getSerialPortConfiguration().getNrOfDataBits());
        assertEquals("Nr of stopBits does not match", NR_OF_STOPBITS, comPort.getSerialPortConfiguration().getNrOfStopBits());
        assertEquals("Parity does not match", PARITY, comPort.getSerialPortConfiguration().getParity());
        assertEquals("Flow control does not match", FlOW_CONTROL, comPort.getSerialPortConfiguration().getFlowControl());
    }

    @Test
    @Transactional
    public void createWithoutModemInitStrings() throws BusinessException, SQLException {
        ModemBasedInboundComPort comPort = createOnlineComServer().newModemBasedInboundComport()
                .name(COMPORT_NAME)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .ringCount(RING_COUNT)
                .maximumDialErrors(MAXIMUM_NUMBER_OF_DIAL_ERRORS)
                .comPortType(ComPortType.SERIAL)
                .comPortPool(createComPortPool())
                .connectTimeout(CONNECT_TIMEOUT)
                .atCommandTimeout(AT_COMMAND_TIMEOUT)
                .atCommandTry(AT_COMMAND_TRY)
                .delayAfterConnect(DELAY_AFTER_CONNECT)
                .delayBeforeSend(DELAY_BEFORE_SEND)
                .addressSelector(ADDRESS_SELECTOR)
                .postDialCommands(POST_DIAL_COMMANDS)
                .serialPortConfiguration(getSerialPortConfiguration(COMPORT_NAME))
                .add();

        ModemBasedInboundComPort reloaded = (ModemBasedInboundComPort) getEngineModelService().findComPort(comPort.getId());
        // asserts
        assertThat(comPort.getModemInitStrings()).isEmpty();
        assertThat(reloaded.getModemInitStrings()).isEmpty();
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithoutComPortPool() throws BusinessException, SQLException {
        createOnlineComServer().newModemBasedInboundComport()
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .ringCount(RING_COUNT)
        .maximumDialErrors(MAXIMUM_NUMBER_OF_DIAL_ERRORS)
        .comPortType(ComPortType.SERIAL).add();

        // Expecting an InvalidValueException to be thrown because the ComPortPool is not set
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithoutName() throws BusinessException, SQLException {
        createOnlineComServer().newModemBasedInboundComport()
        .comPortPool(createComPortPool())
        .description(DESCRIPTION)
        .active(ACTIVE)
        .ringCount(RING_COUNT)
        .maximumDialErrors(MAXIMUM_NUMBER_OF_DIAL_ERRORS)
        .connectTimeout(CONNECT_TIMEOUT)
        .atCommandTimeout(AT_COMMAND_TIMEOUT)
        .atCommandTry(AT_COMMAND_TRY)
        .serialPortConfiguration(getDefaultSerialPortConfiguration(COMPORT_NAME))
        .comPortType(ComPortType.SERIAL).add();

        // Expecting a BusinessException to be thrown because the name is not set
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithExistingName() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        createSimpleComPort(onlineComServer);

        onlineComServer.newModemBasedInboundComport()
        .comPortPool(createComPortPool())
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .ringCount(RING_COUNT)
        .maximumDialErrors(MAXIMUM_NUMBER_OF_DIAL_ERRORS)
        .connectTimeout(CONNECT_TIMEOUT)
        .atCommandTimeout(AT_COMMAND_TIMEOUT)
        .atCommandTry(AT_COMMAND_TRY)
        .serialPortConfiguration(getDefaultSerialPortConfiguration(COMPORT_NAME))
        .comPortType(ComPortType.SERIAL).add();

        // Expecting a BusinessException to be thrown because a ComPort with the same name already exists
    }

    @Test
    @Transactional
    public void testCreateWithExistingButDeletedName() throws BusinessException, SQLException {
        OnlineComServer onlineComServer = createOnlineComServer();
        ModemBasedInboundComPort firstComPort = this.createSimpleComPort(onlineComServer);
        onlineComServer.removeComPort(firstComPort.getId());

        onlineComServer.newModemBasedInboundComport()
        .name(COMPORT_NAME)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .ringCount(RING_COUNT)
                .maximumDialErrors(MAXIMUM_NUMBER_OF_DIAL_ERRORS)
                .connectTimeout(CONNECT_TIMEOUT)
                .atCommandTimeout(AT_COMMAND_TIMEOUT)
                .atCommandTry(AT_COMMAND_TRY)
                .serialPortConfiguration(getDefaultSerialPortConfiguration(COMPORT_NAME))
                .comPortType(ComPortType.SERIAL)
                .comPortPool(createComPortPool()).add();

        // No BusinessException expected, because a new ComPort can have the same name as a deleted one.
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithZeroRingCount() throws BusinessException, SQLException {
        createOnlineComServer().newModemBasedInboundComport()
        .comPortPool(createComPortPool())
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .ringCount(0)
        .maximumDialErrors(MAXIMUM_NUMBER_OF_DIAL_ERRORS)
        .connectTimeout(CONNECT_TIMEOUT)
        .atCommandTimeout(AT_COMMAND_TIMEOUT)
        .atCommandTry(AT_COMMAND_TRY)
        .serialPortConfiguration(getDefaultSerialPortConfiguration(COMPORT_NAME))
        .comPortType(ComPortType.SERIAL).add();

        // Expecting a BusinessException to be thrown because a ComPort with the same name already exists
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithZeroMaximumNumberOfDialErrors() throws BusinessException, SQLException {
        createOnlineComServer().newModemBasedInboundComport()
        .comPortPool(createComPortPool())
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .ringCount(RING_COUNT)
        .maximumDialErrors(0)
        .connectTimeout(CONNECT_TIMEOUT)
        .atCommandTimeout(AT_COMMAND_TIMEOUT)
        .atCommandTry(AT_COMMAND_TRY)
        .serialPortConfiguration(getDefaultSerialPortConfiguration(COMPORT_NAME))
        .comPortType(ComPortType.SERIAL).add();

        // Expecting a BusinessException to be thrown because a ComPort with the same name already exists
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithoutConnectTimeout() throws BusinessException, SQLException {
        createOnlineComServer().newModemBasedInboundComport()
        .comPortPool(createComPortPool())
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .ringCount(RING_COUNT)
        .maximumDialErrors(3)
        .atCommandTimeout(AT_COMMAND_TIMEOUT)
        .atCommandTry(AT_COMMAND_TRY)
        .serialPortConfiguration(getDefaultSerialPortConfiguration(COMPORT_NAME))
        .comPortType(ComPortType.SERIAL)
        .connectTimeout(null).add();

        // Expecting a BusinessException to be thrown because the connect timeout is not specified
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithoutAtCommandTimeout() throws BusinessException, SQLException {
        createOnlineComServer().newModemBasedInboundComport()
        .comPortPool(createComPortPool())
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .ringCount(RING_COUNT)
        .maximumDialErrors(3)
        .connectTimeout(CONNECT_TIMEOUT)
        .atCommandTry(AT_COMMAND_TRY)
        .serialPortConfiguration(getDefaultSerialPortConfiguration(COMPORT_NAME))
        .comPortType(ComPortType.SERIAL)
        .atCommandTimeout(null).add();

        // Expecting a BusinessException to be thrown because the at command timeout is not specified
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithoutAtCommandTry() throws BusinessException, SQLException {
        createOnlineComServer().newModemBasedInboundComport()
        .comPortPool(createComPortPool())
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .ringCount(RING_COUNT)
        .maximumDialErrors(3)
        .connectTimeout(CONNECT_TIMEOUT)
        .atCommandTimeout(AT_COMMAND_TIMEOUT)
        .atCommandTry(null)
        .serialPortConfiguration(getDefaultSerialPortConfiguration(COMPORT_NAME))
        .comPortType(ComPortType.SERIAL).add();

        // Expecting a BusinessException to be thrown because the at command try is not specified
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void testCreateWithoutSerialPortConfiguration() throws BusinessException, SQLException {
        createOnlineComServer().newModemBasedInboundComport()
        .comPortPool(createComPortPool())
        .name(COMPORT_NAME)
        .description(DESCRIPTION)
        .active(ACTIVE)
        .ringCount(RING_COUNT)
        .maximumDialErrors(3)
        .connectTimeout(CONNECT_TIMEOUT)
        .atCommandTimeout(AT_COMMAND_TIMEOUT)
        .atCommandTry(AT_COMMAND_TRY)
        .comPortType(ComPortType.SERIAL)
        .serialPortConfiguration(null).add();

        // Expecting a BusinessException to be thrown because the at command try is not specified
    }

    @Test
    @Transactional
    public void testWithoutComPortType() throws BusinessException, SQLException {

        try {
            createOnlineComServer().newModemBasedInboundComport()
            .comPortPool(createComPortPool())
            .name(COMPORT_NAME)
            .description(DESCRIPTION)
            .active(ACTIVE)
            .ringCount(RING_COUNT)
            .maximumDialErrors(MAXIMUM_NUMBER_OF_DIAL_ERRORS)
            .connectTimeout(CONNECT_TIMEOUT)
            .delayAfterConnect(DELAY_AFTER_CONNECT)
            .delayBeforeSend(DELAY_BEFORE_SEND)
            .atCommandTimeout(AT_COMMAND_TIMEOUT)
            .atCommandTry(AT_COMMAND_TRY)
            .atModemInitStrings(MODEM_INIT_STRINGS)
            .addressSelector(ADDRESS_SELECTOR)
            .postDialCommands(POST_DIAL_COMMANDS)
            .serialPortConfiguration(getDefaultSerialPortConfiguration(COMPORT_NAME))
            .comPortType(null).add();
            failBecauseExceptionWasNotThrown(TranslatableApplicationException.class);
        } catch (TranslatableApplicationException e) {
            if(!e.getMessageId().equals("XcannotBeEmpty")){
                fail("Should have gotten an exception indicating that the comPortType should not be empty, but was " + e.getMessage());
            }
        }
    }

    @Test
    @Transactional
    public void testLoad() throws SQLException, BusinessException {
        ModemBasedInboundComPort comPort = this.createSimpleComPort();

        // Business method
        ModemBasedInboundComPort reloaded = (ModemBasedInboundComPort) getEngineModelService().findComPort(comPort.getId());

        // Asserts
        assertNotNull("Was expecting to find the entity that was just created", reloaded);
        assertEquals("Name does not match", comPort.getName(), reloaded.getName());
        assertEquals("Description does not match", comPort.getDescription(), reloaded.getDescription());
        assertEquals("Was NOT expecting the new com port to be active", comPort.isActive(), reloaded.isActive());
        assertEquals("RingCount does not match", comPort.getRingCount(), reloaded.getRingCount());
        assertEquals("Maximum number of dial errors does not match", comPort.getMaximumDialErrors(), reloaded.getMaximumDialErrors());
        assertEquals("Connect timeout does not match", CONNECT_TIMEOUT, reloaded.getConnectTimeout());
        assertEquals("Delay after connect does not match", DELAY_AFTER_CONNECT, reloaded.getDelayAfterConnect());
        assertEquals("Delay before send does not match", DELAY_BEFORE_SEND, reloaded.getDelayBeforeSend());
        assertEquals("At command timeout does not match", AT_COMMAND_TIMEOUT, reloaded.getAtCommandTimeout());
        assertEquals("At command try does not match", AT_COMMAND_TRY, reloaded.getAtCommandTry());
        assertEquals("Modem initialization strings do not match", MODEM_INIT_STRINGS, reloaded.getModemInitStrings());
        assertEquals("Address selector does not match", ADDRESS_SELECTOR, reloaded.getAddressSelector());
        assertEquals("Post dial commands does not match", POST_DIAL_COMMANDS, comPort.getPostDialCommands());
        assertEquals("Baud rate does nt match", BAUD_RATE, reloaded.getSerialPortConfiguration().getBaudrate());
        assertEquals("Nr of dataBits does nt match", NR_OF_DATABITS, reloaded.getSerialPortConfiguration().getNrOfDataBits());
        assertEquals("Nr of stopBits does nt match", NR_OF_STOPBITS, reloaded.getSerialPortConfiguration().getNrOfStopBits());
        assertEquals("Parity does nt match", PARITY, reloaded.getSerialPortConfiguration().getParity());
        assertEquals("Flow control does nt match", FlOW_CONTROL, reloaded.getSerialPortConfiguration().getFlowControl());
    }

    @Test
    @Transactional
    public void updateWithoutViolations() throws BusinessException, SQLException {
        final int newMaxNumOfDialErrors = 99;
        final int newRingCount = 512;
        final String newName = "NewComPortName";
        final String newDescription = "NewDescriptionForUpdatedModemBasedInboundComPortImpl";
        final boolean newActive = false;
        final BaudrateValue newBaudRate = BaudrateValue.BAUDRATE_1200;
        final NrOfDataBits newNrOfDataBits = NrOfDataBits.SEVEN;
        final NrOfStopBits newNrOfStopBits = NrOfStopBits.TWO;
        final Parities newParity = Parities.MARK;
        final FlowControl newFlowControl = FlowControl.DTRDSR;

        final TimeDuration newConnectTimeout = new TimeDuration(60);
        final TimeDuration newDelayAfterConnect = new TimeDuration(30);
        final TimeDuration newDelayBeforeSend = new TimeDuration(2);
        final TimeDuration newAtCommandTimeout = new TimeDuration(10);
        final BigDecimal newAtCommandTry = new BigDecimal(10);
        final List<String> newModemInitStrings = new ArrayList<String>() {{
            add("ATM1");
        }};
        final String newAddressSelector = "NewSelector";

        ModemBasedInboundComPortImpl comPort = (ModemBasedInboundComPortImpl) this.createSimpleComPort();

        comPort.setName(newName);
        comPort.setDescription(newDescription);
        comPort.setActive(newActive);
        comPort.setRingCount(newRingCount);
        comPort.setMaximumDialErrors(newMaxNumOfDialErrors);
        comPort.setConnectTimeout(newConnectTimeout);
        comPort.setDelayAfterConnect(newDelayAfterConnect);
        comPort.setDelayBeforeSend(newDelayBeforeSend);
        comPort.setAtCommandTimeout(newAtCommandTimeout);
        comPort.setAtCommandTry(newAtCommandTry);
        comPort.setModemInitStrings(newModemInitStrings);
        comPort.setAddressSelector(newAddressSelector);
        comPort.setSerialPortConfiguration(new SerialPortConfiguration(newName,
                newBaudRate,
                newNrOfDataBits,
                newNrOfStopBits,
                newParity,
                newFlowControl));
        comPort.save();

        ModemBasedInboundComPort updatedComPort = (ModemBasedInboundComPort) getEngineModelService().findComPort(comPort.getId());

        // Asserts
        assertNotNull(updatedComPort);
        assertEquals("Name does not match", newName, updatedComPort.getName());
        assertEquals("Description does not match", newDescription, updatedComPort.getDescription());
        assertEquals("Was NOT expecting the new com port to be active", newActive, updatedComPort.isActive());
        assertEquals("RingCount does not match", newRingCount, updatedComPort.getRingCount());
        assertEquals("Maximum number of dial errors does not match", newMaxNumOfDialErrors, updatedComPort.getMaximumDialErrors());
        assertEquals("Connect timeout does not match", newConnectTimeout, updatedComPort.getConnectTimeout());
        assertEquals("Delay after connect does not match", newDelayAfterConnect, updatedComPort.getDelayAfterConnect());
        assertEquals("Delay before send does not match", newDelayBeforeSend, updatedComPort.getDelayBeforeSend());
        assertEquals("At command timeout does not match", newAtCommandTimeout, updatedComPort.getAtCommandTimeout());
        assertEquals("At command try does not match", newAtCommandTry, updatedComPort.getAtCommandTry());
        assertEquals("Modem initialization strings do not match", newModemInitStrings, updatedComPort.getModemInitStrings());
        assertEquals("Address selector does not match", newAddressSelector, updatedComPort.getAddressSelector());
        assertEquals("Baud rate does nt match", newBaudRate, comPort.getSerialPortConfiguration().getBaudrate());
        assertEquals("Nr of dataBits does nt match", newNrOfDataBits, comPort.getSerialPortConfiguration().getNrOfDataBits());
        assertEquals("Nr of stopBits does nt match", newNrOfStopBits, comPort.getSerialPortConfiguration().getNrOfStopBits());
        assertEquals("Parity does nt match", newParity, comPort.getSerialPortConfiguration().getParity());
        assertEquals("Flow control does nt match", newFlowControl, comPort.getSerialPortConfiguration().getFlowControl());
    }

    @Test(expected = TranslatableApplicationException.class)
    @Transactional
    public void updateWithNullName() throws BusinessException, SQLException {
        ModemBasedInboundComPortImpl comPort = (ModemBasedInboundComPortImpl) this.createSimpleComPort();

        comPort.setName(null);

        comPort.save();

        // was expecting a BusinessException because the name is set to null
    }

    private SerialPortConfiguration getDefaultSerialPortConfiguration(String comPortName) {
        return new SerialPortConfiguration(comPortName,
                BAUD_RATE,
                NR_OF_DATABITS,
                NR_OF_STOPBITS,
                PARITY,
                FlOW_CONTROL);
    }
    
    private int onlineNameNumber=1; 
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
    
    private int comPortPoolId=1; 
    private InboundComPortPool createComPortPool() {
        InboundComPortPool inboundComPortPool = getEngineModelService().newInboundComPortPool();
        inboundComPortPool.setComPortType(ComPortType.SERIAL);
        inboundComPortPool.setName("comPortPool");
        inboundComPortPool.setDiscoveryProtocolPluggableClassId(1);
        inboundComPortPool.save();
        return inboundComPortPool;
    }

    private int comPortId=1;
    private ModemBasedInboundComPort createSimpleComPort() {
        return createSimpleComPort(createOnlineComServer());
    }

    private ModemBasedInboundComPort createSimpleComPort(ComServer comServer) {
        return comServer.newModemBasedInboundComport()
                .name(COMPORT_NAME)
                .description(DESCRIPTION)
                .active(ACTIVE)
                .ringCount(RING_COUNT)
                .maximumDialErrors(MAXIMUM_NUMBER_OF_DIAL_ERRORS)
                .comPortType(ComPortType.SERIAL)
                .comPortPool(createComPortPool())
                .connectTimeout(CONNECT_TIMEOUT)
                .atCommandTimeout(AT_COMMAND_TIMEOUT)
                .atCommandTry(AT_COMMAND_TRY)
                .delayAfterConnect(DELAY_AFTER_CONNECT)
                .delayBeforeSend(DELAY_BEFORE_SEND)
                .atModemInitStrings(MODEM_INIT_STRINGS)
                .addressSelector(ADDRESS_SELECTOR)
                .postDialCommands(POST_DIAL_COMMANDS)
                .serialPortConfiguration(getSerialPortConfiguration(COMPORT_NAME))
                .add();
    }

    
}