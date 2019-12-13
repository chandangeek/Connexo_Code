package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;
import com.energyict.mdc.engine.offline.gui.actions.CancelPropsPnlAction;
import com.energyict.mdc.engine.offline.gui.util.EisConst;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * JPanel that can be used within a EisDialog with a DefaultButtonPanel
 * and can closes the dialog...
 * For a better user experience size settings can be stored
 * Date: 8/03/13
 * Time: 14:19
 */
public class EisPanel extends JPanel {

    private boolean storeSizeInfo = false;
    private Preferences userPrefs;
    private String prefKey = null;
    private Preferences externalUserPrefs = null;
    private String externalPrefKeyHeight = null;
    private String externalPrefKeyWidth = null;

    protected DefaultButtonPanel buttonPanel = null;

    /**
     * Creates a new instance of EisPropsPnl
     */
    public EisPanel() {
    }

    public EisPanel(LayoutManager layout) {
        super(layout);
    }

    public void setStoreSizeInfo(String prefKey) {
        storeSizeInfo = true;
        this.prefKey = prefKey;
        userPrefs = Preferences.userNodeForPackage(OfflineFrame.class);
    }

    // used by external (BPM) classes

    public void setStoreSizeInfo(Preferences p, String keyWidth, String keyHeight) {
        storeSizeInfo = true;
        externalUserPrefs = p;
        externalPrefKeyWidth = keyWidth;
        externalPrefKeyHeight = keyHeight;
    }

    // Extra actions to perform when the cancel button was pressed
    // and just before the dialog is closed
    // Needs to be overriden by subclasses

    public void doCancel() {
        // does nothing:
    }

    // Close the (parent) dialog
    public void doClose() {
        Dimension d ;
        if (getRootPane() == null || getRootPane().getParent() == null) {
            return;
        }
        if (getRootPane().getParent() instanceof JInternalFrame) {
            JInternalFrame parentFrame = (JInternalFrame) (getRootPane().getParent());
            d = parentFrame.getSize();
            parentFrame.doDefaultCloseAction();
        } else {
            JDialog parentDialog = (JDialog) (getRootPane().getParent());
            d = parentDialog.getSize();
            parentDialog.setVisible(false);
            parentDialog.dispose();
        }
        if (!storeSizeInfo) {
            return;
        }

        if (externalUserPrefs != null) {
            if (externalPrefKeyHeight != null) {
                externalUserPrefs.putInt(externalPrefKeyHeight, d.height);
            }
            if (externalPrefKeyWidth != null) {
                externalUserPrefs.putInt(externalPrefKeyWidth, d.width);
            }
        } else if (prefKey != null) {
            userPrefs.putInt(prefKey + EisConst.PREFKEY_HEIGHT, d.height);
            userPrefs.putInt(prefKey + EisConst.PREFKEY_WIDTH, d.width);
        }
    }

    public void pack() {
        if (getRootPane() == null || getRootPane().getParent() == null) {
            return;
        }
        if (getRootPane().getParent() instanceof JInternalFrame) {
            JInternalFrame parentFrame = (JInternalFrame) (getRootPane().getParent());
            if (!parentFrame.isMaximum()) // Don't (re-)pack if maximized
            {
                parentFrame.pack();
            }
        } else {
            JDialog parentDialog = (JDialog) (getRootPane().getParent());
            parentDialog.pack();
        }
    }


    public DefaultButtonPanel constructDefaultButtonPanel() {
        this.buttonPanel = new DefaultButtonPanel();
        return this.buttonPanel;
    }


    public DefaultButtonPanel getDefaultButtonPanel() {
        return this.buttonPanel;
    }

    /**
     * OkCancelButtonPanel : the panel is a ChangeListener on the EisPropsPnl
     * if the PropertyChangeEvent's property name = 'shadow' the OK-button is enabled if
     * the newValue of the event = ObjectShadow is Dirty, if not the OK-button is disabled
     */
    public class DefaultButtonPanel extends OkCancelButtonPanel {

        /**
         * Method to be overridden by subclasses
         *
         * @return the action of the 'Cancel'-Button, by default returns null;
         */
        protected AbstractAction getCancelAction() {
            return new CancelPropsPnlAction(EisPanel.this);
        }
    }

    public String translate(String translationKey) {
        return TranslatorProvider.instance.get().getTranslator().getTranslation(translationKey);
    }

}
