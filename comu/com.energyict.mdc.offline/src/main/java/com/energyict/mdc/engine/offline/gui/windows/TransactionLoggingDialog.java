/*
 * TransactionsLogDialog.java
 *
 * Created on 15 oktober 2003, 16:57
 */

package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.offline.core.TaskTransaction;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.rows.transaction.TransactionRow;
import com.energyict.mdc.engine.offline.gui.table.TableCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class TransactionLoggingDialog extends ComServerMobileDialog {

    private JButton refreshBtn;
    private JPanel buttonPnl;
    private JScrollPane centerScrollPane;

    private TableCreator tableCreator;

    public TransactionLoggingDialog(OfflineFrame mainFrame, String title, ComServerMobileDialogSettings settings) {
        super(mainFrame, false, settings);
        initializePnl(title);
    }

    private void initializePnl(String title) {
        initComponents(title);
        initTable();
        updateTable();
    }

    private void initComponents(String title) {
        setTitle(title);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel alignRightPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        alignRightPnl.add(getButtonPanel());
        getContentPane().add(getCenterScrollPane(), BorderLayout.CENTER);
        getContentPane().add(alignRightPnl, BorderLayout.SOUTH);
    }

    private JPanel getButtonPanel() {
        if (this.buttonPnl == null) {
            this.buttonPnl = new JPanel();
            this.buttonPnl.add(getRefreshButton());
        }
        return this.buttonPnl;
    }

    private JButton getRefreshButton() {
        if (this.refreshBtn == null) {
            this.refreshBtn = new JButton();
            this.refreshBtn.setText(UiHelper.translate("buttonrefresh"));
            this.refreshBtn.setMnemonic(KeyEvent.VK_R);
            this.refreshBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    refreshBtnActionPerformed(evt);
                }
            });
        }
        return this.refreshBtn;
    }

    private JScrollPane getCenterScrollPane() {
        if (this.centerScrollPane == null) {
            this.centerScrollPane = new JScrollPane();
        }
        return this.centerScrollPane;
    }

    private void initTable() {
        tableCreator = new TableCreator(TransactionRow.class, false, this);
    }

    private void updateTable() {
        List<TransactionRow> transactionRows = new ArrayList();
        List transactions = TaskTransaction.load(TaskTransaction.class);
        for (Object transaction : transactions) {
            TaskTransaction tt = (TaskTransaction) transaction;
            transactionRows.add(new TransactionRow(tt));
        }
        tableCreator.setRows(transactionRows);
        centerScrollPane.setViewportView(tableCreator.getJTable());
    }


    private void refreshBtnActionPerformed(ActionEvent evt) {
        updateTable();
    }
}