package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.Vault;

import java.util.TimeZone;

import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.mock;

public class TimeSeriesImplTest extends EqualsContractTest {

    private static final long ID = 15L;
    private Vault vault = mock(Vault.class);
    private RecordSpec recordSpec = mock(RecordSpec.class);

    private final TimeSeriesImpl timeSeries = initTimeSeries();

    private TimeSeriesImpl initTimeSeries() {
        TimeSeriesImpl series = new TimeSeriesImpl(vault, recordSpec, TimeZone.getTimeZone("Asia/Calcutta"));
        simulateSaved(series, ID);
        return series;
    }

    @Override
    protected Object getInstanceA() {
        return timeSeries;
    }

    @Override
    protected Object getInstanceEqualToA() {
        TimeSeriesImpl series = new TimeSeriesImpl(vault, recordSpec, TimeZone.getTimeZone("Asia/Calcutta"));
        simulateSaved(series, ID);
        return series;
    }

    @Override
    protected Object getInstanceNotEqualToA() {
        TimeSeriesImpl series = new TimeSeriesImpl(vault, recordSpec, TimeZone.getTimeZone("Asia/Calcutta"));
        simulateSaved(series, ID + 1);
        return series;
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }

    private void simulateSaved(TimeSeriesImpl impl, long id) {
        field("id").ofType(Long.TYPE).in(impl).set(id);
    }
}
