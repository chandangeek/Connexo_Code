package com.energyict.mdc.device.data.impl.configchange;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.devtools.tests.rules.Expected;
import com.elster.jupiter.devtools.tests.rules.ExpectedExceptionRule;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageBuilder;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DevicesForConfigChangeSearch;
import com.energyict.mdc.device.data.ItemizeConfigChangeQueueMessage;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.internal.verification.Times;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.event.EventConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 22.12.15
 * Time: 09:46
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigChangeHandlerTest {

    private static final long DEVICE_CONFIG_CHANGE_REQUEST_ID = 6654L;
    private static final long DEVICE_TYPE_ID = 654L;
    private static final long DEVICE_CONFIG_ID = 44465L;
    private static final long DESTINATION_CONFIG_ID = 465412L;

    @Rule
    public TestRule expectedErrorRule = new ExpectedExceptionRule();

    @Mock
    private JsonService jsonService;
    @Mock
    private MessageService messageService;
    @Mock
    private SearchService searchService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ServerDeviceService deviceService;
    @Mock
    private DeviceDataModelService deviceDataModelService;
    @Mock
    private SearchDomain deviceSearchDomain;
    @Mock
    private DeviceConfigChangeRequest deviceConfigChangeRequest;
    @Mock
    private DeviceConfigChangeInActionImpl deviceConfigChangeInAction;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock(extraInterfaces = HasId.class)
    private DeviceType deviceType;
    @Mock(extraInterfaces = HasId.class)
    private DeviceConfiguration deviceConfiguration;
    @Mock
    private SearchableProperty deviceTypeProperty;
    @Mock
    private SearchableProperty deviceConfigProperty;

    @Before
    public void setup() {
        deviceTypeProperty = mock(SearchableProperty.class);
        when(deviceTypeProperty.getName()).thenReturn(DeviceConfigChangeHandler.deviceTypeSearchPropertyName);
        when(deviceTypeProperty.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.MULTI);
        deviceConfigProperty = mock(SearchableProperty.class);
        when(deviceConfigProperty.getName()).thenReturn(DeviceConfigChangeHandler.deviceConfigurationSearchPropertyName);
        when(deviceConfigProperty.getSelectionMode()).thenReturn(SearchableProperty.SelectionMode.MULTI);
        when(deviceSearchDomain.supports(Device.class)).thenReturn(true);
        when(deviceSearchDomain.getId()).thenReturn("Device");
        when(deviceSearchDomain.getProperties()).thenReturn(Arrays.asList(deviceTypeProperty, deviceConfigProperty));
        when(deviceSearchDomain.getPropertiesValues(Matchers.any())).thenAnswer(
                inv -> deviceSearchDomain.getProperties()
                .stream()
                .map(props -> ((Function<SearchableProperty, SearchablePropertyValue>)inv.getArguments()[0]).apply(props))
                .collect(Collectors.toList())
        );
        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.fromStringValue(Matchers.anyString())).thenAnswer(inv -> inv.getArguments()[0]);
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);
        when(deviceConfigProperty.getSpecification()).thenReturn(propertySpec);
        when(deviceTypeProperty.getSpecification()).thenReturn(propertySpec);
        when(searchService.findDomain(Device.class.getName())).thenReturn(Optional.of(deviceSearchDomain));
        when(deviceService.findDeviceConfigChangeRequestById(DEVICE_CONFIG_CHANGE_REQUEST_ID)).thenReturn(Optional.of(deviceConfigChangeRequest));
        when(deviceType.getId()).thenReturn(DEVICE_TYPE_ID);
        when(deviceConfiguration.getId()).thenReturn(DEVICE_CONFIG_ID);
        when(deviceConfigurationService.findDeviceType(DEVICE_TYPE_ID)).thenReturn(Optional.of(deviceType));
        when(deviceConfigurationService.findDeviceConfiguration(DEVICE_CONFIG_ID)).thenReturn(Optional.of(deviceConfiguration));
        when(deviceConfigChangeRequest.addDeviceInAction(any())).thenReturn(deviceConfigChangeInAction);
    }

    private SearchablePropertyValue.ValueBean getDeviceTypeValueBean(){
        SearchablePropertyValue.ValueBean bean = new SearchablePropertyValue.ValueBean();
        bean.propertyName = DeviceConfigChangeHandler.deviceTypeSearchPropertyName;
        bean.operator = SearchablePropertyOperator.EQUAL;
        bean.values = Collections.singletonList(String.valueOf(DEVICE_TYPE_ID));
        return bean;
    }

    private SearchablePropertyValue.ValueBean getDeviceConfigValueBean(){
        SearchablePropertyValue.ValueBean bean = new SearchablePropertyValue.ValueBean();
        bean.propertyName = DeviceConfigChangeHandler.deviceConfigurationSearchPropertyName;
        bean.operator = SearchablePropertyOperator.EQUAL;
        bean.values = Collections.singletonList(String.valueOf(DEVICE_CONFIG_ID));
        return bean;
    }


    @Test
    public void searchFilterTest() throws JsonProcessingException {
        DevicesForConfigChangeSearch devicesForConfigChangeSearch = new DevicesForConfigChangeSearch();
        devicesForConfigChangeSearch.searchItems.put(DeviceConfigChangeHandler.deviceTypeSearchPropertyName, getDeviceTypeValueBean());
        devicesForConfigChangeSearch.searchItems.put(DeviceConfigChangeHandler.deviceConfigurationSearchPropertyName, getDeviceConfigValueBean());

        ItemizeConfigChangeQueueMessage itemizeConfigChangeQueueMessage = new ItemizeConfigChangeQueueMessage(DESTINATION_CONFIG_ID, Collections.emptyList(), devicesForConfigChangeSearch, DEVICE_CONFIG_CHANGE_REQUEST_ID);

        Finder<Device> finder = mockMessageHandlerInternals(itemizeConfigChangeQueueMessage);
        SearchBuilder searchBuilder = mockSearchBuilder(finder);
        DeviceConfigChangeHandler deviceConfigChangeHandler = getTestInstance();

        Message queueMessage = mock(Message.class);
        deviceConfigChangeHandler.process(queueMessage);

        verify(searchBuilder).where(deviceTypeProperty);
        verify(searchBuilder).where(deviceConfigProperty);
    }

    @Test
    public void noSearchWhenFixedListIsProvidedTest() throws JsonProcessingException {
        DevicesForConfigChangeSearch devicesForConfigChangeSearch = new DevicesForConfigChangeSearch();
        devicesForConfigChangeSearch.searchItems.put(DeviceConfigChangeHandler.deviceTypeSearchPropertyName, getDeviceTypeValueBean());
        devicesForConfigChangeSearch.searchItems.put(DeviceConfigChangeHandler.deviceConfigurationSearchPropertyName, getDeviceConfigValueBean());

        String deviceMRID1 = "deviceMRDI1";
        when(deviceService.findByUniqueMrid(deviceMRID1)).thenReturn(Optional.empty());
        String deviceMRID2 = "deviceMRDI2";
        when(deviceService.findByUniqueMrid(deviceMRID2)).thenReturn(Optional.empty());
        String deviceMRID3 = "deviceMRDI3";
        when(deviceService.findByUniqueMrid(deviceMRID3)).thenReturn(Optional.empty());

        ItemizeConfigChangeQueueMessage itemizeConfigChangeQueueMessage = new ItemizeConfigChangeQueueMessage(DESTINATION_CONFIG_ID, Arrays.asList(deviceMRID1, deviceMRID2, deviceMRID3), devicesForConfigChangeSearch, DEVICE_CONFIG_CHANGE_REQUEST_ID);

        Finder<Device> finder = mockMessageHandlerInternals(itemizeConfigChangeQueueMessage);
        SearchBuilder searchBuilder = mockSearchBuilder(finder);
        DeviceConfigChangeHandler deviceConfigChangeHandler = getTestInstance();

        Message queueMessage = mock(Message.class);
        deviceConfigChangeHandler.process(queueMessage);

        verify(searchBuilder, new Times(0)).where(deviceTypeProperty);
        verify(searchBuilder, new Times(0)).where(deviceConfigProperty);
    }

    @Test
    public void fixedMridListChangeConfigTest() throws JsonProcessingException {
        DevicesForConfigChangeSearch devicesForConfigChangeSearch = new DevicesForConfigChangeSearch();
        devicesForConfigChangeSearch.searchItems.put(DeviceConfigChangeHandler.deviceTypeSearchPropertyName, getDeviceTypeValueBean());
        devicesForConfigChangeSearch.searchItems.put(DeviceConfigChangeHandler.deviceConfigurationSearchPropertyName, getDeviceConfigValueBean());

        String helloFromMyTest = "HelloFromMyTest";

        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        DestinationSpec destinationSpec = mock(DestinationSpec.class);
        when(destinationSpec.message(helloFromMyTest)).thenReturn(messageBuilder);
        MessageService messageService = mock(MessageService.class);
        when(deviceDataModelService.messageService()).thenReturn(messageService);
        when(messageService.getDestinationSpec(ServerDeviceForConfigChange.CONFIG_CHANGE_BULK_QUEUE_DESTINATION)).thenReturn(Optional.of(destinationSpec));
        when(jsonService.serialize(any())).thenReturn(helloFromMyTest);
        String deviceMRID1 = "deviceMRDI1";
        Device device1 = mock(Device.class);
        when(deviceService.findByUniqueMrid(deviceMRID1)).thenReturn(Optional.of(device1));
        String deviceMRID2 = "deviceMRDI2";
        Device device2 = mock(Device.class);
        when(deviceService.findByUniqueMrid(deviceMRID2)).thenReturn(Optional.of(device2));
        String deviceMRID3 = "deviceMRDI3";
        Device device3 = mock(Device.class);
        when(deviceService.findByUniqueMrid(deviceMRID3)).thenReturn(Optional.of(device3));

        List<String> deviceMRIDs = Arrays.asList(deviceMRID1, deviceMRID2, deviceMRID3);
        ItemizeConfigChangeQueueMessage itemizeConfigChangeQueueMessage = new ItemizeConfigChangeQueueMessage(DESTINATION_CONFIG_ID, deviceMRIDs, devicesForConfigChangeSearch, DEVICE_CONFIG_CHANGE_REQUEST_ID);

        Finder<Device> finder = mockMessageHandlerInternals(itemizeConfigChangeQueueMessage);
        SearchBuilder searchBuilder = mockSearchBuilder(finder);
        DeviceConfigChangeHandler deviceConfigChangeHandler = getTestInstance();

        Message queueMessage = mock(Message.class);
        deviceConfigChangeHandler.process(queueMessage);

        verify(messageBuilder, times(deviceMRIDs.size())).send();
    }

    @Test
    @Expected(value = DeviceConfigurationChangeException.class, message = "You need to search a specific device configuration in order to use the bulk action for change device configuration")
    public void noDeviceConfigInSearchFilterTest() throws JsonProcessingException {
        DevicesForConfigChangeSearch devicesForConfigChangeSearch = new DevicesForConfigChangeSearch();
        devicesForConfigChangeSearch.searchItems.put(DeviceConfigChangeHandler.deviceTypeSearchPropertyName, getDeviceTypeValueBean());


        ItemizeConfigChangeQueueMessage itemizeConfigChangeQueueMessage = new ItemizeConfigChangeQueueMessage(DESTINATION_CONFIG_ID, Collections.emptyList(), devicesForConfigChangeSearch, DEVICE_CONFIG_CHANGE_REQUEST_ID);

        Finder<Device> finder = mockMessageHandlerInternals(itemizeConfigChangeQueueMessage);
        SearchBuilder searchBuilder = mockSearchBuilder(finder);
        DeviceConfigChangeHandler deviceConfigChangeHandler = getTestInstance();

        Message queueMessage = mock(Message.class);
        deviceConfigChangeHandler.process(queueMessage);
    }

    @Test
    @Expected(value = DeviceConfigurationChangeException.class, message = "You need to search on a unique device configuration in order to use the bulk action for change device configuration")
    public void multipleDeviceConfigsInSearchFilter() throws JsonProcessingException {
        DevicesForConfigChangeSearch devicesForConfigChangeSearch = new DevicesForConfigChangeSearch();
        devicesForConfigChangeSearch.searchItems.put(DeviceConfigChangeHandler.deviceTypeSearchPropertyName, getDeviceTypeValueBean());
        SearchablePropertyValue.ValueBean deviceConfigValueBean = getDeviceConfigValueBean();
        deviceConfigValueBean.values = Arrays.asList("1", "2", "3");
        devicesForConfigChangeSearch.searchItems.put(DeviceConfigChangeHandler.deviceConfigurationSearchPropertyName, deviceConfigValueBean);

        ItemizeConfigChangeQueueMessage itemizeConfigChangeQueueMessage = new ItemizeConfigChangeQueueMessage(DESTINATION_CONFIG_ID, Collections.emptyList(), devicesForConfigChangeSearch, DEVICE_CONFIG_CHANGE_REQUEST_ID);

        Finder<Device> finder = mockMessageHandlerInternals(itemizeConfigChangeQueueMessage);
        SearchBuilder searchBuilder = mockSearchBuilder(finder);
        DeviceConfigChangeHandler deviceConfigChangeHandler = getTestInstance();

        Message queueMessage = mock(Message.class);
        deviceConfigChangeHandler.process(queueMessage);
    }

    private SearchBuilder mockSearchBuilder(Finder<Device> finder) {
        SearchBuilder searchBuilder = FakeBuilder.initBuilderStub(finder, SearchBuilder.class, SearchBuilder.CriterionBuilder.class);
        when(searchService.search(deviceSearchDomain)).thenReturn(searchBuilder);
        return searchBuilder;
    }

    private Finder<Device> mockMessageHandlerInternals(ItemizeConfigChangeQueueMessage itemizeConfigChangeQueueMessage) throws JsonProcessingException {
        Map<String, Object> message = new HashMap<>(2);
        message.put(EventConstants.EVENT_TOPIC, ServerDeviceForConfigChange.DEVICE_CONFIG_CHANGE_BULK_SETUP_ACTION);
        message.put(ServerDeviceForConfigChange.CONFIG_CHANGE_MESSAGE_VALUE, new ObjectMapper().writeValueAsString(itemizeConfigChangeQueueMessage));

        when(jsonService.deserialize((byte[]) any(), any())).thenReturn(message); // this is for the first deserialization
        when(jsonService.deserialize((String) any(), any())).thenReturn(itemizeConfigChangeQueueMessage); // this is for the second deserialization
        Finder<Device> finder = mock(Finder.class);
        when(finder.stream()).thenReturn(Stream.empty());
        return finder;
    }

    private DeviceConfigChangeHandler getTestInstance() {
        return new DeviceConfigChangeHandler(jsonService, getConfigChangeContext());
    }

    private DeviceConfigChangeHandler.ConfigChangeContext getConfigChangeContext() {
        return new DeviceConfigChangeHandler.ConfigChangeContext(messageService, jsonService, searchService, thesaurus, deviceService, deviceDataModelService, deviceConfigurationService);
    }


}