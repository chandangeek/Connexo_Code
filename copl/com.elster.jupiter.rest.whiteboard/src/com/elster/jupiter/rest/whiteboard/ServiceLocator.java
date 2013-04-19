package com.elster.jupiter.rest.whiteboard;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;

public interface ServiceLocator {
	static final String USERPRINCIPAL = "com.elster.jupiter.userprincipal";
	
	UserService getUserService();
	ThreadPrincipalService getThreadPrincipalService();
}
