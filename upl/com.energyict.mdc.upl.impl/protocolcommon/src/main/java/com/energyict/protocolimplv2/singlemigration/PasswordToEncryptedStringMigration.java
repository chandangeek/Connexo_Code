package com.energyict.protocolimplv2.singlemigration;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.cpo.Transaction;
import com.energyict.mdw.core.DataVault;
import com.energyict.mdw.core.DataVaultProvider;
import com.energyict.mdw.core.TransactionExecutor;
import com.energyict.mdw.core.TransactionExecutorProvider;
import com.energyict.util.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 28/05/13
 * Time: 8:45
 */
public class PasswordToEncryptedStringMigration {

    private static List<Pair<String, String>> statements = new ArrayList<>();

    static {
        statements.add(new Pair<>("select id, password from drudlmssecurity", "update drudlmssecurity set password = ? where id = ?"));
        statements.add(new Pair<>("select id, password from druAnsiC12Security", "update druAnsiC12Security set password = ? where id = ?"));
        statements.add(new Pair<>("select id, password from druExtendedAnsiC12Security", "update druExtendedAnsiC12Security set password = ? where id = ?"));
        statements.add(new Pair<>("select id, password from druIEC1107Security", "update druIEC1107Security set password = ? where id = ?"));
        statements.add(new Pair<>("select id, password from druNoOrPasswordSecurity", "update druNoOrPasswordSecurity set password = ? where id = ?"));
        statements.add(new Pair<>("select id, password from druSimplePassword", "update druSimplePassword set password = ? where id = ?"));
        statements.add(new Pair<>("select id, password from druWavenisSecurity", "update druWavenisSecurity set password = ? where id = ?"));
        statements.add(new Pair<>("select id, encryptionkey from drudlmssecurity", "update drudlmssecurity set encryptionkey = ? where id = ?"));
        statements.add(new Pair<>("select id, encryptionkey from druExtendedAnsiC12Security", "update druExtendedAnsiC12Security set encryptionkey = ? where id = ?"));
        statements.add(new Pair<>("select id, encryptionkey from druWavenisSecurity", "update druWavenisSecurity set encryptionkey = ? where id = ?"));
        statements.add(new Pair<>("select id, authenticationkey from drudlmssecurity", "update drudlmssecurity set authenticationkey = ? where id = ?"));
    }

    private final Logger logger;

    public PasswordToEncryptedStringMigration(Logger logger) {
        this.logger = logger;
    }

    public void migrate() throws SQLException, BusinessException {
        final TransactionExecutor transactionExecutor = TransactionExecutorProvider.instance.get().getTransactionExecutor();
        for (final Pair<String, String> statement : statements) {
            transactionExecutor.execute(new Transaction<Void>() {
                @Override
                public Void doExecute() throws BusinessException, SQLException {
                    try {
                        doMigrate(statement.getFirst(), statement.getLast());
                    } catch (SQLException e) {
                        if (!e.getMessage().contains("ORA-00942")) {    // ORA-00942 -> if the table doesn't exist
                            throw e;
                        } else {
                            logger.info(String.format("Security table %s does not exist.", getTableName(statement.getFirst())));
                        }
                    }
                    return null;
                }
            });

        }
        transactionExecutor.execute(new Transaction<Void>() {
            @Override
            public Void doExecute() throws BusinessException, SQLException {
                try (PreparedStatement updateStatement = Environment.getDefault().getConnection().prepareStatement("update eisrelationattributetype set valuefactory = ? where valuefactory = ?")) {
                    updateStatement.setString(1, "com.energyict.dynamicattributes.EncryptedStringFactory");
                    updateStatement.setString(2, "com.energyict.dynamicattributes.PasswordFactory");
                    updateStatement.executeUpdate();
                }
                return null;
            }
        });
    }

    private void doMigrate(String selectSql, String updateSql) throws SQLException {
        List<PasswordIdObject> passwords = new ArrayList<>();
        try (Statement statement = Environment.getDefault().getConnection().createStatement()) {
            final ResultSet resultSet = statement.executeQuery(selectSql);
            while (resultSet.next()) {
                PasswordIdObject passwordIdObject = new PasswordIdObject();
                passwordIdObject.setId(resultSet.getInt(1));
                passwordIdObject.setPassword(resultSet.getString(2));
                passwords.add(passwordIdObject);
            }
        }

        if (needsMigration(passwords)) {
            for (PasswordIdObject password : passwords) {
                if (password.getPassword() != null) {
                    final DataVault keyVault = getPasswordVault();
                    final String encrypt = keyVault.encrypt(password.getPassword().getBytes());
                    try (PreparedStatement updateStatement = Environment.getDefault().getConnection().prepareStatement(updateSql)) {
                        updateStatement.setString(1, encrypt);
                        updateStatement.setInt(2, password.getId());
                        updateStatement.executeUpdate();
                    }
                    logger.info(String.format("Migrated %s to %s", password.getPassword(), encrypt));
                } else {
                    logger.info("No migration required for object with Id " + password.getId());
                }
            }
        } else {
            logger.info(String.format("No migration required for '%s' of table '%s'", getColumn(selectSql), getTableName(selectSql)));
        }

    }

    private String getTableName(String statement) {
        return statement.substring(getEndIndexColumn(statement) + "from".length() + 1);
    }

    private String getColumn(String selectSql) {
        return selectSql.substring(getStartIndexColumn(selectSql), getEndIndexColumn(selectSql));
    }

    private int getEndIndexColumn(String selectSql) {
        return selectSql.indexOf("from");
    }

    private int getStartIndexColumn(String selectSql) {
        return selectSql.indexOf("select id, ") + "select id, ".length();
    }

    private DataVault getPasswordVault() {
        return DataVaultProvider.instance.get().getKeyVault();
    }

    private boolean needsMigration(List<PasswordIdObject> passwords) {
        final DataVault passwordVault = getPasswordVault();
        for (PasswordIdObject password : passwords) {
            try {
                passwordVault.decrypt(password.getPassword());
            } catch (Exception e) {
                /* If any decryption fails, then we must migrate */
                return true;
            }
        }
        return false;
    }

    private class PasswordIdObject {

        private int id;
        private String password;

        private int getId() {
            return id;
        }

        private void setId(int id) {
            this.id = id;
        }

        private String getPassword() {
            return password;
        }

        private void setPassword(String password) {
            this.password = password;
        }
    }

}
