/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import java.security.Principal;
import java.util.Locale;
import java.util.Objects;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionBuilder;
import com.elster.jupiter.transaction.TransactionEvent;
import com.elster.jupiter.transaction.TransactionService;

class TransactionBuilderImpl implements TransactionBuilder {

	private final TransactionService transactionService;
	private final ThreadPrincipalService threadPrincipalService;
	private Principal principal;
	private String module;
	private String action;
	private Locale locale;
	
	TransactionBuilderImpl(TransactionService transactionService, ThreadPrincipalService threadPrincipalService) {
		this.transactionService = transactionService;
		this.threadPrincipalService = threadPrincipalService;
	}
	
	@Override
	public TransactionBuilder principal(Principal principal) {
		this.principal = Objects.requireNonNull(principal);
		return this;
	}

	@Override
	public TransactionBuilder module(String module) {
		this.module = Objects.requireNonNull(module);
		return this;
	}

	@Override
	public TransactionBuilder action(String action) {
		this.action = Objects.requireNonNull(action);
		return this;
	}

	@Override
	public TransactionBuilder locale(Locale locale) {
		this.locale = Objects.requireNonNull(locale);
		return this;
	}

	@Override
	public <T> T execute(Transaction<T> transaction) {
		setSecurityContext();
		try {
			return transactionService.execute(transaction);
		} finally {
			clearSecurityContext();
		}
	}

	@Override
	public TransactionEvent run(Runnable runnable) {
		setSecurityContext();
		try {
			return transactionService.run(runnable);
		} finally {
			clearSecurityContext();
		}
	}
	
	private void setSecurityContext() {
		threadPrincipalService.set(principal, module, action, locale);
	}

	private void clearSecurityContext() {
		threadPrincipalService.clear();
	}
}
