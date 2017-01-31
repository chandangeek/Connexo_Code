/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction;

import java.security.Principal;
import java.util.Locale;

public interface TransactionBuilder {
	
	TransactionBuilder principal(Principal principal);
	TransactionBuilder module(String module);
	TransactionBuilder action(String action);
	TransactionBuilder locale(Locale locale);
	<T> T execute(Transaction<T> transaction);
	TransactionEvent run(Runnable runnable);
}
