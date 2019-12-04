package com.energyict.mdc.engine.offline.gui.actions;

import com.energyict.mdc.engine.offline.gui.windows.EisPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Action to close a <Code>EisPropsPnl</Code>
 * Copyrights EnergyICT
 * Date: 10-mei-2010
 * Time: 13:26:33
 */
public class CancelPropsPnlAction extends AbstractAction {

    private EisPanel pnl;

    public CancelPropsPnlAction(EisPanel pnl) {
        this.pnl = pnl;
        this.setEnabled(this.pnl != null);
    }

    public void doAction(ActionEvent event) {
        if (this.pnl != null) {
            pnl.doCancel();
            pnl.doClose();
        }
    }

    public void actionPerformed(ActionEvent event) {
        
    }
}