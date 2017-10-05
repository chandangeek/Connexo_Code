/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
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
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SecurityExpirationSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.security.expiration";

    private final PropertySpecService propertySpecService;

    private SearchDomain searchDomain;
    private SearchablePropertyGroup group;

    @Inject
    public SecurityExpirationSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.propertySpecService = propertySpecService;
    }

    SecurityExpirationSearchableProperty init(SearchDomain searchDomain, SearchablePropertyGroup parentGroup) {
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
        SqlBuilder sqlBuilder = new SqlBuilder();

        Comparison comparison = (Comparison) condition;
        if (comparison.getValues().length == 1){
            // the condition coming from FE as 'device.security.expiration ==  Expiration.Type.EXPIRED || Expiration.Type.EXPIRES_1WEEK || Expiration.Type.EXPIRES_1MONTH || Expiration.Type.EXPIRES_3MONTHS
            Expiration expiration = (Expiration) comparison.getValues()[0];
/* Todo: the PkiService also consults the private key factories when looking for expired keys
   Todo: There's no link between DDC_KEYACCESSOR and the SSM_PLAINTEXTPK table as there is no such thing as a PrivateKeyAccessor ???? */
            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".ID IN ");
            sqlBuilder.openBracket();
            //Devices having an actual certificate that is expired
            sqlBuilder.append("SELECT DEVICE FROM DDC_KEYACCESSOR, PKI_CERTIFICATE WHERE (DDC_KEYACCESSOR.DISCRIMINATOR = 'C' AND DDC_KEYACCESSOR.ACTUAL_CERT = PKI_CERTIFICATE.ID AND ");
            sqlBuilder.add(new ComparisonFragment(this, "PKI_CERTIFICATE.EXPIRATION", (Comparison) expiration.isExpired("PKI_CERTIFICATE.EXPIRATION", now)));
            sqlBuilder.closeBracket();
            //
            sqlBuilder.append(" UNION ");
            // Devices having an actual passphrase that is expired
            sqlBuilder.append("SELECT DEVICE FROM DDC_KEYACCESSOR, SSM_PLAINTEXTPW WHERE (DDC_KEYACCESSOR.DISCRIMINATOR = 'P' AND DDC_KEYACCESSOR.ACTUALPASSPHRASEID = SSM_PLAINTEXTPW.ID AND ");
            sqlBuilder.add(new ComparisonFragment(this, "SSM_PLAINTEXTPW.EXPIRATION", (Comparison) expiration.isExpired("SSM_PLAINTEXTPW.EXPIRATION", now)));
            sqlBuilder.closeBracket();
            //
            sqlBuilder.append(" UNION ");
            // Devices having an actual symmetric key that is expired
            sqlBuilder.append("SELECT DEVICE FROM DDC_KEYACCESSOR, SSM_PLAINTEXTSK WHERE (DDC_KEYACCESSOR.DISCRIMINATOR = 'S' AND DDC_KEYACCESSOR.ACTUALSYMKEYID = SSM_PLAINTEXTSK.ID AND ");
            sqlBuilder.add(new ComparisonFragment(this, "SSM_PLAINTEXTSK.EXPIRATION", (Comparison) expiration.isExpired("SSM_PLAINTEXTSK.EXPIRATION", now)));
            sqlBuilder.closeBracket();
            //
            sqlBuilder.closeBracket();
        }
        return sqlBuilder;
    }

    @Override
    public void bindSingleValue(PreparedStatement statement, int bindPosition, Object value) throws SQLException {
        Long expirationDateAsEpochMillis = (Long) value;
        if (expirationDateAsEpochMillis != null) {
            statement.setLong(bindPosition, expirationDateAsEpochMillis);
        }
        else {
            statement.setNull(bindPosition, java.sql.Types.NUMERIC);
        }
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
                .specForValuesOf(new ExpirationFactory())
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
        return PropertyTranslationKeys.DEVICE_EXPIRATION;
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
