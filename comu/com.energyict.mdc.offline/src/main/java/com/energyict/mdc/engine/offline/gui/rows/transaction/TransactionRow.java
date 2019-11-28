/*
 * TransactionRow.java
 *
 * Created on 15 oktober 2003, 17:02
 */

package com.energyict.mdc.engine.offline.gui.rows.transaction;

import com.energyict.mdc.engine.offline.core.TaskTransaction;
import com.energyict.mdc.engine.offline.gui.rows.AbstractRowData;

import java.util.Date;

/**
 * @author Koen
 */
public class TransactionRow extends AbstractRowData {

    //Necessary fields for reflection
    TaskTransaction transaction;
    int transactionRowDeviceId;
    String transactionRowTaskResult;
    Date transactionRowDate;
    String transactionRowTaskState;
    String transactionRowRtuName;
    String transactionRowAttributeDeviceId;

    /**
     * Creates a new instance of TransactionRow
     */
    public TransactionRow() {
    }

    public TransactionRow(TaskTransaction transaction) {
        this.transaction = transaction;
    }

    protected String[] getSelectedColumnProperties() {
        return new String[]{
                "transactionRowDeviceId",
                "transactionRowRtuName",
                "transactionRowDate",
                "transactionRowTaskState",
                "transactionRowTaskResult"
        };
    }

    protected String[] getSelectedColumnTranslationKeys() {
        return new String[]{
                "transactionRowDeviceId",
                "deviceSerialNumber",
                "transactionRowDate",
                "transactionRowTaskState",
                "transactionRowTaskResult"
        };
    }


    protected Object[] getSelectedColumnWidthObjects() {
        return new Object[]{
            "/XYZ012345678901234.012345678",
            ">140",
            ">120",
            "=100",
            "=100"
        };
    }

    /**
     * Getter for property taskResult.
     *
     * @return Value of property taskResult.
     */
    public String getTransactionRowTaskResult() {
        return transaction.getTaskResult().name();
    }

    /**
     * Getter for property date.
     *
     * @return Value of property date.
     */
    public Date getTransactionRowDate() {
        return transaction.getDate();
    }

    /**
     * Getter for property taskState.
     *
     * @return Value of property taskState.
     */
    public String getTransactionRowTaskState() {
        return transaction.getTaskState().name();
    }

    /**
     * Getter for property transactionRowRtuName.
     *
     * @return Value of property transactionRowRtuName.
     */
    public String getTransactionRowRtuName() {
        return transaction.getRtuName();
    }


    /**
     * Getter for property transactionRowDeviceId.
     *
     * @return Value of property transactionRowDeviceId.
     */
    public long getTransactionRowDeviceId() {
        return transaction.getDeviceId();
    }

    /**
     * Getter for property transactionRowAttributeDeviceId.
     *
     * @return Value of property transactionRowAttributeDeviceId.
     */
    public String getTransactionRowAttributeDeviceId() {
        return transaction.getAttributeDeviceId();
    }
}
