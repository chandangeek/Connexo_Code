/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
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
public class SecurityExpirationSearchableProperty extends AbstractSearchableDeviceProperty {

    static final String PROPERTY_NAME = "device.security.expiration";

    private final DataModel dataModel;
    private final SecurityManagementService securityManagementService;
    private final PropertySpecService propertySpecService;

    private SearchDomain searchDomain;
    private SearchablePropertyGroup group;

    @Inject
    public SecurityExpirationSearchableProperty(DataModel datamodel, SecurityManagementService securityManagementService, PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(thesaurus);
        this.dataModel = datamodel;
        this.securityManagementService = securityManagementService;
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
        if (comparison.getValues().length == 1) {
            // the condition coming from FE as 'device.security.expiration ==  Expiration.Type.EXPIRED || Expiration.Type.EXPIRES_1WEEK
            // || Expiration.Type.EXPIRES_1MONTH || Expiration.Type.EXPIRES_3MONTHS || Expiration.Type.OBSOLETE
            Expiration expiration = (Expiration) comparison.getValues()[0];

            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE + ".ID IN ");
            sqlBuilder.openBracket();
            //Devices having an actual certificate that is expired
            sqlBuilder.append("SELECT DEVICE FROM DDC_KEYACCESSOR, PKI_CERTIFICATE WHERE (DDC_KEYACCESSOR.DISCRIMINATOR = 'C' AND DDC_KEYACCESSOR.ACTUAL_CERT = PKI_CERTIFICATE.ID AND ");
            if (Expiration.Type.OBSOLETE == expiration.getType()) {
                sqlBuilder.add(new ComparisonFragment(this, "PKI_CERTIFICATE.STATUS", (Comparison) expiration.isObsolete("PKI_CERTIFICATE.STATUS")));
            } else {
                sqlBuilder.add(new ComparisonFragment(this, "PKI_CERTIFICATE.EXPIRATION", (Comparison) expiration.isExpired("PKI_CERTIFICATE.EXPIRATION", now)));
            }
            sqlBuilder.closeBracket();

            //todo: update central query / 'obsolete'
            appendCentralCertificateAccessorsClause(sqlBuilder, expiration, now);

            if (expiration.getType() != Expiration.Type.OBSOLETE) {
                // Devices having an actual passphrase that is expired
                getPassPhrasePairTableNames().forEach(passPhraseTableName -> appendExpiredKeyClause(sqlBuilder, "P", "ACTUALPASSPHRASEID", passPhraseTableName, expiration, now));
                // Devices having an actual symmetric key that is expired
                getSymmetricKeyTableNames().forEach(symmetricKeyTableName -> appendExpiredKeyClause(sqlBuilder, "S", "ACTUALSYMKEYID", symmetricKeyTableName, expiration, now));
            }

            sqlBuilder.closeBracket();
        }
        return sqlBuilder;
    }

    @Override
    public void bindSingleValue(PreparedStatement statement, int bindPosition, Object value) throws SQLException {
        if (value instanceof Long) {
            statement.setLong(bindPosition, (Long) value);
        } else if (value instanceof String) {
            statement.setString(bindPosition, (String) value);
        } else {
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
    }

    private List<String> getPassPhrasePairTableNames() {
        return addTableNames(new ArrayList<>(), "SELECT DISTINCT ACTUALPASSPHRASETABLE FROM DDC_KEYACCESSOR WHERE ACTUALPASSPHRASEID IS NOT NULL");
    }

    private List<String> getSymmetricKeyTableNames() {
        return addTableNames(new ArrayList<>(), "SELECT DISTINCT ACTUALSYMKEYTABLE FROM DDC_KEYACCESSOR WHERE ACTUALSYMKEYID IS NOT NULL");
    }

    private List<String> addTableNames(final List<String> tableNames, String sql) {
        try (Connection connection = this.dataModel.getConnection(false);
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                tableNames.add(rs.getString(1));
            }
        } catch (SQLException e) {
            // Should not happen
            throw new ApplicationException(e);
        }
        return tableNames;
    }

    private void appendExpiredKeyClause(SqlBuilder sqlBuilder, String keyAccessorDiscriminator, String keyAccessorActualFieldName, String keyTableName, Expiration expiration, Instant when) {
        Optional<Comparison> expirationCondition = securityManagementService.getExpirationCondition(expiration, when, keyTableName);
        ComparisonFragment sqlFragment = new ComparisonFragment(this, expirationCondition.get().getFieldName(), expirationCondition.get());
        sqlBuilder.append(" UNION ");
        sqlBuilder.append("SELECT DEVICE FROM DDC_KEYACCESSOR, ");
        sqlBuilder.append(keyTableName);
        sqlBuilder.append(" WHERE ");
        sqlBuilder.openBracket();
        sqlBuilder.append("DDC_KEYACCESSOR.DISCRIMINATOR = '");
        sqlBuilder.append(keyAccessorDiscriminator);
        sqlBuilder.append("' AND DDC_KEYACCESSOR.");
        sqlBuilder.append(keyAccessorActualFieldName);
        sqlBuilder.append(" = ");
        sqlBuilder.append(keyTableName);
        sqlBuilder.append(".ID ");
        if (expirationCondition.isPresent()) {
            sqlBuilder.append(" AND ");
            sqlBuilder.add(sqlFragment);
        }
        sqlBuilder.closeBracket();
    }

    private void appendCentralCertificateAccessorsClause(SqlBuilder sqlBuilder, Expiration expiration, Instant when) {
        sqlBuilder.append(" UNION SELECT DDC_DEVICE.ID FROM DDC_DEVICE" +
                "  INNER JOIN DTC_SECACCTYPES_ON_DEVICETYPE ON DDC_DEVICE.DEVICETYPE = DTC_SECACCTYPES_ON_DEVICETYPE.DEVICETYPE" +
                "  INNER JOIN PKI_SECACCESSOR ON (DTC_SECACCTYPES_ON_DEVICETYPE.SECACCTYPE = PKI_SECACCESSOR.SECACCESSORTYPE AND PKI_SECACCESSOR.DISCRIMINATOR = 'C')" +
                "  INNER JOIN PKI_CERTIFICATE ON (PKI_SECACCESSOR.ACTUAL_CERT = PKI_CERTIFICATE.ID AND ");

        if (Expiration.Type.OBSOLETE == expiration.getType()) {
            sqlBuilder.add(new ComparisonFragment(this, "PKI_CERTIFICATE.STATUS", (Comparison) expiration.isObsolete("PKI_CERTIFICATE.STATUS")));
        } else {
            sqlBuilder.add(new ComparisonFragment(this, "PKI_CERTIFICATE.EXPIRATION", (Comparison) expiration.isExpired("PKI_CERTIFICATE.EXPIRATION", when)));
        }
        sqlBuilder.closeBracket();
    }
}
