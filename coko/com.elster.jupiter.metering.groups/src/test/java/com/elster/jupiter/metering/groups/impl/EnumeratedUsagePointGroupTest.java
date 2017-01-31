/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.impl.ExecutionTimerServiceImpl;

import com.google.common.collect.Range;
import org.osgi.framework.BundleContext;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.google.common.collect.Range.atLeast;
import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnumeratedUsagePointGroupTest {

    private static final Instant BEFORE = ZonedDateTime.of(2012, 10, 19, 16, 5, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant START = ZonedDateTime.of(2012, 10, 22, 16, 5, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant MIDDLE = ZonedDateTime.of(2012, 10, 25, 16, 5, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant END = ZonedDateTime.of(2012, 10, 28, 16, 5, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant AFTER = ZonedDateTime.of(2012, 10, 29, 16, 5, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final long ID = 2001L;
    private EnumeratedUsagePointGroupImpl usagePointGroup;

    @Mock
    private UsagePoint usagePoint1, usagePoint2, usagePoint3;
    @Mock
    private DataMapper<EnumeratedUsagePointGroupImpl.UsagePointEntryImpl> entryFactory;
    @Mock
    private DataModel dataModel;
    @Mock
    private DataMapper<EnumeratedUsagePointGroup> groupFactory;
    @Mock
    private MeteringService meteringService;
    @Mock
    private EventService eventService;
    @Mock
    private QueryService queryService;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;
    @Captor
    private ArgumentCaptor<List<EnumeratedUsagePointGroupImpl.UsagePointEntryImpl>> listCaptor;

    @Before
    public void setUp() {
        when(dataModel.mapper(EnumeratedUsagePointGroupImpl.UsagePointEntryImpl.class)).thenReturn(entryFactory);
        when(dataModel.getInstance(EnumeratedUsagePointGroupImpl.UsagePointEntryImpl.class)).thenAnswer(invocationOnMock -> new EnumeratedUsagePointGroupImpl.UsagePointEntryImpl(dataModel));
        when(dataModel.mapper(EnumeratedUsagePointGroup.class)).thenReturn(groupFactory);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
//        when(validator.validate(anyObject(), anyVararg())).thenReturn(Collections.emptySet());

        usagePointGroup = new EnumeratedUsagePointGroupImpl(dataModel, eventService, queryService, meteringService,
                new ExecutionTimerServiceImpl(bundleContext).newTimer("Timer", ChronoUnit.MINUTES.getDuration()));
    }

    @Test
    public void testImplementsInterface() {
        assertThat(usagePointGroup).isInstanceOf(EnumeratedUsagePointGroup.class);
    }

    @Test
    public void testGetName() {
        String name = "name";
        usagePointGroup.setName(name);

        assertThat(usagePointGroup.getName()).isEqualTo(name);
    }

    @Test
    public void testGetMRID() {
        String mrid = "MRID";
        usagePointGroup.setMRID(mrid);

        assertThat(usagePointGroup.getMRID()).isEqualTo(mrid);
    }

    @Test
    public void testGetDescription() {
        String description = "description";
        usagePointGroup.setDescription(description);

        assertThat(usagePointGroup.getDescription()).isEqualTo(description);
    }

    @Test
    public void testGetAliasName() {
        String alias = "alias";
        usagePointGroup.setAliasName(alias);

        assertThat(usagePointGroup.getAliasName()).isEqualTo(alias);
    }

    @Test
    public void testGetType() {
        String type = "type";
        usagePointGroup.setType(type);

        assertThat(usagePointGroup.getType()).isEqualTo(type);
    }

    @Test
    public void testGetMembers() {
        when(usagePoint1.getName()).thenReturn("usagePoint1");
        when(usagePoint2.getName()).thenReturn("usagePoint2");
        usagePointGroup.add(usagePoint1, Range.closedOpen(START,END));
        usagePointGroup.add(usagePoint2, Range.closedOpen(START,END));
        usagePointGroup.add(usagePoint3, Range.lessThan(BEFORE));

        assertThat(usagePointGroup.getMembers(MIDDLE))
                .doesNotContain(usagePoint3)
                .contains(usagePoint1, usagePoint2)
                .hasSize(2);
    }

    @Test
    public void testAddMerges() {
        EnumeratedUsagePointGroup.Entry entry1 = usagePointGroup.add(usagePoint1, Range.closedOpen(MIDDLE,AFTER));

        assertThat(entry1.getRange()).isEqualTo(Range.closedOpen(MIDDLE,AFTER));

        EnumeratedUsagePointGroup.Entry entry2 = usagePointGroup.add(usagePoint1, Range.closedOpen(START, END));

        assertThat(entry2.getRange()).isEqualTo(Range.closedOpen(START,AFTER));
    }

    @Test
    public void testAddWithNegativeInfinity() {
        usagePointGroup.add(usagePoint1, Range.closedOpen(MIDDLE, AFTER));

        EnumeratedUsagePointGroup.Entry entry2 = usagePointGroup.add(usagePoint1, Range.lessThan(END));

        assertThat(entry2.getRange()).isEqualTo(Range.lessThan(AFTER));
    }

    @Test
    public void testAddWithPositiveInfinity() {
        usagePointGroup.add(usagePoint1, Range.closedOpen(MIDDLE, AFTER));

        EnumeratedUsagePointGroup.Entry entry2 = usagePointGroup.add(usagePoint1, atLeast(START));

        assertThat(entry2.getRange()).isEqualTo(atLeast(START));
    }

    @Test
    public void testIsMember() {
        usagePointGroup.add(usagePoint1, Range.closedOpen(BEFORE, AFTER));

        assertThat(usagePointGroup.isMember(usagePoint1, MIDDLE)).isTrue();
    }

    @Test
    public void testRemove() {
        EnumeratedUsagePointGroup.Entry<UsagePoint> entry = usagePointGroup.add(usagePoint1, Range.closedOpen(BEFORE, AFTER));

        usagePointGroup.remove(entry);

        assertThat(usagePointGroup.isMember(usagePoint1, MIDDLE)).isFalse();
    }

    @Test
    public void testSaveNew() {
        usagePointGroup.save();

        verify(dataModel).persist(usagePointGroup);
    }

    @Test
    public void testSaveNewWithEntries() {
        usagePointGroup.add(usagePoint1, atLeast(START));

        usagePointGroup.save();

        verify(dataModel).persist(usagePointGroup);
        verify(entryFactory).persist(listCaptor.capture());

        List<EnumeratedUsagePointGroupImpl.UsagePointEntryImpl> list = listCaptor.getValue();
        assertThat(list).hasSize(1);
        EnumeratedUsagePointGroupImpl.UsagePointEntryImpl entry = list.get(0);
        assertThat(entry.getMember()).isEqualTo(usagePoint1);
        assertThat(entry.getRange()).isEqualTo(atLeast(START));
    }

    @Test
    public void testSaveUpdate() {
        simulateSaved();

        usagePointGroup.update();

        verify(dataModel).update(usagePointGroup);
    }

    @Test
    public void testSaveUpdateWithEntries() {
        simulateSaved();

        EnumeratedUsagePointGroupImpl.UsagePointEntryImpl entry1 = new EnumeratedUsagePointGroupImpl.UsagePointEntryImpl(dataModel);
        entry1.init(usagePointGroup, usagePoint1, Range.atLeast(START));
        EnumeratedUsagePointGroupImpl.UsagePointEntryImpl entry2 = new EnumeratedUsagePointGroupImpl.UsagePointEntryImpl(dataModel);
        entry2.init(usagePointGroup, usagePoint2, Range.atLeast(START));

        when(entryFactory.find("group", usagePointGroup)).thenReturn(Arrays.asList(entry1, entry2));

        usagePointGroup.endMembership(usagePoint1, END);
        usagePointGroup.add(usagePoint3, Range.atLeast(END));

        usagePointGroup.update();

        verify(entryFactory).update(Arrays.asList(entry1, entry2));
        verify(entryFactory).persist(listCaptor.capture());
        assertThat(listCaptor.getValue()).hasSize(1);
    }

    private void simulateSaved() {
        field("id").ofType(Long.TYPE).in(usagePointGroup).set(ID);
    }
}
