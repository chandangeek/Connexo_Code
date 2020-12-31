/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.security.thread.ThreadPrincipalService;

import com.google.common.collect.ImmutableList;

import javax.inject.Provider;
import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VaultImplTest extends EqualsContractTest {

    private static long ID = 15L;
    private static int SLOT_COUNT = 54;
    private static String COMPONENT_NAME = "CMP";
    private static String DESCRIPTION = "description";

    private DataModel dataModel;
    @Mock
    private Clock clock;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private Provider<TimeSeriesImpl> provider;
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    private Object a;

    @Override
    protected Object getInstanceA() {
        if (a == null) {
            dataModel = mock(DataModel.class);
            when(dataModel.getSqlDialect()).thenReturn(SqlDialect.H2);
            a = new VaultImpl(dataModel, clock, threadPrincipalService, provider, thesaurus).init(COMPONENT_NAME, ID, DESCRIPTION, SLOT_COUNT, 0, true);
        }
        return a;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new VaultImpl(dataModel, clock, threadPrincipalService, provider, thesaurus).init(COMPONENT_NAME, ID, DESCRIPTION, SLOT_COUNT, 0, true);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(new VaultImpl(dataModel, clock, threadPrincipalService, provider, thesaurus).init(COMPONENT_NAME, ID + 1, DESCRIPTION, SLOT_COUNT, 0, true));
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    @Test
    public void testValidationFailVaultInactive() {
        VaultImpl vault = new VaultImpl(dataModel, clock, threadPrincipalService, provider, thesaurus).init(COMPONENT_NAME, ID, DESCRIPTION, SLOT_COUNT, 0, true);
        Instant instant = ZonedDateTime.of(2012, 10, 10, 3, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        assertThatThrownBy(() -> vault.validateInstant(instant))
                .isInstanceOf(MeasurementTimeIsNotValidException.class)
                .hasMessage("Failed to save measurement: the 'description' vault is inactive. Can be changed in table IDS_VAULT.");
    }

    @Test
    public void testValidationFailIntervalTimestampNotValid() throws NoSuchFieldException, IllegalAccessException {
        VaultImpl vault = new VaultImpl(dataModel, clock, threadPrincipalService, provider, thesaurus).init(COMPONENT_NAME, ID, DESCRIPTION, SLOT_COUNT, 0, true);
        //below used reflection because impossible use spy methods for final class
        Field field = vault.getClass().getDeclaredField("active");
        field.setAccessible(true);
        field.set(vault, true);
        Instant to = ZonedDateTime.of(2012, 11, 10, 3, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        Field maxTime = vault.getClass().getDeclaredField("maxTime");
        maxTime.setAccessible(true);
        maxTime.set(vault, to);
        Instant instant = ZonedDateTime.of(2012, 11, 11, 3, 0, 0, 0, ZoneId.of("UTC")).toInstant();
        String MESSAGE = "Failed to save measurement: measurement time 2012-11-11T03:00:00Z is outside the 'description' vault range [1970-01-01T00:00:00Z..2012-11-10T03:00:00Z]. The range is defined by MINTIME and MAXTIME columns in IDS_VAULT table. These attributes are updated after successful data purge.";
        assertThatThrownBy(() -> vault.validateInstant(instant))
                .isInstanceOf(MeasurementTimeIsNotValidException.class)
                .hasMessage(MESSAGE);
    }
}
