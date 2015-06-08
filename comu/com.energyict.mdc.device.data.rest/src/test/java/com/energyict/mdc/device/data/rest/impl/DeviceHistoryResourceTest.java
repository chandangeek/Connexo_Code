package com.energyict.mdc.device.data.rest.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceLifeCycleChangeEvent;
import com.energyict.mdc.device.data.DeviceLifeCycleChangeEvent.Type;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.jayway.jsonpath.JsonModel;

public class DeviceHistoryResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Mock
    Device device;

    Instant deviceCreationDate = Instant.now();

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(thesaurus.getStringBeyondComponent(anyString(), anyString())).thenAnswer(invocationOnMock -> {
            for (TranslationKey key : DefaultState.values()) {
                if (key.getKey().equals(invocationOnMock.getArguments()[0])) {
                    return key.getDefaultFormat();
                }
            }
            return (String) invocationOnMock.getArguments()[1];
        });

        when(device.getCreateTime()).thenReturn(deviceCreationDate);
        when(deviceService.findByUniqueMrid("DeviceMRID")).thenReturn(Optional.of(device));
        DeviceType deviceType = mock(DeviceType.class);
        when(device.getDeviceType()).thenReturn(deviceType);
        DeviceLifeCycle initialDeviceLifeCycle = mockDeviceLifeCycle(1L, "Default life cycle");
        when(deviceType.getDeviceLifeCycle(deviceCreationDate)).thenReturn(Optional.of(initialDeviceLifeCycle));
    }

    @Test
    public void getDeviceStatesHistory() {
        State stock = mockState(11, DefaultState.IN_STOCK.getKey());
        State commisionning = mockState(22, "commissioning");
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
        when(event3.getState()).thenReturn(commisionning);
        when(event3.getTimestamp()).thenReturn(deviceCreationDate.plusSeconds(3));
        when(event3.getUser()).thenReturn(Optional.of(admin));

        when(device.getDeviceLifeCycleChangeEvents()).thenReturn(Arrays.asList(event1, event2, event3));

        String response = target("/devices/DeviceMRID/history/devicelifecyclechanges").request().get(String.class);

        JsonModel model = JsonModel.model(response);

        assertThat(model.<Number> get("$.total")).isEqualTo(2);
        assertThat(model.<String>get("$.deviceLifeCycleStateChanges[0].fromState")).isNull();
        assertThat(model.<String>get("$.deviceLifeCycleStateChanges[1].fromState")).isEqualTo("In Stock");
        assertThat(model.<List<String>> get("$.deviceLifeCycleStateChanges[*].toState")).containsExactly("In Stock", "commissioning");
        assertThat(model.<List<Number>> get("$.deviceLifeCycleStateChanges[*].modTime")).containsExactly(now.minusMillis(1000).toEpochMilli(), now.toEpochMilli());
        assertThat(model.<List<Number>> get("$.deviceLifeCycleStateChanges[*].author.id")).containsExactly(1, 2);
        assertThat(model.<List<String>> get("$.deviceLifeCycleStateChanges[*].author.name")).containsExactly("admin", "batch executor");
    }

    @Test
    public void getDeviceStatesHistoryNoStates() {
        when(device.getDeviceLifeCycleChangeEvents()).thenReturn(Collections.emptyList());

        String response = target("/devices/DeviceMRID/history/devicelifecyclechanges").request().get(String.class);

        JsonModel model = JsonModel.model(response);

        assertThat(model.<Number> get("$.total")).isEqualTo(0);
        assertThat(model.<List<?>> get("$.deviceLifeCycleChanges")).hasSize(0);
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
