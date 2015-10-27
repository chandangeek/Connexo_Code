package com.elster.insight.usagepoint.data.rest.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.rest.impl.MetrologyConfigurationInfo;
import com.jayway.jsonpath.JsonModel;

public class MetrologyConfigurationResourceTest extends UsagePointConfigurationRestApplicationJerseyTest {

    @Mock
    private MetrologyConfiguration config1, config2;
    
    
    public MetrologyConfigurationResourceTest() {
    }

    @Before
    public void setUpStubs() {
        
        
        
        List<MetrologyConfiguration> configs = new ArrayList<MetrologyConfiguration>();
        configs.add(config1);
        configs.add(config2);
        when(usagePointConfigurationService.findAllMetrologyConfigurations()).thenReturn(configs);
        when(usagePointConfigurationService.findMetrologyConfiguration(1)).thenReturn(Optional.of(config1));
        when(usagePointConfigurationService.findMetrologyConfiguration(2)).thenReturn(Optional.of(config2));
        
        
        when(config1.getName()).thenReturn("config1");
        when(config2.getName()).thenReturn("config2");
        
        when(config1.getId()).thenReturn(1L);
        when(config2.getId()).thenReturn(2L);
        
    }

    @Test
    public void testAllMetrologyConfigurationsInfo() {
        String json = target("metrologyconfigurations").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Integer> get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<Integer> get("$.metrologyconfigurations[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String> get("$.metrologyconfigurations[0].name")).isEqualTo("config1");
        assertThat(jsonModel.<Integer> get("$.metrologyconfigurations[1].id")).isEqualTo(2);
        assertThat(jsonModel.<String> get("$.metrologyconfigurations[1].name")).isEqualTo("config2");
    }
    
    @Test
    public void testMetrologyConfigurationsInfo() {
        String json = target("metrologyconfigurations/1").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Integer> get("$.id")).isEqualTo(1);
        assertThat(jsonModel.<String> get("$.name")).isEqualTo("config1");

        json = target("metrologyconfigurations/2").request().get(String.class);
        jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Integer> get("$.id")).isEqualTo(2);
        assertThat(jsonModel.<String> get("$.name")).isEqualTo("config2");
    }
    
    @Test
    public void testCreateMetrologyConfiguration() {
        MetrologyConfigurationInfo metrologyConfigurationInfo = new MetrologyConfigurationInfo();
        metrologyConfigurationInfo.name = "newName";
        Entity<MetrologyConfigurationInfo> json = Entity.json(metrologyConfigurationInfo);
        MetrologyConfiguration metrologyConfiguration = mock(MetrologyConfiguration.class);
        when(metrologyConfiguration.getId()).thenReturn(3L);
        when(metrologyConfiguration.getVersion()).thenReturn(1L);
        when(metrologyConfiguration.getName()).thenReturn("newName");

        when(transactionService.execute(Matchers.anyObject())).thenReturn(metrologyConfiguration);
        
        Response response = target("/metrologyconfigurations/").request().post(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }
    
    @Test
    public void testUpdateMetrologyConfiguration() {
        MetrologyConfigurationInfo metrologyConfigurationInfo = new MetrologyConfigurationInfo();
        metrologyConfigurationInfo.name = "newName";
        Entity<MetrologyConfigurationInfo> json = Entity.json(metrologyConfigurationInfo);
        MetrologyConfiguration metrologyConfiguration = mock(MetrologyConfiguration.class);
        when(metrologyConfiguration.getId()).thenReturn(2L);
        when(metrologyConfiguration.getVersion()).thenReturn(1L);
        when(metrologyConfiguration.getName()).thenReturn("newName");

        when(transactionService.execute(Matchers.anyObject())).thenReturn(metrologyConfiguration);
        Response response = target("/metrologyconfigurations/2").request().put(json);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

}