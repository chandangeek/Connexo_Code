package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.Interval;

import com.elster.insight.usagepoint.config.Formula;
import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.config.MetrologyContract;
import com.elster.insight.usagepoint.config.ReadingTypeDeliverable;
import com.elster.insight.usagepoint.config.ReadingTypeRequirement;
import com.google.common.collect.Range;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DataAggregationServiceImpl#calculate(UsagePoint, MetrologyContract, Range)} method.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-10 (15:40)
 */
@RunWith(MockitoJUnitRunner.class)
public class DataAggregationServiceImplCalculateTest {

    @Mock
    private VirtualFactory virtualFactory;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private MetrologyConfiguration configuration;
    @Mock
    private MetrologyContract contract;
    @Mock
    private DataModel dataModel;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private SqlBuilderFactory sqlBuilderFactory;
    @Mock
    private ClauseAwareSqlBuilder sqlBuilder;

    @Before
    public void initializeMocks() throws SQLException {
        when(this.sqlBuilderFactory.newClauseAwareSqlBuilder()).thenReturn(this.sqlBuilder);
        when(this.sqlBuilder.finish()).thenReturn(new SqlBuilder());
        when(this.dataModel.getConnection(true)).thenReturn(this.connection);
        when(this.connection.prepareStatement(anyString())).thenReturn(this.preparedStatement);
        when(this.preparedStatement.executeQuery()).thenReturn(this.resultSet);
    }

    /**
     * Tests the simplest case:
     * Metrology configuration
     *    requirements:
     *       A+ ::= any Wh with flow = forward (aka consumption)
     *       A- ::= any Wh with flow = reverse (aka production)
     *    deliverables:
     *       netConsumption (15m kWh) ::= A+ + A-
     * Device:
     *    meter activations:
     *       Jan 1st 2015 -> forever
     *           A+ -> 15 min kWh
     *           A- -> 15 min kWh
     * In other words, simple sum of 2 requirements that are provided
     * by exactly one matching channel with a single meter activation.
     */
    @Test
    public void simplestNetConsumptionOfProsumer() throws SQLException {
        DataAggregationServiceImpl service = this.testInstance();
        // Setup configuration requirements
        ReadingTypeRequirement consumption = mock(ReadingTypeRequirement.class);
        when(consumption.getName()).thenReturn("A+");
        ReadingTypeRequirement production = mock(ReadingTypeRequirement.class);
        when(production.getName()).thenReturn("A-");
        when(this.configuration.getRequirements()).thenReturn(Arrays.asList(consumption, production));
        // Setup configuration deliverables
        ReadingTypeDeliverable netConsumption = mock(ReadingTypeDeliverable.class);
        when(netConsumption.getName()).thenReturn("consumption");
        ReadingType netConsumptionReadingType = this.mock15minReadingType();
        when(netConsumption.getReadingType()).thenReturn(netConsumptionReadingType);
        ServerFormula formula = mock(ServerFormula.class);
        when(formula.getMode()).thenReturn(Formula.Mode.AUTO);
        when(formula.expressionNode())
            .thenReturn(
                    new OperationNode(
                            Operator.PLUS,
                            new ReadingTypeRequirementNode(consumption),
                            new ReadingTypeRequirementNode(production)));
        when(netConsumption.getFormula()).thenReturn(formula);
        // Setup contract deliverables
        when(this.contract.getDeliverables()).thenReturn(Collections.singletonList(netConsumption));
        // Setup meter activations
        MeterActivation meterActivation = mock(MeterActivation.class);
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(this.usagePoint));
        when(meterActivation.getInterval()).thenReturn(Interval.startAt(jan1st2015()));
        when(meterActivation.overlaps(year2016())).thenReturn(true);
        doReturn(Collections.singletonList(meterActivation)).when(this.usagePoint).getMeterActivations();
        Channel chn1 = mock(Channel.class);
        Channel chn2 = mock(Channel.class);
        ReadingType readingType15min = this.mock15minReadingType();
        when(consumption.getMatchesFor(meterActivation)).thenReturn(Collections.singletonList(readingType15min));
        when(consumption.getMatchingChannelsFor(meterActivation)).thenReturn(Collections.singletonList(chn1));
        when(production.getMatchesFor(meterActivation)).thenReturn(Collections.singletonList(readingType15min));
        when(production.getMatchingChannelsFor(meterActivation)).thenReturn(Collections.singletonList(chn2));
        when(this.resultSet.next()).thenReturn(false);

        // Business method
        service.calculate(this.usagePoint, this.contract, year2016());

        // Asserts
        verify(this.virtualFactory).nextMeterActivation(meterActivation);
        verify(this.virtualFactory).requirementFor(consumption, netConsumption, IntervalLength.MINUTE15);
        verify(this.virtualFactory).requirementFor(production, netConsumption, IntervalLength.MINUTE15);
        verify(this.virtualFactory).allRequirements();
        verify(this.virtualFactory).allDeliverables();
        verify(this.dataModel).getConnection(true);
        verify(this.resultSet).next();
        verify(this.resultSet).close();
        verify(this.preparedStatement).close();
        verify(this.connection).close();
    }

    private Instant jan1st2015() {
        return Instant.ofEpochMilli(1420070400000L);
    }

    private Range<Instant> year2016() {
        return Range.atLeast(Instant.ofEpochMilli(1451606400000L));
    }

    private ReadingType mock15minReadingType() {
        ReadingType meterActivationReadingType = mock(ReadingType.class);
        when(meterActivationReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(meterActivationReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        return meterActivationReadingType;
    }

    private ReadingType mockHourlyReadingType() {
        ReadingType meterActivationReadingType = mock(ReadingType.class);
        when(meterActivationReadingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(meterActivationReadingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE60);
        return meterActivationReadingType;
    }

    private DataAggregationServiceImpl testInstance() {
        return new DataAggregationServiceImpl(this.virtualFactory, this.sqlBuilderFactory, this.dataModel);
    }

}