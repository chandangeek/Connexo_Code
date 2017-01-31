/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.fsm.ProcessReference;
import com.google.common.collect.ImmutableSet;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.reflect.core.Reflection.field;

@RunWith(MockitoJUnitRunner.class)
public class ProcessReferenceEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    private ProcessReferenceImpl processReference;

    private void setId(ProcessReference processReference, Long id) {
        field("id").ofType(Long.TYPE).in(processReference).set(id);
    }

    @Override
    protected Object getInstanceA() {
        if (processReference == null) {
            processReference = new ProcessReferenceImpl();
            setId(processReference, ID);
        }
        return processReference;
    }

    @Override
    protected Object getInstanceEqualToA() {
        ProcessReference processReference = new ProcessReferenceImpl();
        setId(processReference, ID);
        return processReference;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        ProcessReference processReference = new ProcessReferenceImpl();
        setId(processReference, OTHER_ID);
        return ImmutableSet.of(processReference);
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
