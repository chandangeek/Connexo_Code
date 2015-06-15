package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.export.StructureMarker;
import com.google.common.collect.Range;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;

public class DefaultStructureMarkerTest extends EqualsContractTest {

    public static final ZonedDateTime NOW = ZonedDateTime.of(2014, 11, 5, 15, 23, 49, 987654321, TimeZoneNeutral.getMcMurdo());
    private Clock clock = Clock.fixed(NOW.toInstant(), TimeZoneNeutral.getMcMurdo());

    private DefaultStructureMarker instanceA;

    @Test
    public void testConstructionOfRoot() {
        DefaultStructureMarker root = DefaultStructureMarker.createRoot(clock, "root");

        assertThat(root.getStructurePath()).isEqualTo(Arrays.asList("root"));
    }

    @Test
    public void testConstructionOfChild() {
        DefaultStructureMarker child = DefaultStructureMarker.createRoot(clock, "root").child("level1");

        assertThat(child.getStructurePath()).isEqualTo(Arrays.asList("root", "level1"));
    }

    @Test
    public void testReplaceTags() {
        DefaultStructureMarker child = DefaultStructureMarker.createRoot(clock, "root").child("level1");

        assertThat(child.replaceTags("test<identifier>test")).isEqualTo("testroottest");
    }

    @Test
    public void testDiffersAt0() {
        DefaultStructureMarker child = DefaultStructureMarker.createRoot(clock, "root").child("level1");

        assertThat(child.differsAt(DefaultStructureMarker.createRoot(clock, "otherroot").child("level1"))).isEqualTo(0);
    }

    @Test
    public void testDiffersAt1() {
        DefaultStructureMarker child = DefaultStructureMarker.createRoot(clock, "root").child("level1");

        assertThat(child.differsAt(DefaultStructureMarker.createRoot(clock, "root").child("level2"))).isEqualTo(1);
    }

    @Test
    public void testDiffersAtForEqualInstances() {
        DefaultStructureMarker child = DefaultStructureMarker.createRoot(clock, "root").child("level1");

        assertThat(child.differsAt(DefaultStructureMarker.createRoot(clock, "root").child("level1"))).isEqualTo(2);
    }

    @Test
    public void testGetParent() {
        DefaultStructureMarker parent = DefaultStructureMarker.createRoot(clock, "root");
        DefaultStructureMarker child = parent.child("level1");

        assertThat(child.getParent()).contains(parent);
        assertThat(child.getParent().get()).isSameAs(parent);
    }

    @Test
    public void testGetPeriod() {
        Range<Instant> period = Range.atLeast(Instant.from(NOW));

        DefaultStructureMarker root = DefaultStructureMarker.createRoot(clock, "root");
        DefaultStructureMarker childBeforePeriod = root.child("level1");
        StructureMarker child = childBeforePeriod.withPeriod(period);

        assertThat(child).isEqualTo(childBeforePeriod);
        assertThat(child.getPeriod()).contains(period);
        assertThat(childBeforePeriod.getPeriod()).isEmpty();
        assertThat(child.getParent()).contains(root);
        assertThat(child.getParent().get()).isSameAs(root);
    }

    @Test
    public void testAdopt() {
        StructureMarker adopter = DefaultStructureMarker.createRoot(clock, "root").child("level1");
        StructureMarker adoptee = DefaultStructureMarker.createRoot(clock, "level2").child("level3");
        StructureMarker adoption = adopter.adopt(adoptee);

        assertThat(adoption.getStructurePath()).isEqualTo(Arrays.asList("root", "level1", "level2", "level3"));
        assertThat(adoption.getParent()).isPresent();
        assertThat(adoption.getParent().get().getParent()).isPresent();
        assertThat(adoption.getParent().get().getParent().get()).isSameAs(adopter);
    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = DefaultStructureMarker.createRoot(clock, "root").child("level1").child("level2");
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return DefaultStructureMarker.createRoot(clock, "root").child("level1").child("level2");
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                DefaultStructureMarker.createRoot(clock, "otherroot").child("level1").child("level2"),
                DefaultStructureMarker.createRoot(clock, "root").child("otherlevel1").child("level2"),
                DefaultStructureMarker.createRoot(clock, "root").child("level1").child("otherlevel2")
        );
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}