package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.offline.gui.decorators.EventDecorator;

import javax.swing.*;
import java.awt.*;

public class CommunicationLoggingDialog extends ComServerMobileDialog {

    private CommunicationLoggingPanel communicationLoggingPnl;

    public CommunicationLoggingDialog(JFrame parent, String title, ComServerMobileDialogSettings settings) {
        super(parent, false, settings);
        initializePnl(title);
    }

    private void initializePnl(String title) {
        this.setLayout(new BorderLayout());
        this.setTitle(title);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.add(getCommunicationLoggingPnl(), BorderLayout.CENTER);
    }

    public void notifyOfComServerMonitorEvent(EventDecorator event) {
        getCommunicationLoggingPnl().notifyOfComServerMonitorEvent(event);
    }

    public CommunicationLoggingPanel getCommunicationLoggingPnl() {
        if (this.communicationLoggingPnl == null) {
            this.communicationLoggingPnl = new CommunicationLoggingPanel(/*this*/);
        }
        return this.communicationLoggingPnl;
    }
}