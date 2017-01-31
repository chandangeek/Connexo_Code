/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.masterdata.LogBookType;

import java.time.Instant;
import java.util.Optional;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LogBookImplTest extends PersistenceIntegrationTest {

    private static final ObisCode logBookObiscode = ObisCode.fromString("0.0.99.98.0.255");

    private DeviceConfiguration deviceConfigurationWithLogBooks;

    private LogBookType logBookType;

    @BeforeClass
    public static void setup() {
        try (TransactionContext context = getTransactionService().getContext()) {
            deviceProtocolPluggableClass = inMemoryPersistence.getProtocolPluggableService().newDeviceProtocolPluggableClass("MyTestProtocol", TestProtocol.class.getName());
            deviceProtocolPluggableClass.save();
            context.commit();
        }
    }

    private Device createSimpleDeviceWithLogBook() {
        Device device = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfigurationWithLogBooks, "DeviceName", "MyUniqueID", Instant.now());
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
        deviceConfiguration.activate();
        return deviceConfiguration;
    }

    @Before
    public void initBefore() {
        deviceConfigurationWithLogBooks = createDeviceConfigurationWithLogBookSpec();
        IssueStatus wontFix = mock(IssueStatus.class);
        when(inMemoryPersistence.getIssueService().findStatus(IssueStatus.WONT_FIX)).thenReturn(Optional.of(wontFix));
    }

    @Test
    @Transactional
    public void createWithNoLogBooksTest() {
        Device deviceWithoutLogBooks = inMemoryPersistence.getDeviceService()
                .newDevice(deviceConfiguration, "DeviceWithoutLogBooks", "mRID", Instant.now());
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

    private LogBook getReloadedLogBook(Device device) {
        Device reloadedDevice = getReloadedDevice(device);
        return reloadedDevice.getLogBooks().get(0);
    }

    @Test
    @Transactional
    public void lastReadingEmptyOnCreationTest() {
        Device simpleDeviceWithLogBook = createSimpleDeviceWithLogBook();

        LogBook reloadedLogBook = getReloadedLogBook(simpleDeviceWithLogBook);
        assertThat(reloadedLogBook.getLastLogBook()).isNotNull();
        assertThat(reloadedLogBook.getLastLogBook().isPresent()).isFalse();
    }

    private void tryToUpdateLastLogBook(Device simpleDeviceWithLogBook, Instant newLastLogBook, LogBook reloadedLogBook) {
        LogBook.LogBookUpdater logBookUpdater = simpleDeviceWithLogBook.getLogBookUpdaterFor(reloadedLogBook);
        logBookUpdater.setLastLogBookIfLater(newLastLogBook);
        logBookUpdater.update();
    }

    @Test
    @Transactional
    public void updateLastLogBookIfLastLogBookWasNullTest() {
        Device simpleDeviceWithLogBook = createSimpleDeviceWithLogBook();
        Instant lastLogbook = Instant.ofEpochMilli(123546);

        LogBook reloadedLogBook = getReloadedLogBook(simpleDeviceWithLogBook);
        tryToUpdateLastLogBook(simpleDeviceWithLogBook, lastLogbook, reloadedLogBook);

        LogBook updatedLogBook = getReloadedLogBook(simpleDeviceWithLogBook);
        assertThat(updatedLogBook.getLastLogBook()).isNotNull();
        assertThat(updatedLogBook.getLastLogBook().isPresent()).isTrue();
        assertThat(updatedLogBook.getLastLogBook().get()).isEqualTo(lastLogbook);
    }

    @Test
    @Transactional
    public void updateLastLogBookIfLastLogBookIsBeforeOldLastReadingTest() {
        Device simpleDeviceWithLogBook = createSimpleDeviceWithLogBook();
        Instant originalLastReading = Instant.ofEpochMilli(123546);
        Instant newLastReading = Instant.ofEpochMilli(1);

        LogBook reloadedLogBook = getReloadedLogBook(simpleDeviceWithLogBook);
        tryToUpdateLastLogBook(simpleDeviceWithLogBook, originalLastReading, reloadedLogBook);
        LogBook updatedLogBook = getReloadedLogBook(simpleDeviceWithLogBook);
        tryToUpdateLastLogBook(simpleDeviceWithLogBook, newLastReading, updatedLogBook);

        LogBook logBook = getReloadedLogBook(simpleDeviceWithLogBook);
        assertThat(logBook.getLastLogBook()).isNotNull();
        assertThat(logBook.getLastLogBook().isPresent()).isTrue();
        assertThat(logBook.getLastLogBook().get()).isEqualTo(originalLastReading);
    }

    @Test
    @Transactional
    public void updateLastLogBookIfLastLogBookIsAfterOldLastLogBookTest() {
        Device simpleDeviceWithLogBook = createSimpleDeviceWithLogBook();
        Instant originalLastReading = Instant.ofEpochMilli(123546);
        Instant newLastReading = Instant.ofEpochMilli(999999);

        LogBook reloadedLogBook = getReloadedLogBook(simpleDeviceWithLogBook);
        tryToUpdateLastLogBook(simpleDeviceWithLogBook, originalLastReading, reloadedLogBook);
        LogBook updatedLogBook = getReloadedLogBook(simpleDeviceWithLogBook);
        tryToUpdateLastLogBook(simpleDeviceWithLogBook, newLastReading, updatedLogBook);

        LogBook logBook = getReloadedLogBook(simpleDeviceWithLogBook);
        assertThat(logBook.getLastLogBook()).isNotNull();
        assertThat(logBook.getLastLogBook().isPresent()).isTrue();
        assertThat(logBook.getLastLogBook().get()).isEqualTo(newLastReading);
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