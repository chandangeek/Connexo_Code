package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.orm.DataModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Duration;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KpiImplTest {

    public static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    @Rule
    public TestRule whereIfAnywhere = Using.timeZoneOfMcMurdo();

    private KpiImpl kpi;
    @Mock
    private EventService eventService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;
    @Mock
    private IdsService idsService;
    @Mock
    private IKpiService kpiService;
    @Mock
    private Vault vault;
    @Mock
    private RecordSpec recordSpec;

    @Before
    public void setUp() {
        kpi = new KpiImpl(dataModel, idsService, kpiService, eventService);

        kpi.init("name", UTC, Duration.ofMinutes(30));

        when(kpiService.getVault()).thenReturn(vault);
        when(kpiService.getRecordSpec()).thenReturn(recordSpec);

    }

    @After
    public void tearDown() {
    }

    @Test
    public void testName() {
        assertThat(kpi.getName()).isEqualTo("name");
    }

    @Test
    public void testTimeZone() {
        assertThat(kpi.getTimeZone()).isEqualTo(UTC);
    }

    @Test
    public void testIntervalLength() {
        assertThat(kpi.getIntervalLength()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    public void testSaveNewCreatesTimeSeries() {
        kpi.dynamicMaximum("max");
        kpi.doSave();

        verify(vault).createRegularTimeSeries(recordSpec, UTC, Duration.ofMinutes(30), 0);
    }


}