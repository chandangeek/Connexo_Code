/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.RelativePeriodFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PredefinedPropertyValuesInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleBuilder;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationVersionStatus;
import com.elster.jupiter.validation.Validator;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ValidationResourceTest extends BaseValidationRestTest {
    public static final long OK_VERSION = 23L;
    public static final long BAD_VERSION = 21L;
    public static final long V_RULE_SET_ID = 14L;
    public static final long V_RULE_VERSION_ID = 18L;
    public static final long V_RULE_ID = 3L;
    private static final String APPLICATION_HEADER_PARAM = "X-CONNEXO-APPLICATION-NAME";
    private final Instant startDate = Instant.ofEpochMilli(1412341200000L);

    @Test
    public void testGetValidationRuleSetsNoRuleSets() {
        mockValidationRuleSets();
        ValidationRuleSetInfos response = target("/validation").request().header(APPLICATION_HEADER_PARAM, "APP").get(ValidationRuleSetInfos.class);

        assertThat(response.total).isEqualTo(0);
        assertThat(response.ruleSets).hasSize(0);
    }

    @Test
    public void testGetValidationRuleSets() {
        mockValidationRuleSets(mockValidationRuleSet(V_RULE_SET_ID));
        ValidationRuleSetInfos response = target("/validation").request().header(APPLICATION_HEADER_PARAM, "APP").get(ValidationRuleSetInfos.class);
        assertThat(response.total).isEqualTo(1);

        List<ValidationRuleSetInfo> ruleSetInfos = response.ruleSets;
        assertThat(ruleSetInfos).hasSize(1);

        ValidationRuleSetInfo ruleSetInfo = ruleSetInfos.get(0);
        assertThat(ruleSetInfo.name).isEqualTo("MyName");
        assertThat(ruleSetInfo.id).isEqualTo(V_RULE_SET_ID);
        assertThat(ruleSetInfo.description).isEqualTo("MyDescription");
        assertThat(ruleSetInfo.version).isEqualTo(OK_VERSION);
    }

    @Test
    public void testGetValidationRuleSetNotFound() {
        when(validationService.getValidationRuleSet(V_RULE_SET_ID)).thenReturn(Optional.empty());

        Response response = target("/validation/" + V_RULE_SET_ID).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testDeleteRuleSet() throws Exception {
        ValidationRuleSet validationRuleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetInfo info = new ValidationRuleSetInfo(validationRuleSet);
        Response response = target("/validation/" + V_RULE_SET_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(validationRuleSet).delete();
    }

    @Test
    public void testDeleteRuleSetBadVersion() throws Exception {
        ValidationRuleSet validationRuleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetInfo info = new ValidationRuleSetInfo(validationRuleSet);
        info.version = BAD_VERSION;
        Response response = target("/validation/" + V_RULE_SET_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(validationRuleSet, never()).delete();
    }

    @Test
    public void testDeleteVersion() throws Exception {
        ValidationRuleSet validationRuleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(V_RULE_VERSION_ID, validationRuleSet);
        ValidationRuleSetVersionInfo info = new ValidationRuleSetVersionInfo(ruleSetVersion);

        Response response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void testDeleteVersionBadVersion() throws Exception {
        ValidationRuleSet validationRuleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(V_RULE_VERSION_ID, validationRuleSet);
        ValidationRuleSetVersionInfo info = new ValidationRuleSetVersionInfo(ruleSetVersion);
        info.version = BAD_VERSION;

        Response response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testDeleteVersionBadParentVersion() throws Exception {
        ValidationRuleSet validationRuleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(V_RULE_VERSION_ID, validationRuleSet);
        ValidationRuleSetVersionInfo info = new ValidationRuleSetVersionInfo(ruleSetVersion);
        info.parent.version = BAD_VERSION;

        Response response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testGetValidationRuleSet() {
        mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetInfo ruleSetInfo = target("/validation/" + V_RULE_SET_ID).request().get(ValidationRuleSetInfo.class);

        assertThat(ruleSetInfo.id).isEqualTo(V_RULE_SET_ID);
        assertThat(ruleSetInfo.name).isEqualTo("MyName");
        assertThat(ruleSetInfo.description).isEqualTo("MyDescription");
        assertThat(ruleSetInfo.version).isEqualTo(OK_VERSION);
    }

    @Test
    public void testGetValidationRulesNoVersions() {
        mockValidationRuleSets(mockValidationRuleSet(V_RULE_SET_ID));

        ValidationRuleSetVersionInfos versionInfos = target("/validation/" + V_RULE_SET_ID + "/versions").request().get(ValidationRuleSetVersionInfos.class);
        assertThat(versionInfos.total).isEqualTo(0);
    }

    @Test
    public void testGetValidationRulesVersionsRules() {
        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(V_RULE_VERSION_ID, ruleSet);
        mockValidationRule(V_RULE_ID, ruleSetVersion);
        mockValidationRuleSets(ruleSet);

        String response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID + "/rules").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.rules")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.rules[0].id")).isEqualTo(((Number) V_RULE_ID).intValue());
        assertThat(jsonModel.<String>get("$.rules[0].name")).isEqualTo("MyRule");
        assertThat(jsonModel.<String>get("$.rules[0].implementation")).isEqualTo("com.blablabla.Validator");
        assertThat(jsonModel.<String>get("$.rules[0].displayName")).isEqualTo("My rule");
        assertThat(jsonModel.<Boolean>get("$.rules[0].active")).isEqualTo(true);
        assertThat(jsonModel.<Number>get("$.rules[0].version")).isEqualTo(((Number) OK_VERSION).intValue());
    }

    @Test
    public void testGetValidatorsNoValidators() throws IOException {
        when(validationService.getAvailableValidators(QualityCodeSystem.MDC)).thenReturn(Collections.emptyList());

        Response response = target("/validation/validators").request().header(APPLICATION_HEADER_PARAM, "MDC").get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel body = JsonModel.create((ByteArrayInputStream)response.getEntity());
        assertThat(body.<Integer>get("$.total")).isZero();
        assertThat(body.<List>get("$.validators")).isEmpty();
    }

    @Test
    public void testGetValidators() throws IOException {
        List<Validator> mockValidator = Arrays.asList(mockValidator("B Validator"), mockValidator("A Validator"));
        when(validationService.getAvailableValidators(QualityCodeSystem.MDC)).thenReturn(mockValidator);

        Response response = target("/validation/validators").request().header(APPLICATION_HEADER_PARAM, "MDC").get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel body = JsonModel.create((ByteArrayInputStream)response.getEntity());
        assertThat(body.<Integer>get("$.total")).isEqualTo(2);
        assertThat(body.<List>get("$.validators")).hasSize(2);
        assertThat(body.<String>get("$.validators[0].displayName")).isEqualTo("A Validator");
        assertThat(body.<String>get("$.validators[0].implementation")).isNotNull();
        assertThat(body.<String>get("$.validators[1].displayName")).isEqualTo("B Validator");
        assertThat(body.<String>get("$.validators[1].implementation")).isNotNull();
        assertThat(body.<List>get("$.validators[0].properties")).hasSize(1);
        JsonModel property0 = JsonModel.model(body.<Object>get("$.validators[0].properties[0]"));
        assertThat(property0.<String>get("key")).isEqualTo("listvalue");
        assertThat(property0.<Boolean>get("required")).isEqualTo(false);
        JsonModel predefinedPropertyValuesInfo = JsonModel.model(property0.<Object>get("$.propertyTypeInfo.predefinedPropertyValuesInfo"));
        assertThat(predefinedPropertyValuesInfo.<String>get("$.selectionMode")).isEqualTo(PropertySelectionMode.LIST.name());
        assertThat(predefinedPropertyValuesInfo.<List>get("$.possibleValues")).hasSize(2);
        assertThat(predefinedPropertyValuesInfo.<String>get("$.possibleValues[0].id")).isEqualTo("1");
        assertThat(predefinedPropertyValuesInfo.<String>get("$.possibleValues[0].name")).isEqualTo("first");
        assertThat(predefinedPropertyValuesInfo.<String>get("$.possibleValues[1].id")).isEqualTo("2");
        assertThat(predefinedPropertyValuesInfo.<String>get("$.possibleValues[1].name")).isEqualTo("second");
    }

    @Test
    public void testGetNoRules() {
        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);
        mockValidationRuleSetVersion(V_RULE_VERSION_ID, ruleSet);
        Response response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID + "/rules/" + V_RULE_ID).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetValidationRules() {
        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(V_RULE_VERSION_ID, ruleSet);
        mockValidationRule(V_RULE_ID, ruleSetVersion);
        mockValidationRuleSets(ruleSet);

        String response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID + "/rules").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);

        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);

        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.rules")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.rules[0].id")).isEqualTo(((Number) V_RULE_ID).intValue());
        assertThat(jsonModel.<String>get("$.rules[0].name")).isEqualTo("MyRule");
        assertThat(jsonModel.<String>get("$.rules[0].implementation")).isEqualTo("com.blablabla.Validator");
        assertThat(jsonModel.<String>get("$.rules[0].displayName")).isEqualTo("My rule");
        assertThat(jsonModel.<Boolean>get("$.rules[0].active")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.rules[0].readingTypes[0].mRID")).isEqualTo("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");

        assertThat(jsonModel.<List>get("$.rules[0].properties")).hasSize(5);
        assertThat(jsonModel.<String>get("$.rules[0].properties[0].key")).isEqualTo("number");
        assertThat(jsonModel.<Boolean>get("$.rules[0].properties[0].required")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.rules[0].properties[0].propertyValueInfo.value")).isEqualTo(13);
        assertThat(jsonModel.<Integer>get("$.rules[0].properties[0].propertyValueInfo.defaultValue")).isEqualTo(null);

        assertThat(jsonModel.<String>get("$.rules[0].properties[1].key")).isEqualTo("nullableboolean");
        assertThat(jsonModel.<Boolean>get("$.rules[0].properties[1].required")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.rules[0].properties[1].propertyValueInfo.value")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.rules[0].properties[1].propertyValueInfo.defaultValue")).isEqualTo(null);

        assertThat(jsonModel.<String>get("$.rules[0].properties[2].key")).isEqualTo("boolean");
        assertThat(jsonModel.<Boolean>get("$.rules[0].properties[2].required")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.rules[0].properties[2].propertyValueInfo.value")).isEqualTo(false);
        assertThat(jsonModel.<Integer>get("$.rules[0].properties[2].propertyValueInfo.defaultValue")).isEqualTo(null);

        assertThat(jsonModel.<String>get("$.rules[0].properties[3].key")).isEqualTo("text");
        assertThat(jsonModel.<Boolean>get("$.rules[0].properties[3].required")).isEqualTo(true);
        assertThat(jsonModel.<String>get("$.rules[0].properties[3].propertyValueInfo.value")).isEqualTo("string");
        assertThat(jsonModel.<Integer>get("$.rules[0].properties[3].propertyValueInfo.defaultValue")).isEqualTo(null);

        assertThat(jsonModel.<String>get("$.rules[0].properties[4].key")).isEqualTo("listvalue");
        assertThat(jsonModel.<Boolean>get("$.rules[0].properties[4].required")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.rules[0].properties[4].propertyValueInfo.defaultValue")).isEqualTo(null);
        assertThat(jsonModel.<List>get("$.rules[0].properties[4].propertyValueInfo.value")).hasSize(2);
        assertThat(jsonModel.<String>get("$.rules[0].properties[4].propertyValueInfo.value[0]")).isEqualTo("1");
        assertThat(jsonModel.<String>get("$.rules[0].properties[4].propertyValueInfo.value[1]")).isEqualTo("2");
    }

    @Test
    public void testAddValidationRule() throws Exception {
        final ValidationRuleInfo info = new ValidationRuleInfo();
        info.name = "MyRule";
        info.implementation = "com.blablabla.Validator";
        info.properties = createPropertyInfos();

        Entity<ValidationRuleInfo> entity = Entity.json(info);

        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(V_RULE_VERSION_ID, ruleSet);
        ValidationRule rule = mockValidationRule(V_RULE_ID, ruleSetVersion);
        ValidationRuleBuilder builder = FakeBuilder.initBuilderStub(rule, ValidationRuleBuilder.class, ValidationRuleBuilder.PropertyBuilder.class);
        when(ruleSetVersion.addRule(Matchers.eq(ValidationAction.FAIL), Matchers.eq(info.implementation), Matchers.eq(info.name))).thenReturn(builder);
        mockPropertyInfos(info);
        Response response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID + "/rules").request().post(entity);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("MyRule");

        verify(builder).havingProperty("number");
        verify((ValidationRuleBuilder.PropertyBuilder) builder).withValue(BigDecimal.valueOf(10.0));
        verify(builder).havingProperty("nullableboolean");
        verify((ValidationRuleBuilder.PropertyBuilder) builder).withValue(false);
        verify(builder).havingProperty("boolean");
        verify((ValidationRuleBuilder.PropertyBuilder) builder).withValue(true);
        verify(builder).havingProperty("text");
        verify((ValidationRuleBuilder.PropertyBuilder) builder).withValue("string");
        verify(builder).havingProperty(Matchers.eq("listvalue"));
        verify((ValidationRuleBuilder.PropertyBuilder) builder).withValue(Matchers.isA(List.class));
    }

    @Test
    public void testAddValidationRuleWarnOnly() throws Exception {
        final ValidationRuleInfo info = new ValidationRuleInfo();
        info.name = "MyRule";
        info.implementation = "com.blablabla.Validator";
        info.properties = createPropertyInfos();
        info.action = ValidationAction.WARN_ONLY;

        Entity<ValidationRuleInfo> entity = Entity.json(info);

        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(V_RULE_VERSION_ID, ruleSet);
        ValidationRule rule = mockValidationRule(V_RULE_ID, ruleSetVersion);
        when(rule.getAction()).thenReturn(ValidationAction.WARN_ONLY);
        ValidationRuleBuilder builder = FakeBuilder.initBuilderStub(rule, ValidationRuleBuilder.class, ValidationRuleBuilder.PropertyBuilder.class);
        when(ruleSetVersion.addRule(Matchers.eq(ValidationAction.WARN_ONLY), Matchers.eq(info.implementation), Matchers.eq(info.name))).thenReturn(builder);
        mockPropertyInfos(info);
        Response response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID + "/rules").request().post(entity);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("MyRule");
        assertThat(jsonModel.<String>get("$.action")).isEqualTo(ValidationAction.WARN_ONLY.name());

        verify(builder).havingProperty("number");
        verify((ValidationRuleBuilder.PropertyBuilder) builder).withValue(BigDecimal.valueOf(10.0));
        verify(builder).havingProperty("nullableboolean");
        verify((ValidationRuleBuilder.PropertyBuilder) builder).withValue(false);
        verify(builder).havingProperty("boolean");
        verify((ValidationRuleBuilder.PropertyBuilder) builder).withValue(true);
        verify(builder).havingProperty("text");
        verify((ValidationRuleBuilder.PropertyBuilder) builder).withValue("string");
        verify(builder).havingProperty(Matchers.eq("listvalue"));
        verify((ValidationRuleBuilder.PropertyBuilder) builder).withValue(Matchers.isA(List.class));
    }

    @Test
    public void testAddValidationRuleToNonExistingRuleSet() {
        final ValidationRuleInfo info = new ValidationRuleInfo();
        info.name = "MyRule";
        info.implementation = "com.blablabla.Validator";
        info.properties = createPropertyInfos();

        Entity<ValidationRuleInfo> entity = Entity.json(info);

        when(validationService.getValidationRuleSet(666)).thenReturn(Optional.empty());
        Response response = target("/validation/666/versions/11/rules").request().post(entity);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testUpdateValidationRuleSet() throws Exception {
        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);

        final ValidationRuleSetInfo info = new ValidationRuleSetInfo(ruleSet);
        info.name = "MyRuleUpdated";
        info.description = "blablabla";

        Response response = target("/validation/" + V_RULE_SET_ID).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(ruleSet).setName(stringArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(info.name);
        verify(ruleSet).setDescription(stringArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(info.description);
        verify(ruleSet, times(1)).save();
    }

    @Test
    public void testUpdateValidationRuleSetBadVersion() throws Exception {
        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);

        final ValidationRuleSetInfo info = new ValidationRuleSetInfo(ruleSet);
        info.name = "MyRuleUpdated";
        info.description = "blablabla";
        info.version = BAD_VERSION;

        Response response = target("/validation/" + V_RULE_SET_ID).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(ruleSet, never()).save();
    }

    @Test
    public void testCreateValidationRuleSetInfo() throws Exception {
        ValidationRuleSetInfo info = new ValidationRuleSetInfo();
        info.name = "ruleset";
        info.description = "desc";

        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);
        when(validationService.createValidationRuleSet(info.name, QualityCodeSystem.MDC, info.description)).thenReturn(ruleSet);
        Response response = target("/validation").request().header(APPLICATION_HEADER_PARAM, "MDC").post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testGetValidationRuleInfo() throws Exception {
        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(V_RULE_VERSION_ID, ruleSet);
        mockValidationRule(V_RULE_ID, ruleSetVersion);
        Response response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID + "/rules/" + V_RULE_ID).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("MyRule");
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(((Number) V_RULE_ID).intValue());
    }

    @Test
    public void testGetRuleSetUsage() throws Exception {
        ValidationRuleSet validationRuleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSet validationRuleSet2 = mockValidationRuleSet(V_RULE_SET_ID + 1);
        when(validationService.isValidationRuleSetInUse(validationRuleSet)).thenReturn(true);
        when(validationService.isValidationRuleSetInUse(validationRuleSet2)).thenReturn(false);
        Response response = target("/validation/" + V_RULE_SET_ID + "/usage").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.isInUse")).isEqualTo(true);
    }

    @Test
    public void testEditValidationRule() throws Exception {
        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(V_RULE_VERSION_ID, ruleSet);
        ValidationRule rule = mockValidationRule(V_RULE_ID, ruleSetVersion);

        final ValidationRuleInfo info = new ValidationRuleInfo();
        info.name = "MyRuleUpdated";
        info.implementation = "com.blablabla.Validator";
        info.properties = new ArrayList<>();
        info.action = ValidationAction.FAIL;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(ruleSetVersion.getId(), ruleSetVersion.getVersion());

        when(rule.getName()).thenReturn("MyRuleUpdated");
        when(ruleSetVersion.updateRule(
                Matchers.eq(V_RULE_ID),
                Matchers.eq("MyRuleUpdated"),
                Matchers.eq(false),
                Matchers.eq(ValidationAction.FAIL),
                Matchers.eq(new ArrayList<>()),
                Matchers.eq(new HashMap<>()))).
                thenReturn(rule);

        Entity<ValidationRuleInfo> entity = Entity.json(info);
        Response response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID + "/rules/" + V_RULE_ID).request().put(entity);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());

        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.rules")).hasSize(1);
        assertThat(jsonModel.<String>get("$.rules[0].name")).isEqualTo("MyRuleUpdated");
        assertThat(jsonModel.<String>get("$.rules[0].action")).isEqualTo(ValidationAction.FAIL.name());
    }


    @Test
    public void testEditValidationRuleBadVersion() throws Exception {
        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(V_RULE_VERSION_ID, ruleSet);
        ValidationRule rule = mockValidationRule(V_RULE_ID, ruleSetVersion);

        final ValidationRuleInfo info = new ValidationRuleInfo();
        info.name = "MyRuleUpdated";
        info.implementation = "com.blablabla.Validator";
        info.properties = new ArrayList<>();
        info.action = ValidationAction.FAIL;
        info.version = BAD_VERSION;
        info.parent = new VersionInfo<>(ruleSetVersion.getId(), ruleSetVersion.getVersion());

        Entity<ValidationRuleInfo> entity = Entity.json(info);
        Response response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID + "/rules/" + V_RULE_ID).request().put(entity);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testEditValidationRuleBadParentVersion() throws Exception {
        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(V_RULE_VERSION_ID, ruleSet);
        ValidationRule rule = mockValidationRule(V_RULE_ID, ruleSetVersion);

        final ValidationRuleInfo info = new ValidationRuleInfo();
        info.name = "MyRuleUpdated";
        info.implementation = "com.blablabla.Validator";
        info.properties = new ArrayList<>();
        info.action = ValidationAction.FAIL;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(ruleSetVersion.getId(), ruleSetVersion.getVersion());
        info.parent.version = BAD_VERSION;

        Entity<ValidationRuleInfo> entity = Entity.json(info);
        Response response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID + "/rules/" + V_RULE_ID).request().put(entity);
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testEditValidationRuleWarnOnly() throws Exception {
        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(V_RULE_VERSION_ID, ruleSet);
        ValidationRule rule = mockValidationRule(V_RULE_ID, ruleSetVersion);

        final ValidationRuleInfo info = new ValidationRuleInfo();
        info.name = "MyRuleUpdated";
        info.implementation = "com.blablabla.Validator";
        info.properties = new ArrayList<>();
        info.action = ValidationAction.WARN_ONLY;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(ruleSetVersion.getId(), ruleSetVersion.getVersion());

        when(rule.getName()).thenReturn("MyRuleUpdated");
        when(rule.getAction()).thenReturn(ValidationAction.WARN_ONLY);
        when(ruleSetVersion.updateRule(
                Matchers.eq(V_RULE_ID),
                Matchers.eq("MyRuleUpdated"),
                Matchers.eq(false),
                Matchers.eq(ValidationAction.WARN_ONLY),
                Matchers.eq(new ArrayList<>()),
                Matchers.eq(new HashMap<>()))).
                thenReturn(rule);

        Entity<ValidationRuleInfo> entity = Entity.json(info);
        Response response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID + "/rules/" + V_RULE_ID).request().put(entity);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());

        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List>get("$.rules")).hasSize(1);
        assertThat(jsonModel.<String>get("$.rules[0].name")).isEqualTo("MyRuleUpdated");
        assertThat(jsonModel.<String>get("$.rules[0].action")).isEqualTo(ValidationAction.WARN_ONLY.name());
    }

    @Test
    public void testDeleteValidationRule() {
        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(V_RULE_VERSION_ID, ruleSet);
        ValidationRule rule = mockValidationRule(V_RULE_ID, ruleSetVersion);

        ValidationRuleInfo info = new ValidationRuleInfo();
        info.id = V_RULE_ID;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(ruleSetVersion.getId(), ruleSetVersion.getVersion());
        Response response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID + "/rules/" + V_RULE_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void testDeleteValidationRuleNoRuleSetVersion() {
        when(validationService.findValidationRuleSetVersion(13)).thenReturn(Optional.empty());
        when(validationService.findAndLockValidationRuleSetVersionByIdAndVersion(13, OK_VERSION)).thenReturn(Optional.empty());
        when(validationService.findValidationRule(V_RULE_ID)).thenReturn(Optional.empty());

        ValidationRuleInfo info = new ValidationRuleInfo();
        info.id = V_RULE_ID;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(Long.valueOf(13), OK_VERSION);
        Response response = target("/validation/" + V_RULE_SET_ID + "/versions/13/rules/" + V_RULE_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testDeleteValidationRuleNoRule() {
        ValidationRuleSet ruleSet = mockValidationRuleSet(V_RULE_SET_ID);
        ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(V_RULE_VERSION_ID, ruleSet);
        ValidationRuleInfo info = new ValidationRuleInfo();
        info.id = V_RULE_ID;
        info.version = OK_VERSION;
        info.parent = new VersionInfo<>(ruleSetVersion.getId(), ruleSetVersion.getVersion());
        doReturn(Optional.empty()).when(validationService).findValidationRule(V_RULE_ID);
        doReturn(Optional.empty()).when(validationService).findAndLockValidationRuleByIdAndVersion(V_RULE_ID, OK_VERSION);
        Response response = target("/validation/" + V_RULE_SET_ID + "/versions/" + V_RULE_VERSION_ID + "/rules/" + V_RULE_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    private void mockValidationRuleSets(ValidationRuleSet... validationRuleSets) {
        Query<ValidationRuleSet> query = mock(Query.class);
        when(validationService.getRuleSetQuery()).thenReturn(query);
        RestQuery<ValidationRuleSet> restQuery = mock(RestQuery.class);
        when(restQueryService.wrap(query)).thenReturn(restQuery);
        when(restQuery.select(any(QueryParameters.class), any(Order.class))).thenReturn(Arrays.asList(validationRuleSets));
    }

    private ValidationRuleSet mockValidationRuleSet(long id) {
        ValidationRuleSet ruleSet = mock(ValidationRuleSet.class);
        when(ruleSet.getId()).thenReturn(Long.valueOf(id));
        when(ruleSet.getName()).thenReturn("MyName");
        when(ruleSet.getDescription()).thenReturn("MyDescription");
        when(ruleSet.getVersion()).thenReturn(OK_VERSION);
        List versions = new ArrayList<>();
        when(ruleSet.getRuleSetVersions()).thenReturn(versions);

        doReturn(Optional.of(ruleSet)).when(validationService).getValidationRuleSet(id);
        doReturn(Optional.of(ruleSet)).when(validationService).findAndLockValidationRuleSetByIdAndVersion(id, OK_VERSION);
        doReturn(Optional.empty()).when(validationService).findAndLockValidationRuleSetByIdAndVersion(id, BAD_VERSION);
        return ruleSet;
    }

    private ValidationRuleSetVersion mockValidationRuleSetVersion(long id, ValidationRuleSet validationRuleSet) {
        ValidationRuleSetVersion ruleSetVersion = mock(ValidationRuleSetVersion.class);
        when(ruleSetVersion.getDescription()).thenReturn("descriptionOfVersion");
        when(ruleSetVersion.getId()).thenReturn(id);
        when(ruleSetVersion.getStartDate()).thenReturn(startDate);
        when(ruleSetVersion.getRuleSet()).thenReturn(validationRuleSet);
        when(ruleSetVersion.getStatus()).thenReturn(ValidationVersionStatus.CURRENT);
        when(ruleSetVersion.getVersion()).thenReturn(OK_VERSION);
        List validationRules = new ArrayList();
        when(ruleSetVersion.getRules()).thenReturn(validationRules);
        List ruleSetVersions = validationRuleSet.getRuleSetVersions();
        ruleSetVersions.add(ruleSetVersion);

        doReturn(Optional.of(ruleSetVersion)).when(validationService).findValidationRuleSetVersion(id);
        doReturn(Optional.of(ruleSetVersion)).when(validationService).findAndLockValidationRuleSetVersionByIdAndVersion(id, OK_VERSION);
        doReturn(Optional.empty()).when(validationService).findAndLockValidationRuleSetVersionByIdAndVersion(id, BAD_VERSION);
        return ruleSetVersion;
    }

    private ValidationRule mockValidationRule(long id, ValidationRuleSetVersion ruleSetVersion) {
        ValidationRule rule = mock(ValidationRule.class);
        when(rule.getName()).thenReturn("MyRule");
        when(rule.getId()).thenReturn(id);
        when(rule.getAction()).thenReturn(ValidationAction.FAIL);
        when(rule.getImplementation()).thenReturn("com.blablabla.Validator");
        when(rule.getDisplayName()).thenReturn("My rule");
        when(rule.isActive()).thenReturn(true);
        when(rule.getRuleSetVersion()).thenReturn(ruleSetVersion);
        ValidationRuleSet ruleSet = ruleSetVersion.getRuleSet();
        when(rule.getRuleSet()).thenReturn(ruleSet);
        when(rule.getVersion()).thenReturn(OK_VERSION);
        when(rule.getObsoleteDate()).thenReturn(null);
        List rules = ruleSetVersion.getRules();
        rules.add(rule);

        ReadingType readingType = mockReadingType();
        Set<ReadingType> readingTypes = new HashSet<>();
        readingTypes.add(readingType);
        when(rule.getReadingTypes()).thenReturn(readingTypes);

        List<PropertySpec> propertySpes = Arrays.asList(
                mockPropertySpec(SimplePropertyType.NUMBER, "number", true),
                mockPropertySpec(SimplePropertyType.NULLABLE_BOOLEAN, "nullableboolean", true),
                mockPropertySpec(SimplePropertyType.BOOLEAN, "boolean", true),
                mockPropertySpec(SimplePropertyType.TEXT, "text", true),
                mockListValueBeanPropertySpec("listvalue", true));
        when(rule.getPropertySpecs()).thenReturn(propertySpes);
        Validator validator = mock(Validator.class);
        when(validator.getPropertySpecs()).thenReturn(propertySpes);
        when(validationService.getValidator("com.blablabla.Validator")).thenReturn(validator);

        Map<String, Object> props = new HashMap<>();
        props.put("number", 13);
        props.put("nullableboolean", true);
        props.put("boolean", false);
        props.put("text", "string");
        List<ListValueBean> listValue = new ArrayList<>();
        listValue.add(Finder.bean1);
        listValue.add(Finder.bean2);
        props.put("listvalue", listValue);
        when(rule.getProps()).thenReturn(props);

        PropertyInfo numberPropertyInfo = new PropertyInfo("number", "number", new PropertyValueInfo<>(13, null), null, true);
        PropertyInfo nullableBooleanPropertyInfo = new PropertyInfo("nullableboolean", "nullableboolean", new PropertyValueInfo<>(true, null), null, true);
        PropertyInfo booleanPropertyInfo = new PropertyInfo("boolean", "boolean", new PropertyValueInfo<>(false, null), null, true);
        PropertyInfo textPropertyInfo = new PropertyInfo("text", "text", new PropertyValueInfo<>("string", null), null, true);
        PropertyInfo listValuePropertyInfo = new PropertyInfo("listvalue", "listvalue", new PropertyValueInfo<>(Arrays.asList("1", "2"), null), null, true);
        List<PropertyInfo> propertyInfoList = Arrays.asList(numberPropertyInfo, nullableBooleanPropertyInfo, booleanPropertyInfo, textPropertyInfo, listValuePropertyInfo);
        when(propertyValueInfoService.getPropertyInfos(propertySpes, props)).thenReturn(propertyInfoList);

        doReturn(Optional.of(rule)).when(validationService).findValidationRule(id);
        doReturn(Optional.of(rule)).when(validationService).findAndLockValidationRuleByIdAndVersion(id, OK_VERSION);
        doReturn(Optional.empty()).when(validationService).findAndLockValidationRuleByIdAndVersion(id, BAD_VERSION);

        return rule;
    }

    private ReadingType mockReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getAggregate()).thenReturn(Aggregate.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType.getAccumulation()).thenReturn(Accumulation.NOTAPPLICABLE);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.NOTAPPLICABLE);
        when(readingType.getCommodity()).thenReturn(Commodity.NOTAPPLICABLE);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.NOTAPPLICABLE);
        when(readingType.getInterharmonic()).thenReturn(RationalNumber.NOTAPPLICABLE);
        when(readingType.getArgument()).thenReturn(RationalNumber.NOTAPPLICABLE);
        when(readingType.getPhases()).thenReturn(Phase.NOTAPPLICABLE);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.ZERO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.NOTAPPLICABLE);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("XXX"));
        return readingType;
    }

    private List<PropertyInfo> createPropertyInfos() {
        List<PropertyInfo> infos = new ArrayList<>();
        PropertyInfo numberInfo = new PropertyInfo("number", "number", new PropertyValueInfo<>(Double.valueOf(10), null), null, true);
        PropertyInfo nullableInfo = new PropertyInfo("nullableboolean", "nullableboolean", new PropertyValueInfo<>(false, null), null, true);
        PropertyInfo booleanInfo = new PropertyInfo("boolean", "boolean", new PropertyValueInfo<>(true, null), null, true);
        PropertyInfo textInfo = new PropertyInfo("text", "text", new PropertyValueInfo<>("string", null), null, true);
        PropertyInfo listValueInfo = new PropertyInfo();
        listValueInfo.key = "listvalue";
        listValueInfo.propertyValueInfo = new PropertyValueInfo<>(new String[]{"1", "2"}, null);

        infos.add(numberInfo);
        infos.add(nullableInfo);
        infos.add(booleanInfo);
        infos.add(textInfo);
        infos.add(listValueInfo);
        return infos;
    }

    private PropertySpec mockPropertySpec(SimplePropertyType propertyType, String name, boolean isRequired) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        when(propertySpec.isRequired()).thenReturn(isRequired);
        when(propertySpec.getValueFactory()).thenReturn(this.getValueFactoryFor(propertyType));
        return propertySpec;
    }

    private PropertySpec mockListValueBeanPropertySpec(String name, boolean isRequired) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getSelectionMode()).thenReturn(PropertySelectionMode.LIST);
        when(possibleValues.getAllValues()).thenReturn(Arrays.asList(Finder.bean1, Finder.bean2));
        when(possibleValues.isExhaustive()).thenReturn(true);
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);
        when(propertySpec.isRequired()).thenReturn(isRequired);
        when(propertySpec.getValueFactory()).thenReturn(new ListValueFactory(new Finder()));
        return propertySpec;
    }

    private ValueFactory getValueFactoryFor(SimplePropertyType propertyType) {
        switch (propertyType) {
            case NUMBER:
               return new BigDecimalFactory();
            case NULLABLE_BOOLEAN:
               return new ThreeStateFactory();
            case BOOLEAN:
               return new BooleanFactory();
            case TEXT:
               return new StringFactory();
            case RELATIVEPERIOD:
               return new RelativePeriodFactory(this.timeService);
            case UNKNOWN:
            default:
                return null;
        }
    }

    private Validator mockValidator(String displayName) {
        Validator validator = mock(Validator.class);
        when(validator.getDisplayName()).thenReturn(displayName);
        List<PropertySpec> propertySpecs = Collections.singletonList(mockListValueBeanPropertySpec("listvalue", false));
        when(validator.getPropertySpecs()).thenReturn(propertySpecs);
        when(propertyValueInfoService.getPropertyInfos(propertySpecs)).thenReturn(getPropertyInfos());
        return validator;
    }

    private List<PropertyInfo> getPropertyInfos() {
        PredefinedPropertyValuesInfo predefinedPropertyValuesInfo = new PredefinedPropertyValuesInfo();
        predefinedPropertyValuesInfo.selectionMode = PropertySelectionMode.LIST;
        IdWithNameInfo[] arr = new IdWithNameInfo[2];
        IdWithNameInfo firstPossibleValue = new IdWithNameInfo();
        firstPossibleValue.id = "1";
        firstPossibleValue.name = "first";
        IdWithNameInfo secondPossibleValue = new IdWithNameInfo();
        secondPossibleValue.id = "2";
        secondPossibleValue.name = "second";
        arr[0] = firstPossibleValue;
        arr[1] = secondPossibleValue;
        predefinedPropertyValuesInfo.possibleValues = arr;
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.predefinedPropertyValuesInfo = predefinedPropertyValuesInfo;
        PropertyInfo propertyInfo = new PropertyInfo();
        propertyInfo.key = "listvalue";
        propertyInfo.required = false;
        propertyInfo.propertyTypeInfo = propertyTypeInfo;
        return Collections.singletonList(propertyInfo);
    }

    private void mockPropertyInfos(ValidationRuleInfo info) {
        validationService.getValidator(info.implementation).getPropertySpecs()
                .forEach(propertySpec -> {
                    switch (propertySpec.getName()) {
                        case "number":
                            when(propertyValueInfoService.findPropertyValue(eq(propertySpec), any())).thenReturn(BigDecimal.valueOf(10.0));
                            break;
                        case "nullableboolean":
                            when(propertyValueInfoService.findPropertyValue(eq(propertySpec), any())).thenReturn(false);
                            break;
                        case "boolean":
                            when(propertyValueInfoService.findPropertyValue(eq(propertySpec), any())).thenReturn(true);
                            break;
                        case "text":
                            when(propertyValueInfoService.findPropertyValue(eq(propertySpec), any())).thenReturn("string");
                            break;
                        case "listvalue":
                            when(propertyValueInfoService.findPropertyValue(eq(propertySpec), any())).thenReturn(Collections.singletonList(info));
                            break;
                    }
                });
    }

    private static class Finder extends AbstractValueFactory<ListValueBean> {

        static ListValueBean bean1 = new ListValueBean("1", "first");
        static ListValueBean bean2 = new ListValueBean("2", "second");

        private Finder() {
            super(ListValueBean.class);
        }

        @Override
        public ListValueBean fromStringValue(String stringValue) {
            switch (stringValue) {
                case "1":
                    return bean1;
                case "2":
                    return bean2;
                default:
                    return null;
            }
        }

        @Override
        public String toStringValue(ListValueBean object) {
            return object.getId();
        }
    }

    private static class ListValueBean extends HasIdAndName {

        private String id;
        private String name;

        private ListValueBean(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
