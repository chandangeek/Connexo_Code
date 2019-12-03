package com.energyict.mdc.engine.offline.gui.windows;

import javax.swing.*;
import java.awt.*;

/**
 * Copyrights EnergyICT
 * Date: 12-mei-2010
 * Time: 13:57:10
 */
public class DefaultButtonPanel extends JPanel implements DefaultButtonOwner {

    private JButton defaultButton = null;

    public DefaultButtonPanel() {
        super();
    }

    public DefaultButtonPanel(LayoutManager layout) {
        super(layout);
    }

    public void setDefaultButton(JButton button) {
        this.defaultButton = button;
    }

    public JButton getDefaultButton() {
        return this.defaultButton;
    }
}
