package com.energyict.mdc.gogo;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.security.Principal;
import java.sql.*;
import java.util.Optional;

/**
 * Gogo that can be used to replace all values in DEVICEMESSAGEID column of DTC_MESSAGEENABLEMENT Table
 * by the MessageID.dbValue()
 * Date: 18/08/2015
 * Time: 16:24
 */
@Component(name = "com.energyict.mdc.gogo.DeviceMessageEnablementMigration", service = DeviceMessageEnablementMigration.class,
           property = {"usage=If you need to migrate the Device Message Enablement (Replacing ordinal by DeviceMessageId:dbValue().",
                    "osgi.command.scope=mdc.deviceMessageEnablement",
                    "osgi.command.function=migrate"},
        immediate = true)
@SuppressWarnings("unused")
public class DeviceMessageEnablementMigration implements Migrator {

    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;
    private volatile OrmService ormService;

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void migrate() {
        executeTransaction(() -> {
            Optional<? extends DataModel> dataModel = this.ormService.getDataModel("DTC");
            if (dataModel.isPresent()) {
                try (Connection connection = dataModel.get().getConnection(false);
                     final PreparedStatement statement = connection.prepareStatement(getUpdateClause())) {
                     dataModel.get().mapper(DeviceMessageEnablement.class).find()
                            .forEach(enablement -> updateRecord(statement, enablement));
                     statement.executeBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return null;
        });
    }

    private <T> T executeTransaction(Transaction<T> transaction) {
        setPrincipal();
        try {
            return transactionService.execute(transaction);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            clearPrincipal();
        }
    }

    private void setPrincipal() {
        threadPrincipalService.set(getPrincipal());
    }

    private void clearPrincipal() {
        threadPrincipalService.clear();
    }

    private Principal getPrincipal() {
        return this.userService.findUser("admin").get();
    }

    private String getUpdateClause() {
        return "UPDATE DTC_MESSAGEENABLEMENT SET DEVICEMESSAGEID = ? WHERE ID = ? AND DEVICEMESSAGEID = ?";
    }

    private void updateRecord(PreparedStatement statement, DeviceMessageEnablement enablement){
        int ordinal = (int) enablement.getDeviceMessageDbValue();
        DeviceMessageId deviceMessageId = DeviceMessageId.values()[ordinal];
        try {
            statement.setLong(1, deviceMessageId.dbValue());
            statement.setLong(2, enablement.getId());
            statement.setLong(3, ordinal);
            statement.addBatch();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
}
