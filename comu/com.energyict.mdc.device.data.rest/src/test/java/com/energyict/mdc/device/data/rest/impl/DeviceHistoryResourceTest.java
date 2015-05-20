package com.energyict.mdc.device.data.rest.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeSlice;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

public class DeviceHistoryResourceTest extends DeviceDataRestApplicationJerseyTest {

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
    }

    @Test
    public void getDeviceStatesHistory() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("DeviceWithLifeCycle")).thenReturn(Optional.of(device));
        StateTimeline stateTimeline = mock(StateTimeline.class);
        when(device.getStateTimeline()).thenReturn(stateTimeline);

        StateTimeSlice slice1 = mock(StateTimeSlice.class), slice2 = mock(StateTimeSlice.class);
        when(stateTimeline.getSlices()).thenReturn(Arrays.asList(slice1, slice2));

        State stock = mockState(DefaultState.IN_STOCK.getKey()), commisionning = mockState("commissioning");
        when(slice1.getState()).thenReturn(stock);
        when(slice2.getState()).thenReturn(commisionning);
        Instant now = Instant.now();
        Range<Instant> range1 = Range.closedOpen(now.minusMillis(1000), now);
        Range<Instant> range2 = Range.closedOpen(now, Instant.MAX);
        when(slice1.getPeriod()).thenReturn(range1);
        when(slice2.getPeriod()).thenReturn(range2);
        User user1 = mockUser(1, "admin"), user2 = mockUser(2, "batch executor");
        when(slice1.getUser()).thenReturn(Optional.of(user1));
        when(slice2.getUser()).thenReturn(Optional.of(user2));

        String response = target("/devices/DeviceWithLifeCycle/history/devicelifecyclestates").request().get(String.class);

        JsonModel model = JsonModel.model(response);

        assertThat(model.<Number> get("$.total")).isEqualTo(2);
        assertThat(model.<List<String>> get("$.deviceLifeCycleStateChanges[*].fromState")).containsExactly(null, "In Stock");
        assertThat(model.<List<String>> get("$.deviceLifeCycleStateChanges[*].toState")).containsExactly("In Stock", "commissioning");
        assertThat(model.<List<Number>> get("$.deviceLifeCycleStateChanges[*].modTime")).containsExactly(now.minusMillis(1000).toEpochMilli(), now.toEpochMilli());
        assertThat(model.<List<Number>> get("$.deviceLifeCycleStateChanges[*].author.id")).containsExactly(1, 2);
        assertThat(model.<List<String>> get("$.deviceLifeCycleStateChanges[*].author.name")).containsExactly("admin", "batch executor");
    }

    @Test
    public void getDeviceStatesHistoryNoStates() {
        Device device = mock(Device.class);
        when(deviceService.findByUniqueMrid("DeviceWithoutLifeCycle")).thenReturn(Optional.of(device));
        StateTimeline stateTimeline = mock(StateTimeline.class);
        when(device.getStateTimeline()).thenReturn(stateTimeline);
        when(stateTimeline.getSlices()).thenReturn(Arrays.asList());

        String response = target("/devices/DeviceWithoutLifeCycle/history/devicelifecyclestates").request().get(String.class);

        JsonModel model = JsonModel.model(response);

        assertThat(model.<Number> get("$.total")).isEqualTo(0);
        assertThat(model.<List<?>> get("$.deviceLifeCycleStateChanges")).hasSize(0);
    }

    private State mockState(String name) {
        State state = mock(State.class);
        when(state.getName()).thenReturn(name);
        return state;
    }

    private User mockUser(long id, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        return user;
    }

}
