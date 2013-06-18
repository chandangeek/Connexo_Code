package com.elster.jupiter.util.time;

import com.elster.jupiter.tasks.impl.test.util.EqualsContractTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 */
public class UtcInstantTest extends EqualsContractTest {
    private static final long MILLIS = 60464650116354L;
    private UtcInstant utcInstant;
    private UtcInstant first;
    private UtcInstant last;
    private Date firstDate;
    private Date lastDate;

    @Before
    public void setUp() {
        first = new UtcInstant(MILLIS);
        last = new UtcInstant(MILLIS + 1);
        firstDate = new Date(MILLIS);
        lastDate = new Date(MILLIS + 1);
    }

    @Test
    public void testAfterOrEqualTrue() throws Exception {
        assertThat(last.afterOrEqual(first)).isTrue();
    }

    @Test
    public void testAfterOrEqualFalse() throws Exception {
        assertThat(first.afterOrEqual(last)).isFalse();
    }

    @Test
    public void testAfterOrEqualOnSelf() throws Exception {
        assertThat(first.afterOrEqual(first)).isTrue();
    }

    @Test
    public void testAfterTrue() throws Exception {
        assertThat(last.after(first)).isTrue();
    }

    @Test
    public void testAfterFalse() throws Exception {
        assertThat(first.after(last)).isFalse();
    }

    @Test
    public void testAfterOnSelf() throws Exception {
        assertThat(first.after(first)).isFalse();
    }

    @Test
    public void testBeforeTrue() throws Exception {
        assertThat(last.before(first)).isFalse();
    }

    @Test
    public void testBeforeFalse() throws Exception {
        assertThat(first.before(last)).isTrue();
    }

    @Test
    public void testBeforeOnSelf() throws Exception {
        assertThat(first.before(first)).isFalse();
    }

    @Test
    public void testBeforeOrEqualTrue() throws Exception {
        assertThat(first.beforeOrEqual(last)).isTrue();
    }

    @Test
    public void testBeforeOrEqualFalse() throws Exception {
        assertThat(last.beforeOrEqual(first)).isFalse();
    }

    @Test
    public void testBeforeOrEqualOnSelf() throws Exception {
        assertThat(first.beforeOrEqual(first)).isTrue();
    }

    @Test
    public void testCompareToStrictlyGreater() throws Exception {
        assertThat(last.compareTo(first)).isGreaterThan(0);
    }

    @Test
    public void testCompareToStrictlySmaller() throws Exception {
        assertThat(first.compareTo(last)).isLessThan(0);
    }

    @Test
    public void testCompareToSame() throws Exception {
        assertThat(first.compareTo(first)).isEqualTo(0);
    }

    @Test
    public void testCompareToEqualButNotSame() throws Exception {
        assertThat(getInstanceA().compareTo(getInstanceEqualToA())).isEqualTo(0);
    }

    @Test
    public void testGetTime() throws Exception {
        assertThat(new UtcInstant(MILLIS).getTime()).isEqualTo(MILLIS);
    }

    @Test
    public void testToDate() throws Exception {
        assertThat(new UtcInstant(MILLIS).toDate()).isEqualTo(new Date(MILLIS));
    }

    @Test
    public void testAfterDateTrue() throws Exception {
        assertThat(last.after(firstDate)).isTrue();
    }

    @Test
    public void testAfterDateFalse() throws Exception {
        assertThat(first.after(lastDate)).isFalse();
    }

    @Test
    public void testAfterOnSameDate() throws Exception {
        assertThat(first.after(firstDate)).isFalse();
    }

    @Test
    public void testBeforeDateTrue() throws Exception {
        assertThat(last.before(firstDate)).isFalse();
    }

    @Test
    public void testBeforeDateFalse() throws Exception {
        assertThat(first.before(lastDate)).isTrue();
    }

    @Test
    public void testBeforeOnSameDate() throws Exception {
        assertThat(first.before(firstDate)).isFalse();
    }

    @Test
    public void testAfterOrEqualDateTrue() throws Exception {
        assertThat(last.afterOrEqual(firstDate)).isTrue();
    }

    @Test
    public void testAfterOrEqualDateFalse() throws Exception {
        assertThat(first.afterOrEqual(lastDate)).isFalse();
    }

    @Test
    public void testAfterOrEqualOnSameDate() throws Exception {
        assertThat(first.afterOrEqual(firstDate)).isTrue();
    }

    @Test
    public void testBeforeOrEqualDateTrue() throws Exception {
        assertThat(first.beforeOrEqual(lastDate)).isTrue();
    }

    @Test
    public void testBeforeOrEqualDateFalse() throws Exception {
        assertThat(last.beforeOrEqual(firstDate)).isFalse();
    }

    @Test
    public void testBeforeOrEqualOnSameDate() throws Exception {
        assertThat(first.beforeOrEqual(firstDate)).isTrue();
    }


    @Test
    public void testToString() throws Exception {
        TimeZone toRestore = TimeZone.getDefault();
        try {
            System.out.println(Arrays.toString(TimeZone.getAvailableIDs()));
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Calcutta"));
            assertThat(new UtcInstant(1369926198384L).toString()).isEqualTo("2013-05-30T20:33:18.384+0530");
        } finally {
            TimeZone.setDefault(toRestore);
        }
    }








    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected UtcInstant getInstanceA() {
        if (utcInstant == null) {
            utcInstant = new UtcInstant(MILLIS);
        }
        return utcInstant;
    }

    @Override
    protected UtcInstant getInstanceEqualToA() {
        return new UtcInstant(60464650116354L);
    }

    @Override
    protected UtcInstant getInstanceNotEqualToA() {
        return new UtcInstant(60464650116353L);
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
