package com.energyict.mdc.engine.offline.gui.windows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ComServerMobileDialog extends JDialog {

    private ComServerMobileDialogSettings settings;

    public ComServerMobileDialog(JFrame parent, boolean modal, ComServerMobileDialogSettings settings) {
        super(parent, modal);
        this.settings = settings;
        this.settings.restore();
        if (getSettings().getPosX()==ComServerMobileDialogSettings.DO_CENTER && getSettings().getPosY()==ComServerMobileDialogSettings.DO_CENTER) {
            if (getParent() != null) {
                Dimension parentSize = getParent().getSize();
                Point point = getParent().getLocation();
                setLocation(point.x + parentSize.width / 2 - getSettings().getPreferredWidth() / 2,
                    point.y + parentSize.height / 2 - getSettings().getPreferredHeight() / 2);
            }
        } else {
            setLocation(getSettings().getPosX(), getSettings().getPosY());
        }
        setSize(getSettings().getPreferredWidth(), getSettings().getPreferredHeight());
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                getSettings().setPreferredWidth(getWidth());
                getSettings().setPreferredHeight(getHeight());
                storeSettings();
            }

            public void componentMoved(java.awt.event.ComponentEvent evt) {
                getSettings().setPosX(getLocation().x);
                getSettings().setPosY(getLocation().y);
                storeSettings();
            }
        });
    }

    protected void storeSettings() {
        this.settings.store();
    }

    public ComServerMobileDialogSettings getSettings() {
        return settings;
    }
}
