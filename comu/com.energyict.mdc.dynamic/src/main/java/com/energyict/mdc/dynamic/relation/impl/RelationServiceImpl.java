package com.energyict.mdc.dynamic.relation.impl;

import com.energyict.mdc.common.BusinessObject;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.RelationTypeShadow;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.TransactionService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

/**
 * Provides an implementation for the {@link RelationService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (10:28)
 */
@Component(name="com.energyict.mdc.dynamic.relation", service = {RelationService.class, InstallService.class}, property = "name=" + RelationService.COMPONENT_NAME, immediate = true)
public class RelationServiceImpl implements RelationService, ServiceLocator, InstallService {

    private volatile DataModel dataModel;
    private volatile OrmClient ormClient;
    private volatile Thesaurus thesaurus;
    private volatile TransactionService transactionService;

    private volatile PropertySpecService propertySpecService;

    public RelationServiceImpl() {
        super();
    }

    @Inject
    public RelationServiceImpl(TransactionService transactionService, OrmService ormService, NlsService nlsService, PropertySpecService propertySpecService) {
        this();
        this.setTransactionService(transactionService);
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
            }
        };
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setOrmService (OrmService ormService) {
        this.dataModel = ormService.newDataModel(COMPONENT_NAME, "ComServer dynamic relations");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(this.dataModel);
        }
        this.ormClient = new OrmClientImpl(this.dataModel);
    }

    @Reference
    public void setNlsService (NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.DOMAIN);
    }

    @Activate
    public void activate () {
        Bus.setServiceLocator(this);
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.thesaurus).install(true, true);
    }

    @Deactivate
    public void deactivate () {
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
        return this.ormClient.getRelationTypeFactory().getUnique("id", id).orNull();
    }

    @Override
    public RelationType findRelationType(String name) {
        List<RelationType> relationTypes = this.ormClient.getRelationTypeFactory().find("name", name);
        if (relationTypes.isEmpty()) {
            return null;
        }
        else {
            return relationTypes.get(0);
        }
    }

    @Override
    public List<RelationType> findRelationTypesByParticipant(RelationParticipant participant) {
        return this.ormClient.findRelationTypesByParticipant((BusinessObject) participant);
    }

    @Override
    public RelationAttributeType findRelationAttributeType(int id) {
        return this.ormClient.getRelationAttributeTypeFactory().getUnique("id", id).orNull();
    }

}