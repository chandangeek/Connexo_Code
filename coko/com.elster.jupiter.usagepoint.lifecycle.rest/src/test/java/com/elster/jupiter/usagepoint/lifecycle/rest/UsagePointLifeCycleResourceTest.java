package com.elster.jupiter.usagepoint.lifecycle.rest;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycle;

import com.jayway.jsonpath.JsonModel;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointLifeCycleResourceTest extends UsagePointLifeCycleApplicationTest {
    @Mock
    private UsagePointLifeCycle lifeCycle;

    @Test
    public void testGetAllLifeCycles() {
        Finder<UsagePointLifeCycle> finder = FakeBuilder.initBuilderStub(Collections.singletonList(lifeCycle), Finder.class);
        when(usagePointLifeCycleConfigurationService.getUsagePointLifeCycles()).thenReturn(finder);
        when(lifeCycle.getId()).thenReturn(12L);
        when(lifeCycle.getName()).thenReturn("Life cycle");
        when(lifeCycle.getVersion()).thenReturn(4L);

        String response = target("/lifecycle").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List>get("$.lifeCycles")).hasSize(1);
        assertThat(model.<Number>get("$.lifeCycles[0].id")).isEqualTo(12);
        assertThat(model.<Number>get("$.lifeCycles[0].version")).isEqualTo(4);
        assertThat(model.<String>get("$.lifeCycles[0].name")).isEqualTo("Life cycle");
        assertThat(model.<Boolean>get("$.lifeCycles[0].isDefault")).isEqualTo(false);
    }

    @Test
    public void testGetEmptyList() {
        Finder<UsagePointLifeCycle> finder = FakeBuilder.initBuilderStub(Collections.emptyList(), Finder.class);
        when(usagePointLifeCycleConfigurationService.getUsagePointLifeCycles()).thenReturn(finder);

        String response = target("/lifecycle").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(0);
        assertThat(model.<List>get("$.lifeCycles")).hasSize(0);
    }

    @Test
    public void testGetLifeCycleById() {
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.of(lifeCycle));
        when(lifeCycle.getId()).thenReturn(12L);
        when(lifeCycle.getName()).thenReturn("Life cycle");
        when(lifeCycle.getVersion()).thenReturn(4L);

        String response = target("/lifecycle/12").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.version")).isEqualTo(4);
        assertThat(model.<String>get("$.name")).isEqualTo("Life cycle");
    }

    @Test
    public void testGetLifeCycleByIdUnExisting() throws Exception {
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.empty());

        Response response = target("/lifecycle/12").request().get();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(model.<Number>get("$.message")).isEqualTo("No life cycle with id 12");
        assertThat(model.<String>get("$.error")).isEqualTo("no.such.life.cycle");
    }

    @Test
    public void testUpdateLifeCycle() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));
        when(lifeCycle.getId()).thenReturn(12L);
        when(lifeCycle.getName()).thenReturn("Life cycle changed");
        when(lifeCycle.getVersion()).thenReturn(4L);

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.version = 4L;
        info.name = "Life cycle changed";
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12").request().put(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.version")).isEqualTo(4);
        assertThat(model.<String>get("$.name")).isEqualTo("Life cycle changed");
    }

    @Test
    public void testUpdateLifeCycleConcurrent() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 3L)).thenReturn(Optional.empty());
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.of(lifeCycle));

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.version = 3L;
        info.name = "Life cycle";
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12").request().put(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        assertThat(model.<Number>get("$.message")).isEqualTo("Failed to save 'Life cycle'");
        assertThat(model.<String>get("$.error")).isEqualTo("Life cycle has changed since the page was last updated.");
    }

    @Test
    public void testDeleteLifeCycle() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 4L)).thenReturn(Optional.of(lifeCycle));

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.version = 4L;
        info.name = "Life cycle";
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12").request().build(HttpMethod.DELETE, json).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void testDeleteLifeCycleConcurrent() throws Exception {
        when(usagePointLifeCycleConfigurationService.findAndLockUsagePointLifeCycleByIdAndVersion(12L, 3L)).thenReturn(Optional.empty());
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.of(lifeCycle));
        when(lifeCycle.getId()).thenReturn(12L);
        when(lifeCycle.getName()).thenReturn("Life cycle");
        when(lifeCycle.getVersion()).thenReturn(4L);

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.version = 3L;
        info.name = "Life cycle";
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12").request().build(HttpMethod.DELETE, json).invoke();
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        assertThat(model.<String>get("$.error")).isEqualTo("Life cycle has changed since the page was last updated.");
    }

    @Test
    public void testCreateLifeCycle() throws Exception {
        when(usagePointLifeCycleConfigurationService.newUsagePointLifeCycle("Life cycle")).thenReturn(lifeCycle);
        when(lifeCycle.getId()).thenReturn(12L);
        when(lifeCycle.getName()).thenReturn("Life cycle");
        when(lifeCycle.getVersion()).thenReturn(1L);

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.version = 4L;
        info.name = "Life cycle";
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle").request().post(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(12);
        assertThat(model.<Number>get("$.version")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo("Life cycle");
    }

    @Test
    public void testCreateLifeCycleConstraintViolation() throws Exception {
        ConstraintViolation violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("msg");
        Path nodePath = mock(Path.class);
        when(nodePath.toString()).thenReturn("name");
        when(violation.getPropertyPath()).thenReturn(nodePath);
        ConstraintViolationException violationException = mock(ConstraintViolationException.class);
        when(violationException.getConstraintViolations()).thenReturn(Collections.singleton(violation));
        when(usagePointLifeCycleConfigurationService.newUsagePointLifeCycle(null)).thenThrow(violationException);

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle").request().post(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(model.<String>get("$.errors[0].id")).isEqualTo("name");
        assertThat(model.<String>get("$.errors[0].msg")).isEqualTo("msg");
    }

    @Test
    public void testCloneLifeCycle() throws Exception {
        UsagePointLifeCycle clone = mock(UsagePointLifeCycle.class);
        when(clone.getId()).thenReturn(13L);
        when(clone.getName()).thenReturn("Clone");
        when(clone.getVersion()).thenReturn(1L);
        when(usagePointLifeCycleConfigurationService.findUsagePointLifeCycle(12L)).thenReturn(Optional.of(lifeCycle));
        when(usagePointLifeCycleConfigurationService.cloneUsagePointLifeCycle("Clone", lifeCycle)).thenReturn(clone);

        UsagePointLifeCycleInfo info = new UsagePointLifeCycleInfo();
        info.id = 12L;
        info.name = "Clone";
        Entity<UsagePointLifeCycleInfo> json = Entity.json(info);

        Response response = target("/lifecycle/12/clone").request().post(json);
        JsonModel model = JsonModel.model((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Number>get("$.id")).isEqualTo(13);
        assertThat(model.<Number>get("$.version")).isEqualTo(1);
        assertThat(model.<String>get("$.name")).isEqualTo("Clone");
    }

}
