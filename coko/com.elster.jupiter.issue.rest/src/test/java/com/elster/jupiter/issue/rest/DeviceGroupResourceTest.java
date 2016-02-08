package com.elster.jupiter.issue.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceGroupResourceTest extends IssueRestApplicationJerseyTest {
    @Mock
    EndDeviceGroup endDeviceGroup;

    @Test
    public void testGetDeviceGroupsEmpty(){
        Query<EndDeviceGroup> query = mock(Query.class);
        when(query.select(Matchers.<Condition>anyObject(), Matchers.<Order>anyObject())).thenReturn(Collections.<EndDeviceGroup>emptyList());
        when(meteringGroupsService.getEndDeviceGroupQuery()).thenReturn(query);


        Map<String, Object> map = target("/devicegroups").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).isEmpty();
    }

    @Test
    public void testGetDeviceGroupsWithoutParams() {
        Query<EndDeviceGroup> query = mock(Query.class);
        when(endDeviceGroup.getId()).thenReturn(1L);
        when(endDeviceGroup.getName()).thenReturn("name");
        List<EndDeviceGroup> endDeviceGroupList = Arrays.asList(endDeviceGroup);
        when(meteringGroupsService.getEndDeviceGroupQuery()).thenReturn(query);
        when(query.select(Matchers.<Condition>anyObject(), Matchers.<Order>anyObject())).thenReturn(endDeviceGroupList);
        Map<String, Object> map = target("/devicegroups").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        List data = (List) map.get("data");
        assertThat(data).hasSize(1);

        assertThat(((Map) data.get(0)).get("name")).isEqualTo(endDeviceGroup.getName());
    }
}
