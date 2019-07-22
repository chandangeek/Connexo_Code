/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction;

import com.elster.jupiter.util.streams.ExceptionThrowingRunnable;
import com.elster.jupiter.util.streams.ExceptionThrowingSupplier;

import aQute.bnd.annotation.ProviderType;

import java.security.Principal;
import java.util.Locale;

@ProviderType
public interface TransactionBuilder {

    TransactionBuilder principal(Principal principal);

    TransactionBuilder module(String module);

    TransactionBuilder action(String action);

    TransactionBuilder locale(Locale locale);

    <T, E extends Throwable> T execute(ExceptionThrowingSupplier<T, E> transaction) throws E;

    <E extends Throwable> TransactionEvent run(ExceptionThrowingRunnable<E> runnable) throws E;
}
