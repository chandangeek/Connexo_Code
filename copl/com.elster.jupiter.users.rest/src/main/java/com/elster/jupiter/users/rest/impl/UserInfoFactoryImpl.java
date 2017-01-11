package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.rest.GroupInfo;
import com.elster.jupiter.users.rest.LocaleInfo;
import com.elster.jupiter.users.rest.UserInfo;
import com.elster.jupiter.users.rest.UserInfoFactory;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;


@Component(name = "com.elster.jupiter.users.rest.UserInfoFactory",
        immediate = true,
        service = UserInfoFactory.class)
public class UserInfoFactoryImpl implements com.elster.jupiter.users.rest.UserInfoFactory {
    private GroupInfoFactory groupInfoFactory;
    private volatile ThreadPrincipalService threadPrincipalService;

    public UserInfoFactoryImpl() {

    }

    @Inject
    public UserInfoFactoryImpl(ThreadPrincipalService threadPrincipalService, GroupInfoFactory groupInfoFactory) {
        this.threadPrincipalService = threadPrincipalService;
        this.groupInfoFactory = groupInfoFactory;
    }

    @Override
    public UserInfo from(NlsService nlsService, User user) {
        UserInfo userInfo = new UserInfo();
        userInfo.id = user.getId();
        userInfo.authenticationName = user.getName();
        userInfo.description = user.getDescription();
        userInfo.active = user.getStatus();
        userInfo.version = user.getVersion();
        userInfo.domain = user.getDomain();
        userInfo.language = user.getLocale().map((locale) -> new LocaleInfo(locale, locale)).orElse(null);
        userInfo.createdOn = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(user.getCreationDate().atZone(ZoneId.systemDefault()));
        userInfo.modifiedOn = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(user.getModifiedDate().atZone(ZoneId.systemDefault()));
        userInfo.lastSuccessfulLogin = user.getLastSuccessfulLogin() == null ? null : user.getLastSuccessfulLogin().toString();
        userInfo.lastUnSuccessfulLogin = user.getLastUnSuccessfulLogin() == null ? null : user.getLastUnSuccessfulLogin().toString();
        for (Group group : user.getGroups()) {
            userInfo.groups.add(groupInfoFactory.from(nlsService, group));
        }

        Collections.sort(userInfo.groups, (g1, g2) -> g1.name.compareTo(g2.name));

        return userInfo;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
        groupInfoFactory = new GroupInfoFactory(threadPrincipalService);
    }
}
