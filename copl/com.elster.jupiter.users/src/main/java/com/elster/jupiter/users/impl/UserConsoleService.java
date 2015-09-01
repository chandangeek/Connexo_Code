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
import java.util.Locale;

/**
 * Created by albertv on 12/16/2014.
 */

@Component(name = "com.elster.jupiter.users.console",service = {UserConsoleService.class}, property = {"name=" + "USR" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=addUser","osgi.command.function=addApacheUserDirectory","osgi.command.function=addActiveUserDirectory"}, immediate = true)

public class UserConsoleService {

    private volatile UserService userService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;


    public void addUser(String name,String pass){

         try ( TransactionContext context = transactionService.getContext()){

             UserDirectory userDirectory = userService.findDefaultUserDirectory();
             User user = userDirectory.newUser(name, "", false);
             user.setPassword(pass);
             user.save();
             context.commit();
         }

    }

    public void addUser() {
        System.out.println("Please add username and password!\n   Exemple: addUser \"username\" \"password\"" );
    }

    public void addApacheUserDirectory(String domain, String dirUser,String password, String url, String baseUser, String baseGroup,String security,String backupUrl){
        createApacheDirectory(domain,dirUser,password,url,baseUser,baseGroup,security,backupUrl);
    }

    public void addActiveUserDirectory(String domain, String dirUser,String password, String url, String baseUser, String baseGroup){
        createActiveDirectory(domain,dirUser,password,url,baseUser,baseGroup);
    }

    public void createActiveDirectory(String domain, String dirUser,String password, String url, String baseUser, String baseGroup){
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory activeDirectory = userService.createActiveDirectory(domain);
            activeDirectory.setDefault(false);
            activeDirectory.setBaseUser(baseUser);
            activeDirectory.setUrl(url);
            activeDirectory.setBaseGroup(baseGroup);
            activeDirectory.setDirectoryUser(dirUser);
            activeDirectory.setPassword(password);
            threadPrincipalService.set(getPrincipal());
            activeDirectory.save();
            context.commit();
        }
    }

    public void createApacheDirectory(String domain, String dirUser,String password, String url, String baseUser, String baseGroup,String security,String backupUrl){
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
            activeDirectory.save();
            context.commit();
        }

    }

    public void addApacheUserDirectory(){
        System.out.println("Please add domain, dirUser, password, url, baseUser, baseGroup, security, backupUrl!\n  " +
                " Exemple: addApacheUserDirectory \"MyDomain\" \"user\" \"password\" \"url\" \"baseUser\" \"baseGroup\" \"SSL\" \"backupURL\"");

    }

    public void addActiveUserDirectory(){
        System.out.println("Please add domain, dirUser, password, url, baseUser, baseGroup!\n  " +
                " Exemple: addActiveUserDirectory \"MyDomain\" \"user\" \"password\" \"url\" \"baseUser\" \"baseGroup\"");
    }

    private Principal getPrincipal() {
        return new Principal() {

            @Override
            public String getName() {
                return "Jupiter Installer";
            }
        };
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
