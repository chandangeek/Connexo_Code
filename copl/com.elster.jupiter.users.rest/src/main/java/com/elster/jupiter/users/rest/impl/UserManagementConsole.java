/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest.impl;

import org.osgi.service.component.annotations.*;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.*;
import com.elster.jupiter.users.*;
import java.util.Optional;

@Component(name = "com.elster.jupiter.users.rest.usermanagement", service = UserManagementConsole.class, property = {"osgi.command.scope=jupiter", "osgi.command.function=userpass"}, immediate = true)
public class UserManagementConsole {

    private volatile UserService userService;
    private volatile TransactionService txService;
    private volatile ThreadPrincipalService threadPrincipalService;
    
    public UserManagementConsole() {
    	
    }
    
    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setTransactionService(TransactionService txService) {
        this.txService = txService;
    }
    
    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }
    
    public void userpass(String authenticationName, String password) {
    	try {
    		setUserPassword(authenticationName,password);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
    public void setUserPassword(String authenticationName, String password) {
    	Optional<User> user = userService.findUser(authenticationName);
    	if (!user.isPresent()) {
    		throw new IllegalArgumentException("Invalid autentication name");
    	} 
    	try {
    		threadPrincipalService.set(user.get());
    		try (TransactionContext context = txService.getContext()) {
    			user.get().setPassword(password);
    			user.get().update();
    			context.commit();
    		} 
    	} finally {
    		threadPrincipalService.clear();
    	}
    }
    
}
