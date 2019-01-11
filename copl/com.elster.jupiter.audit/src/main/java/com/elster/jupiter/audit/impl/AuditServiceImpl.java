/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.Audit;
import com.elster.jupiter.audit.AuditDecoderHandle;
import com.elster.jupiter.audit.AuditFilter;
import com.elster.jupiter.audit.AuditLog;
import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.V10_6SimpleUpgrader;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
    private final Map<String, AuditDecoderHandle> auditDecoderHandles = new ConcurrentHashMap<>();

    public AuditServiceImpl() {
    }

    @Inject
    public AuditServiceImpl(OrmService ormService, NlsService nlsService, UpgradeService upgradeService) {
        this();
        setOrmService(ormService);
        setNlsService(nlsService);
        setUpgradeService(upgradeService);
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

                }
            });
            upgradeService.register(InstallIdentifier.identifier("Pulse", COMPONENTNAME),
                    dataModel,
                    Installer.class,
                    ImmutableMap.of(
                            version(10, 6), V10_6SimpleUpgrader.class
                    ));

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
        translationKeys.addAll(Arrays.asList(MessageSeeds.values()));
        return translationKeys;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
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

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addAuditDecoderHandle(AuditDecoderHandle auditDecoderHandle) {
        auditDecoderHandles.put(auditDecoderHandle.getIdentifier(), auditDecoderHandle);
    }

    public void removeAuditDecoderHandle(AuditDecoderHandle auditDecoderHandle) {
        auditDecoderHandles.remove(auditDecoderHandle.getIdentifier());
    }

    public Optional<AuditDecoderHandle> getAuditReferenceResolver(String key) {
        return Optional.of(auditDecoderHandles.get(key));
    }

    @Override
    public Finder<Audit> getAudit(AuditFilter filter) {
        return DefaultFinder.of(Audit.class, filter.toCondition(), dataModel, AuditLog.class).defaultSortColumn(AuditImpl.Field.CREATETIME.fieldName(), false);
    }

    @Override
    public AuditFilter newAuditFilter() {
        return new AuditFilterImpl();
    }
}
