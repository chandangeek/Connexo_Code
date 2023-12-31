/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.LdapUserDirectory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserDirectory;
import com.elster.jupiter.users.UserService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.security.Principal;
import java.util.Optional;

/**
 * Created by albertv on 12/16/2014.
 */
@Component(name = "com.elster.jupiter.users.console", service = {UserConsoleService.class},
        property = {"name=" + "USR" + ".console",
                "osgi.command.scope=jupiter",
                "osgi.command.function=addUser",
                "osgi.command.function=addApacheUserDirectory",
                "osgi.command.function=addActiveUserDirectory",
                "osgi.command.function=renameGroupName"},
        immediate = true)
public class UserConsoleService {
    private volatile UserService userService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

    public void addUser(String name, String pass) {
        addUser(name, pass, null);
    }

    public void addUser(String name, String pass, String email) {
        this.threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            // Fix for CONM-1382: Not allowing duplicate user names. Enforcing case-sensitivity.
            User compareUserObj = userService.findUser(name).isPresent() ? userService.findUser(name).get() : null;
            if (compareUserObj != null) {
                System.out.println("Username is case-insensitive. Connexo already has a user with same name");
            } else {
                UserDirectory userDirectory = userService.findDefaultUserDirectory();
                if (!((userDirectory.getType().equals("INT")) && (userDirectory.getDomain().equals("Local")))) {
                    Optional<UserDirectory> localDirectory = userService.findUserDirectory("Local");
                    if (localDirectory.isPresent() && localDirectory.get().getType().equals("INT")) {
                        userDirectory = localDirectory.get();
                    } else {
                        throw new RuntimeException("No internal local directory available");
                    }
                }
                User user = userDirectory.newUser(name, "", false, true);
                user.setPassword(pass);
                user.setEmail(email);
                user.update();
                context.commit();
            }
        }
    }

    public void addUser() {
        System.out.println("Please add username, password and optionally email.\n   Example: addUser \"username\" \"password\" [\"email@connexo.com\"]");
    }

    public void addApacheUserDirectory(String domain, String dirUser, String password, String url, String baseUser, String baseGroup, String security, String backupUrl) {
        createApacheDirectory(domain, dirUser, password, url, baseUser, baseGroup, security, backupUrl);
    }

    public void addActiveUserDirectory(String domain, String dirUser, String password, String url, String baseUser, String baseGroup, String security, String backupUrl) {
        createActiveDirectory(domain, dirUser, password, url, baseUser, baseGroup, security, backupUrl);
    }

    public void createActiveDirectory(String domain, String dirUser, String password, String url, String baseUser, String baseGroup, String security, String backupUrl) {
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory activeDirectory = userService.createActiveDirectory(domain);
            activeDirectory.setDefault(false);
            activeDirectory.setBaseUser(baseUser);
            activeDirectory.setUrl(url);
            activeDirectory.setBaseGroup(baseGroup);
            activeDirectory.setDirectoryUser(dirUser);
            activeDirectory.setBackupUrl(backupUrl);
            activeDirectory.setSecurity(security);
            activeDirectory.setPassword(password);
            threadPrincipalService.set(getPrincipal());
            activeDirectory.update();
            context.commit();
        }
    }

    public void createApacheDirectory(String domain, String dirUser, String password, String url, String baseUser, String baseGroup, String security, String backupUrl) {
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory activeDirectory = userService.createApacheDirectory(domain);
            activeDirectory.setDefault(false);
            activeDirectory.setBaseUser(baseUser);
            activeDirectory.setUrl(url);
            activeDirectory.setBaseGroup(baseGroup);
            activeDirectory.setDirectoryUser(dirUser);
            activeDirectory.setBackupUrl(backupUrl);
            activeDirectory.setSecurity(security);
            activeDirectory.setPassword(password);
            threadPrincipalService.set(getPrincipal());
            activeDirectory.update();
            context.commit();
        }
    }

    public void addApacheUserDirectory() {
        System.out.println("Please add domain, dirUser, password, url, baseUser, baseGroup, security, backupUrl!\n  " +
                " Example: addApacheUserDirectory \"MyDomain\" \"user\" \"password\" \"url\" \"baseUser\" \"baseGroup\" \"SSL\" \"backupURL\"");
    }

    public void addActiveUserDirectory() {
        System.out.println("Please add domain, dirUser, password, url, baseUser, baseGroup, security, backupUrl!\n  " +
                " Example: addActiveUserDirectory \"MyDomain\" \"user\" \"password\" \"url\" \"baseUser\" \"baseGroup\" \"NONE\" \"backupURL\"");
    }

    private Principal getPrincipal() {
        return new Principal() {
            @Override
            public String getName() {
                return "Jupiter Installer";
            }
        };
    }

    @SuppressWarnings("unused")
    public void renameGroupName(String groupName, String newName) {
        this.threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {

            GroupImpl group = (GroupImpl) userService.findGroup(groupName).get();
            group.setName(newName);
            group.update();
            context.commit();
        }
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }
}
