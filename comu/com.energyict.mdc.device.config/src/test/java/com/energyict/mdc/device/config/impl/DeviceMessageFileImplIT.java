/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.orm.UnderlyingIOException;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageFile;
import com.energyict.mdc.device.config.DeviceMessageUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.MinimalDeviceTypeInMemoryBootstrapModule;
import com.energyict.mdc.device.config.exceptions.DuplicateDeviceMessageFileException;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.google.common.base.Strings;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration testing for the {@link DeviceMessageFileImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-11 (15:19)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceMessageFileImplIT {

    private static final long DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID = 139;

    private static MinimalDeviceTypeInMemoryBootstrapModule inMemoryBootstrapModule = new MinimalDeviceTypeInMemoryBootstrapModule();

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();

    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceProtocol deviceProtocol;
    @Mock
    private FileSystem fileSystem;
    @Mock
    private FileSystemProvider fileSystemProvider;
    @Mock
    private Path path;
    @Mock
    private Path filePart;
    @Mock
    private InputStream pathIS;

    private ProtocolPluggableService protocolPluggableService;

    @BeforeClass
    public static void setUp() {
        inMemoryBootstrapModule.activate();
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            inMemoryBootstrapModule.getDeviceConfigurationService();
            context.commit();
        }
    }

    @AfterClass
    public static void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Before
    public void initializeMocks() throws IOException {
        when(this.deviceProtocol.getDeviceProtocolCapabilities()).thenReturn(Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_MASTER));
        when(this.deviceProtocolPluggableClass.getId()).thenReturn(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID);
        when(this.deviceProtocolPluggableClass.getDeviceProtocol()).thenReturn(deviceProtocol);
        this.protocolPluggableService = inMemoryBootstrapModule.getProtocolPluggableService();
        when(this.protocolPluggableService
                .findDeviceProtocolPluggableClass(DEVICE_PROTOCOL_PLUGGABLE_CLASS_ID))
            .thenReturn(Optional.of(this.deviceProtocolPluggableClass));
        when(this.deviceProtocolPluggableClass.supportsFileManagement()).thenReturn(true);
        this.initializeFileRelatedMocks();
    }

    private void initializeFileRelatedMocks() throws IOException {
        when(this.fileSystem.provider()).thenReturn(this.fileSystemProvider);
        when(this.filePart.toString()).thenReturn(DeviceMessageFileImplIT.class.getSimpleName());
        when(this.path.getFileSystem()).thenReturn(this.fileSystem);
        when(this.path.getFileName()).thenReturn(this.filePart);
        when(this.pathIS.available()).thenReturn(0);
        when(this.pathIS.read()).thenReturn(-1);
        when(this.pathIS.read(any(byte[].class))).thenReturn(-1);
        when(this.pathIS.read(any(byte[].class), anyInt(), anyInt())).thenReturn(-1);
        when(this.fileSystemProvider.newInputStream(this.path)).thenReturn(this.pathIS);
    }

    @Test
    @Transactional
    public void addWithoutViolations() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        long version = deviceType.getVersion();

        // Business method
        DeviceMessageFile deviceMessageFile = deviceType.addDeviceMessageFile(this.path);

        // Asserts
        assertThat(deviceMessageFile).isNotNull();
        assertThat(deviceMessageFile.getId()).isGreaterThan(0);
        assertThat(deviceMessageFile.getName()).isEqualTo(DeviceMessageFileImplIT.class.getSimpleName());
        assertThat(deviceType.getVersion()).isGreaterThan(version);
        DeviceType reloaded = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        assertThat(reloaded.getVersion()).isGreaterThan(version);
    }

    @Test
    @Transactional
    public void addWithoutViolationsFromPath() throws IOException {
        String expectedContents = "addWithoutViolationsFromPath";
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        FileSystem jimfs = Jimfs.newFileSystem(Configuration.unix());

        Path path = jimfs.getPath("/temp.txt");
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(path))) {
            writer.write(expectedContents);
        }

        // Business method
        DeviceMessageFile deviceMessageFile = deviceType.addDeviceMessageFile(path);

        // Asserts
        assertThat(deviceMessageFile.getId()).isGreaterThan(0);
        assertThat(deviceMessageFile.getName()).isEqualTo("temp.txt");
        DeviceMessageFileContentReader reader = new DeviceMessageFileContentReader();
        deviceMessageFile.readWith(reader);
        assertThat(reader.whatWasRead()).isEqualTo(expectedContents);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", property = "name")
    @Transactional
    public void addWithEmptyFileName() throws IOException {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        when(this.filePart.toString()).thenReturn("");

        // Business method
        deviceType.addDeviceMessageFile(path);

        // Asserts: see expected constraint violation rule
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.MAX_FILE_SIZE_EXCEEDED + "}")
    @Transactional
    public void addWithFileThatIsTooLarge() throws IOException {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        FileSystem jimfs = Jimfs.newFileSystem(Configuration.unix());

        Path path = jimfs.getPath("/too-big.txt");
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(path))) {
            writer.write(Strings.repeat("7", DeviceConfigurationService.MAX_DEVICE_MESSAGE_FILE_SIZE_BYTES + 10));
        }

        // Business method
        deviceType.addDeviceMessageFile(path);

        // Asserts: see expected constraint violation rule
    }

    @Test(expected = DuplicateDeviceMessageFileException.class)
    @Transactional
    public void addDuplicateName() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        deviceType.addDeviceMessageFile(this.path);

        // Business method
        deviceType.addDeviceMessageFile(this.path);

        // Asserts: see expected exception rule
    }

    @Test
    @Transactional
    public void getDeviceMessageFilesWhenNoneCreated() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceType deviceType = inMemoryBootstrapModule
                .getDeviceConfigurationService()
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();

        // Business method
        List<DeviceMessageFile> deviceMessageFiles = deviceType.getDeviceMessageFiles();

        // Asserts
        assertThat(deviceMessageFiles).isEmpty();
    }

    @Test
    @Transactional
    public void getDeviceMessageFilesWhenOnlyOneCreated() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceType deviceType = inMemoryBootstrapModule
                .getDeviceConfigurationService()
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        deviceType.addDeviceMessageFile(this.path);

        // Business method
        List<DeviceMessageFile> deviceMessageFiles = deviceType.getDeviceMessageFiles();

        // Asserts
        assertThat(deviceMessageFiles).hasSize(1);
    }

    @Test
    @Transactional
    public void getDeviceMessageFilesAfterReloadWhenOnlyOneCreated() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        deviceType.addDeviceMessageFile(this.path);
        DeviceType reloaded = deviceConfigurationService.findDeviceType(deviceType.getId()).get();

        // Business method
        List<DeviceMessageFile> deviceMessageFiles = reloaded.getDeviceMessageFiles();

        // Asserts
        assertThat(deviceMessageFiles).hasSize(1);
    }

    @Test
    @Transactional
    public void getDeviceMessageFilesWhenMultipleCreated() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceType deviceType = inMemoryBootstrapModule
                .getDeviceConfigurationService()
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        when(this.filePart.toString()).thenReturn("First");
        deviceType.addDeviceMessageFile(this.path);
        when(this.filePart.toString()).thenReturn("Second");
        deviceType.addDeviceMessageFile(this.path);

        // Business method
        List<DeviceMessageFile> deviceMessageFiles = deviceType.getDeviceMessageFiles();

        // Asserts
        assertThat(deviceMessageFiles).hasSize(2);
    }

    @Test
    @Transactional
    public void getDeviceMessageFilesAfterReloadWhenMultipleCreated() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        when(this.filePart.toString()).thenReturn("First");
        deviceType.addDeviceMessageFile(this.path);
        when(this.filePart.toString()).thenReturn("Second");
        deviceType.addDeviceMessageFile(this.path);
        DeviceType reloaded = deviceConfigurationService.findDeviceType(deviceType.getId()).get();

        // Business method
        List<DeviceMessageFile> deviceMessageFiles = reloaded.getDeviceMessageFiles();

        // Asserts
        assertThat(deviceMessageFiles).hasSize(2);
    }

    @Test
    @Transactional
    @Ignore
    //Ignoring as clearing blob content isn't allowed by H2. Waiting for that feature to be implemented
    public void removeDeviceMessageFileIncrementsVersion() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        DeviceMessageFile deviceMessageFile = deviceType.addDeviceMessageFile(this.path);
        long version = deviceType.getVersion();

        // Business method
        deviceType.removeDeviceMessageFile(deviceMessageFile);

        // Asserts
        DeviceType reloaded = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        assertThat(deviceType.getVersion()).isGreaterThan(version);
        assertThat(reloaded.getVersion()).isGreaterThan(version);
    }

    @Test
    @Transactional
    public void removeDeviceMessageFileDoesNotIncrementsVersionWhenNoneExist() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        long version = deviceType.getVersion();

        // Business method
        deviceType.removeDeviceMessageFile(mock(ServerDeviceMessageFile.class));

        // Asserts
        assertThat(deviceType.getVersion()).isEqualTo(version);
        assertThat(deviceType.getDeviceMessageFiles()).isEmpty();
        DeviceType reloaded = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        assertThat(reloaded.getVersion()).isEqualTo(version);
        assertThat(reloaded.getDeviceMessageFiles()).isEmpty();
    }

    @Test
    @Transactional
    public void removeDeviceMessageFileDoesNotIncrementsVersionWhenFileIsNotPartOfDeviceType() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        deviceType.addDeviceMessageFile(this.path);
        long version = deviceType.getVersion();

        // Business method
        deviceType.removeDeviceMessageFile(mock(ServerDeviceMessageFile.class));

        // Asserts
        assertThat(deviceType.getDeviceMessageFiles()).isNotEmpty();
        assertThat(deviceType.getVersion()).isEqualTo(version);
        DeviceType reloaded = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        assertThat(reloaded.getDeviceMessageFiles()).isNotEmpty();
        assertThat(reloaded.getVersion()).isEqualTo(version);
    }

    @Test
    @Transactional
    @Ignore
    //Ignoring as clearing blob content isn't allowed by H2. Waiting for that feature to be implemented
    public void removeDeviceMessageFileRelyingOnEquals() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        deviceType.addDeviceMessageFile(this.path);
        long version = deviceType.getVersion();
        DeviceType reloaded = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        DeviceMessageFile reloadedDeviceMessageFile = reloaded.getDeviceMessageFiles().get(0);

        // Business method
        deviceType.removeDeviceMessageFile(reloadedDeviceMessageFile);

        // Asserts
        assertThat(deviceType.getDeviceMessageFiles()).isEmpty();
        assertThat(deviceType.getVersion()).isGreaterThan(version);
        DeviceType reloadedAgain = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        assertThat(reloadedAgain.getDeviceMessageFiles()).isEmpty();
        assertThat(reloadedAgain.getVersion()).isGreaterThan(version);
    }

    @Test
    @Transactional
    @Ignore
    //Ignoring as clearing blob content isn't allowed by H2. Waiting for that feature to be implemented
    public void removeDeviceMessageFilesWithMultipleCreated() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        when(this.filePart.toString()).thenReturn("First");
        DeviceMessageFile first = deviceType.addDeviceMessageFile(this.path);
        when(this.filePart.toString()).thenReturn("Second");
        DeviceMessageFile second = deviceType.addDeviceMessageFile(this.path);

        // Business method
        deviceType.removeDeviceMessageFile(second);

        // Asserts
        assertThat(deviceType.getDeviceMessageFiles()).hasSize(1);
        DeviceType reloaded = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        assertThat(reloaded.getDeviceMessageFiles()).hasSize(1);
        assertThat(reloaded.getDeviceMessageFiles().get(0)).isEqualTo(first);
    }

    @Test
    @Transactional
    public void enableFileManagementDoesNotIncrementVersionWhenAlreadyEnabled() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        assertThat(deviceType.isFileManagementEnabled()).isTrue();
        long version = deviceType.getVersion();

        // Business method
        deviceType.enableFileManagement();

        // Asserts
        assertThat(deviceType.isFileManagementEnabled()).isTrue();
        assertThat(deviceType.getVersion()).isEqualTo(version);
        DeviceType reloaded = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        assertThat(reloaded.isFileManagementEnabled()).isTrue();
        assertThat(reloaded.getVersion()).isEqualTo(version);
    }

    @Test
    @Transactional
    public void disableFileManagementDoesNotIncrementVersionWhenAlreadyDisabled() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .create();
        assertThat(deviceType.isFileManagementEnabled()).isFalse();
        long version = deviceType.getVersion();

        // Business method
        deviceType.disableFileManagement();

        // Asserts
        assertThat(deviceType.isFileManagementEnabled()).isFalse();
        assertThat(deviceType.getVersion()).isEqualTo(version);
        DeviceType reloaded = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        assertThat(reloaded.isFileManagementEnabled()).isFalse();
        assertThat(reloaded.getVersion()).isEqualTo(version);
    }

    @Test
    @Transactional
    @Ignore
    //Ignoring as clearing blob content isn't allowed by H2. Waiting for that feature to be implemented
    public void disableFileManagementDeletesAllFiles() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        when(this.filePart.toString()).thenReturn("First");
        DeviceMessageFile first = deviceType.addDeviceMessageFile(this.path);
        when(this.filePart.toString()).thenReturn("Second");
        DeviceMessageFile second = deviceType.addDeviceMessageFile(this.path);
        long version = deviceType.getVersion();

        // Business method
        deviceType.disableFileManagement();

        // Asserts
        assertThat(deviceType.isFileManagementEnabled()).isFalse();
        assertThat(deviceType.getVersion()).isGreaterThan(version);
        assertThat(deviceType.getDeviceMessageFiles()).isEmpty();
        DeviceType reloaded = deviceConfigurationService.findDeviceType(deviceType.getId()).get();
        assertThat(reloaded.isFileManagementEnabled()).isFalse();
        assertThat(reloaded.getVersion()).isGreaterThan(version);
        assertThat(reloaded.getDeviceMessageFiles()).isEmpty();
    }

    @Test
    @Transactional
    @Ignore
    //Ignoring as clearing blob content isn't allowed by H2. Waiting for that feature to be implemented
    public void disableFileManagementDeletesAllFileManagementRelatedEnablements() {
        DeviceLifeCycle defaultDeviceLifeCycle = inMemoryBootstrapModule.getDeviceLifeCycleConfigurationService().findDefaultDeviceLifeCycle().get();
        DeviceConfigurationService deviceConfigurationService = inMemoryBootstrapModule.getDeviceConfigurationService();
        DeviceType deviceType = deviceConfigurationService
                .newDeviceTypeBuilder(DeviceMessageFileImplIT.class.getSimpleName(), this.deviceProtocolPluggableClass, defaultDeviceLifeCycle)
                .enableFileManagement()
                .create();
        DeviceConfiguration deviceConfiguration = deviceType.newConfiguration("WithFileSupport").add();
        DeviceMessageId.fileManagementRelated()
                .stream()
                .forEach(deviceMessageId ->
                        deviceConfiguration
                                .createDeviceMessageEnablement(deviceMessageId)
                                .addUserActions(
                                        DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1,
                                        DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2,
                                        DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3,
                                        DeviceMessageUserAction.EXECUTEDEVICEMESSAGE4)
                                .build());
        assertThat(deviceConfiguration.getDeviceMessageEnablements()).isNotEmpty();

        when(this.filePart.toString()).thenReturn("First");
        DeviceMessageFile first = deviceType.addDeviceMessageFile(this.path);
        when(this.filePart.toString()).thenReturn("Second");
        DeviceMessageFile second = deviceType.addDeviceMessageFile(this.path);

        // Business method
        deviceType.disableFileManagement();

        // Asserts
        DeviceConfiguration reloaded = deviceConfigurationService.findDeviceConfiguration(deviceConfiguration.getId()).get();
        assertThat(reloaded.getDeviceMessageEnablements()).isEmpty();
    }

    private class DeviceMessageFileContentReader implements Consumer<InputStream> {
        private String read;

        String whatWasRead() {
            return read;
        }

        @Override
        public void accept(InputStream inputStream) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                this.read = reader.readLine();
            } catch (IOException e) {
                throw new UnderlyingIOException(e);
            }
        }
    }
}