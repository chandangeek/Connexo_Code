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
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataSendingStatusTest {
    private static final Thesaurus THESAURUS = NlsModule.FakeThesaurus.INSTANCE;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReadingTypeDataExportItem m1r1, m2r2;

    @Before
    public void setUp() {
        when(m1r1.getDomainObject().getName()).thenReturn("m1");
        when(m1r1.getReadingType().getMRID()).thenReturn("r1");
        when(m2r2.getDomainObject().getName()).thenReturn("m2");
        when(m2r2.getReadingType().getMRID()).thenReturn("r2");
    }

    @Test
    public void testSuccessBuilder() {
        DataSendingStatus status = DataSendingStatus.success();
        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailed(m1r1)).isFalse();
        assertThat(status.isFailed(m2r2)).isFalse();
        status.throwExceptionIfFailed(THESAURUS);
    }

    @Test
    public void testFailureBuilderWithAllDataSourcesImplicitly() {
        DataSendingStatus status = DataSendingStatus.failure().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(m1r1)).isTrue();
        assertThat(status.isFailed(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testFailureBuilderWithAllDataSources() {
        DataSendingStatus status = DataSendingStatus.failure().withAllDataSourcesFailed().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(m1r1)).isTrue();
        assertThat(status.isFailed(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testFailureBuilderWithDataSource1() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSource(m1r1).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(m1r1)).isTrue();
        assertThat(status.isFailed(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export the following data sources to one or more destinations: [m1:r1].");
    }

    @Test
    public void testFailureBuilderWithDataSource2() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSources(Collections.singleton(m2r2)).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(m1r1)).isFalse();
        assertThat(status.isFailed(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export the following data sources to one or more destinations: [m2:r2].");
    }

    @Test
    public void testBuilderWithAllDataSources() {
        DataSendingStatus status = DataSendingStatus.builder().withAllDataSourcesFailed().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(m1r1)).isTrue();
        assertThat(status.isFailed(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testBuilderWithDataSource1() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSources(Collections.singleton(m1r1)).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(m1r1)).isTrue();
        assertThat(status.isFailed(m2r2)).isFalse();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export the following data sources to one or more destinations: [m1:r1].");
    }

    @Test
    public void testBuilderWithDataSource2() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSource(m2r2).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(m1r1)).isFalse();
        assertThat(status.isFailed(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export the following data sources to one or more destinations: [m2:r2].");
    }

    @Test
    public void testBuilderWithDataSources() {
        DataSendingStatus status = DataSendingStatus.failure().withFailedDataSources(Arrays.asList(m1r1, m2r2)).build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(m1r1)).isTrue();
        assertThat(status.isFailed(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export the following data sources to one or more destinations: [m1:r1], [m2:r2].");
    }

    @Test
    public void testBuilderWithDataSource1AndThenAllDataSources() {
        DataSendingStatus status = DataSendingStatus.builder().withFailedDataSource(m1r1).withAllDataSourcesFailed().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(m1r1)).isTrue();
        assertThat(status.isFailed(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testBuilderWithDataSource2AndThenAllDataSources() {
        DataSendingStatus status = DataSendingStatus.builder().withFailedDataSources(Collections.singleton(m2r2)).withAllDataSourcesFailed().build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(m1r1)).isTrue();
        assertThat(status.isFailed(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testBuilderWithAllDataSourcesAndThenDataSource1And2() {
        DataSendingStatus status = DataSendingStatus.builder()
                .withAllDataSourcesFailed()
                .withFailedDataSource(m1r1)
                .withFailedDataSources(Collections.singleton(m1r1))
                .withFailedDataSources(Collections.singleton(m2r2))
                .withFailedDataSource(m2r2)
                .build();
        assertThat(status.isFailed()).isTrue();
        assertThat(status.isFailed(m1r1)).isTrue();
        assertThat(status.isFailed(m2r2)).isTrue();
        assertThatThrownBy(() -> status.throwExceptionIfFailed(THESAURUS))
                .isInstanceOf(DestinationFailedException.class)
                .hasMessage("Failed to export all data to one or more destinations.");
    }

    @Test
    public void testBuilderSuccess() {
        DataSendingStatus status = DataSendingStatus.builder().build();
        assertThat(status.isFailed()).isFalse();
        assertThat(status.isFailed(m1r1)).isFalse();
        assertThat(status.isFailed(m2r2)).isFalse();
        status.throwExceptionIfFailed(THESAURUS);
    }
}
