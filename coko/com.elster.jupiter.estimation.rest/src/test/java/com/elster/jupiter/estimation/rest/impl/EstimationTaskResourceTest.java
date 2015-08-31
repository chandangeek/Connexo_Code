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
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.EstimationTaskBuilder;
import com.elster.jupiter.estimation.Estimator;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
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
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Never;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EstimationTaskResourceTest extends EstimationApplicationJerseyTest {

    private EstimationTaskBuilder builder = initBuilderStub();
    @Mock
    private EstimationTask estimationTask;
    @Mock
    protected RelativePeriod period;
    @Mock
    private EndDeviceGroup endDeviceGroup;
    @Mock
    private Query<EstimationTask> query;
    @Mock
    private RestQuery<EstimationTask> restQuery;

    private EstimationTaskBuilder initBuilderStub() {
        final Object proxyInstance = Proxy.newProxyInstance(EstimationTaskBuilder.class.getClassLoader(), new Class<?>[]{EstimationTaskBuilder.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (EstimationTaskBuilder.class.isAssignableFrom(method.getReturnType())) {
                    return builderGetter.get();
                }
                return taskGetter.get();
            }

            private Supplier<EstimationTask> taskGetter = () -> estimationTask;
            private Supplier<EstimationTaskBuilder> builderGetter = () -> builder;
        });
        return (EstimationTaskBuilder) proxyInstance;
    }

    public static final ZonedDateTime NEXT_EXECUTION = ZonedDateTime.of(2015, 1, 13, 0, 0, 0, 0, ZoneId.systemDefault());
    public static final int TASK_ID = 750;

    @Before
    public void setUpMocks() {
        doReturn(query).when(estimationService).getEstimationTaskQuery();
        doReturn(restQuery).when(restQueryService).wrap(query);
        doReturn(Arrays.asList(estimationTask)).when(restQuery).select(any(), any(Order.class), any(Order.class));
        when(estimationTask.getEndDeviceGroup()).thenReturn(endDeviceGroup);
        when(estimationTask.getPeriod()).thenReturn(Optional.of(period));
        when(period.getRelativeDateFrom()).thenReturn(new RelativeDate(RelativeField.DAY.minus(1)));
        when(period.getRelativeDateTo()).thenReturn(new RelativeDate());
        when(estimationTask.getNextExecution()).thenReturn(NEXT_EXECUTION.toInstant());
        when(meteringGroupsService.findEndDeviceGroup(5)).thenReturn(Optional.of(endDeviceGroup));
        when(estimationTask.getScheduleExpression()).thenReturn(Never.NEVER);
        when(estimationService.newBuilder()).thenReturn(builder);
//        when(estimationTask.getOccurrencesFinder()).thenReturn(finder);
        when(estimationTask.getName()).thenReturn("Name");
        when(estimationTask.getLastOccurrence()).thenReturn(Optional.empty());
        when(estimationTask.getLastRun()).thenReturn(Optional.<Instant>empty());

        doReturn(Optional.of(estimationTask)).when(estimationService).findEstimationTask(TASK_ID);
//        setUpStubs();
    }

    @Test
    public void getTasksTest() {
        EstimationTaskInfo info = new EstimationTaskInfo();

        Response response1 = target("/estimation/tasks").request().get();
        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        EstimationTaskInfos infos = response1.readEntity(EstimationTaskInfos.class);
        assertThat(infos.total).isEqualTo(1);
        assertThat(infos.estimationTasks).hasSize(1);
    }

    @Test
    public void triggerTaskTest() {
        EstimationTaskInfo info = new EstimationTaskInfo();

        Response response1 = target("/estimation/tasks/"+TASK_ID+"/trigger").request().post(Entity.json(null));
        assertThat(response1.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(estimationTask).triggerNow();
    }


    @Test
    public void getCreateTasksTest() {
        EstimationTaskInfo info = new EstimationTaskInfo();
        info.name = "newName";
        info.nextRun = 250L;
        info.deviceGroup = new MeterGroupInfo();
        info.deviceGroup.id = 5;

        Entity<EstimationTaskInfo> json = Entity.json(info);

        Response response = target("/estimation/tasks").request().post(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test
    public void updateTasksTest() {
        EstimationTaskInfo info = new EstimationTaskInfo();
        info.id = TASK_ID;
        info.deviceGroup = new MeterGroupInfo();
        info.deviceGroup.id = 5;

        Entity<EstimationTaskInfo> json = Entity.json(info);

        Response response = target("/estimation/tasks/" + TASK_ID).request().put(json);

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
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
