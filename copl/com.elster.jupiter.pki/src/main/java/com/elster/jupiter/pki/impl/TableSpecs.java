package com.elster.jupiter.pki.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DeleteRule;
import com.elster.jupiter.orm.Encrypter;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.impl.wrappers.certificate.AbstractCertificateWrapperImpl;

import static com.elster.jupiter.orm.ColumnConversion.BLOB2BYTE;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2ENUM;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2INT;
import static com.elster.jupiter.orm.ColumnConversion.NUMBER2LONG;

public enum TableSpecs {
    PKI_KEYTYPES {
        @Override
        void addTo(DataModel dataModel, Encrypter encrypter) {
            Table<KeyType> table = dataModel.addTable(this.name(), KeyType.class).since(Version.version(10,3));
            table.map(KeyTypeImpl.class);
            Column id = table.addAutoIdColumn();
            Column name = table.column("NAME").varChar().notNull().map(KeyTypeImpl.Fields.NAME.fieldName()).add();
            table.column("ALGORITHM").varChar().map(KeyTypeImpl.Fields.ALGORITHM.fieldName()).add();
            table.column("DESCRIPTION").varChar().map(KeyTypeImpl.Fields.DESCRIPTION.fieldName()).add();
            table.column("CURVE").varChar().map(KeyTypeImpl.Fields.CURVE.fieldName()).add();
            table.column("KEYSIZE").number().conversion(NUMBER2INT).map(KeyTypeImpl.Fields.KEY_SIZE.fieldName()).add();
            table.column("CRYPTOTYPE").number().map(KeyTypeImpl.Fields.CRYPTOGRAPHIC_TYPE.fieldName()).conversion(NUMBER2ENUM).add();
            table.column("KEYUSAGES").number().map(KeyTypeImpl.Fields.KEY_USAGES.fieldName()).conversion(NUMBER2LONG).add();
            table.column("EXTKEYUSAGES").number().map(KeyTypeImpl.Fields.EXTENDED_KEY_USAGES.fieldName()).conversion(NUMBER2LONG).add();
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
            table.addRefAnyColumns("PRIVATEKEY", true, AbstractCertificateWrapperImpl.Fields.PRIVATE_KEY.fieldName());
            table.column("ALIAS")
                    .varChar()
                    .notNull()
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
            Column trustStoreColumn = table.column("TRUSTSTORE")
                    .number()
                    .add();
            table.addMessageAuthenticationCodeColumn(encrypter);

            table.primaryKey("PK_PKI_CERTIFICATE").on(id).add();
            table.foreignKey("PKI_FK_CERT_TS").on(trustStoreColumn)
                    .references(TrustStoreImpl.class)
//                    .composition() // Due to bug CXO-5905
                    .map(AbstractCertificateWrapperImpl.Fields.TRUST_STORE.fieldName())
                    .reverseMap(TrustStoreImpl.Fields.CERTIFICATES.fieldName())
                    .onDelete(DeleteRule.CASCADE)
                    .add();
        }
    }
    ;

    abstract void addTo(DataModel component, Encrypter encrypter);

}
