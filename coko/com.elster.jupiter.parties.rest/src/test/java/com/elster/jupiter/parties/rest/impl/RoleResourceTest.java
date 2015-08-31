package com.elster.jupiter.parties.rest.impl;

import com.elster.jupiter.parties.PartyRole;
import com.jayway.jsonpath.JsonModel;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RoleResourceTest extends PartiesApplicationJerseyTest {

    @Test
    public void testGetRoleById(){
        PartyRole role = mock(PartyRole.class);
        when(role.getComponentName()).thenReturn("PTR");
        when(role.getMRID()).thenReturn("1");
        when(role.getName()).thenReturn("Role");
        when(role.getAliasName()).thenReturn("Alias");
        when(role.getDescription()).thenReturn("Description");
        when(role.getVersion()).thenReturn(1L);
        when(partyService.findPartyRoleByMRID("1")).thenReturn(Optional.of(role));

        String answer = target("/roles/1").request().get(String.class);
        JsonModel model = JsonModel.create(answer);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.roles")).hasSize(1);
        assertThat(model.<String>get("$.roles[0].componentName")).isEqualTo("PTR");
        assertThat(model.<String>get("$.roles[0].mRID")).isEqualTo("1");
        assertThat(model.<String>get("$.roles[0].name")).isEqualTo("Role");
        assertThat(model.<String>get("$.roles[0].aliasName")).isEqualTo("Alias");
        assertThat(model.<String>get("$.roles[0].description")).isEqualTo("Description");
        assertThat(model.<Number>get("$.roles[0].version")).isEqualTo(1);
    }
}
