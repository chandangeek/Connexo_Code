package com.elster.jupiter.security.thread.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import oracle.jdbc.OracleConnection;
import org.osgi.service.component.annotations.Component;

import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

import static oracle.jdbc.OracleConnection.*;

@Component (name = "com.elster.jupiter.security.thread")
public class ThreadPrincipalServiceImpl implements ThreadPrincipalService {
	private ThreadLocal<ThreadContext> threadContexts = new ThreadLocal<>();
			
	@Override
	public void runAs(Principal user, Runnable runnable, Locale locale) {
		ThreadContext oldContext = threadContexts.get();
		threadContexts.set(new ThreadContext(user, null, null, locale));
		try {
			runnable.run();
		} finally {
			threadContexts.set(oldContext);
		}
	}

	@Override
	public Principal getPrincipal() {
		ThreadContext context = threadContexts.get();
		return context == null ? null : context.getPrincipal();
	}

	@Override
	public void set(Principal principal) {
		threadContexts.set(new ThreadContext(principal));		
	}

	@Override
	public String getApplicationName() {
		ThreadContext context = threadContexts.get();
		return context == null ? null : context.getApplicationName();
	}

	@Override
	public void setApplicationName(String applicationName) {
		ThreadContext context = threadContexts.get();
		if(context != null) {
			context.setApplicationName(applicationName);
		}
	}

	@Override
	public void clear() {
		threadContexts.remove();
	}

	@Override
	public String getModule() {
		ThreadContext context = threadContexts.get();
		return context == null ? null : context.getModule();
	}

	@Override
	public String getAction() {
		ThreadContext context = threadContexts.get();
		return context == null ? null : context.getAction();	
	}

    @Override
    public Locale getLocale() {
        ThreadContext context = threadContexts.get();
        return context == null ? Locale.getDefault() : context.getLocale();
    }

    @Override
	public void set(Principal principal, String module, String action, Locale locale) {
		threadContexts.set(new ThreadContext(principal,module,action, locale));
	}

	@Override
	public void set(String module, String action) {
		ThreadContext context = threadContexts.get();
		if (context == null) {
			threadContexts.set(new ThreadContext(null,module,action, null));
		} else {
			context.set(module,action);
		}
	}
	
	@Override
	public void setEndToEndMetrics(Connection connection) throws SQLException {
        OracleConnection oraConnection;
        try {
            oraConnection = connection.unwrap(OracleConnection.class);
        } catch (SQLException e) {
            return;
        }
        ThreadContext context = threadContexts.get();
		String[] metrics = new String[END_TO_END_STATE_INDEX_MAX];
		short ecid = Short.MIN_VALUE;
		if (context != null) {			
			Principal principal = context.getPrincipal();
			metrics[END_TO_END_CLIENTID_INDEX] = principal == null ? null : principal.getName();
			metrics[END_TO_END_MODULE_INDEX] = context.getModule();
			metrics[END_TO_END_ACTION_INDEX] = context.getAction();
			ecid = 0;
		}
		oraConnection.setEndToEndMetrics(metrics, ecid);
	}

    @Override
    public Runnable withContextAdded(Runnable runnable, Principal principal) {
        return new RunnableWithContext(runnable, this, principal);
    }

    @Override
    public Runnable withContextAdded(Runnable runnable, Principal principal, String module, String action, Locale locale) {
        return new RunnableWithContext(runnable, this, principal, module, action, locale);
    }
}
