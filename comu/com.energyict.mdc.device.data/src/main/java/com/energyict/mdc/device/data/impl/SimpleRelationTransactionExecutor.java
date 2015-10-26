package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.device.data.exceptions.NestedRelationTransactionException;
import com.energyict.mdc.device.data.exceptions.NoAttributesExpectedException;
import com.energyict.mdc.dynamic.relation.DefaultRelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationTransaction;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.pluggable.PluggableClassUsageProperty;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasDynamicProperties;

import java.sql.SQLException;
import java.time.Instant;

/**
* Provides an implementation for the {@link RelationTransactionExecutor} interface
* for {@link HasDynamicProperties} that has 1 single set of properties
 * and therefore only 1 single relation to create (or update).
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-08-09 (17:30)
*/
public class SimpleRelationTransactionExecutor<T extends HasDynamicProperties> implements RelationTransactionExecutor<T> {
    private DefaultRelationParticipant propertyOwner;
    private Instant now;
    private RelationTransaction transaction;
    private Thesaurus thesaurus;

    public SimpleRelationTransactionExecutor (DefaultRelationParticipant propertyOwner, Instant now, RelationType relationType, Thesaurus thesaurus) {
        super();
        this.propertyOwner = propertyOwner;
        this.now = now;
        this.thesaurus = thesaurus;
        this.initializeTransaction(relationType);
    }

    @Override
    public void add (PluggableClassUsageProperty<T> property) {
        if (this.transaction != null) {
            this.transaction.set(property.getName(), property.getValue());
        }
        else {
            throw new NoAttributesExpectedException(this.thesaurus, property.getName(), MessageSeeds.CODING_NO_PROPERTIES_EXPECTED);
        }
    }

    private void initializeTransaction (RelationType relationType) {
        // RelationType is null when T does not have any properties
        if (relationType != null) {
            this.transaction = relationType.newRelationTransaction();
            this.transaction.setFrom(this.now);
            this.transaction.setTo(null);
            this.transaction.set(this.propertyOwner.getDefaultAttributeType(), this.propertyOwner);
        }
    }

    @Override
    public void execute () {
        if (this.transaction != null) {
            try {
                this.transaction.execute();   // Use doExecute now that we are in Jupiter orbit
            }
            catch (BusinessException e) {
                throw new NestedRelationTransactionException(e, this.transaction.getRelationType().getName(), this.thesaurus, MessageSeeds.UNEXPECTED_RELATION_TRANSACTION_ERROR);
            }
            // Cannot collapse catch blocks because of the constructor
            catch (SQLException e) {
                throw new NestedRelationTransactionException(this.thesaurus, e, this.transaction.getRelationType().getName(), MessageSeeds.UNEXPECTED_RELATION_TRANSACTION_ERROR);
            }
        }
    }

}