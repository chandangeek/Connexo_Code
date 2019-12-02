package com.energyict.mdc.engine.offline.gui.actions;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.windows.ComServerMobileDialogSettings;
import com.energyict.mdc.engine.offline.gui.windows.CommunicationLoggingDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Koen
 */
public class ToolsProtocolLogfileAction extends AbstractAction {

    JCheckBoxMenuItem checkBoxMenuItem;
    CommunicationLoggingDialog communicationLoggingDialog;

    /**
     * Creates a new instance of ToolsProtocolLogfileAction
     */
    public ToolsProtocolLogfileAction(OfflineFrame mainFrame) {
        putValue(Action.NAME, TranslatorProvider.instance.get().getTranslator().getTranslation("toolsprotocollogfile"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_P));
        putValue(ActionKeys.MAIN_FRAME, mainFrame);

        // Make sure the width & height falls inside the screen boundaries
        Dimension fullScreenSize = mainFrame.getToolkit().getScreenSize();
        Dimension preferredSize = new Dimension((int) (fullScreenSize.getWidth() * 0.70), (int) (fullScreenSize.getHeight() * 0.70));
        Dimension maxSize = new Dimension((int) (fullScreenSize.getWidth() * 0.90), (int) (fullScreenSize.getHeight() * 0.90));
        ComServerMobileDialogSettings settings = new ComServerMobileDialogSettings("protocollogdialog", (int) preferredSize.getWidth(), (int) preferredSize.getHeight());
        settings.setMaxWidth(maxSize.width);
        settings.setMaxHeight(maxSize.height);

        initCommunicationLoggingDialog(mainFrame, UiHelper.translate("ProtocolLog"), settings);
    }

    private void initCommunicationLoggingDialog(OfflineFrame mainFrame, String title, ComServerMobileDialogSettings settings) {
        this.communicationLoggingDialog = new CommunicationLoggingDialog(mainFrame, title, settings);
        this.communicationLoggingDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                getCheckBoxMenuItem().setSelected(false);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        getComServerMobileMainFrame().startWaitCursor();
        checkBoxMenuItem = (JCheckBoxMenuItem) ev.getSource();
        getCommunicationLoggingDialog().setVisible(checkBoxMenuItem.isSelected());
        getComServerMobileMainFrame().stopWaitCursor();
    }

    private OfflineFrame getComServerMobileMainFrame() {
        return ((OfflineFrame) getValue(ActionKeys.MAIN_FRAME));
    }

    public CommunicationLoggingDialog getCommunicationLoggingDialog() {
        return communicationLoggingDialog;
    }

    public JCheckBoxMenuItem getCheckBoxMenuItem() {
        return checkBoxMenuItem;
    }
}