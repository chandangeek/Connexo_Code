/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

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
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleBuilder;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PredefinedPropertyValuesInfo;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.rest.util.ConcurrentModificationInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.util.conditions.Order;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
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
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EstimationResourceTest extends EstimationApplicationJerseyTest {

    private static final String APPLICATION_HEADER_PARAM = "X-CONNEXO-APPLICATION-NAME";

    private static final long RULE_SET_ID = 27L;
    private static final long RULE_SET_SUCCESS_VERSION = 6L;
    private static final long RULE_SET_FAILURE_VERSION = 3L;

    private static final long RULE_ID = 1348L;
    private static final long RULE_SUCCESS_VERSION = 12L;
    private static final long RULE_FAILURE_VERSION = 7L;

    private static EstimationRuleSetInfo baseRuleSetInfo(){
        EstimationRuleSetInfo info = new EstimationRuleSetInfo();
        info.id = RULE_SET_ID;
        info.version = RULE_SET_SUCCESS_VERSION;
        return info;
    }

    private static EstimationRuleInfo baseRuleInfo(){
        EstimationRuleInfo info = new EstimationRuleInfo();
        info.id = RULE_ID;
        info.version = RULE_SUCCESS_VERSION;
        info.parent = baseRuleSetInfo();
        return info;
    }

    @Test
    public void testGetEstimationRuleSetsNoRuleSets() {
        mockRuleSetQuery();
        EstimationRuleSetInfos response = target("/estimation").request().header(APPLICATION_HEADER_PARAM, "MDC").get(EstimationRuleSetInfos.class);

        assertThat(response.total).isEqualTo(0);
        assertThat(response.ruleSets).hasSize(0);
    }

    @Test
    public void testGetEstimationRuleSets() {
        mockRuleSetQuery(mockDefaultRuleSet());
        EstimationRuleSetInfos response = target("/estimation").request().header(APPLICATION_HEADER_PARAM, "MDC").get(EstimationRuleSetInfos.class);

        assertThat(response.total).isEqualTo(1);
        List<EstimationRuleSetInfo> ruleSetInfos = response.ruleSets;
        assertThat(ruleSetInfos).hasSize(1);
        EstimationRuleSetInfo ruleSetInfo = ruleSetInfos.get(0);
        assertThat(ruleSetInfo.name).isEqualTo("MyName");
        assertThat(ruleSetInfo.id).isEqualTo(RULE_SET_ID);
        assertThat(ruleSetInfo.description).isEqualTo("MyDescription");
        assertThat(ruleSetInfo.numberOfInactiveRules).isEqualTo(0);
        assertThat(ruleSetInfo.numberOfRules).isEqualTo(0);
    }

    @Test
    public void testGetEstimationRuleSetNotFound() {
        when(estimationService.getEstimationRuleSet(RULE_SET_ID)).thenReturn(Optional.empty());
        Response response = target("/estimation/" + RULE_SET_ID).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetEstimationRuleSet() {
        mockEstimationRuleInRuleSet(RULE_ID, mockDefaultRuleSet());

        EstimationRuleSetInfo ruleSetInfo = target("/estimation/" + RULE_SET_ID).request().get(EstimationRuleSetInfo.class);

        assertThat(ruleSetInfo.id).isEqualTo(RULE_SET_ID);
        assertThat(ruleSetInfo.name).isEqualTo("MyName");
        assertThat(ruleSetInfo.description).isEqualTo("MyDescription");
        assertThat(ruleSetInfo.numberOfInactiveRules).isEqualTo(0);
        assertThat(ruleSetInfo.numberOfRules).isEqualTo(1);
    }

    @Test
    public void testGetEstimationRulesNoRules() {
        mockRuleSetQuery(mockDefaultRuleSet());
        EstimationRuleInfos ruleInfos = target("/estimation/" + RULE_SET_ID + "/rules").request().get(EstimationRuleInfos.class);

        assertThat(ruleInfos.total).isEqualTo(0);
        assertThat(ruleInfos.rules).hasSize(0);
    }

    @Test
    public void testGetEstimationRules() {
        EstimationRuleSet ruleSet = mockDefaultRuleSet();
        mockEstimationRuleInRuleSet(RULE_ID, ruleSet);
        mockRuleSetQuery(ruleSet);

        EstimationRuleInfos ruleInfos = target("/estimation/" + RULE_SET_ID + "/rules").request().get(EstimationRuleInfos.class);

        assertThat(ruleInfos.total).isEqualTo(1);

        List<EstimationRuleInfo> rules = ruleInfos.rules;
        assertThat(rules).hasSize(1);

        EstimationRuleInfo ruleInfo = rules.get(0);
        assertThat(ruleInfo.id).isEqualTo(RULE_ID);
        assertThat(ruleInfo.name).isEqualTo("MyRule");
        assertThat(ruleInfo.implementation).isEqualTo("com.blablabla.Estimator");
        assertThat(ruleInfo.displayName).isEqualTo("My rule");
        assertThat(ruleInfo.active).isEqualTo(true);

        EstimationRuleSetInfo ruleSetInfo = ruleInfo.ruleSet;
        assertThat(ruleSetInfo.id).isEqualTo(RULE_SET_ID);
        assertThat(ruleSetInfo.name).isEqualTo("MyName");
        assertThat(ruleSetInfo.description).isEqualTo("MyDescription");
        assertThat(ruleSetInfo.numberOfInactiveRules).isEqualTo(0);
        assertThat(ruleSetInfo.numberOfRules).isEqualTo(1);

        List<ReadingTypeInfo> readingTypeInfos = ruleInfo.readingTypes;
        assertThat(readingTypeInfos.get(0).mRID).isEqualTo("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");

        List<PropertyInfo> propertyInfos = ruleInfo.properties;
        assertThat(propertyInfos).hasSize(5);

        PropertyInfo propertyNumberInfo = propertyInfos.get(0);
        assertThat(propertyNumberInfo.key).isEqualTo("number");
        assertThat(propertyNumberInfo.required).isEqualTo(true);

        PropertyValueInfo<?> numberValueInfo = propertyNumberInfo.propertyValueInfo;
        assertThat(numberValueInfo.value).isEqualTo(13);
        assertThat(numberValueInfo.defaultValue).isEqualTo(null);

        PropertyInfo propertyNullableBooleanInfo = propertyInfos.get(1);
        assertThat(propertyNullableBooleanInfo.key).isEqualTo("nullableboolean");
        assertThat(propertyNullableBooleanInfo.required).isEqualTo(true);

        PropertyValueInfo<?> nullableBooleanValueInfo = propertyNullableBooleanInfo.propertyValueInfo;
        assertThat(nullableBooleanValueInfo.value).isEqualTo(true);
        assertThat(nullableBooleanValueInfo.defaultValue).isEqualTo(null);

        PropertyInfo propertyBooleanInfo = propertyInfos.get(2);
        assertThat(propertyBooleanInfo.key).isEqualTo("boolean");
        assertThat(propertyBooleanInfo.required).isEqualTo(true);

        PropertyValueInfo<?> booleanValueInfo = propertyBooleanInfo.propertyValueInfo;
        assertThat(booleanValueInfo.value).isEqualTo(false);
        assertThat(booleanValueInfo.defaultValue).isEqualTo(null);

        PropertyInfo propertyTextInfo = propertyInfos.get(3);
        assertThat(propertyTextInfo.key).isEqualTo("text");
        assertThat(propertyTextInfo.required).isEqualTo(true);

        PropertyValueInfo<?> textValueInfo = propertyTextInfo.propertyValueInfo;
        assertThat(textValueInfo.value).isEqualTo("string");
        assertThat(textValueInfo.defaultValue).isEqualTo(null);

        PropertyInfo propertyListValueInfo = propertyInfos.get(4);
        assertThat(propertyListValueInfo.key).isEqualTo("listvalue");
        assertThat(propertyListValueInfo.required).isEqualTo(true);

        PropertyValueInfo<?> listValueInfo = propertyListValueInfo.propertyValueInfo;
        assertThat(listValueInfo.defaultValue).isEqualTo(null);

        List<?> listValue = (List<?>) listValueInfo.value;
        assertThat(listValue).hasSize(2);

        assertThat(listValue.get(0)).isEqualTo("1");
        assertThat(listValue.get(1)).isEqualTo("2");
    }

    @Test
    public void testGetEstimatorsNoEstimators() throws IOException {
        Response response = target("/estimation/estimators").request().header(APPLICATION_HEADER_PARAM, "APP").get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel body = JsonModel.create((ByteArrayInputStream)response.getEntity());
        assertThat(body.<Integer>get("$.total")).isZero();
        assertThat(body.<List>get("$.estimators")).isEmpty();
    }

    @Test
    public void testGetEstimators() throws IOException {
        List<Estimator> mockEstimators = Arrays.asList(mockEstimator("B Estimator"), mockEstimator("A Estimator"));
        when(estimationService.getAvailableEstimators(QualityCodeSystem.MDC)).thenReturn(mockEstimators);

        Response response = target("/estimation/estimators").request().header(APPLICATION_HEADER_PARAM, "MDC").get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel body = JsonModel.model((ByteArrayInputStream)response.getEntity());
        assertThat(body.<Integer>get("$.total")).isEqualTo(2);
        assertThat(body.<List>get("$.estimators")).hasSize(2);
        assertThat(body.<String>get("$.estimators[0].displayName")).isEqualTo("A Estimator");
        assertThat(body.<String>get("$.estimators[0].implementation")).isNotNull();
        assertThat(body.<String>get("$.estimators[1].displayName")).isEqualTo("B Estimator");
        assertThat(body.<String>get("$.estimators[1].implementation")).isNotNull();
        assertThat(body.<List>get("$.estimators[0].properties")).hasSize(1);
        JsonModel property0 = JsonModel.model(body.<Object>get("$.estimators[0].properties[0]"));
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
    public void testAddEstimationRule() {
        final EstimationRuleInfo info = baseRuleInfo();
        info.name = "MyRule";
        info.implementation = "com.blablabla.Estimator";
        info.properties = createPropertyInfos();

        Entity<EstimationRuleInfo> entity = Entity.json(info);

        EstimationRuleSet ruleSet = mockDefaultRuleSet();
        EstimationRule rule = mockEstimationRuleInRuleSet(RULE_ID, ruleSet);
        EstimationRuleBuilder estimationRuleBuilder = FakeBuilder.initBuilderStub(rule, EstimationRuleBuilder.class, EstimationRuleBuilder.PropertyBuilder.class);
        when(ruleSet.addRule(Matchers.eq(info.implementation), Matchers.eq(info.name))).thenReturn(estimationRuleBuilder);
        estimationService.getEstimator(info.implementation).get().getPropertySpecs()
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
                            when(propertyValueInfoService.findPropertyValue(eq(propertySpec), any())).thenReturn(Collections.singletonList(rule));
                            break;
                    }
                });
        Response response = target("/estimation/" + RULE_SET_ID + "/rules").request().post(entity);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        EstimationRuleInfo resultInfo = response.readEntity(EstimationRuleInfo.class);
        assertThat(resultInfo.name).isEqualTo("MyRule");

        verify(estimationRuleBuilder).havingProperty("number");
        verify((EstimationRuleBuilder.PropertyBuilder) estimationRuleBuilder).withValue(BigDecimal.valueOf(10.0));
        verify(estimationRuleBuilder).havingProperty("nullableboolean");
        verify((EstimationRuleBuilder.PropertyBuilder) estimationRuleBuilder).withValue(false);
        verify(estimationRuleBuilder).havingProperty("boolean");
        verify((EstimationRuleBuilder.PropertyBuilder) estimationRuleBuilder).withValue(true);
        verify(estimationRuleBuilder).havingProperty("text");
        verify((EstimationRuleBuilder.PropertyBuilder) estimationRuleBuilder).withValue("string");
        verify(estimationRuleBuilder).havingProperty(Matchers.eq("listvalue"));
        verify((EstimationRuleBuilder.PropertyBuilder) estimationRuleBuilder).withValue(isA(List.class));
    }

    @Test
    public void testAddEstimationRuleToNonExistingRuleSet() {
        final EstimationRuleInfo info = new EstimationRuleInfo();
        info.name = "MyRule";
        info.implementation = "com.blablabla.Estimator";
        info.properties = createPropertyInfos();

        Entity<EstimationRuleInfo> entity = Entity.json(info);

        when(estimationService.getEstimationRuleSet(666)).thenReturn(Optional.empty());
        Response response = target("/estimation/666/rules").request().post(entity);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testUpdateEstimationRuleSet() throws Exception {
        EstimationRuleSet ruleSet = mockDefaultRuleSet();
        mockEstimationRuleInRuleSet(RULE_ID, ruleSet);

        final EstimationRuleSetInfo info = baseRuleSetInfo();
        info.name = "MyRuleUpdated";
        info.description = "blablabla";
        info.rules = new ArrayList<>();

        EstimationRuleInfo ruleInfo = baseRuleInfo();
        info.rules.add(ruleInfo);

        Response response = target("/estimation/" + RULE_SET_ID).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(ruleSet).setName(stringArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(info.name);
        verify(ruleSet).setDescription(stringArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(info.description);
        verify(ruleSet, times(1)).save();
    }

    @Test
    public void testUpdateEstimationRuleSetConcurrentModification() throws Exception {
        EstimationRuleSet ruleSet = mockDefaultRuleSet();
        final EstimationRuleSetInfo info = baseRuleSetInfo();
        info.version = RULE_SET_FAILURE_VERSION;

        Response response = target("/estimation/" + RULE_SET_ID).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(ruleSet, never()).save();
    }

    @Test
    public void testCreateEstimationRuleSetInfo() throws Exception {
        EstimationRuleSetInfo info = baseRuleSetInfo();
        info.name="ruleset";
        info.description="desc";

        EstimationRuleSet ruleSet = mockDefaultRuleSet();
        when(estimationService.createEstimationRuleSet(info.name, QualityCodeSystem.MDC, info.description)).thenReturn(ruleSet);
        Response response = target("/estimation").request().header(APPLICATION_HEADER_PARAM, "MDC").post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testGetEstimationRuleInfo() throws Exception {
        EstimationRuleSet ruleSet = mockDefaultRuleSet();
        EstimationRule estimationRule = mockEstimationRuleInRuleSet(RULE_ID, ruleSet);
        doReturn(Collections.singletonList(estimationRule)).when(ruleSet).getRules();
        Response response = target("/estimation/"+RULE_SET_ID+"/rules/"+RULE_ID).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("MyRule");
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(((Number)RULE_ID).intValue());
    }

    @Test
    public void testDeleteRuleSet() throws Exception {
        EstimationRuleSetInfo ruleInfo = baseRuleSetInfo();
        EstimationRuleSet estimationRuleSet = mockDefaultRuleSet();
        Response response = target("/estimation/" + RULE_SET_ID).request().build(HttpMethod.DELETE, Entity.json(ruleInfo)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(estimationRuleSet).delete();
    }

    @Test
    public void testDeleteRuleSetConcurrentModification() throws Exception {
        EstimationRuleSet estimationRuleSet = mockDefaultRuleSet();
        EstimationRuleSetInfo info = baseRuleSetInfo();
        info.version = RULE_SET_FAILURE_VERSION;
        Response response = target("/estimation/"+RULE_SET_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(estimationRuleSet, never()).delete();
    }

    @Test
    public void testGetRuleSetUsage() throws Exception {
        EstimationRuleSet estimationRuleSet = mockDefaultRuleSet();
        EstimationRuleSet estimationRuleSet2 = mockEstimationRuleSet(2);
        when(estimationService.isEstimationRuleSetInUse(estimationRuleSet)).thenReturn(true);
        when(estimationService.isEstimationRuleSetInUse(estimationRuleSet2)).thenReturn(false);
        Response response = target("/estimation/"+RULE_SET_ID+"/usage").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.isInUse")).isEqualTo(true);
    }

    @Test
    public void testEditEstimationRule() {
        final EstimationRuleInfo info = baseRuleInfo();
        info.name = "MyRuleUpdated";
        info.implementation = "com.blablabla.Estimator";
        info.properties = new ArrayList<>();

        EstimationRuleSet ruleSet = mockDefaultRuleSet();
        EstimationRule rule = mockEstimationRuleInRuleSet(RULE_ID, ruleSet);
        when(rule.getName()).thenReturn("MyRuleUpdated");

        Map<String, Object> props = new HashMap<>();
        props.put("number", null);
        props.put("nullableboolean", null);
        props.put("boolean", null);
        props.put("text", null);
        props.put("listvalue", null);

        when(ruleSet.updateRule(
                Matchers.eq(RULE_ID),
                Matchers.eq("MyRuleUpdated"),
                Matchers.eq(false),
                Matchers.eq(new ArrayList<>()),
                Matchers.eq(props))).
        thenReturn(rule);

        Entity<EstimationRuleInfo> entity = Entity.json(info);
        Response response = target("/estimation/"+RULE_SET_ID+"/rules/"+RULE_ID).request().put(entity);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        EstimationRuleInfos resultInfos = response.readEntity(EstimationRuleInfos.class);
        assertThat(resultInfos.total).isEqualTo(1);
        assertThat(resultInfos.rules).hasSize(1);
        assertThat(resultInfos.rules.get(0).name).isEqualTo("MyRuleUpdated");
    }

    @Test
    public void testDeleteEstimationRule() {
        mockEstimationRuleInRuleSet(RULE_ID, mockDefaultRuleSet());
        EstimationRuleInfo info = baseRuleInfo();
        Response response = target("/estimation/"+RULE_SET_ID+"/rules/"+RULE_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void testDeleteEstimationRuleNoRuleSet() {
        when(estimationService.getEstimationRuleSet(RULE_SET_ID)).thenReturn(Optional.empty());
        when(estimationService.findAndLockEstimationRuleSet(RULE_SET_ID, RULE_SET_SUCCESS_VERSION)).thenReturn(Optional.empty());
        EstimationRuleInfo info = baseRuleInfo();
        Response response = target("/estimation/"+RULE_SET_ID+"/rules/"+RULE_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testDeleteEstimationRuleNoRule() {
        mockDefaultRuleSet();
        when(estimationService.getEstimationRule(RULE_ID)).thenReturn(Optional.empty());
        when(estimationService.findAndLockEstimationRule(RULE_ID, RULE_SUCCESS_VERSION)).thenReturn(Optional.empty());
        EstimationRuleInfo info = baseRuleInfo();
        Response response = target("/estimation/"+RULE_SET_ID+"/rules/"+RULE_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        ConcurrentModificationInfo errorInfo = response.readEntity(ConcurrentModificationInfo.class);
        assertThat(errorInfo.version).isNull();
        assertThat(errorInfo.parent.version).isEqualTo(RULE_SET_SUCCESS_VERSION);
    }

    private void mockRuleSetQuery(EstimationRuleSet... ruleSets) {
        Query<EstimationRuleSet> query = mock(Query.class);
        when(estimationService.getEstimationRuleSetQuery()).thenReturn(query);
        RestQuery<EstimationRuleSet> restQuery = mock(RestQuery.class);
        when(restQueryService.wrap(query)).thenReturn(restQuery);
        when(restQuery.select(any(QueryParameters.class), any(Order.class))).thenReturn(Arrays.asList(ruleSets));
    }

    private EstimationRuleSet mockDefaultRuleSet(){
        return mockEstimationRuleSet(RULE_SET_ID);
    }

    private EstimationRuleSet mockEstimationRuleSet(long id) {
        EstimationRuleSet ruleSet = mock(EstimationRuleSet.class);
        when(ruleSet.getId()).thenReturn(id);
        when(ruleSet.getName()).thenReturn("MyName");
        when(ruleSet.getDescription()).thenReturn("MyDescription");
        when(ruleSet.getVersion()).thenReturn(RULE_SET_SUCCESS_VERSION);
        List rules = new ArrayList<>();
        when(ruleSet.getRules()).thenReturn(rules);

        doReturn(Optional.of(ruleSet)).when(estimationService).getEstimationRuleSet(id);
        doReturn(Optional.of(ruleSet)).when(estimationService).findAndLockEstimationRuleSet(id, RULE_SET_SUCCESS_VERSION);
        doReturn(Optional.empty()).when(estimationService).findAndLockEstimationRuleSet(id, RULE_SET_FAILURE_VERSION);
        return ruleSet;
    }

    private EstimationRule mockEstimationRuleInRuleSet(long id, EstimationRuleSet ruleSet) {
        Estimator estimator = mock(Estimator.class);
        EstimationRule rule = mock(EstimationRule.class);
        when(rule.getName()).thenReturn("MyRule");
        when(rule.getId()).thenReturn(id);
        when(rule.getImplementation()).thenReturn("com.blablabla.Estimator");
        when(rule.getDisplayName()).thenReturn("My rule");
        when(rule.isActive()).thenReturn(true);
        when(rule.getRuleSet()).thenReturn(ruleSet);
        when(estimationService.getEstimator("com.blablabla.Estimator")).thenReturn(Optional.of(estimator));

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
        when(estimator.getPropertySpecs()).thenReturn(propertySpes);

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

        List helper = new ArrayList();
        helper.add(rule);
        ruleSet.getRules().addAll(helper);

        doReturn(Optional.of(rule)).when(estimationService).getEstimationRule(id);
        doReturn(Optional.of(rule)).when(estimationService).findAndLockEstimationRule(id, RULE_SUCCESS_VERSION);
        doReturn(Optional.empty()).when(estimationService).findAndLockEstimationRule(id, RULE_FAILURE_VERSION);
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
        PropertyInfo numberInfo = new PropertyInfo("number","number", new PropertyValueInfo<>(Double.valueOf(10), null), null, true);
        PropertyInfo nullableInfo = new PropertyInfo("nullableboolean","nullableboolean", new PropertyValueInfo<>(false, null), null, true);
        PropertyInfo booleanInfo = new PropertyInfo("boolean","boolean", new PropertyValueInfo<>(true, null), null, true);
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
        when(propertySpec.getValueFactory()).thenReturn(new Finder());
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
            case UNKNOWN:
            default:
                return null;
        }
    }

    private Estimator mockEstimator(String displayName) {
        Estimator estimator = mock(Estimator.class);
        when(estimator.getDisplayName()).thenReturn(displayName);
        List<PropertySpec> propertySpecs = Collections.singletonList(mockListValueBeanPropertySpec("listvalue", false));
        when(estimator.getPropertySpecs()).thenReturn(propertySpecs);
        when(propertyValueInfoService.getPropertyInfos(propertySpecs)).thenReturn(getPropertyInfos());
        return estimator;
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

}
