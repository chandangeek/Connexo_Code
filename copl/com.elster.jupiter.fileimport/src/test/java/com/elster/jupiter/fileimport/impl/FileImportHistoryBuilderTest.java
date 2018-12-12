/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.impl;

import com.elster.jupiter.fileimport.FileImportHistory;
import com.elster.jupiter.fileimport.FileImportHistoryBuilder;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.orm.DataModel;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Instant;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileImportHistoryBuilderTest {
    @Mock
    private DataModel dataModel;
    @Mock
    private ImportSchedule importSchedule;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;

    @Before
    public void configure() {
        when(dataModel.getInstance(FileImportHistoryImpl.class)).thenReturn(new FileImportHistoryImpl(dataModel));
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(any(), anyVararg())).thenReturn(Collections.<ConstraintViolation<Object>>emptySet());
    }

    @Test
    public void testBuilder() {
        Instant time = Instant.MAX;
        FileImportHistoryBuilder builder = new FileImportHistoryBuilderImpl(dataModel);
        builder.setImportSchedule(importSchedule);
        builder.setFileName("fileName");
        builder.setUserName("userName");
        builder.setUploadTime(time);
        FileImportHistory importHistory = builder.create();

        assertThat(importHistory.getImportSchedule()).isEqualTo(importSchedule);
        assertThat(importHistory.getFileName()).isEqualTo("fileName");
        assertThat(importHistory.getUserName()).isEqualTo("userName");
        assertThat(importHistory.getUploadTime()).isEqualTo(time);
    }
}
