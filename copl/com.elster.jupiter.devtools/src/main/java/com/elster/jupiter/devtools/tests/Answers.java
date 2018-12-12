/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.devtools.tests;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public enum Answers implements Answer<Object> {
    RETURNS_SELF;

    public Object answer(InvocationOnMock invocation) throws Throwable {
        Object mock = invocation.getMock();
        if( invocation.getMethod().getReturnType().isInstance( mock )){
            return mock;
        }
        else{
            return Mockito.RETURNS_DEFAULTS.answer(invocation);
        }
    }
}