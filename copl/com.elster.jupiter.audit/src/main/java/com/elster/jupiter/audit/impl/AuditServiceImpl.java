/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */



package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.ApplicationType;
import com.elster.jupiter.audit.AuditDomainContextType;
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

import com.google.common.collect.ImmutableMap;
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
import java.util.stream.Collectors;

import static com.elster.jupiter.orm.Version.version;

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
                    ImmutableMap.of(version(10, 6), UpgraderV10_6.class,
                                    version(10, 7, 1), UpgraderV10_7_1.class,
                                    version(10, 9), UpgraderV10_9.class));

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
    public void addAuditTrailDecoderHandle(AuditTrailDecoderHandle auditTrailDecoderHandle) {
        auditTrailDecoderHandles.add(auditTrailDecoderHandle);
    }

    public void removeAuditTrailDecoderHandle(AuditTrailDecoderHandle auditTrailDecoderHandle) {
        auditTrailDecoderHandles.remove(auditTrailDecoderHandle);
    }

    public List<AuditTrailDecoderHandle> getAuditTrailDecoderHandles(AuditDomainContextType domainContext) {
        return auditTrailDecoderHandles.stream()
                .filter(auditDecoderHandleEntry -> auditDecoderHandleEntry.getAuditDomainContextType() == domainContext)
                .collect(Collectors.toList());
    }

    public List<AuditTrailDecoderHandle> getAuditTrailDecoderHandles() {
        return auditTrailDecoderHandles;
    }

    @Override
    public Finder<AuditTrail> getAuditTrail(AuditTrailFilter filter) {
        return DefaultFinder.of(AuditTrail.class, filter.toCondition(), dataModel).defaultSortColumn(AuditTrailImpl.Field.CREATETIME.fieldName(), false);
    }

    @Override
    public AuditTrailFilter newAuditTrailFilter(ApplicationType applicationType) {
        return new AuditTrailFilterImpl(threadPrincipalService, applicationType, this);
    }
}
