package com.energyict.mdc.dynamic.relation.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.BusinessObject;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.DefaultAttributeTypeDetective;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.RelationTypeShadow;
import com.energyict.mdc.dynamic.relation.exceptions.MessageSeeds;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Provides an implementation for the {@link RelationService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (10:28)
 */
@Component(name = "com.energyict.mdc.dynamic.relation", service = {RelationService.class, InstallService.class, TranslationKeyProvider.class, DefaultAttributeTypeDetective.class}, property = "name=" + RelationService.COMPONENT_NAME, immediate = true)
public class RelationServiceImpl implements RelationService, ServiceLocator, InstallService, TranslationKeyProvider, DefaultAttributeTypeDetective {

    private volatile DataModel dataModel;
    private volatile OrmClient ormClient;
    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;
    private volatile Clock clock;
    private volatile PropertySpecService propertySpecService;
    private volatile List<DefaultAttributeTypeDetective> detectives = new CopyOnWriteArrayList<>();

    public RelationServiceImpl() {
        super();
    }

    @Inject
    public RelationServiceImpl(TransactionService transactionService, Clock clock, OrmService ormService, NlsService nlsService, PropertySpecService propertySpecService) {
        this();
        this.setTransactionService(transactionService);
        this.setClock(clock);
        this.setOrmService(ormService);
        this.setNlsService(nlsService);
        this.setPropertySpecService(propertySpecService);
        this.activate();
        this.install();
    }

    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(TransactionService.class).toInstance(transactionService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(DataModel.class).toInstance(dataModel);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(DefaultAttributeTypeDetective.class).toInstance(RelationServiceImpl.this);
            }
        };
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Clock clock() {
        return this.clock;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.dataModel = ormService.newDataModel(COMPONENT_NAME, "ComServer dynamic relations");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(this.dataModel);
        }
        this.ormClient = new OrmClientImpl(this.dataModel);
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addDeviceProtocolService(DefaultAttributeTypeDetective detective) {
        this.detectives.add(detective);
    }

    @SuppressWarnings("unused")
    public void removeDeviceProtocolService(DefaultAttributeTypeDetective detective) {
        this.detectives.remove(detective);
    }

    @Activate
    public void activate() {
        Bus.setServiceLocator(this);
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        new Installer(this.dataModel).install(true, true);
    }

    @Override
    public String getComponentName() {
        return RelationService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "EVT", "NLS");
    }

    @Deactivate
    public void deactivate() {
        Bus.clearServiceLocator(this);
    }

    @Override
    public OrmClient getOrmClient() {
        return this.ormClient;
    }

    @Override
    public RelationType createRelationType(RelationTypeShadow shadow, PropertySpecService propertySpecService) {
        RelationTypeImpl relationType = new RelationTypeImpl(this.dataModel, this.transactionService, this.thesaurus);
        relationType.init(shadow, propertySpecService);
        return relationType;
    }

    @Override
    public List<RelationType> findAllActiveRelationTypes() {
        return this.ormClient.getRelationTypeFactory().find("active", true);
    }

    @Override
    public RelationType findRelationType(int id) {
        return this.ormClient.getRelationTypeFactory().getUnique("id", id).orElse(null);
    }

    @Override
    public RelationType findRelationType(String name) {
        List<RelationType> relationTypes = this.ormClient.getRelationTypeFactory().find("name", name);
        if (relationTypes.isEmpty()) {
            return null;
        } else {
            return relationTypes.get(0);
        }
    }

    @Override
    public List<RelationType> findRelationTypesByParticipant(RelationParticipant participant) {
        return this.ormClient.findRelationTypesByParticipant((BusinessObject) participant);
    }

    @Override
    public RelationAttributeType findRelationAttributeType(int id) {
        return this.ormClient.getRelationAttributeTypeFactory().getUnique("id", id).orElse(null);
    }

    @Override
    public boolean isDefaultAttribute(RelationAttributeType attributeType) {
        return this.detectives
                .stream()
                .filter(each -> each.isDefaultAttribute(attributeType))
                .findFirst()
                .isPresent();
    }

}