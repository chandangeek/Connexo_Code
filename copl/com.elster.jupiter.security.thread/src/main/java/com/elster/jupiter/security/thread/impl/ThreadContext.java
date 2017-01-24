package com.elster.jupiter.security.thread.impl;

import java.security.Principal;
import java.util.Locale;

class ThreadContext {
	private final Principal principal;
	private String module;
	private String action;
    private Locale locale;
	private String applicationName;
	
	ThreadContext(Principal principal, String module, String action, Locale locale) {
		this.principal = principal;
		this.module = module;
		this.action = action;
        this.locale = locale;
    }
	
	ThreadContext(Principal principal) {
		this(principal,null,null, null);
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

    public Locale getLocale() {
        return locale == null ? Locale.getDefault() : locale;
    }

	protected String getApplicationName() {
		return applicationName;
	}

	protected void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
}
