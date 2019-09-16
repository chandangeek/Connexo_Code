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
 * Created by bvn on 6/9/15.
 */
@Component(name = "user.info.factory", service = {InfoFactory.class}, immediate = true)
public class UserInfoFactory implements InfoFactory<User> {

    private GroupInfoFactory groupInfoFactory;

    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile ThreadPrincipalService threadPrincipalService;

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
        groupInfoFactory = new GroupInfoFactory(threadPrincipalService);
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

        return userInfo;
    }

    @Override
    public List<PropertyDescriptionInfo> modelStructure() {
        List<PropertyDescriptionInfo> infos = new ArrayList<>();
        infos.add(createDescription(TranslationSeeds.NAME, String.class));
        infos.add(createDescription(TranslationSeeds.DESCRIPTION, String.class));
        infos.add(createDescription(TranslationSeeds.STATUS, String.class));
        infos.add(createDescription(TranslationSeeds.DOMAIN, Group.class));
        infos.add(createDescription(TranslationSeeds.LANGUAGE, String.class));
        infos.add(createDescription(TranslationSeeds.CREATETIME, Instant.class));
        infos.add(createDescription(TranslationSeeds.MODTIME, Instant.class));
        infos.add(createDescription(TranslationSeeds.LASTSUCCESSFULLOGIN, Instant.class));
        infos.add(createDescription(TranslationSeeds.LASTUNSUCCESSFULLOGIN, Instant.class));
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
