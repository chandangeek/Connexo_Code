package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.time.Interval;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EnumeratedUsagePointGroupTest {

    private static final Date BEFORE = new DateTime(2012, 10, 19, 16, 5, 0).toDate();
    private static final Date START = new DateTime(2012, 10, 22, 16, 5, 0).toDate();
    private static final Date MIDDLE = new DateTime(2012, 10, 25, 16, 5, 0).toDate();
    private static final Date END = new DateTime(2012, 10, 28, 16, 5, 0).toDate();
    private static final Date AFTER = new DateTime(2012, 10, 29, 16, 5, 0).toDate();
    private EnumeratedUsagePointGroupImpl usagePointGroup;

    @Mock
    private UsagePoint usagePoint1, usagePoint2, usagePoint3;

    @Before
    public void setUp() {
        usagePointGroup = new EnumeratedUsagePointGroupImpl();
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
        usagePointGroup.add(usagePoint1, Interval.startAt(START).withEnd(END));
        usagePointGroup.add(usagePoint2, Interval.startAt(START).withEnd(END));
        usagePointGroup.add(usagePoint3, new Interval(null, BEFORE));

        assertThat(usagePointGroup.getMembers(MIDDLE)).doesNotContain(usagePoint3)
                .contains(usagePoint1, usagePoint2)
                .hasSize(2);
    }

    @Test
    public void testAddMerges() {
        EnumeratedUsagePointGroup.Entry entry = usagePointGroup.add(usagePoint1, Interval.startAt(MIDDLE).withEnd(AFTER));

        assertThat(entry.getInterval()).isEqualTo(Interval.startAt(MIDDLE).withEnd(AFTER));

        entry = usagePointGroup.add(usagePoint1, Interval.startAt(START).withEnd(END));

        assertThat(entry.getInterval()).isEqualTo(Interval.startAt(START).withEnd(AFTER));
    }

    @Test
    public void testAddWithNegativeInfinity() {
        EnumeratedUsagePointGroup.Entry entry = usagePointGroup.add(usagePoint1, Interval.startAt(MIDDLE).withEnd(AFTER));

        entry = usagePointGroup.add(usagePoint1, Interval.startAt(null).withEnd(END));

        assertThat(entry.getInterval()).isEqualTo(Interval.startAt(null).withEnd(AFTER));
    }

    @Test
    public void testAddWithPositiveInfinity() {
        EnumeratedUsagePointGroup.Entry entry = usagePointGroup.add(usagePoint1, Interval.startAt(MIDDLE).withEnd(AFTER));

        entry = usagePointGroup.add(usagePoint1, Interval.startAt(START).withEnd(null));

        assertThat(entry.getInterval()).isEqualTo(Interval.startAt(START).withEnd(null));
    }

    @Test
    public void testIsMember() {
        EnumeratedUsagePointGroup.Entry entry = usagePointGroup.add(usagePoint1, Interval.startAt(BEFORE).withEnd(AFTER));

        assertThat(usagePointGroup.isMember(usagePoint1, MIDDLE)).isTrue();

    }

    @Test
    public void testRemove() {
        EnumeratedUsagePointGroup.Entry entry = usagePointGroup.add(usagePoint1, Interval.startAt(BEFORE).withEnd(AFTER));

        usagePointGroup.remove(entry);

        assertThat(usagePointGroup.isMember(usagePoint1, MIDDLE)).isFalse();

    }

}
