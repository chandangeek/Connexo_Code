package com.elster.jupiter.pki.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.RequestableCertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessorUserAction;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustedCertificate;
import com.elster.jupiter.pki.impl.accessors.SecurityAccessorTypeImpl;
import com.elster.jupiter.pki.impl.wrappers.PkiLocalizedException;
import com.elster.jupiter.pki.security.Privileges;
import com.elster.jupiter.upgrade.Upgrader;

import javax.inject.Inject;
import java.security.cert.CertificateParsingException;
import java.util.Arrays;
import java.util.stream.Collectors;

@LiteralSql
public class UpgraderV10_4 implements Upgrader {
    private final SecurityManagementService securityManagementService;
    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final EventService eventService;
    private final PrivilegesProviderV10_4 privilegesProviderV10_4;

    @Inject
    UpgraderV10_4(DataModel dataModel,
                  SecurityManagementService securityManagementService,
                  Thesaurus thesaurus,
                  EventService eventService,
                  PrivilegesProviderV10_4 privilegesProviderV10_4) {
        super();
        this.securityManagementService = securityManagementService;
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.eventService = eventService;
        this.privilegesProviderV10_4 = privilegesProviderV10_4;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        renameSecurityAccessorTypes();
        renameJournalTables();

        dataModelUpgrader.upgrade(dataModel, Version.version(10, 4));

        updateCertificates();
        updateTrustedCertificates();
        moveSecurityAccessorPrivileges();
        privilegesProviderV10_4.install();
        installNewEventTypes();
    }

    private void renameJournalTables() {
        execute(dataModel,
                renameTableSql(
                        TableSpecs.Constants.PKI_SECACCESSORTYPE_JOURNAL_TABLE_UP_TO_10_4,
                        TableSpecs.Constants.PKI_SECACCESSORTYPE_JOURNAL_TABLE),
                renameTableSql(
                        TableSpecs.Constants.PKI_SECACCTYPEUSRACTN_JOURNAL_TABLE_UP_TO_10_4,
                        TableSpecs.Constants.PKI_SECACCTYPEUSRACTN_JOURNAL_TABLE)
        );
    }

    private static String renameTableSql(String oldName, String newName) {
        return "alter table " + oldName + " rename to " + newName;
    }

    private void updateCertificates() {
        securityManagementService.findAllCertificates().find().forEach(x -> {
                    RequestableCertificateWrapper csr = (RequestableCertificateWrapper) x;
                    if (csr.hasCSR()) {
                        csr.getCSR().ifPresent(cert -> csr.setSubject(cert.getSubject().toString()));
                    } else {
                        try {
                            csr.getCertificate().ifPresent(cert -> csr.setSubject(cert.getSubjectDN().toString()));
                            csr.getCertificate().ifPresent(cert -> csr.setIssuer(cert.getIssuerDN().toString()));
                            csr.setKeyUsagesCsv(csr.stringifyKeyUsages(csr.getKeyUsages(), csr.getExtendedKeyUsages()));
                            csr.save();
                        } catch (CertificateParsingException e) {
                            throw new PkiLocalizedException(thesaurus, MessageSeeds.COULD_NOT_READ_KEY_USAGES);
                        }
                    }
                });
    }

    private void updateTrustedCertificates() {
        securityManagementService.getAllTrustStores().forEach(x -> {
                    for (TrustedCertificate trustedCertificate : x.getCertificates()) {
                        try {
                            trustedCertificate.getCertificate().ifPresent(cert -> trustedCertificate.setSubject(cert.getSubjectDN().toString()));
                            trustedCertificate.getCertificate().ifPresent(cert -> trustedCertificate.setIssuer(cert.getIssuerDN().toString()));
                            trustedCertificate.setKeyUsagesCsv(trustedCertificate.stringifyKeyUsages(trustedCertificate.getKeyUsages(), trustedCertificate.getExtendedKeyUsages()));
                            trustedCertificate.save();
                        } catch (CertificateParsingException e) {
                            throw new PkiLocalizedException(thesaurus, MessageSeeds.COULD_NOT_READ_KEY_USAGES);
                        }
                    }
                });
    }

    private void moveSecurityAccessorPrivileges() {
        String oldValues = Arrays.stream(SecurityAccessorUserAction.values())
                .map(SecurityAccessorUserAction::getPrivilege)
                .map(privilege -> privilege.replaceFirst("\\.", ".device."))
                .map(privilege -> "'" + privilege + "'")
                .collect(Collectors.joining(", ", "(", ")"));
        execute(dataModel,
                "update USR_RESOURCE" +
                        " set NAME = '" + Privileges.RESOURCE_SECURITY_ACCESSOR_ATTRIBUTES.getKey() + "'," +
                        " DESCRIPTION = '" + Privileges.RESOURCE_SECURITY_ACCESSOR_ATTRIBUTES_DESCRIPTION.getKey() + "'," +
                        " COMPONENT = '" + SecurityManagementService.COMPONENTNAME + "'" +
                        " where NAME = 'deviceSecurity.deviceSecurities'",
                "insert into USR_PRIVILEGE (NAME, DISCRIMINATOR, RESOURCEID, CATEGORY)" +
                        " select REPLACE(NAME, '.device', ''), DISCRIMINATOR, RESOURCEID, CATEGORY" +
                        " from USR_PRIVILEGE" +
                        " where NAME in " + oldValues,
                "update USR_PRIVILEGEINGROUP" +
                        " set PRIVILEGENAME = REPLACE(PRIVILEGENAME, '.device', '')" +
                        " where PRIVILEGENAME in " + oldValues,
                "update USR_PRIVILEGEINGROUPJRNL" +
                        " set PRIVILEGENAME = REPLACE(PRIVILEGENAME, '.device', '')" +
                        " where PRIVILEGENAME in " + oldValues,
                "delete from USR_PRIVILEGE" +
                        " where NAME in " + oldValues
        );
    }

    private void renameSecurityAccessorTypes() {
        execute(dataModel,
                "merge into " + TableSpecs.Constants.PKI_SECACCESSORTYPE_TABLE_UP_TO_10_4 + " sa" +
                        " using DTC_DEVICETYPE dt" +
                        " on (sa.DEVICETYPEID = dt.ID)" +
                        " when matched then update" +
                        " set sa." + SecurityAccessorTypeImpl.Fields.NAME.name() +
                        " = sa." + SecurityAccessorTypeImpl.Fields.NAME.name() + "||'_'||dt.NAME",
                "merge into " + TableSpecs.Constants.PKI_SECACCESSORTYPE_JOURNAL_TABLE_UP_TO_10_4 + " saj" +
                        " using DTC_DEVICETYPE dt" +
                        " on (saj.DEVICETYPEID = dt.ID)" +
                        " when matched then update" +
                        " set saj." + SecurityAccessorTypeImpl.Fields.NAME.name() +
                        " = saj." + SecurityAccessorTypeImpl.Fields.NAME.name() + "||'_'||dt.NAME"
        );
    }

    private void installNewEventTypes() {
        EventType.SECURITY_ACCESSOR_TYPE_VALIDATE_DELETE.createIfNotExists(eventService);
        EventType.SECURITY_ACCESSOR_TYPE_DELETED.createIfNotExists(eventService);
    }
}
