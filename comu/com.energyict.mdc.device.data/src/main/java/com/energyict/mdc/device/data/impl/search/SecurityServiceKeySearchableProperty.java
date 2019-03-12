/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.Expiration;
import com.elster.jupiter.properties.ExpirationFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@LiteralSql
public class SecurityServiceKeySearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.security.servicekey";

    private final DataModel dataModel;
    private final SecurityManagementService securityManagementService;
    private final PropertySpecService propertySpecService;

    private SearchDomain searchDomain;
    private SearchablePropertyGroup group;

    @Inject
    public SecurityServiceKeySearchableProperty(DataModel datamodel, SecurityManagementService securityManagementService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.dataModel = datamodel;
        this.securityManagementService = securityManagementService;
        this.propertySpecService = propertySpecService;
    }

    SecurityServiceKeySearchableProperty init(SearchDomain searchDomain, SearchablePropertyGroup parentGroup) {
        this.searchDomain = searchDomain;
        this.group = parentGroup;
        return this;
    }

    @Override
    protected boolean valueCompatibleForDisplay(Object value) {
        return false;
    }

    @Override
    protected String toDisplayAfterValidation(Object value) {
        return null;
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        if (condition.getClass().isAssignableFrom(Comparison.class)) {
            SqlBuilder sqlBuilder = new SqlBuilder();
            Comparison comparison = (Comparison) condition;
/*            String value = "N";
            if (comparison.getValues()[0].toString().equalsIgnoreCase("true")) {
                value = "Y";
            }
*/ 
            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".ID IN ");
            sqlBuilder.openBracket();
            //Devices having a service key
            String query = "SELECT DISTINCT DEVICE FROM DDC_KEYACCESSOR WHERE DDC_KEYACCESSOR.SERVICEKEY = 'Y'";
            if (comparison.getValues()[0].toString().equalsIgnoreCase("true"))
            {
            	sqlBuilder.append(query);
	    } else {
                sqlBuilder.append("SELECT ID FROM DDC_DEVICE WHERE ID NOT IN (" + query + ")");
	    }
            sqlBuilder.closeBracket();
            return sqlBuilder;
        }
        throw new IllegalArgumentException("Condition should be a comparison");
    }

    @Override
    public SearchDomain getDomain() {
        return this.searchDomain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.of(this.group);
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .booleanSpec()
                .named(PROPERTY_NAME, this.getNameTranslationKey())
                .fromThesaurus(this.getThesaurus())
                .finish();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE;
    }

    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.SERVICE_KEY;
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
	// no refresh
    }
}
