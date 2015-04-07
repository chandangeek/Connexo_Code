package com.elster.jupiter.validation.rest;

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
import com.elster.jupiter.properties.FindById;
import com.elster.jupiter.properties.ListValue;
import com.elster.jupiter.properties.ListValueEntry;
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
import com.elster.jupiter.validation.*;
import com.jayway.jsonpath.JsonModel;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationResourceTest extends BaseValidationRestTest {

    @Test
    public void testGetValidationRuleSetsNoRuleSets() {
        mockValidationRuleSets();

        ValidationRuleSetInfos response = target("/validation").request().get(ValidationRuleSetInfos.class);

        assertThat(response.total).isEqualTo(0);
        assertThat(response.ruleSets).hasSize(0);
    }

    @Test
    public void testGetValidationRuleSets() {
        mockValidationRuleSets(mockValidationRuleSet(13, false));

        ValidationRuleSetInfos response = target("/validation").request().get(ValidationRuleSetInfos.class);

        assertThat(response.total).isEqualTo(1);

        List<ValidationRuleSetInfo> ruleSetInfos = response.ruleSets;
        assertThat(ruleSetInfos).hasSize(1);

        ValidationRuleSetInfo ruleSetInfo = ruleSetInfos.get(0);
        assertThat(ruleSetInfo.name).isEqualTo("MyName");
        assertThat(ruleSetInfo.id).isEqualTo(13);
        assertThat(ruleSetInfo.description).isEqualTo("MyDescription");
        assertThat(ruleSetInfo.numberOfInactiveRules).isEqualTo(0);
        assertThat(ruleSetInfo.numberOfRules).isEqualTo(0);
    }

    @Test
    public void testGetValidationRuleSetNotFound() {
        when(validationService.getValidationRuleSet(13)).thenReturn(Optional.empty());

        Response response = target("/validation/13").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetValidationRuleSet() {
        mockValidationRuleSet(13, true);

        ValidationRuleSetInfo ruleSetInfo = target("/validation/13").request().get(ValidationRuleSetInfo.class);

        assertThat(ruleSetInfo.id).isEqualTo(13);
        assertThat(ruleSetInfo.name).isEqualTo("MyName");
        assertThat(ruleSetInfo.description).isEqualTo("MyDescription");
        assertThat(ruleSetInfo.numberOfInactiveRules).isEqualTo(0);
        assertThat(ruleSetInfo.numberOfRules).isEqualTo(1);
    }

    @Test
    public void testGetValidationRulesNoVersions() {
        mockValidationRuleSets(mockValidationRuleSet(13, false));

        ValidationRuleSetVersionInfos versionInfos = target("/validation/13/versions").request().get(ValidationRuleSetVersionInfos.class);

        assertThat(versionInfos.total).isEqualTo(0);
    }

    @Test
    public void testGetValidationRulesVersions(){

    }

//    @Test
//    public void testGetValidationRules() {
//        mockValidationRuleSets(mockValidationRuleSet(13, true));
//
//        ValidationRuleInfos ruleInfos = target("/validation/13/rules").request().get(ValidationRuleInfos.class);
//
//        assertThat(ruleInfos.total).isEqualTo(1);
//
//        List<ValidationRuleInfo> rules = ruleInfos.rules;
//        assertThat(rules).hasSize(1);
//
//        ValidationRuleInfo ruleInfo = rules.get(0);
//        assertThat(ruleInfo.id).isEqualTo(1);
//        assertThat(ruleInfo.name).isEqualTo("MyRule");
//        assertThat(ruleInfo.implementation).isEqualTo("com.blablabla.Validator");
//        assertThat(ruleInfo.displayName).isEqualTo("My rule");
//        assertThat(ruleInfo.active).isEqualTo(true);
//
//        ValidationRuleSetInfo ruleSetInfo = ruleInfo.ruleSet;
//        assertThat(ruleSetInfo.id).isEqualTo(13);
//        assertThat(ruleSetInfo.name).isEqualTo("MyName");
//        assertThat(ruleSetInfo.description).isEqualTo("MyDescription");
//        assertThat(ruleSetInfo.numberOfInactiveRules).isEqualTo(0);
//        assertThat(ruleSetInfo.numberOfRules).isEqualTo(1);
//
//        List<ReadingTypeInfo> readingTypeInfos = ruleInfo.readingTypes;
//        assertThat(readingTypeInfos.get(0).mRID).isEqualTo("0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0");
//
//        List<PropertyInfo> propertyInfos = ruleInfo.properties;
//        assertThat(propertyInfos).hasSize(5);
//
//        PropertyInfo propertyNumberInfo = propertyInfos.get(0);
//        assertThat(propertyNumberInfo.key).isEqualTo("number");
//        assertThat(propertyNumberInfo.required).isEqualTo(true);
//
//        PropertyValueInfo<?> numberValueInfo = propertyNumberInfo.propertyValueInfo;
//        assertThat(numberValueInfo.value).isEqualTo(13);
//        assertThat(numberValueInfo.defaultValue).isEqualTo(null);
//
//        PropertyInfo propertyNullableBooleanInfo = propertyInfos.get(1);
//        assertThat(propertyNullableBooleanInfo.key).isEqualTo("nullableboolean");
//        assertThat(propertyNullableBooleanInfo.required).isEqualTo(true);
//
//        PropertyValueInfo<?> nullableBooleanValueInfo = propertyNullableBooleanInfo.propertyValueInfo;
//        assertThat(nullableBooleanValueInfo.value).isEqualTo(true);
//        assertThat(nullableBooleanValueInfo.defaultValue).isEqualTo(null);
//
//        PropertyInfo propertyBooleanInfo = propertyInfos.get(2);
//        assertThat(propertyBooleanInfo.key).isEqualTo("boolean");
//        assertThat(propertyBooleanInfo.required).isEqualTo(true);
//
//        PropertyValueInfo<?> booleanValueInfo = propertyBooleanInfo.propertyValueInfo;
//        assertThat(booleanValueInfo.value).isEqualTo(false);
//        assertThat(booleanValueInfo.defaultValue).isEqualTo(null);
//
//        PropertyInfo propertyTextInfo = propertyInfos.get(3);
//        assertThat(propertyTextInfo.key).isEqualTo("text");
//        assertThat(propertyTextInfo.required).isEqualTo(true);
//
//        PropertyValueInfo<?> textValueInfo = propertyTextInfo.propertyValueInfo;
//        assertThat(textValueInfo.value).isEqualTo("string");
//        assertThat(textValueInfo.defaultValue).isEqualTo(null);
//
//        PropertyInfo propertyListValueInfo = propertyInfos.get(4);
//        assertThat(propertyListValueInfo.key).isEqualTo("listvalue");
//        assertThat(propertyListValueInfo.required).isEqualTo(true);
//
//        PropertyValueInfo<?> listValueInfo = propertyListValueInfo.propertyValueInfo;
//        assertThat(listValueInfo.defaultValue).isEqualTo(null);
//
//        List<?> listValue = (List<?>) listValueInfo.value;
//        assertThat(listValue).hasSize(2);
//
//        assertThat(listValue.get(0)).isEqualTo("1");
//        assertThat(listValue.get(1)).isEqualTo("2");
//    }
//
//    @Test
//    public void testGetValidatorsNoValidators() {
//        ValidatorInfos validatorInfos = target("/validation/validators").request().get(ValidatorInfos.class);
//
//        assertThat(validatorInfos.total).isEqualTo(0);
//        assertThat(validatorInfos.validators).hasSize(0);
//    }
//
//    @Test
//    public void testGetValidators() {
//        List<Validator> mockValidator = Arrays.asList(mockValidator("B Validator"), mockValidator("A Validator"));
//        when(validationService.getAvailableValidators()).thenReturn(mockValidator);
//
//        ValidatorInfos validatorInfos = target("/validation/validators").request().get(ValidatorInfos.class);
//
//        assertThat(validatorInfos.total).isEqualTo(2);
//        List<ValidatorInfo> validators = validatorInfos.validators;
//        assertThat(validators).hasSize(2);
//
//        ValidatorInfo validatorAInfo = validators.get(0);
//        assertThat(validatorAInfo.displayName).isEqualTo("A Validator");
//        assertThat(validatorAInfo.implementation).isNotNull();
//
//        ValidatorInfo validatorBInfo = validators.get(1);
//        assertThat(validatorBInfo.displayName).isEqualTo("B Validator");
//        assertThat(validatorBInfo.implementation).isNotNull();
//
//        List<PropertyInfo> propertyInfos = validatorAInfo.properties;
//        assertThat(propertyInfos).hasSize(1);
//
//        PropertyInfo propertyInfo = propertyInfos.get(0);
//        assertThat(propertyInfo.key).isEqualTo("listvalue");
//        assertThat(propertyInfo.required).isEqualTo(false);
//
//        PropertyTypeInfo typeInfo = propertyInfo.propertyTypeInfo;
//        PredefinedPropertyValuesInfo<?> predefinedValuesInfo = typeInfo.predefinedPropertyValuesInfo;
//        assertThat(predefinedValuesInfo.selectionMode).isEqualTo(PropertySelectionMode.LIST);
//
//        Object[] possibleValuesInfo = predefinedValuesInfo.possibleValues;
//        assertThat(possibleValuesInfo).hasSize(2);
//
//        Map<?, ?> possibleValue1 = (Map<?, ?>) possibleValuesInfo[0];
//        assertThat(possibleValue1.get("id")).isEqualTo("1");
//        assertThat(possibleValue1.get("name")).isEqualTo("first");
//
//        Map<?, ?> possibleValue2 = (Map<?, ?>) possibleValuesInfo[1];
//        assertThat(possibleValue2.get("id")).isEqualTo("2");
//        assertThat(possibleValue2.get("name")).isEqualTo("second");
//    }
//
//    @Test
//    public void testAddValidationRule() {
//        final ValidationRuleInfo info = new ValidationRuleInfo();
//        info.name = "MyRule";
//        info.implementation = "com.blablabla.Validator";
//        info.properties = createPropertyInfos();
//
//        Entity<ValidationRuleInfo> entity = Entity.json(info);
//
//        ValidationRuleSet ruleSet = mockValidationRuleSet(13, false);
//        ValidationRule rule = mockValidationRuleInRuleSet(1L, ruleSet);
//        when(ruleSet.addRule(Matchers.eq(ValidationAction.FAIL), Matchers.eq(info.implementation), Matchers.eq(info.name))).thenReturn(rule);
//
//        Response response = target("/validation/13/rules").request().post(entity);
//
//        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
//        ValidationRuleInfo resultInfo = response.readEntity(ValidationRuleInfo.class);
//        assertThat(resultInfo.name).isEqualTo("MyRule");
//
//        verify(rule).addProperty("number", BigDecimal.valueOf(10.0));
//        verify(rule).addProperty("nullableboolean", false);
//        verify(rule).addProperty("boolean", true);
//        verify(rule).addProperty("text", "string");
//        verify(rule).addProperty(Matchers.eq("listvalue"), Matchers.any(ListValue.class));
//    }
//
//    @Test
//    public void testAddValidationRuleWarnOnly() {
//        final ValidationRuleInfo info = new ValidationRuleInfo();
//        info.name = "MyRule";
//        info.implementation = "com.blablabla.Validator";
//        info.properties = createPropertyInfos();
//        info.action = ValidationAction.WARN_ONLY;
//
//        Entity<ValidationRuleInfo> entity = Entity.json(info);
//
//        ValidationRuleSet ruleSet = mockValidationRuleSet(13, false);
//        ValidationRule rule = mockValidationRuleInRuleSet(1L, ruleSet);
//        when(rule.getAction()).thenReturn(ValidationAction.WARN_ONLY);
//        when(ruleSet.addRule(Matchers.eq(ValidationAction.WARN_ONLY), Matchers.eq(info.implementation), Matchers.eq(info.name))).thenReturn(rule);
//
//        Response response = target("/validation/13/rules").request().post(entity);
//
//        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
//        ValidationRuleInfo resultInfo = response.readEntity(ValidationRuleInfo.class);
//        assertThat(resultInfo.name).isEqualTo("MyRule");
//        assertThat(resultInfo.action).isEqualTo(ValidationAction.WARN_ONLY);
//
//        verify(rule).addProperty("number", BigDecimal.valueOf(10.0));
//        verify(rule).addProperty("nullableboolean", false);
//        verify(rule).addProperty("boolean", true);
//        verify(rule).addProperty("text", "string");
//        verify(rule).addProperty(Matchers.eq("listvalue"), Matchers.any(ListValue.class));
//    }
//
//    @Test
//    public void testAddValidationRuleToNonExistingRuleSet() {
//        final ValidationRuleInfo info = new ValidationRuleInfo();
//        info.name = "MyRule";
//        info.implementation = "com.blablabla.Validator";
//        info.properties = createPropertyInfos();
//
//        Entity<ValidationRuleInfo> entity = Entity.json(info);
//
//        when(validationService.getValidationRuleSet(666)).thenReturn(Optional.empty());
//        Response response = target("/validation/666/rules").request().post(entity);
//        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
//    }
//
//    @Test
//    public void testUpdateValidationRuleSet() throws Exception {
//        ValidationRuleSet ruleSet = mockValidationRuleSet(15, true);
//
//        final ValidationRuleSetInfo info = new ValidationRuleSetInfo();
//        info.name = "MyRuleUpdated";
//        info.description = "blablabla";
//
//        Response response = target("/validation/15").request().put(Entity.json(info));
//        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
//        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
//        verify(ruleSet).setName(stringArgumentCaptor.capture());
//        assertThat(stringArgumentCaptor.getValue()).isEqualTo(info.name);
//        verify(ruleSet).setDescription(stringArgumentCaptor.capture());
//        assertThat(stringArgumentCaptor.getValue()).isEqualTo(info.description);
//        verify(ruleSet, times(1)).save();
//    }
//
//    @Test
//    public void testCreateValidationRuleSetInfo() throws Exception {
//        ValidationRuleSetInfo info = new ValidationRuleSetInfo();
//        info.name="ruleset";
//        info.description="desc";
//
//        ValidationRuleSet ruleSet = mockValidationRuleSet(12, false);
//        when(validationService.createValidationRuleSet(info.name, info.description)).thenReturn(ruleSet);
//        Response response = target("/validation").request().post(Entity.json(info));
//        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
//    }
//
//    @Test
//    public void testGetValidationRuleInfo() throws Exception {
//        ValidationRuleSet ruleSet = mockValidationRuleSet(12, false);
//        ValidationRule validationRule = mockValidationRuleInRuleSet(13, ruleSet);
//        doReturn(Arrays.asList(validationRule)).when(ruleSet).getRules();
//        Response response = target("/validation/12/rules/13").request().get();
//        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
//        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
//        assertThat(jsonModel.<String>get("$.name")).isEqualTo("MyRule");
//        assertThat(jsonModel.<Integer>get("$.id")).isEqualTo(13);
//    }
//
//    @Test
//    public void testDeleteRuleSet() throws Exception {
//        ValidationRuleSet validationRuleSet = mockValidationRuleSet(99, false);
//        Response response = target("/validation/99").request().delete();
//        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
//        verify(validationRuleSet).delete();
//    }
//
//    @Test
//    public void testGetRuleSetUsage() throws Exception {
//        ValidationRuleSet validationRuleSet = mockValidationRuleSet(1, false);
//        ValidationRuleSet validationRuleSet2 = mockValidationRuleSet(2, false);
//        when(validationService.isValidationRuleSetInUse(validationRuleSet)).thenReturn(true);
//        when(validationService.isValidationRuleSetInUse(validationRuleSet2)).thenReturn(false);
//        Response response = target("/validation/1/usage").request().get();
//        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
//        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
//        assertThat(jsonModel.<Boolean>get("$.isInUse")).isEqualTo(true);
//    }
//
//    @Test
//    public void testEditValidationRule() {
//        final ValidationRuleInfo info = new ValidationRuleInfo();
//        info.name = "MyRuleUpdated";
//        info.implementation = "com.blablabla.Validator";
//        info.properties = new ArrayList<>();
//        info.action = ValidationAction.FAIL;
//
//        ValidationRuleSet ruleSet = mockValidationRuleSet(13, true);
//        ValidationRule rule = ruleSet.getRules().get(0);
//        when(rule.getName()).thenReturn("MyRuleUpdated");
//        when(ruleSet.updateRule(
//                Matchers.eq(1L),
//                Matchers.eq("MyRuleUpdated"),
//                Matchers.eq(false),
//                Matchers.eq(ValidationAction.FAIL),
//                Matchers.eq(new ArrayList<>()),
//                Matchers.eq(new HashMap<>()))).
//                thenReturn(rule);
//
//        Entity<ValidationRuleInfo> entity = Entity.json(info);
//        Response response = target("/validation/13/rules/1").request().put(entity);
//
//        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
//
//        ValidationRuleInfos resultInfos = response.readEntity(ValidationRuleInfos.class);
//        assertThat(resultInfos.total).isEqualTo(1);
//        assertThat(resultInfos.rules).hasSize(1);
//        assertThat(resultInfos.rules.get(0).name).isEqualTo("MyRuleUpdated");
//        assertThat(resultInfos.rules.get(0).action).isEqualTo(ValidationAction.FAIL);
//    }
//
//    @Test
//    public void testEditValidationRuleWarnOnly() {
//        final ValidationRuleInfo info = new ValidationRuleInfo();
//        info.name = "MyRuleUpdated";
//        info.implementation = "com.blablabla.Validator";
//        info.properties = new ArrayList<>();
//        info.action = ValidationAction.WARN_ONLY;
//
//        ValidationRuleSet ruleSet = mockValidationRuleSet(13, true);
//        ValidationRule rule = ruleSet.getRules().get(0);
//        when(rule.getName()).thenReturn("MyRuleUpdated");
//        when(rule.getAction()).thenReturn(ValidationAction.WARN_ONLY);
//        when(ruleSet.updateRule(
//                Matchers.eq(1L),
//                Matchers.eq("MyRuleUpdated"),
//                Matchers.eq(false),
//                Matchers.eq(ValidationAction.WARN_ONLY),
//                Matchers.eq(new ArrayList<>()),
//                Matchers.eq(new HashMap<>()))).
//                thenReturn(rule);
//
//        Entity<ValidationRuleInfo> entity = Entity.json(info);
//        Response response = target("/validation/13/rules/1").request().put(entity);
//
//        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
//
//        ValidationRuleInfos resultInfos = response.readEntity(ValidationRuleInfos.class);
//        assertThat(resultInfos.total).isEqualTo(1);
//        assertThat(resultInfos.rules).hasSize(1);
//        assertThat(resultInfos.rules.get(0).name).isEqualTo("MyRuleUpdated");
//        assertThat(resultInfos.rules.get(0).action).isEqualTo(ValidationAction.WARN_ONLY);
//    }
//
//    @Test
//    public void testDeleteValidationRule() {
//        mockValidationRuleSet(13, true);
//        Response response = target("/validation/13/rules/1").request().delete();
//
//        assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
//    }
//
//    @Test
//    public void testDeleteValidationRuleNoRuleSet() {
//        when(validationService.getValidationRuleSet(13)).thenReturn(Optional.empty());
//
//        Response response = target("/validation/13/rules/12").request().delete();
//
//        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
//    }
//
//    @Test
//    public void testDeleteValidationRuleNoRule() {
//        mockValidationRuleSet(13, false);
//        when(validationService.getValidationRule(1)).thenReturn(Optional.empty());
//
//        Response response = target("/validation/13/rules/1").request().delete();
//
//        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
//    }
//
//    @Test
//    public void testGetReadingTypes() throws Exception {
//        ValidationRuleSet validationRuleSet = mockValidationRuleSet(26, false);
//        ValidationRule validationRule = mockValidationRuleInRuleSet(1, validationRuleSet);
//        doReturn(Arrays.asList(validationRule)).when(validationRuleSet).getRules();
//        ReadingType readingType = mockReadingType();
//        when(validationRule.getReadingTypes()).thenReturn(new HashSet<>(Arrays.asList(readingType)));
//        Response response = target("/validation/26/rule/1/readingtypes").request().get();
//        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
//        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
//        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
//    }
//
    private void mockValidationRuleSets(ValidationRuleSet... validationRuleSets) {
        Query<ValidationRuleSet> query = mock(Query.class);
        when(validationService.getRuleSetQuery()).thenReturn(query);
        RestQuery<ValidationRuleSet> restQuery = mock(RestQuery.class);
        when(restQueryService.wrap(query)).thenReturn(restQuery);
        when(restQuery.select(any(QueryParameters.class), any(Order.class))).thenReturn(Arrays.asList(validationRuleSets));
    }

    private ValidationRuleSet mockValidationRuleSet(int id, boolean version) {
        ValidationRuleSet ruleSet = mock(ValidationRuleSet.class);
        when(ruleSet.getId()).thenReturn(Long.valueOf(id));
        when(ruleSet.getName()).thenReturn("MyName");
        when(ruleSet.getDescription()).thenReturn("MyDescription");

        if (version) {
            List versions = Arrays.asList(mockValidationRuleSetVersion(1L));
            when(ruleSet.getRules()).thenReturn(versions);
        }

        doReturn(Optional.of(ruleSet)).when(validationService).getValidationRuleSet(id);

        return ruleSet;
    }

    private ValidationRuleSetVersion mockValidationRuleSetVersion(long id){
        final Instant date = ZonedDateTime.of(1983, 5, 31, 14, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        ValidationRuleSetVersion ruleSetVersion = mock(ValidationRuleSetVersion.class);
        when(ruleSetVersion.getDescription()).thenReturn("descriptionOfVersion");
        when(ruleSetVersion.getId()).thenReturn(id);
        when(ruleSetVersion.getName()).thenReturn("nameOfVersion");
        when(ruleSetVersion.getStartDate()).thenReturn(date);
        return ruleSetVersion;
    }

    private ValidationRule mockValidationRuleInRuleSet(long id, ValidationRuleSet ruleSet) {
        ValidationRule rule = mock(ValidationRule.class);
        when(rule.getName()).thenReturn("MyRule");
        when(rule.getId()).thenReturn(id);
        when(rule.getAction()).thenReturn(ValidationAction.FAIL);
        when(rule.getImplementation()).thenReturn("com.blablabla.Validator");
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

        when(validationService.getValidationRule(1)).thenReturn(Optional.of(rule));
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
        PropertyInfo numberInfo = new PropertyInfo("number", new PropertyValueInfo<>(Double.valueOf(10), null), null, true);
        PropertyInfo nullableInfo = new PropertyInfo("nullableboolean", new PropertyValueInfo<>(false, null), null, true);
        PropertyInfo booleanInfo = new PropertyInfo("boolean", new PropertyValueInfo<>(true, null), null, true);
        PropertyInfo textInfo = new PropertyInfo("text", new PropertyValueInfo<>("string", null), null, true);
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

    private Validator mockValidator(String displayName) {
        Validator validator = mock(Validator.class);
        when(validator.getDisplayName()).thenReturn(displayName);

        List<PropertySpec> propertySpecs = Arrays.asList(mockPropertySpec(PropertyType.LISTVALUE, "listvalue", false));
        when(validator.getPropertySpecs()).thenReturn(propertySpecs);

        return validator;
    }

    private static class ListValueBean implements ListValueEntry {

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

    private static class Finder implements FindById<ListValueBean> {

        static ListValueBean bean1 = new ListValueBean("1", "first");
        static ListValueBean bean2 = new ListValueBean("2", "second");

        @Override
        public Optional<ListValueBean> findById(final String id) {
            switch (id) {
            case "1":
                return Optional.of(bean1);
            case "2":
                return Optional.of(bean2);
            default:
                return Optional.empty();
            }
        }
    }
}
