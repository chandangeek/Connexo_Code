/*
 * ToolsTransactionLogAction.java
 *
 * Created on 15 oktober 2003, 16:52
 */

package com.energyict.mdc.engine.offline.gui.actions;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.windows.ComServerMobileDialogSettings;
import com.energyict.mdc.engine.offline.gui.windows.TransactionLoggingDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Koen
 */
public class ToolsTransactionLogAction extends AbstractAction {

    JCheckBoxMenuItem checkBoxMenuItem;
    TransactionLoggingDialog transactionLoggingDialog;

    /**
     * Creates a new instance of ToolsProtocolLogfileAction
     */
    public ToolsTransactionLogAction(OfflineFrame mainFrame) {
        putValue(Action.NAME, TranslatorProvider.instance.get().getTranslator().getTranslation("toolstransactionlogfile"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
        putValue(ActionKeys.MAIN_FRAME, mainFrame);

        ComServerMobileDialogSettings settings = new ComServerMobileDialogSettings("transactionslogdialog", 700, 350);
        // Make sure the width & height falls inside the screen boundaries
        Dimension fullScreenSize = mainFrame.getToolkit().getScreenSize();
        Dimension maxSize = new Dimension((int) (fullScreenSize.getWidth() * 0.90), (int) (fullScreenSize.getHeight() * 0.90));
        settings.setMaxWidth(maxSize.width);
        settings.setMaxHeight(maxSize.height);

        initTransactionLoggingDialog(mainFrame, UiHelper.translate("toolstransactionlogfile"), settings);
    }

    private void initTransactionLoggingDialog(OfflineFrame mainFrame, String title, ComServerMobileDialogSettings settings) {
        this.transactionLoggingDialog = new TransactionLoggingDialog(mainFrame, title, settings);
        this.transactionLoggingDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                getCheckBoxMenuItem().setSelected(false);
            }
        });

    }

    public void actionPerformed(ActionEvent ev) {
        getComServerMobileMainFrame().startWaitCursor();
        checkBoxMenuItem = (JCheckBoxMenuItem) ev.getSource();
        getTransactionLoggingDialog().setVisible(checkBoxMenuItem.isSelected());
        getComServerMobileMainFrame().stopWaitCursor();
    }

    private OfflineFrame getComServerMobileMainFrame() {
        return ((OfflineFrame) getValue(ActionKeys.MAIN_FRAME));
    }

    public TransactionLoggingDialog getTransactionLoggingDialog() {
        return transactionLoggingDialog;
    }

    public JCheckBoxMenuItem getCheckBoxMenuItem() {
        return checkBoxMenuItem;
    }
}
