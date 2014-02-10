package com.elster.jupiter.metering.readings;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import org.junit.Test;

import java.util.Arrays;

import static com.elster.jupiter.metering.readings.ProfileStatus.Flag.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ProfileStatusTest extends EqualsContractTest {

    private ProfileStatus instanceA;

    @Test
    public void testConstruction() {
        ProfileStatus status = ProfileStatus.of(BATTERY_LOW, OVERFLOW);
        assertThat(status.getFlags()).containsOnly(BATTERY_LOW, OVERFLOW);
    }

    @Test
    public void testConstructionOfBits() {

        long bits = (1L << BATTERY_LOW.ordinal()) | (1L << OVERFLOW.ordinal());

        ProfileStatus status = new ProfileStatus(bits);
        assertThat(status.getFlags()).containsOnly(BATTERY_LOW, OVERFLOW);
    }

    @Test
    public void testShortNotSetWhenLongSet() {
        long bits = (1L << LONG.ordinal()) | (1L << SHORTLONG.ordinal());

        ProfileStatus status = new ProfileStatus(bits);
        assertThat(status.getFlags()).containsOnly(LONG, SHORTLONG);
        assertThat(status.get(SHORT)).isFalse();

    }


    @Test(expected = IllegalArgumentException.class)
    public void testInvalidConflict() {
        ProfileStatus status = ProfileStatus.of(ProfileStatus.Flag.LONG, ProfileStatus.Flag.SHORT);

    }

    @Test
    public void testJustSettingLongAlsoActivatesShortLong() {
        ProfileStatus status = ProfileStatus.of(ProfileStatus.Flag.LONG);
        assertThat(status.getFlags()).containsOnly(LONG, SHORTLONG);
    }

    @Test
    public void testJustSettingShortAlsoActivatesShortLong() {
        ProfileStatus status = ProfileStatus.of(SHORT);
        assertThat(status.getFlags()).containsOnly(SHORT, SHORTLONG);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJustSettingShortBitIsInvalid() {
        ProfileStatus status = new ProfileStatus(1L << SHORT.ordinal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJustSettingLongBitIsInvalid() {
        ProfileStatus status = new ProfileStatus(1L << LONG.ordinal());
    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = ProfileStatus.of(BATTERY_LOW, OVERFLOW);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return ProfileStatus.of(BATTERY_LOW, OVERFLOW);
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return Arrays.asList(
                ProfileStatus.of(),
                ProfileStatus.of(BATTERY_LOW),
                ProfileStatus.of(BATTERY_LOW, REVERSERUN),
                ProfileStatus.of(BATTERY_LOW, OVERFLOW, REVERSERUN)
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
