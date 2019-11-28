package com.energyict.mdc.engine.offline.gui.dialogs;

import com.energyict.mdc.engine.offline.core.EisHelp;
import com.energyict.mdc.engine.offline.gui.windows.DefaultButtonPanel;
import com.energyict.mdc.engine.offline.gui.windows.EisPropsPnl;
import com.energyict.mdc.engine.offline.gui.windows.IconProvider;
import com.energyict.mdc.engine.offline.gui.windows.OkCancelButtonPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Copyrights EnergyICT
 * Date: 12-mei-2010
 * Time: 14:21:55
 */
public class DefaultPropsPnlContainer extends DefaultButtonPanel implements EisHelp, IconProvider {

    OkCancelButtonPanel buttonPanel;
    private Icon icon;
    private String helpId;

    public DefaultPropsPnlContainer(EisPropsPnl propsPnl) {
        super(new BorderLayout());
        icon = propsPnl.getIcon();
        helpId = propsPnl.getHelpId();
        this.add(propsPnl, BorderLayout.CENTER);
    }

    public void add(OkCancelButtonPanel panel) {
        super.add(panel, BorderLayout.SOUTH);
        this.buttonPanel = panel;
        if (panel.getOkButton()!=null) {
            setDefaultButton(panel.getOkButton());
        }
    }

    public JButton getOkButton() {
        if (this.buttonPanel == null) {
            return null;
        }
        return buttonPanel.getOkButton();
    }

    public JButton getCancelButton() {
        if (this.buttonPanel == null) {
            return null;
        }
        return buttonPanel.getCancelButton();
    }

    public Icon getIcon() {
        return icon;
    }

    public String getHelpId() {
        return helpId;
    }
}
