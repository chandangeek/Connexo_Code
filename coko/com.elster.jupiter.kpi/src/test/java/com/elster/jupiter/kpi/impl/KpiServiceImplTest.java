/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.EventTypeBuilder;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.RecordSpecBuilder;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiMember;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KpiServiceImplTest {

    public static final String KPI_NAME = "kpiName";
    public static final String READ_METERS = "readMeters";
    public static final String NON_COMMUNICATING_METERS = "nonCommunicatingMeters";
    KpiServiceImpl kpiService = new KpiServiceImpl();
    Instant date = ZonedDateTime.of(2000, 2, 11, 20, 0, 0, 0, ZoneId.of("Europe/Brussels")).toInstant();

    @Mock
    private IdsService idsService;
    @Mock
    private EventService eventService;
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
        when(idsService.createVault(anyString(), anyLong(), anyString(), anyInt(), anyInt(), anyBoolean())).thenReturn(vault);
        when(idsService.getRecordSpec(anyString(), anyLong())).thenReturn(Optional.of(recordSpec));
        when(dataModel.getInstance(KpiImpl.class)).thenAnswer(invocation -> new KpiImpl(dataModel, idsService, kpiService, eventService));
        when(dataModel.getInstance(Installer.class)).thenAnswer(invocation -> new Installer(dataModel, eventService, idsService));
        when(idsService.createRecordSpec(anyString(), anyLong(), anyString())).thenReturn(FakeBuilder.initBuilderStub(recordSpec, RecordSpecBuilder.class));
        when(eventService.buildEventTypeWithTopic(anyString())).thenReturn(FakeBuilder.initBuilderStub(null, EventTypeBuilder.class));
        doReturn(dataModel).when(ormService).newDataModel(anyString(), anyString());
        kpiService.setIdsService(idsService);
        kpiService.setOrmService(ormService);
        kpiService.setEventService(eventService);
        kpiService.setUpgradeService(UpgradeModule.FakeUpgradeService.getInstance());
        kpiService.activate();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testBuildingAKpi() {

        Kpi kpi = kpiService.newKpi().named(KPI_NAME).interval(Period.ofDays(1))
                .member().named(READ_METERS).withDynamicTarget().asMinimum().add()
                .member().named(NON_COMMUNICATING_METERS).withTargetSetAt(BigDecimal.valueOf(1, 2)).asMaximum().add()
                .create();

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
