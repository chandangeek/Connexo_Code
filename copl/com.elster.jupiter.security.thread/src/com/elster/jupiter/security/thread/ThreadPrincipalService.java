package com.elster.jupiter.security.thread;

import java.security.Principal;

public interface ThreadPrincipalService {
	Principal getPrincipal();
	void setPrincipal(Principal principal);
	void clearPrincipal();
	void runAs(Principal principal , Runnable runnable);
}
