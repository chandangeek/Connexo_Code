package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link com.energyict.mdc.device.data.impl.LogBookImpl} component
 *
 * Copyrights EnergyICT
 * Date: 26/03/14
 * Time: 10:29
 */
public class LogBookImplTest extends PersistenceIntegrationTest{

    private static final ObisCode logBookObiscode = ObisCode.fromString("0.0.99.98.0.255");

    private DeviceConfiguration deviceConfigurationWithLogBooks;

    private LogBookType logBookType;

    private Device createSimpleDeviceWithLogBook() {
        Device device = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfigurationWithLogBooks, "DeviceName", "MyUniqueID");
        device.save();
        return device;
    }

    private DeviceConfiguration createDeviceConfigurationWithLogBookSpec() {
        logBookType = inMemoryPersistence.getMasterDataService().newLogBookType("DefaultTestLogBookType", logBookObiscode);
        logBookType.save();
        deviceType.addLogBookType(logBookType);
        DeviceType.DeviceConfigurationBuilder configWithLogBookSpec = deviceType.newConfiguration("ConfigurationWithLogBookSpec");
        configWithLogBookSpec.newLogBookSpec(logBookType);
        DeviceConfiguration deviceConfiguration = configWithLogBookSpec.add();
        deviceType.save();
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    @Before
    public void initBefore() {
        deviceConfigurationWithLogBooks = createDeviceConfigurationWithLogBookSpec();
    }

    @Test
    @Transactional
    public void createWithNoLogBooksTest() {
        Device deviceWithoutLogBooks = inMemoryPersistence.getDeviceDataService().newDevice(deviceConfiguration, "DeviceWithoutLogBooks", "mRID");
        deviceWithoutLogBooks.save();

        Device reloadedDevice = getReloadedDevice(deviceWithoutLogBooks);
        assertThat(reloadedDevice.getLogBooks()).isEmpty();
    }

    @Test
    @Transactional
    public void createWithoutViolationsTest() {
        Device deviceWithLogBooks = createSimpleDeviceWithLogBook();

        Device reloadedDevice = getReloadedDevice(deviceWithLogBooks);
        assertThat(reloadedDevice.getLogBooks()).hasSize(1);
        assertThat(reloadedDevice.getLogBooks().get(0).getDeviceObisCode()).isEqualTo(logBookObiscode);
    }

    private LogBook getReloadedLogBook(Device device){
        Device reloadedDevice = getReloadedDevice(device);
        return (LogBook) reloadedDevice.getLogBooks().get(0);
    }

    @Test
    @Transactional
    public void lastReadingEmptyOnCreationTest() {
        Device simpleDeviceWithLogBook = createSimpleDeviceWithLogBook();

        LogBook reloadedLogBook = getReloadedLogBook(simpleDeviceWithLogBook);
        assertThat(reloadedLogBook.getLastLogBook()).isNull();
    }

    private void tryToUpdateLastReading(Device simpleDeviceWithLogBook, Date newLastReading, LogBook reloadedLogBook) {
        LogBook.LogBookUpdater logBookUpdater = simpleDeviceWithLogBook.getLogBookUpdaterFor(reloadedLogBook);
        logBookUpdater.setLastLogBookIfLater(newLastReading);
        logBookUpdater.update();
    }

    @Test
    @Transactional
    public void updateLastReadingIfLastReadingWasNullTest() {
        Device simpleDeviceWithLogBook = createSimpleDeviceWithLogBook();
        Date newLastReading = new Date(123546);

        LogBook reloadedLogBook = getReloadedLogBook(simpleDeviceWithLogBook);
        tryToUpdateLastReading(simpleDeviceWithLogBook, newLastReading, reloadedLogBook);

        LogBook updatedLogBook = getReloadedLogBook(simpleDeviceWithLogBook);
        assertThat(updatedLogBook.getLastLogBook()).isEqualTo(newLastReading);
    }

    @Test
    @Transactional
    public void updateLastReadingIfLastReadingIsBeforeOldLastReadingTest() {
        Device simpleDeviceWithLogBook = createSimpleDeviceWithLogBook();
        Date originalLastReading = new Date(123546);
        Date newLastReading = new Date(1);

        LogBook reloadedLogBook = getReloadedLogBook(simpleDeviceWithLogBook);
        tryToUpdateLastReading(simpleDeviceWithLogBook, originalLastReading, reloadedLogBook);
        LogBook updatedLogBook = getReloadedLogBook(simpleDeviceWithLogBook);
        tryToUpdateLastReading(simpleDeviceWithLogBook, newLastReading, updatedLogBook);

        LogBook logBook = getReloadedLogBook(simpleDeviceWithLogBook);
        assertThat(logBook.getLastLogBook()).isEqualTo(originalLastReading);
    }

    @Test
    @Transactional
    public void updateLastReadingIfLastReadingIsAfterOldLastReadingTest() {
        Device simpleDeviceWithLogBook = createSimpleDeviceWithLogBook();
        Date originalLastReading = new Date(123546);
        Date newLastReading = new Date(999999);

        LogBook reloadedLogBook = getReloadedLogBook(simpleDeviceWithLogBook);
        tryToUpdateLastReading(simpleDeviceWithLogBook, originalLastReading, reloadedLogBook);
        LogBook updatedLogBook = getReloadedLogBook(simpleDeviceWithLogBook);
        tryToUpdateLastReading(simpleDeviceWithLogBook, newLastReading, updatedLogBook);

        LogBook logBook = getReloadedLogBook(simpleDeviceWithLogBook);
        assertThat(logBook.getLastLogBook()).isEqualTo(newLastReading);
    }

    @Test
    @Transactional
    public void verifyLogBookIsDeletedAfterDeviceIsDeletedTest() {
        Device simpleDeviceWithLogBook = createSimpleDeviceWithLogBook();

        Device reloadedDevice = getReloadedDevice(simpleDeviceWithLogBook);
        reloadedDevice.delete();

        assertThat(inMemoryPersistence.getDataModel().mapper(LogBook.class).find()).isEmpty();
    }
}
