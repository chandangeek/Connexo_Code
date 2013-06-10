package com.elster.jupiter.security.thread.impl;

import java.security.Principal;

class ThreadContext {
	private final Principal principal;
	private String module;
	private String action;
	
	ThreadContext(Principal principal, String module, String action) {
		this.principal = principal;
		this.module = module;
		this.action = action;
	}
	
	ThreadContext(Principal principal) {
		this(principal,null,null);
	}
	
	void set(String module, String action) {
		this.module = module;
		this.action = action;
	}

	Principal getPrincipal() {
		return principal;
	}

	String getModule() {
		return module;
	}

	String getAction() {
		return action;
	}
}
