package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.comserver.exceptions.CodingException;
import com.energyict.comserver.logging.LogLevel;
import com.energyict.mdc.commands.ComCommandTypes;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.commands.MessagesCommand;
import com.energyict.mdc.common.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageSpecFactoryImpl;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategoryPrimaryKey;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.protocol.tasks.ServerMessagesTask;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.test.MockEnvironmentTranslations;
import org.json.JSONException;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link com.energyict.comserver.commands.deviceactions.MessagesCommandImpl} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 27/06/12
 * Time: 14:49
 */
@RunWith(MockitoJUnitRunner.class)
public class MessagesCommandImplTest extends CommonCommandImplTests {

    @ClassRule
    public static TestRule mockEnvironmentTranslactions = new MockEnvironmentTranslations();

    @Mock
    ComTaskExecution comTaskExecution;

    @Before
    public void initializeManager() {
        when(manager.getDeviceMessageSpecFactory()).thenReturn(new DeviceMessageSpecFactoryImpl());
    }

    @Test(expected = CodingException.class)
    public void messageTaskNullTest() {
        OfflineDevice device = mock(OfflineDevice.class);
        new MessagesCommandImpl(null, device, createCommandRoot());
        // exception should have occurred
    }

    @Test(expected = CodingException.class)
    public void deviceNullTest() {
        ServerMessagesTask messagesTask = mock(ServerMessagesTask.class);
        new MessagesCommandImpl(messagesTask, null, createCommandRoot());
        // exception should have occurred
    }

    @Test(expected = CodingException.class)
    public void commandRootNullTest() {
        ServerMessagesTask messagesTask = mock(ServerMessagesTask.class);
        OfflineDevice device = mock(OfflineDevice.class);
        new MessagesCommandImpl(messagesTask, device, null);
        // exception should have occurred
    }

    @Test
    public void commandTypeTest() {
        ServerMessagesTask messagesTask = mock(ServerMessagesTask.class);
        OfflineDevice device = mock(OfflineDevice.class);
        MessagesCommand messagesCommand = new MessagesCommandImpl(messagesTask, device, createCommandRoot());

        // assert
        Assertions.assertThat(messagesCommand.getCommandType()).isEqualTo(ComCommandTypes.MESSAGES_COMMAND);
    }

    @Test
    public void updateMessageListsTest() {
        OfflineDevice device = getMockedDeviceWithPendingAndSentMessages();
        CommandRoot commandRoot = createCommandRoot(device);
        MessagesCommandImpl messagesCommand = (MessagesCommandImpl) commandRoot.getMessagesCommand(createMockedMessagesTaskWithCategories(), commandRoot, comTaskExecution);

        //asserts
        assertThat(messagesCommand.getPendingMessages()).isNotNull();
        assertThat(messagesCommand.getPendingMessages()).hasSize(2);    // Expecting only the messages who belong to the corresponding message categories
        assertThat(messagesCommand.getSentMessages()).isNotNull();
        assertThat(messagesCommand.getSentMessages()).hasSize(1);       // Expecting only the messages who belong to the corresponding message categories
    }

    @Test
    public void updateMessageListsWhenSelectAllIsActiveTest() {
        OfflineDevice device = getMockedDeviceWithPendingAndSentMessages();
        CommandRoot commandRoot = createCommandRoot(device);
        MessagesCommandImpl messagesCommand = (MessagesCommandImpl) commandRoot.getMessagesCommand(createMockedMessagesTaskWithSelectAll(), commandRoot, comTaskExecution);

        //asserts
        assertThat(messagesCommand.getPendingMessages()).isNotNull();
        assertThat(messagesCommand.getPendingMessages()).hasSize(3);    // Expecting messages from all message categories
        assertThat(messagesCommand.getSentMessages()).isNotNull();
        assertThat(messagesCommand.getSentMessages()).hasSize(2);       // Expecting messages from all message categories

        assertEquals(
                "MessagesCommandImpl {pendingMessages: 0 (DeviceMessageSpec1, DeviceMessageCategory), 0 (DeviceMessageSpec2, DeviceMessageCategory)0 (DeviceMessageSpec5, DeviceMessageCategory); messagesFromPreviousSession: 0 (DeviceMessageSpec3, DeviceMessageCategory), 0 (DeviceMessageSpec4, DeviceMessageCategory)}",
                messagesCommand.toJournalMessageDescription(LogLevel.ERROR));
    }

    @Test
    public void doExecuteTest() {
        OfflineDevice device = getMockedDeviceWithPendingAndSentMessages();
        CommandRoot commandRoot = createCommandRoot(device);
        MessagesCommandImpl messagesCommand = (MessagesCommandImpl) commandRoot.getMessagesCommand(createMockedMessagesTaskWithCategories(), commandRoot, comTaskExecution);
        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);

        // Business logic
        messagesCommand.execute(deviceProtocol, AbstractComCommandExecuteTest.newTestExecutionContext());

        // asserts
        verify(deviceProtocol, times(1)).updateSentMessages(Matchers.<List<OfflineDeviceMessage>>any());
        verify(deviceProtocol, times(1)).executePendingMessages(Matchers.<List<OfflineDeviceMessage>>any());
    }

    @Test
    public void testJournalMessageDescription() throws JSONException {
        OfflineDevice device = this.getMockedDeviceWithPendingAndSentMessages();
        CommandRoot commandRoot = createCommandRoot(device);
        ServerMessagesTask messagesTask = this.createMockedMessagesTaskWithCategories();
        MessagesCommandImpl messagesCommand = (MessagesCommandImpl) commandRoot.getMessagesCommand(messagesTask, commandRoot, comTaskExecution);

        // Business method
        String description = messagesCommand.toJournalMessageDescription(LogLevel.TRACE);

        // Asserts
        Assertions.assertThat(description).isNotEmpty();
    }

    private ServerMessagesTask createMockedMessagesTaskWithCategories() {
        ServerMessagesTask messagesTask = mock(ServerMessagesTask.class);
        when(messagesTask.getDeviceMessageCategories()).thenReturn(
                Arrays.<DeviceMessageCategory>asList(
                        DeviceMessageTestCategories.FIRST_TEST_CATEGORY,
                        DeviceMessageTestCategories.THIRD_TEST_CATEGORY));
        return messagesTask;
    }

    private ServerMessagesTask createMockedMessagesTaskWithSelectAll() {
        ServerMessagesTask messagesTask = mock(ServerMessagesTask.class);
        when(messagesTask.isAllCategories()).thenReturn(true);
        return messagesTask;
    }

    private OfflineDevice getMockedDeviceWithPendingAndSentMessages() {
        DeviceMessageCategory deviceMessageCategory = mock(DeviceMessageCategory.class);
        when(deviceMessageCategory.getName()).thenReturn("DeviceMessageCategory");
        DeviceMessageSpec deviceMessageSpec1 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec1.getName()).thenReturn("DeviceMessageSpec1");
        when(deviceMessageSpec1.getCategory()).thenReturn(deviceMessageCategory);
        DeviceMessageSpec deviceMessageSpec2 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec2.getName()).thenReturn("DeviceMessageSpec2");
        when(deviceMessageSpec2.getCategory()).thenReturn(deviceMessageCategory);
        DeviceMessageSpec deviceMessageSpec3 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec3.getName()).thenReturn("DeviceMessageSpec3");
        when(deviceMessageSpec3.getCategory()).thenReturn(deviceMessageCategory);
        DeviceMessageSpec deviceMessageSpec4 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec4.getName()).thenReturn("DeviceMessageSpec4");
        when(deviceMessageSpec4.getCategory()).thenReturn(deviceMessageCategory);
        DeviceMessageSpec deviceMessageSpec5 = mock(DeviceMessageSpec.class);
        when(deviceMessageSpec5.getName()).thenReturn("DeviceMessageSpec5");
        when(deviceMessageSpec5.getCategory()).thenReturn(deviceMessageCategory);
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        OfflineDeviceMessage offlineDeviceMessage1 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage1.getDeviceMessageSpecPrimaryKey()).thenReturn(DeviceMessageTestSpec.TEST_SPEC_WITH_EXTENDED_SPECS.getPrimaryKey());
        when(offlineDeviceMessage1.getSpecification()).thenReturn(deviceMessageSpec1);
        OfflineDeviceMessage offlineDeviceMessage2 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage2.getDeviceMessageSpecPrimaryKey()).thenReturn(AnotherDeviceMessageTestSpec.TEST_SPEC_WITH_SIMPLE_SPECS.getPrimaryKey());
        when(offlineDeviceMessage2.getSpecification()).thenReturn(deviceMessageSpec2);
        OfflineDeviceMessage offlineDeviceMessage3 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage3.getDeviceMessageSpecPrimaryKey()).thenReturn(DeviceMessageTestSpec.TEST_SPEC_WITH_SIMPLE_SPECS.getPrimaryKey());
        when(offlineDeviceMessage3.getSpecification()).thenReturn(deviceMessageSpec3);
        OfflineDeviceMessage offlineDeviceMessage4 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage4.getDeviceMessageSpecPrimaryKey()).thenReturn(AnotherDeviceMessageTestSpec.TEST_SPEC_WITHOUT_SPECS.getPrimaryKey());
        when(offlineDeviceMessage4.getSpecification()).thenReturn(deviceMessageSpec4);
        OfflineDeviceMessage offlineDeviceMessage5 = mock(OfflineDeviceMessage.class);
        when(offlineDeviceMessage5.getDeviceMessageSpecPrimaryKey()).thenReturn(DeviceMessageTestSpec.TEST_SPEC_WITHOUT_SPECS.getPrimaryKey());
        when(offlineDeviceMessage5.getSpecification()).thenReturn(deviceMessageSpec5);
        when(offlineDevice.getAllPendingDeviceMessages()).thenReturn(Arrays.asList(offlineDeviceMessage1, offlineDeviceMessage2, offlineDeviceMessage5));
        when(offlineDevice.getAllSentDeviceMessages()).thenReturn(Arrays.asList(offlineDeviceMessage3, offlineDeviceMessage4));
        return offlineDevice;
    }

    /**
     * Test enum for DeviceMessageCategories
     * <p/>
     * Copyrights EnergyICT
     * Date: 8/02/13
     * Time: 15:30
     */
    public enum DeviceMessageTestCategories implements DeviceMessageCategory {

        FIRST_TEST_CATEGORY {
            @Override
            public List<DeviceMessageSpec> getMessageSpecifications() {
                return Arrays.<DeviceMessageSpec>asList(DeviceMessageTestSpec.values());

            }
        },
        SECOND_TEST_CATEGORY {
            @Override
            public List<DeviceMessageSpec> getMessageSpecifications() {
                return Arrays.<DeviceMessageSpec>asList(DeviceMessageTestSpec.values());

            }
        },
        THIRD_TEST_CATEGORY {
            @Override
            public List<DeviceMessageSpec> getMessageSpecifications() {
                return Arrays.<DeviceMessageSpec>asList(DeviceMessageTestSpec.values());

            }
        };

        @Override
        public String getName() {
            return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
        }

        /**
         * Gets the resource key that determines the name
         * of this category to the user's language settings.
         *
         * @return The resource key
         */
        private String getNameResourceKey() {
            return DeviceMessageTestCategories.class.getSimpleName() + "." + this.toString();
        }

        @Override
        public String getDescription() {
            return UserEnvironment.getDefault().getTranslation(this.getDescriptionResourceKey());
        }

        /**
         * Gets the resource key that determines the description
         * of this category to the user's language settings.
         *
         * @return The resource key
         */
        private String getDescriptionResourceKey() {
            return this.getNameResourceKey() + ".description";
        }

        @Override
        public int getId() {
            return this.ordinal();
        }

        @Override
        public abstract List<DeviceMessageSpec> getMessageSpecifications();

        @Override
        public DeviceMessageCategoryPrimaryKey getPrimaryKey() {
            return new DeviceMessageCategoryPrimaryKey(this, name());
        }
    }

    /**
     * Test enum implementing DeviceMessageSpec
     * <p/>
     * Copyrights EnergyICT
     * Date: 8/02/13
     * Time: 15:16
     */
    public enum DeviceMessageTestSpec implements DeviceMessageSpec {

        TEST_SPEC_WITH_SIMPLE_SPECS(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec("testMessageSpec.simpleBigDecimal"),
                RequiredPropertySpecFactory.newInstance().stringPropertySpec("testMessageSpec.simpleString")),
        TEST_SPEC_WITH_EXTENDED_SPECS(RequiredPropertySpecFactory.newInstance().referencePropertySpec("testMessageSpec.codetable", MeteringWarehouse.getCurrent().getCodeFactory()),
                RequiredPropertySpecFactory.newInstance().dateTimePropertySpec("testMessageSpec.activationdate")),
        TEST_SPEC_WITHOUT_SPECS;

        private static final DeviceMessageCategory activityCalendarCategory = DeviceMessageTestCategories.FIRST_TEST_CATEGORY;

        private List<PropertySpec> deviceMessagePropertySpecs;

        DeviceMessageTestSpec(PropertySpec... deviceMessagePropertySpecs) {
            this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
        }

        @Override
        public DeviceMessageCategory getCategory() {
            return activityCalendarCategory;
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return deviceMessagePropertySpecs;
        }

        @Override
        public PropertySpec getPropertySpec(String name) {
            for (PropertySpec securityProperty : getPropertySpecs()) {
                if (securityProperty.getName().equals(name)) {
                    return securityProperty;
                }
            }
            return null;
        }

        @Override
        public DeviceMessageSpecPrimaryKey getPrimaryKey() {
            return new DeviceMessageSpecPrimaryKey(this, name());
        }
    }

    /**
     * Test enum implementing DeviceMessageSpec
     * <p/>
     * Copyrights EnergyICT
     * Date: 8/02/13
     * Time: 15:16
     */
    public enum AnotherDeviceMessageTestSpec implements DeviceMessageSpec {

        TEST_SPEC_WITH_SIMPLE_SPECS(RequiredPropertySpecFactory.newInstance().bigDecimalPropertySpec("testMessageSpec.simpleBigDecimal"),
                RequiredPropertySpecFactory.newInstance().stringPropertySpec("testMessageSpec.simpleString")),
        TEST_SPEC_WITH_EXTENDED_SPECS(RequiredPropertySpecFactory.newInstance().referencePropertySpec("testMessageSpec.codetable", MeteringWarehouse.getCurrent().getCodeFactory()),
                RequiredPropertySpecFactory.newInstance().dateTimePropertySpec("testMessageSpec.activationdate")),
        TEST_SPEC_WITHOUT_SPECS;

        private static final DeviceMessageCategory activityCalendarCategory = DeviceMessageTestCategories.SECOND_TEST_CATEGORY;

        private List<PropertySpec> deviceMessagePropertySpecs;

        AnotherDeviceMessageTestSpec(PropertySpec... deviceMessagePropertySpecs) {
            this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
        }

        @Override
        public DeviceMessageCategory getCategory() {
            return activityCalendarCategory;
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return deviceMessagePropertySpecs;
        }

        @Override
        public PropertySpec getPropertySpec(String name) {
            for (PropertySpec securityProperty : getPropertySpecs()) {
                if (securityProperty.getName().equals(name)) {
                    return securityProperty;
                }
            }
            return null;
        }

        @Override
        public DeviceMessageSpecPrimaryKey getPrimaryKey() {
            return new DeviceMessageSpecPrimaryKey(this, name());
        }
    }
}
