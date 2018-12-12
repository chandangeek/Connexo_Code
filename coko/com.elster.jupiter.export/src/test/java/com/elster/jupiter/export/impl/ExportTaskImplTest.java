/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.TaskService;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class ExportTaskImplTest extends EqualsContractTest {

    public static final long INSTANCE_A_ID = 45L;
    private ExportTaskImpl instanceA;

    @Mock
    private DataModel dataModel;
    @Mock
    private IDataExportService dataExportService;
    @Mock
    private TaskService taskService;
    @Mock
    private Thesaurus thesaurus;

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = new ExportTaskImpl(dataModel, dataExportService, taskService, thesaurus);
            field("id").ofType(Long.TYPE).in(instanceA).set(INSTANCE_A_ID);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        ExportTaskImpl other = new ExportTaskImpl(dataModel, dataExportService, taskService, thesaurus);
        field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID);
        return other;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        ExportTaskImpl other = new ExportTaskImpl(dataModel, dataExportService, taskService, thesaurus);
        field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID + 1);
        return Collections.singletonList(other);
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