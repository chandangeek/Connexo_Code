package com.energyict.mdc.dynamic.relation.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.BusinessObject;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.dynamic.relation.RelationTypeShadow;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;

/**
 * Provides an implementation for the {@link RelationService}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (10:28)
 */
@Component(name="com.energyict.mdc.dynamic.relation", service = RelationService.class)
public class RelationServiceImpl implements RelationService, ServiceLocator {

    private volatile OrmClient ormClient;

    public RelationServiceImpl() {
        super();
    }

    @Inject
    public RelationServiceImpl(OrmService ormService) {
        this();
        this.setOrmService(ormService);
        this.activate();
    }

    @Reference
    public void setOrmService (OrmService ormService) {
        DataModel dataModel = ormService.newDataModel("CDR", "ComServer dynamic relations");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        ormService.register(dataModel);
        this.ormClient = new OrmClientImpl(dataModel);
    }

    @Activate
    public void activate () {
        Bus.setServiceLocator(this);
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
    public RelationType createRelationType(RelationTypeShadow shadow) throws BusinessException, SQLException {
        RelationTypeImpl relationType = new RelationTypeImpl();
        relationType.init(shadow);
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