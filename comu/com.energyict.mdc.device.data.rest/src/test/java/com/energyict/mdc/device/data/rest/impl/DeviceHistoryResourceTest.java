package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceLifeCycleChangeEvent;
import com.energyict.mdc.device.data.DeviceLifeCycleChangeEvent.Type;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;

import com.jayway.jsonpath.JsonModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceHistoryResourceTest extends DeviceDataRestApplicationJerseyTest {

    private static final String DEVICE_NAME = "DeviceName";

    @Mock
    Device device;
    @Mock
    MeterActivation meterActivation;
    @Mock
    UsagePoint usagePoint;

    private Instant deviceCreationDate = Instant.now();

    @Override
    protected void setupTranslations() {
        super.setupTranslations();
        when(this.deviceLifeCycleConfigurationService.getDisplayName(any(DefaultState.class)))
            .thenAnswer(invocationOnMock -> {
                DefaultState state = (DefaultState) invocationOnMock.getArguments()[0];
                return state.getDefaultFormat();
            });
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(device.getCreateTime()).thenReturn(deviceCreationDate);
        when(deviceService.findDeviceByName(DEVICE_NAME)).thenReturn(Optional.of(device));
        DeviceType deviceType = mock(DeviceType.class);
        when(device.getDeviceType()).thenReturn(deviceType);
        DeviceLifeCycle initialDeviceLifeCycle = mockDeviceLifeCycle(1L, "Standard life cycle");
        when(deviceType.getDeviceLifeCycle(deviceCreationDate)).thenReturn(Optional.of(initialDeviceLifeCycle));
        Instant start = Instant.ofEpochMilli(1410774620100L);
        when(device.getMeterActivationsMostRecentFirst()).thenReturn(Collections.singletonList(meterActivation));
        DeviceConfiguration deviceConfiguration = mock(DeviceConfiguration.class);
        when(deviceConfiguration.getId()).thenReturn(1L);
        when(deviceConfiguration.getName()).thenReturn("DeviceConfig");
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(device.getHistory(any(Instant.class))).thenReturn(Optional.of(device));
        when(device.getMultiplierAt(any(Instant.class))).thenReturn(Optional.of(BigDecimal.TEN));
        when(usagePoint.getId()).thenReturn(1L);
        when(usagePoint.getName()).thenReturn("UsagePoint");
        when(meterActivation.getId()).thenReturn(1L);
        when(meterActivation.getVersion()).thenReturn(1L);
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(meterActivation.isCurrent()).thenReturn(true);
        when(meterActivation.getStart()).thenReturn(start);
        when(meterActivation.getEnd()).thenReturn(Instant.ofEpochMilli(1410774820100L));
        when(meterActivation.getCreateDate()).thenReturn(deviceCreationDate);
        when(meterActivation.getModificationDate()).thenReturn(deviceCreationDate);
    }

    @Test
    public void getDeviceStatesHistory() {
        State stock = mockState(11, DefaultState.IN_STOCK.getKey());
        State commissioning = mockState(22, "commissioning");
        User admin = mockUser(1, "admin");
        User batchExecutor = mockUser(2, "batch executor");
        DeviceLifeCycle newDeviceLifeCycle = mockDeviceLifeCycle(2L, "New device life cycle");

        //Initial state 'In Stock'
        DeviceLifeCycleChangeEvent event1 = mock(DeviceLifeCycleChangeEvent.class);
        when(event1.getType()).thenReturn(Type.STATE);
        when(event1.getState()).thenReturn(stock);
        when(event1.getTimestamp()).thenReturn(deviceCreationDate);
        when(event1.getUser()).thenReturn(Optional.of(admin));

        //Life cycle change: default -> new
        DeviceLifeCycleChangeEvent event2 = mock(DeviceLifeCycleChangeEvent.class);
        when(event2.getType()).thenReturn(Type.LIFE_CYCLE);
        when(event2.getDeviceLifeCycle()).thenReturn(newDeviceLifeCycle);
        when(event2.getTimestamp()).thenReturn(deviceCreationDate.plusSeconds(1));
        when(event2.getUser()).thenReturn(Optional.of(batchExecutor));

        //State change: 'In Stock' -> 'Commissioning'
        DeviceLifeCycleChangeEvent event3 = mock(DeviceLifeCycleChangeEvent.class);
        when(event3.getType()).thenReturn(Type.STATE);
        when(event3.getState()).thenReturn(commissioning);
        when(event3.getTimestamp()).thenReturn(deviceCreationDate.plusSeconds(3));
        when(event3.getUser()).thenReturn(Optional.of(admin));

        when(device.getDeviceLifeCycleChangeEvents()).thenReturn(Arrays.asList(event1, event2, event3));

        String response = target("/devices/" + DEVICE_NAME + "/history/devicelifecyclechanges").request().get(String.class);

        JsonModel model = JsonModel.model(response);

        assertThat(model.<Number> get("$.total")).isEqualTo(3);
        assertThat(model.<List<Number>> get("$.deviceLifeCycleChanges[*].from.id")).containsExactly(1, 11);
        assertThat(model.<List<String>>get("$.deviceLifeCycleChanges[*].from.name")).containsExactly("Standard life cycle", "In stock");
        assertThat(model.<List<Number>> get("$.deviceLifeCycleChanges[*].to.id")).containsExactly(11, 2, 22);
        assertThat(model.<List<String>>get("$.deviceLifeCycleChanges[*].to.name")).containsExactly("In stock", "New device life cycle", "commissioning");
        assertThat(model.<List<Number>> get("$.deviceLifeCycleChanges[*].modTime")).containsExactly(deviceCreationDate.toEpochMilli(), deviceCreationDate.plusSeconds(1).toEpochMilli(), deviceCreationDate.plusSeconds(3).toEpochMilli());
        assertThat(model.<List<Number>> get("$.deviceLifeCycleChanges[*].author.id")).containsExactly(1, 2, 1);
        assertThat(model.<List<String>> get("$.deviceLifeCycleChanges[*].author.name")).containsExactly("admin", "batch executor", "admin");
    }

    @Test
    public void getDeviceStatesHistoryNoStates() {
        when(device.getDeviceLifeCycleChangeEvents()).thenReturn(Collections.emptyList());

        String response = target("/devices/" + DEVICE_NAME + "/history/devicelifecyclechanges").request().get(String.class);

        JsonModel model = JsonModel.model(response);

        assertThat(model.<Number> get("$.total")).isEqualTo(0);
        assertThat(model.<List<?>> get("$.deviceLifeCycleChanges")).hasSize(0);
    }

    @Test
    public void testMeterActivationsHistory() {
        String json = target("/devices/" + DEVICE_NAME + "/history/meteractivations").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number> get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.meterActivations[0].id")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.meterActivations[0].start")).isEqualTo(1410774620100L);
        assertThat(jsonModel.<Number>get("$.meterActivations[0].end")).isEqualTo(1410774820100L);
        assertThat(jsonModel.<Number>get("$.meterActivations[0].multiplier")).isEqualTo(10);
        assertThat(jsonModel.<Number>get("$.meterActivations[0].version")).isEqualTo(1);
        assertThat(jsonModel.<Boolean>get("$.meterActivations[0].active")).isEqualTo(true);
        assertThat(jsonModel.<Number>get("$.meterActivations[0].usagePoint.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.meterActivations[0].usagePoint.name")).isEqualTo("UsagePoint");
        assertThat(jsonModel.<Number>get("$.meterActivations[0].deviceConfiguration.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.meterActivations[0].deviceConfiguration.name")).isEqualTo("DeviceConfig");
    }

    private State mockState(long id, String name) {
        State state = mock(State.class);
        when(state.getId()).thenReturn(id);
        when(state.getName()).thenReturn(name);
        return state;
    }

    private DeviceLifeCycle mockDeviceLifeCycle(long id, String name) {
        DeviceLifeCycle deviceLifeCycle = mock(DeviceLifeCycle.class);
        when(deviceLifeCycle.getId()).thenReturn(id);
        when(deviceLifeCycle.getName()).thenReturn(name);
        return deviceLifeCycle;
    }

    private User mockUser(long id, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        return user;
    }
}
