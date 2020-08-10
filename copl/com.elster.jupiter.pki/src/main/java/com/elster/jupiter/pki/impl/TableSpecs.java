package com.elster.jupiter.pki.impl;

import com.elster.jupiter.orm.*;
import com.elster.jupiter.pki.*;
import com.elster.jupiter.pki.impl.accessors.AbstractSecurityAccessorImpl;
import com.elster.jupiter.pki.impl.accessors.SecurityAccessorTypeImpl;
import com.elster.jupiter.pki.impl.accessors.UserActionRecord;
import com.elster.jupiter.pki.impl.wrappers.certificate.AbstractCertificateWrapperImpl;
import com.elster.jupiter.pki.impl.wrappers.keypair.KeypairWrapperImpl;
import com.elster.jupiter.users.UserDirectory;
import com.google.common.collect.Range;

import static com.elster.jupiter.orm.ColumnConversion.*;
import static com.elster.jupiter.orm.Table.SHORT_DESCRIPTION_LENGTH;
import static com.elster.jupiter.orm.Version.version;

public enum TableSpecs {
    PKI_KEYTYPES {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<KeyType> table = dataModel.addTable(this.name(), KeyType.class).since(Version.version(10,3));
            table.map(KeyTypeImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar().notNull().map(KeyTypeImpl.Fields.NAME.fieldName()).add();
            table.column("ALGORITHM").varChar().map(KeyTypeImpl.Fields.KEY_ALGORITHM.fieldName()).add();
            table.column("SIGNATURE").varChar().map(KeyTypeImpl.Fields.SIGNATURE_ALGORITHM.fieldName()).add();
            table.column("DESCRIPTION").varChar().map(KeyTypeImpl.Fields.DESCRIPTION.fieldName()).add();
            table.column("CURVE").varChar().map(KeyTypeImpl.Fields.CURVE.fieldName()).add();
            table.column("KEYSIZE").number().conversion(NUMBER2INT).map(KeyTypeImpl.Fields.KEY_SIZE.fieldName()).add();
            table.column("CRYPTOTYPE").number().map(KeyTypeImpl.Fields.CRYPTOGRAPHIC_TYPE.fieldName()).conversion(NUMBER2ENUM).add();
            table.column("KEYUSAGES").number().map(KeyTypeImpl.Fields.KEY_USAGES.fieldName()).conversion(NUMBER2LONG).add();
            table.column("EXTKEYUSAGES").number().map(KeyTypeImpl.Fields.EXTENDED_KEY_USAGES.fieldName()).conversion(NUMBER2LONG).add();
            table.column("PASSWORDLENGTH").number().conversion(NUMBER2INT).map(KeyTypeImpl.Fields.LENGTH.fieldName()).add();
            table.column("LOWERCASES").type("CHAR(1)").conversion(ColumnConversion.CHAR2BOOLEAN).map(KeyTypeImpl.Fields.LOWERCASE.fieldName()).add();
            table.column("UPPERCASES").type("CHAR(1)").conversion(ColumnConversion.CHAR2BOOLEAN).map(KeyTypeImpl.Fields.UPPERCASE.fieldName()).add();
            table.column("NUMBERS").type("CHAR(1)").conversion(ColumnConversion.CHAR2BOOLEAN).map(KeyTypeImpl.Fields.NUMBERS.fieldName()).add();
            table.column("SPECIALCHARS").type("CHAR(1)").conversion(ColumnConversion.CHAR2BOOLEAN).map(KeyTypeImpl.Fields.SPECIAL_CHARS.fieldName()).add();
            table.primaryKey("PK_PKI_KEYTYPE").on(id).add();
            table.unique("UK_PKI_KEYTYPE").on(name).add();
        }
    },
    PKI_TRUSTSTORE {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<TrustStore> table = dataModel.addTable(this.name(), TrustStore.class).since(Version.version(10,3));
            table.map(TrustStoreImpl.class);
            Column id = table.addAutoIdColumn();
            table.column("NAME")
                    .varChar()
                    .map(TrustStoreImpl.Fields.NAME.fieldName())
                    .add();
            table.column("DESCRIPTION")
                    .varChar()
                    .map(TrustStoreImpl.Fields.DESCRIPTION.fieldName())
                    .add();
            table.addAuditColumns();
            table.primaryKey("PK_PKI_TRUSTSTORE").on(id).add();
        }
    },
    PKI_CERTIFICATE {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<CertificateWrapper> table = dataModel.addTable(this.name(), CertificateWrapper.class)
                    .since(Version.version(10, 3));
            table.map(AbstractCertificateWrapperImpl.IMPLEMENTERS);
            Column id = table.addAutoIdColumn();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");
            table.addRefAnyColumns("PRIVATEKEY", false, AbstractCertificateWrapperImpl.Fields.PRIVATE_KEY.fieldName());
            table.column("ALIAS")
                    .varChar()
                    .map(AbstractCertificateWrapperImpl.Fields.ALIAS.fieldName())
                    .add();
            table.column("CERTIFICATE")
                    .type("blob")
                    .conversion(BLOB2BYTE)
                    .map(AbstractCertificateWrapperImpl.Fields.CERTIFICATE.fieldName())
                    .add();
            table.column("CRL")
                    .type("blob")
                    .conversion(BLOB2BYTE)
                    .map(AbstractCertificateWrapperImpl.Fields.CRL.fieldName())
                    .add();
            table.column("CSR")
                    .type("blob")
                    .conversion(BLOB2BYTE)
                    .map(AbstractCertificateWrapperImpl.Fields.CSR.fieldName())
                    .add();
            table.column("EXPIRATION")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(AbstractCertificateWrapperImpl.Fields.EXPIRATION.fieldName())
                    .add();
            table.column("LASTREADDATE")
                    .number()
                    .conversion(NUMBER2INSTANT)
                    .map(AbstractCertificateWrapperImpl.Fields.LAST_READ_DATE.fieldName())
                    .add();
            table.column("SUBJECT")
                    .varChar()
                    .map(AbstractCertificateWrapperImpl.Fields.SUBJECT.fieldName())
                    .since(Version.version(10, 4))
                    .add();
            table.column("ISSUER")
                    .varChar()
                    .map(AbstractCertificateWrapperImpl.Fields.ISSUER.fieldName())
                    .since(Version.version(10, 4))
                    .add();
            table.column("KEYUSAGESCSV")
                    .varChar()
                    .map(AbstractCertificateWrapperImpl.Fields.KEY_USAGES.fieldName())
                    .since(Version.version(10, 4))
                    .add();
            table.column("CA_NAME")
                    .varChar()
                    .map(AbstractCertificateWrapperImpl.Fields.CA_NAME.fieldName())
                    .since(Version.version(10, 4, 3))
                    .add();
            table.column("CA_PROFILE_NAME")
                    .varChar()
                    .map(AbstractCertificateWrapperImpl.Fields.CA_PROFILE_NAME.fieldName())
                    .since(Version.version(10, 4, 3))
                    .add();
            table.column("CA_END_ENTITY_NAME")
                    .varChar()
                    .map(AbstractCertificateWrapperImpl.Fields.CA_END_ENTITY_NAME.fieldName())
                    .since(Version.version(10, 4, 3))
                    .add();
            table.column("STATUS")
                    .varChar(Table.NAME_LENGTH)
                    .conversion(CHAR2ENUM)
                    .notNull()
                    .installValue("'" + CertificateWrapperStatus.NATIVE.name() + "'")
                    .map(AbstractCertificateWrapperImpl.Fields.WRAPPER_STATUS.fieldName())
                    .since(Version.version(10, 4, 1))
                    .add();
            Column trustStoreColumn = table.column("TRUSTSTORE")
                    .number()
                    .add();
            Column keyTypeColumn = table.column("KEYTYPE")
                    .number()
                    .add();
            table.addAuditColumns();
            table.addMessageAuthenticationCodeColumn(encrypter);

            table.primaryKey("PK_PKI_CERTIFICATE").on(id).add();
            table.foreignKey("PKI_FK_CERT_TS").on(trustStoreColumn)
                    .references(TrustStoreImpl.class)
//                    .composition() // Due to bug CXO-5905
                    .map(AbstractCertificateWrapperImpl.Fields.TRUST_STORE.fieldName())
                    .reverseMap(TrustStoreImpl.Fields.CERTIFICATES.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
            table.foreignKey("PKI_FK_CERT_KEYTYPE").on(keyTypeColumn)
                    .references(KeyTypeImpl.class)
//                    .composition() // Due to bug CXO-5905
                    .map(AbstractCertificateWrapperImpl.Fields.KEY_TYPE.fieldName())
                    .add();
        }
    },
    PKI_KEYPAIR {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<KeypairWrapper> table = dataModel.addTable(this.name(), KeypairWrapper.class)
                    .since(Version.version(10, 4));
            table.map(KeypairWrapperImpl.class);
            Column id = table.addAutoIdColumn();
            table.addRefAnyColumns("PRIVATEKEY", false, KeypairWrapperImpl.Fields.PRIVATE_KEY.fieldName());
            table.column("ALIAS")
                    .varChar()
                    .map(KeypairWrapperImpl.Fields.ALIAS.fieldName())
                    .add();
            table.column("PUBLICKEY")
                    .type("blob")
                    .conversion(BLOB2BYTE)
                    .map(KeypairWrapperImpl.Fields.PUBLIC_KEY.fieldName())
                    .add();
            table.column("EXPIRATION")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INSTANT)
                    .map(KeypairWrapperImpl.Fields.EXPIRATION.fieldName())
                    .add();
            Column keyTypeColumn = table.column("KEYTYPE")
                    .number()
                    .add();
            table.primaryKey("PK_PKI_KEYPAIR").on(id).add();
            table.foreignKey("PKI_FK_PUBKEY_KEYTYPE").on(keyTypeColumn)
                    .references(KeyTypeImpl.class)
//                    .composition() // Due to bug CXO-5905
                    .map(KeypairWrapperImpl.Fields.KEY_TYPE.fieldName())
                    .add();

        }
    },

    PKI_DIRECTORY_CERTIFICATE {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<DirectoryCertificateUsage> table = dataModel.addTable(this.name(), DirectoryCertificateUsage.class).since(Version.version(10, 4, 1));
            table.map(DirectoryCertificateUsageImpl.class);
            Column id = table.addAutoIdColumn();
            Column directory = table.column(DirectoryCertificateUsageImpl.Fields.DIRECTORY.name())
                    .number()
                    .conversion(ColumnConversion.NUMBER2LONG)
                    .notNull()
                    .add();
            Column certificate = table.column(DirectoryCertificateUsageImpl.Fields.CERTIFICATE.name())
                    .number()
                    .conversion(NUMBER2LONG)
                    .add();
            Column truststore = table.column(DirectoryCertificateUsageImpl.Fields.TRUSTSTORE.name())
                    .number()
                    .conversion(NUMBER2LONG)
                    .add();
            table.addAuditColumns();
            table.primaryKey("PKI_PK_DIRECTORY_CERT").on(id).add();
            table.foreignKey("PKI_FK_DIRECTORY_CERT_DIR")
                    .on(directory)
                    .references(UserDirectory.class)
                    .map(DirectoryCertificateUsageImpl.Fields.DIRECTORY.fieldName())
                    .add();
            table.foreignKey("PKI_FK_DIRECTORY_CERT_CERT")
                    .on(certificate)
                    .references(CertificateWrapper.class)
                    .map(DirectoryCertificateUsageImpl.Fields.CERTIFICATE.fieldName())
                    .add();
            table.foreignKey("PKI_FK_DIRECTORY_CERT_TSTORE")
                    .on(truststore)
                    .references(TrustStore.class)
                    .map(DirectoryCertificateUsageImpl.Fields.TRUSTSTORE.fieldName())
                    .add();

        }
    },

    PKI_SECACCESSORTYPE {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<SecurityAccessorType> table = dataModel.addTable(name(), SecurityAccessorType.class);
            table.map(SecurityAccessorTypeImpl.class);
            table.previouslyNamed(Range.lessThan(Version.version(10, 4)), Constants.PKI_SECACCESSORTYPE_TABLE_UP_TO_10_4);
            Column id = table.addAutoIdColumn();
            table.setJournalTableName(Constants.PKI_SECACCESSORTYPE_JOURNAL_TABLE);
            table.addAuditColumns();
            Column nameColumn = table.column(SecurityAccessorTypeImpl.Fields.NAME.name())
                    .varChar()
                    .notNull()
                    .map(SecurityAccessorTypeImpl.Fields.NAME.fieldName())
                    .since(Version.version(10, 3))
                    .add();
            Column deviceType = table.column("DEVICETYPEID")
                    .number()
                    .notNull()
                    .during(Range.closedOpen(Version.version(10, 3), Version.version(10, 4)))
                    .add();
            table.column("DESCRIPTION")
                    .varChar()
                    .map(SecurityAccessorTypeImpl.Fields.DESCRIPTION.fieldName())
                    .since(Version.version(10, 3))
                    .add();
            table.column("DURATION").number()
                    .conversion(NUMBER2INT)
                    .map(SecurityAccessorTypeImpl.Fields.DURATION.fieldName() + ".count")
                    .since(Version.version(10, 3))
                    .add();
            table.column("DURATIONCODE").number()
                    .conversion(NUMBER2INT)
                    .map(SecurityAccessorTypeImpl.Fields.DURATION.fieldName() + ".timeUnitCode")
                    .since(Version.version(10, 3))
                    .add();
            table.column("ENCRYPTION")
                    .varChar()
                    .map(SecurityAccessorTypeImpl.Fields.ENCRYPTIONMETHOD.fieldName())
                    .since(Version.version(10, 3))
                    .add();
            table.column("HSM_JSS_KEY_TYPE")
                    .varChar(30)
                    .conversion(CHAR2ENUM)
                    .map(SecurityAccessorTypeImpl.Fields.HSM_JSS_KEY_TYPE.fieldName())
                    .since(version(10,4,3))
                    .add();
            table.column("LABEL")
                    .varChar(SHORT_DESCRIPTION_LENGTH)
                    .map(SecurityAccessorTypeImpl.Fields.LABEL.fieldName())
                    .since(version(10,4,3))
                    .add();
            table.column("IMPORT_CAPABILITY")
                    .varChar(30)
                    .conversion(CHAR2ENUM)
                    .map(SecurityAccessorTypeImpl.Fields.IMPORT_CAPABILITY.fieldName())
                    .since(version(10,4,3))
                    .add();
            table.column("RENEW_CAPABILITY")
                    .varChar(30)
                    .conversion(CHAR2ENUM)
                    .map(SecurityAccessorTypeImpl.Fields.RENEW_CAPABILITY.fieldName())
                    .since(version(10,4,3))
                    .add();
            table.column("KEY_SIZE")
                    .number()
                    .conversion(ColumnConversion.NUMBER2INT)
                    .map(SecurityAccessorTypeImpl.Fields.KEY_SIZE.fieldName())
                    .since(version(10,4,3))
                    .add();
            table.column("REVERSIBLE")
                    .type("char(1)").conversion(CHAR2BOOLEAN).installValue("'N'")
                    .map(SecurityAccessorTypeImpl.Fields.REVERSIBLE.fieldName())
                    .since(version(10,4,4))
                    .add();
            table.column("ISWRAPPER")
                    .type("char(1)").conversion(CHAR2BOOLEAN).installValue("'N'")
                    .map(SecurityAccessorTypeImpl.Fields.ISWRAPPER.fieldName())
                    .since(version(10, 7))
                    .add();
            Column keytypeid = table.column("KEYTYPEID")
                    .number()
                    .notNull()
                    .since(Version.version(10, 3))
                    .add();
            table.column("KEYPURPOSE")
                    .varChar(30)
                    .conversion(ColumnConversion.CHAR2ENUM)
                    .map(SecurityAccessorTypeImpl.Fields.KEYPURPOSE.fieldName())
                    .add();
            Column trustStoreId = table.column("TRUSTSTOREID")
                    .number()
                    .since(Version.version(10, 3))
                    .add();
            table.column(SecurityAccessorTypeImpl.Fields.MANAGED_CENTRALLY.name())
                    .bool()
                    .map(SecurityAccessorTypeImpl.Fields.MANAGED_CENTRALLY.fieldName())
                    .installValue("'N'")
                    .since(Version.version(10, 4))
                    .add();
            table.column(SecurityAccessorTypeImpl.Fields.PURPOSE.name())
                    .varChar(30)
                    .conversion(CHAR2ENUM)
                    .map(SecurityAccessorTypeImpl.Fields.PURPOSE.fieldName())
                    .notNull()
                    .since(Version.version(10, 4, 1))
                    .installValue("'" + SecurityAccessorType.Purpose.DEVICE_OPERATIONS.name() + "'")
                    .add();

            table.foreignKey("FK_DTC_KEYACCESSOR_DEVTYPE")
                    .on(deviceType)
                    // need to reference some existent table here to pass orm checks,
                    // even if this constraint is obsolete
                    .references(name())
                    .map("deviceType")
                    .composition()
                    .during(Range.closedOpen(Version.version(10, 3), Version.version(10, 4)))
                    .add();
            ForeignKeyConstraint oldKeyTypeConstraint = table.foreignKey("FK_DTC_KEYACCCESSOR_KEYTYPE")
                    .on(keytypeid)
                    .references(KeyType.class)
                    .map(SecurityAccessorTypeImpl.Fields.KEYTYPE.fieldName())
                    .upTo(Version.version(10, 4))
                    .add();
            table.foreignKey("FK_PKI_SECACCESSOR_KEYTYPE")
                    .on(keytypeid)
                    .references(KeyType.class)
                    .map(SecurityAccessorTypeImpl.Fields.KEYTYPE.fieldName())
                    .since(Version.version(10, 4))
                    .previously(oldKeyTypeConstraint)
                    .add();
            ForeignKeyConstraint oldTrustStoreConstraint = table.foreignKey("FK_DTC_KEYACCCESSOR_TRUSTSTORE")
                    .on(trustStoreId)
                    .references(TrustStore.class)
                    .map(SecurityAccessorTypeImpl.Fields.TRUSTSTORE.fieldName())
                    .upTo(Version.version(10, 4))
                    .add();
            table.foreignKey("FK_PKI_SECACCESSOR_TRUSTSTORE")
                    .on(trustStoreId)
                    .references(TrustStore.class)
                    .map(SecurityAccessorTypeImpl.Fields.TRUSTSTORE.fieldName())
                    .since(Version.version(10, 4))
                    .previously(oldTrustStoreConstraint)
                    .add();
            PrimaryKeyConstraint oldPrimaryKey = table.primaryKey("PK_DTC_KEYACCESSOR")
                    .on(id)
                    .upTo(Version.version(10, 4))
                    .add();
            table.primaryKey("PK_PKI_SECACCESSORTYPE")
                    .on(id)
                    .since(Version.version(10, 4))
                    .previously(oldPrimaryKey)
                    .add();
            table.unique("UK_PKI_SECACCESSORNAME").on(nameColumn).since(version(10, 4)).add();
            table.cacheWholeTable(true);
        }
    },

    PKI_SECACCTYPEUSRACTN {
        @Override
        public void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<UserActionRecord> table = dataModel.addTable(name(), UserActionRecord.class).since(version(10, 3));
            table.map(UserActionRecord.class);
            table.previouslyNamed(Range.lessThan(Version.version(10, 4)), "DTC_KEYACCTYPEUSRACTN");
            Column userAction = table.column("USERACTION")
                    .number()
                    .conversion(NUMBER2ENUM)
                    .notNull()
                    .map("userAction")
                    .add();
            Column keyAccessorType = table.column("KEYACCESSORTYPE")
                    .number()
                    .notNull()
                    .add();
            table.setJournalTableName(Constants.PKI_SECACCTYPEUSRACTN_JOURNAL_TABLE);
            table.addAuditColumns();
            ForeignKeyConstraint oldAccessorTypeConstraint = table.foreignKey("FK_DTC_KEYACCTYPE_USRACTN")
                    .on(keyAccessorType)
                    .references(SecurityAccessorType.class)
                    .reverseMap("userActionRecords")
                    .composition()
                    .map("keyAccessorType")
                    .upTo(Version.version(10, 4))
                    .add();
            table.foreignKey("FK_PKI_SECACCTYPE_USRACTN")
                    .on(keyAccessorType)
                    .references(SecurityAccessorType.class)
                    .reverseMap("userActionRecords")
                    .composition()
                    .map("keyAccessorType")
                    .since(Version.version(10, 4))
                    .previously(oldAccessorTypeConstraint)
                    .add();
            PrimaryKeyConstraint oldPrimaryKey = table.primaryKey("PK_DTC_KEYACCTYPEUSRACTN")
                    .on(userAction, keyAccessorType)
                    .upTo(Version.version(10, 4))
                    .add();
            table.primaryKey("PK_PKI_SECACCTYPEUSRACTN")
                    .on(userAction, keyAccessorType)
                    .since(Version.version(10, 4))
                    .previously(oldPrimaryKey)
                    .add();
            table.cacheWholeTable(true);
        }
    },

    PKI_SECACCESSOR {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<SecurityAccessor> table = dataModel.addTable(name(), SecurityAccessor.class).since(version(10, 4));
            table.map(AbstractSecurityAccessorImpl.IMPLEMENTERS);
            Column keyAccessorType = table.column("SECACCESSORTYPE").number().notNull().add();
            table.addDiscriminatorColumn("DISCRIMINATOR", "char(1)");

            Column actualCertificate = table.column("ACTUAL_CERT").number().add();
            Column tempCertificate = table.column("TEMP_CERT").number().add();
            table.column("SWAPPED")
                    .bool()
                    .map(AbstractSecurityAccessorImpl.Fields.SWAPPED.fieldName())
                    .add();
            table.addAuditColumns();

            table.primaryKey("PK_PKI_SECACCESSOR").on(keyAccessorType).add();
            table.foreignKey("FK_PKI_SECACC_2_TYPE")
                    .on(keyAccessorType)
                    .references(SecurityAccessorType.class)
                    .map(AbstractSecurityAccessorImpl.Fields.KEY_ACCESSOR_TYPE.fieldName())
                    .add();
            table.foreignKey("FK_PKI_SECACC_2_ACT_CERT")
                    .on(actualCertificate)
                    .references(CertificateWrapper.class)
                    .map(AbstractSecurityAccessorImpl.Fields.CERTIFICATE_WRAPPER_ACTUAL.fieldName())
                    .add();
            table.foreignKey("FK_PKI_SECACC_2_TEMP_CERT")
                    .on(tempCertificate)
                    .references(CertificateWrapper.class)
                    .map(AbstractSecurityAccessorImpl.Fields.CERTIFICATE_WRAPPER_TEMP.fieldName())
                    .add();
        }
    }
    ;

    abstract void addTo(DataModel component, Encrypter encrypter);

    public interface Constants {
        String PKI_SECACCESSORTYPE_TABLE_UP_TO_10_4 = "DTC_KEYACCESSORTYPE";
        String PKI_SECACCESSORTYPE_JOURNAL_TABLE_UP_TO_10_4 = "DTC_KEYACCESSORTYPEJRNL";
        String PKI_SECACCESSORTYPE_JOURNAL_TABLE = "PKI_SECACCESSORTYPEJRNL";
        String PKI_SECACCTYPEUSRACTN_JOURNAL_TABLE_UP_TO_10_4 = "DTC_KEYACCTYPE_USRACTNJRNL";
        String PKI_SECACCTYPEUSRACTN_JOURNAL_TABLE = "PKI_SECACCTYPEUSRACTNJRNL";
    }
}
