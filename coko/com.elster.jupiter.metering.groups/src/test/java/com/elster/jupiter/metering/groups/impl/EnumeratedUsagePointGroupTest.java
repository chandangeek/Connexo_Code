package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    private DataMapper<EnumeratedUsagePointGroup.Entry> entryFactory;
    @Mock
    private DataModel dataModel;
    @Mock
    private DataMapper<EnumeratedUsagePointGroup> groupFactory;
    @Mock
    private MeteringService meteringService;

    @Before
    public void setUp() {
        when(dataModel.mapper(EnumeratedUsagePointGroup.Entry.class)).thenReturn(entryFactory);
        when(dataModel.getInstance(EnumeratedUsagePointGroupImpl.EntryImpl.class)).thenAnswer(invocationOnMock -> new EnumeratedUsagePointGroupImpl.EntryImpl(dataModel, meteringService));
        when(dataModel.mapper(EnumeratedUsagePointGroup.class)).thenReturn(groupFactory);

        usagePointGroup = new EnumeratedUsagePointGroupImpl(dataModel);
    }

    @After
    public void tearDown() {
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
        usagePointGroup.add(usagePoint1, Range.closedOpen(START,END));
        usagePointGroup.add(usagePoint2, Range.closedOpen(START,END));
        usagePointGroup.add(usagePoint3, Range.lessThan(BEFORE));

        assertThat(usagePointGroup.getMembers(MIDDLE)).doesNotContain(usagePoint3)
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
        EnumeratedUsagePointGroup.Entry entry1 = usagePointGroup.add(usagePoint1, Range.closedOpen(MIDDLE, AFTER));

        EnumeratedUsagePointGroup.Entry entry2 = usagePointGroup.add(usagePoint1, Range.lessThan(END));

        assertThat(entry2.getRange()).isEqualTo(Range.lessThan(AFTER));
    }

    @Test
    public void testAddWithPositiveInfinity() {
        EnumeratedUsagePointGroup.Entry entry1 = usagePointGroup.add(usagePoint1, Range.closedOpen(MIDDLE, AFTER));

        EnumeratedUsagePointGroup.Entry entry2 = usagePointGroup.add(usagePoint1, Range.atLeast(START));

        assertThat(entry2.getRange()).isEqualTo(Range.atLeast(START));
    }

    @Test
    public void testIsMember() {
        EnumeratedUsagePointGroup.Entry entry = usagePointGroup.add(usagePoint1, Range.closedOpen(BEFORE, AFTER));

        assertThat(usagePointGroup.isMember(usagePoint1, MIDDLE)).isTrue();

    }

    @Test
    public void testRemove() {
        EnumeratedUsagePointGroup.Entry entry = usagePointGroup.add(usagePoint1, Range.closedOpen(BEFORE, AFTER));

        usagePointGroup.remove(entry);

        assertThat(usagePointGroup.isMember(usagePoint1, MIDDLE)).isFalse();

    }

    @Test
    public void testSaveNew() {
        usagePointGroup.save();

        verify(dataModel.mapper(EnumeratedUsagePointGroup.class)).persist(usagePointGroup);
    }

    @Test
    public void testSaveNewWithEntries() {
        usagePointGroup.add(usagePoint1, Range.atLeast(START));

        usagePointGroup.save();

        verify(dataModel.mapper(EnumeratedUsagePointGroup.class)).persist(usagePointGroup);
        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(entryFactory).persist(listCaptor.capture());

        List<?> list = listCaptor.getValue();
        assertThat(list).hasSize(1);
        EnumeratedUsagePointGroup.Entry entry = (EnumeratedUsagePointGroup.Entry) list.get(0);
        assertThat(entry.getUsagePoint()).isEqualTo(usagePoint1);
        assertThat(entry.getRange()).isEqualTo(Range.atLeast(START));
    }

    @Test
    public void testSaveUpdate() {
        simulateSaved();

        usagePointGroup.save();

        verify(dataModel.mapper(EnumeratedUsagePointGroup.class)).update(usagePointGroup);
    }

    @Test
    public void testSaveUpdateWithEntries() {
        simulateSaved();

        EnumeratedUsagePointGroup.Entry entry1 = EnumeratedUsagePointGroupImpl.EntryImpl.from(dataModel, usagePointGroup, usagePoint1, Range.atLeast(START));
        EnumeratedUsagePointGroup.Entry entry2 = EnumeratedUsagePointGroupImpl.EntryImpl.from(dataModel, usagePointGroup, usagePoint2, Range.atLeast(START));

        when(entryFactory.find("usagePointGroup", usagePointGroup)).thenReturn(Arrays.asList(entry1, entry2));

        usagePointGroup.endMembership(usagePoint1, END);
        usagePointGroup.add(usagePoint3, Range.atLeast(END));

        usagePointGroup.save();

        verify(entryFactory).update(Arrays.asList(entry1, entry2));
        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(entryFactory).persist(listCaptor.capture());
        assertThat(listCaptor.getValue()).hasSize(1);
    }

    private void simulateSaved() {
        field("id").ofType(Long.TYPE).in(usagePointGroup).set(ID);
    }

}