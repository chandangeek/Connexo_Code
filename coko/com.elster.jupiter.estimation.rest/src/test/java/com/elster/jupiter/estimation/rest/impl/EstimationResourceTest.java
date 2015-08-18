package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.CanFindByStringKey;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListValue;
import com.elster.jupiter.properties.ListValuePropertySpec;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.properties.PredefinedPropertyValuesInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertySelectionMode;
import com.elster.jupiter.rest.util.properties.PropertyTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.Estimator;
import com.jayway.jsonpath.JsonModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EstimationResourceTest extends BaseEstimationRestTest {

    @Test
    public void testGetEstimationRuleSetsNoRuleSets() {
        mockEstimationRuleSets();

        EstimationRuleSetInfos response = target("/estimation").request().get(EstimationRuleSetInfos.class);

        assertThat(response.total).isEqualTo(0);
        assertThat(response.ruleSets).hasSize(0);
    }

    @Test
    public void testGetEstimationRuleSets() {
        mockEstimationRuleSets(mockEstimationRuleSet(13, false));

        EstimationRuleSetInfos response = target("/estimation").request().get(EstimationRuleSetInfos.class);

        assertThat(response.total).isEqualTo(1);

        List<EstimationRuleSetInfo> ruleSetInfos = response.ruleSets;
        assertThat(ruleSetInfos).hasSize(1);

        EstimationRuleSetInfo ruleSetInfo = ruleSetInfos.get(0);
        assertThat(ruleSetInfo.name).isEqualTo("MyName");
        assertThat(ruleSetInfo.id).isEqualTo(13);
        assertThat(ruleSetInfo.description).isEqualTo("MyDescription");
        assertThat(ruleSetInfo.numberOfInactiveRules).isEqualTo(0);
        assertThat(ruleSetInfo.numberOfRules).isEqualTo(0);
    }

    @Test
    public void testGetEstimationRuleSetNotFound() {
        when(estimationService.getEstimationRuleSet(13)).thenReturn(Optional.empty());

        Response response = target("/estimation/13").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetEstimationRuleSet() {
        mockEstimationRuleSet(13, true);

        EstimationRuleSetInfo ruleSetInfo = target("/estimation/13").request().get(EstimationRuleSetInfo.class);

        assertThat(ruleSetInfo.id).isEqualTo(13);
        assertThat(ruleSetInfo.name).isEqualTo("MyName");
        assertThat(ruleSetInfo.description).isEqualTo("MyDescription");
        assertThat(ruleSetInfo.numberOfInactiveRules).isEqualTo(0);
        assertThat(ruleSetInfo.numberOfRules).isEqualTo(1);
    }

    @Test
    public void testGetEstimationRulesNoRules() {
        mockEstimationRuleSets(mockEstimationRuleSet(13, false));

        EstimationRuleInfos ruleInfos = target("/estimation/13/rules").request().get(EstimationRuleInfos.class);

        assertThat(ruleInfos.total).isEqualTo(0);
        assertThat(ruleInfos.rules).hasSize(0);
    }

    @Test
    public void testGetEstimationRules() {
        mockEstimationRuleSets(mockEstimationRuleSet(13, true));

        EstimationRuleInfos ruleInfos = target("/estimation/13/rules").request().get(EstimationRuleInfos.class);

        assertThat(ruleInfos.total).isEqualTo(1);

        List<EstimationRuleInfo> rules = ruleInfos.rules;
        assertThat(rules).hasSize(1);

        EstimationRuleInfo ruleInfo = rules.get(0);
        assertThat(ruleInfo.id).isEqualTo(1);
        assertThat(ruleInfo.name).isEqualTo("MyRule");
        assertThat(ruleInfo.implementation).isEqualTo("com.blablabla.Estimator");
        assertThat(ruleInfo.displayName).isEqualTo("My rule");
        assertThat(ruleInfo.active).isEqualTo(true);

        EstimationRuleSetInfo ruleSetInfo = ruleInfo.ruleSet;
        assertThat(ruleSetInfo.id).isEqualTo(13);
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
    public void testGetEstimatorsNoEstimators() {
        EstimatorInfos estimatorInfos = target("/estimation/estimators").request().get(EstimatorInfos.class);

        assertThat(estimatorInfos.total).isEqualTo(0);
        assertThat(estimatorInfos.estimators).hasSize(0);
    }

    @Test
    public void testGetEstimators() {
        List<Estimator> mockEstimator = Arrays.asList(mockEstimator("B Estimator"), mockEstimator("A Estimator"));
        when(estimationService.getAvailableEstimators()).thenReturn(mockEstimator);

        EstimatorInfos estimatorInfos = target("/estimation/estimators").request().get(EstimatorInfos.class);

        assertThat(estimatorInfos.total).isEqualTo(2);
        List<EstimationInfo> estimators = estimatorInfos.estimators;
        assertThat(estimators).hasSize(2);

        EstimationInfo estimatorAInfo = estimators.get(0);
        assertThat(estimatorAInfo.displayName).isEqualTo("A Estimator");
        assertThat(estimatorAInfo.implementation).isNotNull();

        EstimationInfo estimatorBInfo = estimators.get(1);
        assertThat(estimatorBInfo.displayName).isEqualTo("B Estimator");
        assertThat(estimatorBInfo.implementation).isNotNull();

        List<PropertyInfo> propertyInfos = estimatorAInfo.properties;
        assertThat(propertyInfos).hasSize(1);

        PropertyInfo propertyInfo = propertyInfos.get(0);
        assertThat(propertyInfo.key).isEqualTo("listvalue");
        assertThat(propertyInfo.required).isEqualTo(false);

        PropertyTypeInfo typeInfo = propertyInfo.propertyTypeInfo;
        PredefinedPropertyValuesInfo<?> predefinedValuesInfo = typeInfo.predefinedPropertyValuesInfo;
        assertThat(predefinedValuesInfo.selectionMode).isEqualTo(PropertySelectionMode.LIST);

        Object[] possibleValuesInfo = predefinedValuesInfo.possibleValues;
        assertThat(possibleValuesInfo).hasSize(2);

        Map<?, ?> possibleValue1 = (Map<?, ?>) possibleValuesInfo[0];
        assertThat(possibleValue1.get("id")).isEqualTo("1");
        assertThat(possibleValue1.get("name")).isEqualTo("first");

        Map<?, ?> possibleValue2 = (Map<?, ?>) possibleValuesInfo[1];
        assertThat(possibleValue2.get("id")).isEqualTo("2");
        assertThat(possibleValue2.get("name")).isEqualTo("second");
    }

    @Test
    public void testAddEstimationRule() {
        final EstimationRuleInfo info = new EstimationRuleInfo();
        info.name = "MyRule";
        info.implementation = "com.blablabla.Estimator";
        info.properties = createPropertyInfos();

        Entity<EstimationRuleInfo> entity = Entity.json(info);

        EstimationRuleSet ruleSet = mockEstimationRuleSet(13, false);
        EstimationRule rule = mockEstimationRuleInRuleSet(1L, ruleSet);
        when(ruleSet.addRule(Matchers.eq(info.implementation), Matchers.eq(info.name))).thenReturn(rule);

        Response response = target("/estimation/13/rules").request().post(entity);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
        EstimationRuleInfo resultInfo = response.readEntity(EstimationRuleInfo.class);
        assertThat(resultInfo.name).isEqualTo("MyRule");

        verify(rule).addProperty("number", BigDecimal.valueOf(10.0));
        verify(rule).addProperty("nullableboolean", false);
        verify(rule).addProperty("boolean", true);
        verify(rule).addProperty("text", "string");
        verify(rule).addProperty(Matchers.eq("listvalue"), Matchers.any(ListValue.class));
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
        EstimationRuleSet ruleSet = mockEstimationRuleSet(15, true);

        final EstimationRuleSetInfo info = new EstimationRuleSetInfo();
        info.name = "MyRuleUpdated";
        info.description = "blablabla";
        info.rules = new ArrayList<>();
        EstimationRuleInfo ruleInfo = new EstimationRuleInfo();
        ruleInfo.id = 1L;
        info.rules.add(ruleInfo);

        Response response = target("/estimation/15").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(ruleSet).setName(stringArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(info.name);
        verify(ruleSet).setDescription(stringArgumentCaptor.capture());
        assertThat(stringArgumentCaptor.getValue()).isEqualTo(info.description);
        verify(ruleSet, times(1)).save();
    }

    @Test
    public void testCreateEstimationRuleSetInfo() throws Exception {
        EstimationRuleSetInfo info = new EstimationRuleSetInfo();
        info.name="ruleset";
        info.description="desc";

        EstimationRuleSet ruleSet = mockEstimationRuleSet(12, false);
        when(estimationService.createEstimationRuleSet(info.name, info.description)).thenReturn(ruleSet);
        Response response = target("/estimation").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void testGetEstimationRuleInfo() throws Exception {
        EstimationRuleSet ruleSet = mockEstimationRuleSet(12, false);
        EstimationRule estimationRule = mockEstimationRuleInRuleSet(13, ruleSet);
        doReturn(Arrays.asList(estimationRule)).when(ruleSet).getRules();
        Response response = target("/estimation/12/rules/13").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("MyRule");
        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(13);
    }

    @Test
    public void testDeleteRuleSet() throws Exception {
        EstimationRuleSet estimationRuleSet = mockEstimationRuleSet(99, false);
        Response response = target("/estimation/99").request().delete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
        verify(estimationRuleSet).delete();
    }

    @Test
    public void testGetRuleSetUsage() throws Exception {
        EstimationRuleSet estimationRuleSet = mockEstimationRuleSet(1, false);
        EstimationRuleSet estimationRuleSet2 = mockEstimationRuleSet(2, false);
        when(estimationService.isEstimationRuleSetInUse(estimationRuleSet)).thenReturn(true);
        when(estimationService.isEstimationRuleSetInUse(estimationRuleSet2)).thenReturn(false);
        Response response = target("/estimation/1/usage").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.isInUse")).isEqualTo(true);
    }

    @Test
    public void testEditEstimationRule() {
        final EstimationRuleInfo info = new EstimationRuleInfo();
        info.name = "MyRuleUpdated";
        info.implementation = "com.blablabla.Estimator";
        info.properties = new ArrayList<>();

        EstimationRuleSet ruleSet = mockEstimationRuleSet(13, true);
        EstimationRule rule = ruleSet.getRules().get(0);
        when(rule.getName()).thenReturn("MyRuleUpdated");

        Map<String, Object> props = new HashMap<>();
        props.put("number", null);
        props.put("nullableboolean", null);
        props.put("boolean", null);
        props.put("text", null);
        props.put("listvalue", null);

        when(ruleSet.updateRule(
                Matchers.eq(1L),
                Matchers.eq("MyRuleUpdated"),
                Matchers.eq(false),
                Matchers.eq(new ArrayList<>()),
                Matchers.eq(props))).
        thenReturn(rule);

        Entity<EstimationRuleInfo> entity = Entity.json(info);
        Response response = target("/estimation/13/rules/1").request().put(entity);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        EstimationRuleInfos resultInfos = response.readEntity(EstimationRuleInfos.class);
        assertThat(resultInfos.total).isEqualTo(1);
        assertThat(resultInfos.rules).hasSize(1);
        assertThat(resultInfos.rules.get(0).name).isEqualTo("MyRuleUpdated");
    }

    @Test
    public void testDeleteEstimationRule() {
        mockEstimationRuleSet(13, true);
        Response response = target("/estimation/13/rules/1").request().delete();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void testDeleteEstimationRuleNoRuleSet() {
        when(estimationService.getEstimationRuleSet(13)).thenReturn(Optional.empty());

        Response response = target("/estimation/13/rules/12").request().delete();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testDeleteEstimationRuleNoRule() {
        mockEstimationRuleSet(13, false);
        when(estimationService.getEstimationRule(1)).thenReturn(Optional.empty());

        Response response = target("/estimation/13/rules/1").request().delete();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetReadingTypes() throws Exception {
        EstimationRuleSet estimationRuleSet = mockEstimationRuleSet(26, false);
        EstimationRule estimationRule = mockEstimationRuleInRuleSet(1, estimationRuleSet);
        doReturn(Arrays.asList(estimationRule)).when(estimationRuleSet).getRules();
        ReadingType readingType = mockReadingType();
        when(estimationRule.getReadingTypes()).thenReturn(new HashSet<>(Arrays.asList(readingType)));
        Response response = target("/estimation/26/rule/1/readingtypes").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
    }

    private void mockEstimationRuleSets(EstimationRuleSet... estimationRuleSets) {
        Query<EstimationRuleSet> query = mock(Query.class);
        when(estimationService.getEstimationRuleSetQuery()).thenReturn(query);
        RestQuery<EstimationRuleSet> restQuery = mock(RestQuery.class);
        when(restQueryService.wrap(query)).thenReturn(restQuery);
        when(restQuery.select(any(QueryParameters.class), any(Order.class))).thenReturn(Arrays.asList(estimationRuleSets));
    }

    private EstimationRuleSet mockEstimationRuleSet(int id, boolean addRules) {
        EstimationRuleSet ruleSet = mock(EstimationRuleSet.class);
        when(ruleSet.getId()).thenReturn(Long.valueOf(id));
        when(ruleSet.getName()).thenReturn("MyName");
        when(ruleSet.getDescription()).thenReturn("MyDescription");

        if (addRules) {
            List rules = Arrays.asList(mockEstimationRuleInRuleSet(1L, ruleSet));
            when(ruleSet.getRules()).thenReturn(rules);
        }

        doReturn(Optional.of(ruleSet)).when(estimationService).getEstimationRuleSet(id);

        return ruleSet;
    }

    private EstimationRule mockEstimationRuleInRuleSet(long id, EstimationRuleSet ruleSet) {
        EstimationRule rule = mock(EstimationRule.class);
        when(rule.getName()).thenReturn("MyRule");
        when(rule.getId()).thenReturn(id);
        when(rule.getImplementation()).thenReturn("com.blablabla.Estimator");
        when(rule.getDisplayName()).thenReturn("My rule");
        when(rule.isActive()).thenReturn(true);
        when(rule.getRuleSet()).thenReturn(ruleSet);

        ReadingType readingType = mockReadingType();
        Set<ReadingType> readingTypes = new HashSet<>();
        readingTypes.add(readingType);
        when(rule.getReadingTypes()).thenReturn(readingTypes);

        List<PropertySpec> propertySpes = Arrays.asList(
                mockPropertySpec(PropertyType.NUMBER, "number", true),
                mockPropertySpec(PropertyType.NULLABLE_BOOLEAN, "nullableboolean", true),
                mockPropertySpec(PropertyType.BOOLEAN, "boolean", true),
                mockPropertySpec(PropertyType.TEXT, "text", true),
                mockPropertySpec(PropertyType.LISTVALUE, "listvalue", true));
        when(rule.getPropertySpecs()).thenReturn(propertySpes);

        Map<String, Object> props = new HashMap<>();
        props.put("number", 13);
        props.put("nullableboolean", true);
        props.put("boolean", false);
        props.put("text", "string");
        ListValue<ListValueBean> listValue = new ListValue<>();
        listValue.addValue(Finder.bean1);
        listValue.addValue(Finder.bean2);
        props.put("listvalue", listValue);
        when(rule.getProps()).thenReturn(props);

        doReturn(Optional.of(rule)).when(estimationService).getEstimationRule(1);
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

    private PropertySpec mockPropertySpec(PropertyType propertyType, String name, boolean isRequired) {
        PropertySpec propertySpec = null;
        switch (propertyType) {
        case NUMBER:
            propertySpec = new BasicPropertySpec(name, isRequired, new BigDecimalFactory());
            break;
        case NULLABLE_BOOLEAN:
            propertySpec = new BasicPropertySpec(name, isRequired, new ThreeStateFactory());
            break;
        case BOOLEAN:
            propertySpec = new BasicPropertySpec(name, isRequired, new BooleanFactory());
            break;
        case TEXT:
            propertySpec = new BasicPropertySpec(name, isRequired, new StringFactory());
            break;
        case LISTVALUE:
            propertySpec = new ListValuePropertySpec<>(name, isRequired, new Finder(), Finder.bean1, Finder.bean2);
            break;
        default:
            break;
        }
        return propertySpec;
    }

    private Estimator mockEstimator(String displayName) {
        Estimator estimator = mock(Estimator.class);
        when(estimator.getDisplayName()).thenReturn(displayName);

        List<PropertySpec> propertySpecs = Arrays.asList(mockPropertySpec(PropertyType.LISTVALUE, "listvalue", false));
        when(estimator.getPropertySpecs()).thenReturn(propertySpecs);

        return estimator;
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

    private static class Finder implements CanFindByStringKey<ListValueBean> {

        static ListValueBean bean1 = new ListValueBean("1", "first");
        static ListValueBean bean2 = new ListValueBean("2", "second");

        @Override
        public Optional<ListValueBean> find(String key) {
            switch (key) {
            case "1":
                return Optional.of(bean1);
            case "2":
                return Optional.of(bean2);
            default:
                return Optional.empty();
            }
        }
        
        @Override
        public Class<ListValueBean> valueDomain() {
            return ListValueBean.class;
        }
    }
}
