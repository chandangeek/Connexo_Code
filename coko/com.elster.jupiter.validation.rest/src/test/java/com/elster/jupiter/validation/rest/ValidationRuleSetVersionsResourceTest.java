package com.elster.jupiter.validation.rest;

import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationVersionStatus;
import com.jayway.jsonpath.JsonModel;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidationRuleSetVersionsResourceTest extends BaseValidationRestTest {

    public static final Instant JUN_2014 = LocalDateTime.of(2014, 6, 1, 10, 0, 0).toInstant(ZoneOffset.UTC);

    @Test
    public void getCreateTasksTest() {

        Response response1 = target("/rulesetversions").request().get();
        assertThat(response1.getStatus()).isEqualTo(404);
    }

    @Test
    public void testGetRuleSetVersions(){
        mockValidationRuleSet(10);

        String response = target("/validation/10/versions").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);

        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List>get("$.versions")).hasSize(2);
        assertThat(jsonModel.<Integer>get("$.versions[0].id")).isEqualTo(11);
        assertThat(jsonModel.<String>get("$.versions[0].description")).isEqualTo("descriptionOfVersion");
        assertThat(jsonModel.<Number>get("$.versions[0].startDate")).isEqualTo(JUN_2014.toEpochMilli());

        assertThat(jsonModel.<Integer>get("$.versions[1].id")).isEqualTo(12);
        assertThat(jsonModel.<String>get("$.versions[1].description")).isEqualTo("descriptionOfVersion");
        assertThat(jsonModel.<Number>get("$.versions[1].startDate")).isEqualTo(JUN_2014.toEpochMilli());
    }


    @Test
    public void testUpdateRuleSetVersions() throws Exception{
        ValidationRuleSet ruleSet = mockValidationRuleSet(10);

        final ValidationRuleSetVersionInfo info = new ValidationRuleSetVersionInfo();
        info.startDate = JUN_2014.toEpochMilli();
        info.description = "blablabla";
        ValidationRuleSetVersion version = ruleSet.getRuleSetVersions().get(0);
        when(version.getDescription()).thenReturn("blablabla");
        when(ruleSet.updateRuleSetVersion(
                Matchers.eq(11L),
                Matchers.eq("blablabla"),
                Matchers.eq(JUN_2014))).
                thenReturn(version);

        Response response = target("/validation/10/versions/11").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());

        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(11);
        assertThat(jsonModel.<String>get("$.description")).isEqualTo("blablabla");
        assertThat(jsonModel.<Number>get("$.startDate")).isEqualTo(JUN_2014.toEpochMilli());
    }

    @Test
    public void testPutRuleSetVersions(){
        ValidationRuleSet ruleSet = mockValidationRuleSet(10);
        Response response = target("/validation/10/versions/11").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void testCreateRuleSetVersions() throws Exception{
        ValidationRuleSet ruleSet = mockValidationRuleSet(10);

        final ValidationRuleSetVersionInfo info = new ValidationRuleSetVersionInfo();
        info.startDate = JUN_2014.toEpochMilli();
        info.description = "blablabla";
        ValidationRuleSetVersion version = mockValidationRuleSetVersion(32, ruleSet);
        when(version.getDescription()).thenReturn(info.description);
        when(ruleSet.addRuleSetVersion(
                Matchers.eq("blablabla"),
                Matchers.eq(JUN_2014))).
                thenReturn(version);

        Response response = target("/validation/10/versions").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());

        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(32);
        assertThat(jsonModel.<String>get("$.description")).isEqualTo("blablabla");
        assertThat(jsonModel.<Number>get("$.startDate")).isEqualTo(JUN_2014.toEpochMilli());
    }

    private ValidationRuleSet mockValidationRuleSet(int id) {
        ValidationRuleSet ruleSet = mock(ValidationRuleSet.class);
        when(ruleSet.getId()).thenReturn(Long.valueOf(id));
        when(ruleSet.getName()).thenReturn("MyName");
        when(ruleSet.getDescription()).thenReturn("MyDescription");
        ValidationRuleSetVersion ruleSetVersion1 = mockValidationRuleSetVersion(11, ruleSet);
        ValidationRuleSetVersion ruleSetVersion2 = mockValidationRuleSetVersion(12, ruleSet);
        List versions = Arrays.asList(ruleSetVersion1, ruleSetVersion2);
        when(ruleSet.getRuleSetVersions()).thenReturn(versions);
        doReturn(Optional.of(ruleSet)).when(validationService).getValidationRuleSet(id);
        return ruleSet;
    }

    private ValidationRuleSetVersion mockValidationRuleSetVersion(long id, ValidationRuleSet ruleSet){
        ValidationRuleSetVersion ruleSetVersion = mock(ValidationRuleSetVersion.class);
        when(ruleSetVersion.getDescription()).thenReturn("descriptionOfVersion");
        when(ruleSetVersion.getId()).thenReturn(id);
        when(ruleSetVersion.getStartDate()).thenReturn(JUN_2014);
        when(ruleSetVersion.getRuleSet()).thenReturn(ruleSet);
        when(ruleSetVersion.getStatus()).thenReturn(ValidationVersionStatus.CURRENT);
        return ruleSetVersion;
    }

}