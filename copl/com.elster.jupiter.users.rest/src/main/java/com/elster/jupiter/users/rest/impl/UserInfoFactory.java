package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.InfoFactory;
import com.elster.jupiter.rest.util.PropertyDescriptionInfo;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.rest.LocaleInfo;
import com.elster.jupiter.users.rest.UserInfo;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class to create Info objects. This class will register on the InfoFactoryWhiteboard and is used by DynamicSearch.
 */
@Component(name = "user.info.factory", service = {InfoFactory.class}, immediate = true)
public class UserInfoFactory implements InfoFactory<User> {

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUserService(final UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(UserService.COMPONENTNAME, Layer.REST);
    }

    @Override
    public Object from(User user) {
        UserInfo userInfo = new UserInfo();
        userInfo.id = user.getId();
        userInfo.email = user.getEmail();
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
        userInfo.isUserLocked = user.isUserLocked(userService.getLockingAccountSettings());
        final GroupInfoFactory groupInfoFactory = new GroupInfoFactory(threadPrincipalService, userService);
        for (Group group : user.getGroups()) {
            userInfo.groups.add(groupInfoFactory.from(nlsService, group));
        }

        return userInfo;
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        List<PropertyDescriptionInfo> infos = new ArrayList<>();
        infos.add(createDescription(TranslationSeeds.NAME, String.class));
        infos.add(createDescription(TranslationSeeds.DESCRIPTION, String.class));
        infos.add(createDescription(TranslationSeeds.STATUS, String.class));
        infos.add(createDescription(TranslationSeeds.EMAIL, String.class));
        infos.add(createDescription(TranslationSeeds.DOMAIN, Group.class));
        infos.add(createDescription(TranslationSeeds.LANGUAGE, LocaleInfo.class));
        infos.add(createDescription(TranslationSeeds.CREATETIME, String.class));
        infos.add(createDescription(TranslationSeeds.MODTIME, String.class));
        infos.add(createDescription(TranslationSeeds.LASTSUCCESSFULLOGIN, String.class));
        infos.add(createDescription(TranslationSeeds.LASTUNSUCCESSFULLOGIN, String.class));
        infos.add(createDescription(TranslationSeeds.ISUSERLOCKED, String.class));
        return infos;
    }

    private PropertyDescriptionInfo createDescription(TranslationSeeds propertyName, Class<?> aClass) {
        return new PropertyDescriptionInfo(propertyName.getKey(), aClass, thesaurus.getString(propertyName.getKey(), propertyName.getDefaultFormat()));
    }

    @Override
    public Class<User> getDomainClass() {
        return User.class;
    }
}
