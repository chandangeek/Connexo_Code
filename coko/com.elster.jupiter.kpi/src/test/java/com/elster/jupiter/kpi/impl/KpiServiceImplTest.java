package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.IntervalLength;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KpiServiceImplTest {

    public static final String KPI_NAME = "kpiName";
    public static final String READ_METERS = "readMeters";
    public static final String NON_COMMUNICATING_METERS = "nonCommunicatingMeters";
    KpiServiceImpl kpiService = new KpiServiceImpl();
    Date date = new DateTime(2000, 2, 11, 20, 0, 0, 0, DateTimeZone.forID("Europe/Brussels")).toDate();

    @Mock
    private IdsService idsService;
    @Mock
    private Vault vault;
    @Mock
    private RecordSpec recordSpec;
    @Mock
    private OrmService ormService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataModel dataModel;

    @Before
    public void setUp() {
        when(idsService.getVault(anyString(), anyLong())).thenReturn(Optional.of(vault));
        when(idsService.newVault(anyString(), anyLong(), anyString(), anyInt(), anyBoolean())).thenReturn(vault);
        when(idsService.getRecordSpec(anyString(), anyLong())).thenReturn(Optional.of(recordSpec));
        when(dataModel.getInstance(KpiImpl.class)).thenAnswer(new Answer<KpiImpl>() {
            @Override
            public KpiImpl answer(InvocationOnMock invocation) throws Throwable {
                return new KpiImpl(dataModel, idsService, kpiService);
            }
        });
        when(idsService.newRecordSpec(anyString(), anyLong(), anyString())).thenReturn(recordSpec);
        doReturn(dataModel).when(ormService).newDataModel(anyString(), anyString());
        kpiService.setIdsService(idsService);
        kpiService.setOrmService(ormService);
        kpiService.install();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testBuildingAKpi() {

        Kpi kpi = kpiService.newKpi().named(KPI_NAME).interval(IntervalLength.ofDay())
                .member().named(READ_METERS).withDynamicTarget().asMinimum().add()
                .member().named(NON_COMMUNICATING_METERS).withTargetSetAt(BigDecimal.valueOf(1, 2)).asMaximum().add()
                .build();

        assertThat(kpi.getName()).isEqualTo(KPI_NAME);
        assertThat(kpi.getMembers()).hasSize(2);
        KpiMember first = kpi.getMembers().get(0);
        assertThat(first.getName()).isEqualTo(READ_METERS);
        assertThat(first.hasDynamicTarget()).isTrue();
        assertThat(first.targetIsMinimum()).isTrue();
        assertThat(first.targetIsMaximum()).isFalse();

        KpiMember second = kpi.getMembers().get(1);
        assertThat(second.getName()).isEqualTo(NON_COMMUNICATING_METERS);
        assertThat(second.hasDynamicTarget()).isFalse();
        assertThat(second.getTarget(date)).isEqualTo(BigDecimal.valueOf(1, 2));
        assertThat(second.targetIsMinimum()).isFalse();
        assertThat(second.targetIsMaximum()).isTrue();

    }


}
