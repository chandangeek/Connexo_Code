/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */



package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.Audit;
import com.elster.jupiter.audit.AuditDecoderHandle;
import com.elster.jupiter.audit.AuditFilter;
import com.elster.jupiter.audit.AuditLog;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.audit.AuditTrail;
import com.elster.jupiter.audit.AuditTrailDecoderHandle;
import com.elster.jupiter.audit.AuditTrailFilter;
import com.elster.jupiter.audit.security.Privileges;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;

import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component(
        name = "com.elster.jupiter.audit",
        service = {AuditService.class, TranslationKeyProvider.class},
        property = "name=" + AuditService.COMPONENTNAME,
        immediate = true)
public class AuditServiceImpl implements AuditService, TranslationKeyProvider {

    private volatile DataModel dataModel;
    private volatile UpgradeService upgradeService;
    private volatile Thesaurus thesaurus;
    private volatile OrmService ormService;
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private final Map<String, AuditDecoderHandle> auditDecoderHandles = new ConcurrentHashMap<>();
    private final List<AuditTrailDecoderHandle> auditTrailDecoderHandles = Collections.synchronizedList(new ArrayList<AuditTrailDecoderHandle>());

    public AuditServiceImpl() {
    }

    @Inject
    public AuditServiceImpl(OrmService ormService, NlsService nlsService, UpgradeService upgradeService, UserService userService, ThreadPrincipalService threadPrincipalService) {
        this();
        setOrmService(ormService);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
        setThreadPrincipalService(threadPrincipalService);
        setUserService(userService);
        activate();
    }

    @Activate
    public void activate() {
        try {
            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Thesaurus.class).toInstance(thesaurus);
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                    bind(AuditService.class).toInstance(AuditServiceImpl.this);
                    bind(DataModel.class).toInstance(dataModel);
                    bind(UserService.class).toInstance(userService);

                }
            });
            upgradeService.register(InstallIdentifier.identifier("Pulse", COMPONENTNAME),
                    dataModel,
                    InstallerImpl.class,
                    Collections.emptyMap());

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public String getComponentName() {
        return COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        translationKeys.addAll(Arrays.asList(Privileges.values()));

        return translationKeys;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
        dataModel = ormService.newDataModel(COMPONENTNAME, "Audit");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(AuditService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addAuditDecoderHandle(AuditDecoderHandle auditDecoderHandle) {
        auditDecoderHandles.put(auditDecoderHandle.getDomain(), auditDecoderHandle);
    }

    public void removeAuditDecoderHandle(AuditDecoderHandle auditDecoderHandle) {
        auditDecoderHandles.remove(auditDecoderHandle.getDomain());
    }

    public Optional<AuditDecoderHandle> getAuditDecoderHandles(String key) {
        return auditDecoderHandles.entrySet().stream()
                .filter(auditDecoderHandleEntry -> auditDecoderHandleEntry.getKey().compareToIgnoreCase(key) == 0)
                .findFirst()
                .map(auditDecoderHandleEntry -> auditDecoderHandleEntry.getValue());
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addAuditTrailDecoderHandle(AuditTrailDecoderHandle auditTrailDecoderHandle) {
        auditTrailDecoderHandles.add(auditTrailDecoderHandle);
    }

    public void removeAuditTrailDecoderHandle(AuditTrailDecoderHandle auditTrailDecoderHandle) {
        auditTrailDecoderHandles.remove(auditTrailDecoderHandle);
    }

    public Optional<AuditTrailDecoderHandle> getAuditTrailDecoderHandles(String domain, String context) {
        return auditTrailDecoderHandles.stream()
                .filter(auditDecoderHandleEntry ->
                        (auditDecoderHandleEntry.getDomain().compareToIgnoreCase(domain) == 0) &&
                                (auditDecoderHandleEntry.getContext().compareToIgnoreCase(context) == 0))
                .findFirst();
    }

    @Override
    public Finder<Audit> getAudit(AuditFilter filter) {
        return DefaultFinder.of(Audit.class, filter.toCondition(), dataModel, AuditLog.class).defaultSortColumn(AuditImpl.Field.CREATETIME.fieldName(), false);
    }

    @Override
    public Finder<AuditTrail> getAuditTrail(AuditTrailFilter filter) {
        return DefaultFinder.of(AuditTrail.class, filter.toCondition(), dataModel).defaultSortColumn(AuditImpl.Field.CREATETIME.fieldName(), false);
    }

    @Override
    public AuditFilter newAuditFilter() {
        return new AuditFilterImpl(ormService, threadPrincipalService, this);
    }

    @Override
    public AuditTrailFilter newAuditTrailFilter() {
        return new AuditTrailFilterImpl(ormService, threadPrincipalService, this);
    }
}
