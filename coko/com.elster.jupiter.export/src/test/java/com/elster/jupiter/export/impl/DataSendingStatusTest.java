/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.export.ReadingTypeDataExportItem;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataSendingStatusTest {
    private static final Thesaurus THESAURUS = NlsModule.FakeThesaurus.INSTANCE;
    @Mock
    private ReadingTypeDataExportItem m1r1, m2r2;

    @Before
    public void setUp() {
        when(m1r1.getDescription()).thenReturn("m1:r1");
        when(m2r2.getDescription()).thenReturn("m2:r2");
    }

    @Test
    public void testSuccessBuilder() {
        DataSendingStatus status = DataSendingStatus.success();
        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        status.throwExceptionIfFailed(THESAURUS);
    }

    @Test
    public void testFailureBuilderWithAllDataSourcesImplicitly() {
        DataSendingStatus status = DataSendingStatus.failure().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isTrue();
        assertThat(status.isFailedForChangedData(m1r1)).isTrue();
        assertThat(status.isFailedForNewData(m2r2)).isTrue();
        assertThat(status.isFailedForChangedData(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testFailureBuilderWithAllDataSourcesForNewData() {
        DataSendingStatus status = DataSendingStatus.failure().withAllDataSourcesFailedForNewData().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isTrue();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isTrue();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testFailureBuilderWithAllDataSourcesForChangedData() {
        DataSendingStatus status = DataSendingStatus.failure().withAllDataSourcesFailedForChangedData().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isTrue();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testFailureBuilderWithAllDataSources() {
        DataSendingStatus status = DataSendingStatus.failure().withAllDataSourcesFailed().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isTrue();
        assertThat(status.isFailedForChangedData(m1r1)).isTrue();
        assertThat(status.isFailedForNewData(m2r2)).isTrue();
        assertThat(status.isFailedForChangedData(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testFailureBuilderWithDataSource1() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSource(m1r1).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isTrue();
        assertThat(status.isFailedForChangedData(m1r1)).isTrue();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export 1 data source(s) to one or more destinations: <m1:r1>.");
    }

    @Test
    public void testFailureBuilderWithDataSource1ForNewData() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSourceForNewData(m1r1).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isTrue();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export 1 data source(s) to one or more destinations: <m1:r1>.");
    }

    @Test
    public void testFailureBuilderWithDataSource1ForChangedData() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSourceForChangedData(m1r1).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isTrue();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export 1 data source(s) to one or more destinations: <m1:r1>.");
    }

    @Test
    public void testFailureBuilderWithDataSource2() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSources(Collections.singleton(m2r2)).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isTrue();
        assertThat(status.isFailedForChangedData(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export 1 data source(s) to one or more destinations: <m2:r2>.");
    }

    @Test
    public void testFailureBuilderWithDataSource2ForNewData() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSourcesForNewData(Collections.singleton(m2r2)).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isTrue();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export 1 data source(s) to one or more destinations: <m2:r2>.");
    }

    @Test
    public void testFailureBuilderWithDataSource2ForChangedData() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSourcesForChangedData(Collections.singleton(m2r2)).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export 1 data source(s) to one or more destinations: <m2:r2>.");
    }

    @Test
    public void testBuilderWithAllDataSources() {
        DataSendingStatus status = DataSendingStatus.builder().withAllDataSourcesFailed().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isTrue();
        assertThat(status.isFailedForChangedData(m1r1)).isTrue();
        assertThat(status.isFailedForNewData(m2r2)).isTrue();
        assertThat(status.isFailedForChangedData(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testBuilderWithAllDataSourcesForNewData() {
        DataSendingStatus status = DataSendingStatus.builder().withAllDataSourcesFailedForNewData().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isTrue();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isTrue();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testBuilderWithAllDataSourcesForChangedData() {
        DataSendingStatus status = DataSendingStatus.builder().withAllDataSourcesFailedForChangedData().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isTrue();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testBuilderWithDataSource1() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSources(Collections.singleton(m1r1)).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isTrue();
        assertThat(status.isFailedForChangedData(m1r1)).isTrue();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export 1 data source(s) to one or more destinations: <m1:r1>.");
    }

    @Test
    public void testBuilderWithDataSource1ForNewData() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSourcesForNewData(Collections.singleton(m1r1)).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isTrue();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export 1 data source(s) to one or more destinations: <m1:r1>.");
    }

    @Test
    public void testBuilderWithDataSource1ForChangedData() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSourcesForChangedData(Collections.singleton(m1r1)).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isTrue();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export 1 data source(s) to one or more destinations: <m1:r1>.");
    }

    @Test
    public void testBuilderWithDataSource2() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSource(m2r2).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isTrue();
        assertThat(status.isFailedForChangedData(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export 1 data source(s) to one or more destinations: <m2:r2>.");
    }

    @Test
    public void testBuilderWithDataSource2ForNewData() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSourceForNewData(m2r2).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isTrue();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export 1 data source(s) to one or more destinations: <m2:r2>.");
    }

    @Test
    public void testBuilderWithDataSource2ForChangedData() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSourceForChangedData(m2r2).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export 1 data source(s) to one or more destinations: <m2:r2>.");
    }

    @Test
    public void testBuilderWithDataSourcesForNewData() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSourcesForNewData(Arrays.asList(m1r1, m2r2)).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isTrue();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isTrue();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export 2 data source(s) to one or more destinations: <m1:r1>, <m2:r2>.");
    }

    @Test
    public void testBuilderWithDataSourcesForChangedData() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSourcesForChangedData(Arrays.asList(m1r1, m2r2)).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isTrue();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export 2 data source(s) to one or more destinations: <m1:r1>, <m2:r2>.");
    }

    @Test
    public void testBuilderWithDataSource1AndThenAllDataSources() {
        DataSendingStatus status = DataSendingStatus.builder().withFailedDataSourceForNewData(m1r1).withAllDataSourcesFailedForNewData().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isTrue();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isTrue();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testBuilderWithDataSource1AndThenAllDataSourcesForDifferentData() {
        DataSendingStatus status = DataSendingStatus.builder().withFailedDataSourceForNewData(m1r1).withAllDataSourcesFailedForChangedData().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isTrue();
        assertThat(status.isFailedForChangedData(m1r1)).isTrue();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testBuilderWithDataSource2AndThenAllDataSources() {
        DataSendingStatus status = DataSendingStatus.builder().withFailedDataSourcesForChangedData(Collections.singleton(m2r2)).withAllDataSourcesFailedForChangedData().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isTrue();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testBuilderWithDataSource2AndThenAllDataSourcesForDifferentData() {
        DataSendingStatus status = DataSendingStatus.builder().withFailedDataSourcesForChangedData(Collections.singleton(m2r2)).withAllDataSourcesFailedForNewData().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isTrue();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isTrue();
        assertThat(status.isFailedForChangedData(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testBuilderWithAllDataSourcesAndThenDataSource1And2ForNewData() {
        DataSendingStatus status = DataSendingStatus.builder()
                .withAllDataSourcesFailedForNewData()
                .withFailedDataSourceForNewData(m1r1)
                .withFailedDataSourcesForNewData(Collections.singleton(m1r1))
                .withFailedDataSourcesForNewData(Collections.singleton(m2r2))
                .withFailedDataSourceForNewData(m2r2)
                .build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isTrue();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isTrue();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testBuilderWithAllDataSourcesAndThenDataSource1And2ForChangedData() {
        DataSendingStatus status = DataSendingStatus.builder()
                .withAllDataSourcesFailedForChangedData()
                .withFailedDataSourceForChangedData(m1r1)
                .withFailedDataSourcesForChangedData(Collections.singleton(m1r1))
                .withFailedDataSourcesForChangedData(Collections.singleton(m2r2))
                .withFailedDataSourceForChangedData(m2r2)
                .build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isTrue();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testBuilderSuccess() {
        DataSendingStatus status = DataSendingStatus.builder().build();
        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailedForNewData(m1r1)).isFalse();
        assertThat(status.isFailedForChangedData(m1r1)).isFalse();
        assertThat(status.isFailedForNewData(m2r2)).isFalse();
        assertThat(status.isFailedForChangedData(m2r2)).isFalse();
        status.throwExceptionIfFailed(THESAURUS);
    }
}
