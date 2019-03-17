/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpgraderV10_6 implements Upgrader {

    private DataModel dataModel;

    @Inject
    public UpgraderV10_6(OrmService ormService) {
        this.dataModel = ormService.getDataModel("ADT").get();
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        upgradeAuditTable();

    }

    private void upgradeAuditTable() {
        try (Connection connection = this.dataModel.getConnection(true)) {
            List<String> sqlStatements = new ArrayList<>();

            sqlStatements.add("ALTER TABLE ADT_AUDIT_TRAIL ADD PKCONTEXT NUMBER DEFAULT 0 NOT NULL");
            sqlStatements.add("ALTER TABLE ADT_AUDIT_TRAIL ADD PKDOMAIN NUMBER");
            sqlStatements.add("UPDATE ADT_AUDIT_TRAIL SET PKDOMAIN = PKCOLUMN");
            sqlStatements.add("ALTER TABLE ADT_AUDIT_TRAIL MODIFY PKDOMAIN NOT NULL");
            sqlStatements.add("ALTER TABLE ADT_AUDIT_TRAIL ADD DOMAINCONTEXT NUMBER");

            Arrays.stream(AuditDomainContextType.values()).forEach(
                    auditDomainContextType -> sqlStatements.add(
                            String.format("UPDATE ADT_AUDIT_TRAIL SET DOMAINCONTEXT = %s WHERE DOMAIN = '%s' AND CONTEXT = '%s'",
                                    auditDomainContextType.ordinal(), auditDomainContextType.domainType().name(), auditDomainContextType.name())
                    )
            );

            sqlStatements.add("ALTER TABLE ADT_AUDIT_TRAIL MODIFY DOMAINCONTEXT NOT NULL");
            sqlStatements.add("ALTER TABLE ADT_AUDIT_TRAIL DROP COLUMN DOMAIN");
            sqlStatements.add("ALTER TABLE ADT_AUDIT_TRAIL DROP COLUMN CONTEXT");
            sqlStatements.add("ALTER TABLE ADT_AUDIT_TRAIL DROP COLUMN PKCOLUMN");
            sqlStatements.add("ALTER TABLE ADT_AUDIT_TRAIL DROP COLUMN TABLENAME");
            for (String sqlStatement : sqlStatements) {
                try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
                    statement.executeUpdate();
                } catch (SQLException e) {
                    throw new UnderlyingSQLFailedException(e);
                }
            }


        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

}
