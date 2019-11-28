/*
 * EisPropsPnl.java
 *
 * Created on 17 juli 2003, 13:16
 */

package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.offline.core.EisHelp;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.exception.BusinessException;
import com.energyict.mdc.engine.offline.core.exception.DatabaseException;
import com.energyict.mdc.engine.offline.core.exception.OptimisticLockingFailedException;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.actions.EisAbstractAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;


public class EisPropsPnl extends EisPanel implements EisHelp, IconProvider {


    private EisInternalFrame frame = null;

    // key in Language Bundle that tells that the object( Device, Virtual Meter...) was modified by another user
    protected String optimisticLockingFailMessage = "objectModifiedByOtherUser";
    private EisPropsPnlState panelState;

    /**
     * Creates a new instance of EisPropsPnl
     */
    public EisPropsPnl() {
    }

    public EisPropsPnl(LayoutManager layout) {
        super(layout);
    }


    // -------- Update the item

    public boolean updateItem() {
        Cursor c = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                doUpdate();
                return true;
            } catch (OptimisticLockingFailedException e) {
                return (refreshAfterOptimisticLock(e));
            } catch (BusinessException | SQLException | DatabaseException e) {
                return UiHelper.reportException(e, frame.getMainWindow());
            }
        } finally {
            setCursor(c);
        }
    }

    // -------- Create a new item

    public boolean createItem() {
        Cursor c = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                doCreate();
                return true;
            } catch (BusinessException | SQLException e) {
                return UiHelper.reportException(e, frame.getMainWindow());
            }
        } finally {
            setCursor(c);
        }
    }

    // -------- Delete the item

    public boolean deleteItem() {
        if (!doConfirm()) {
            return false;
        }
        Cursor c = getCursor();
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            try {
                doDelete();
                return true;
            } catch (BusinessException | SQLException e) {
                return UiHelper.reportException(e, frame.getMainWindow());
            }
        } finally {
            setCursor(c);
        }
    }

    // Can be overridden whenever needed (to show the right upper left corner icon in the window)
    public Icon getIcon() { // IconProvider interface
        return null;
    }

    // --------- Returns the item
    // Needs to be overriden by subclasses

    // Functions to be overwritten in the subclasses:

    public void doUpdate() throws BusinessException, SQLException {
    }

    public void doCreate() throws BusinessException, SQLException {
    }

    public void doDelete() throws BusinessException, SQLException {
    }

    public boolean doConfirm() { // returns true if the user confirmed
        return true;
    }

    public boolean refreshAfterOptimisticLock(OptimisticLockingFailedException e) {
        Object[] options = {TranslatorProvider.instance.get().getTranslator().getTranslation("refresh"), TranslatorProvider.instance.get().getTranslator().getTranslation("cancel")};
        if (JOptionPane.showOptionDialog(getFrame(),
                TranslatorProvider.instance.get().getTranslator().getTranslation(optimisticLockingFailMessage),
                TranslatorProvider.instance.get().getTranslator().getTranslation("Error"),
                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                null, options, options[0]) == 0) {
            try {
                doRefreshObject(e.getObject());
                return false;
            } catch (BusinessException | SQLException ex) {
                UiHelper.reportException(e, frame.getMainWindow());
                return true;
            }
        }
        return true;
    }

    public void doRefreshObject(Object o) throws BusinessException, SQLException {
        //does nothing: subclasses need to override if necessary
    }

    public String getHelpId() {
        return "eiserver.nohelpyet";
    }

    public void setFrame(EisInternalFrame frame) {
        this.frame = frame;
    }

    public EisInternalFrame getFrame() {
        return frame;
    }

    // Added for the "History of Changes" case

    public JTabbedPane getTabbedPane() {
        return null;
    }

    public int getActiveTabIndex() {
        JTabbedPane tabbedPane = getTabbedPane();
        if (tabbedPane == null) {
            return -1;
        }
        return tabbedPane.getSelectedIndex();
    }

    public void activateTab(int index) {
        JTabbedPane tabbedPane = getTabbedPane();
        if (tabbedPane == null) {
            return;
        }
        if (index < 0 || index >= tabbedPane.getTabCount() || !tabbedPane.isEnabledAt(index)) {
            return;
        }
        tabbedPane.setSelectedIndex(index);
    }

    public DefaultButtonPanel constructCreateItemButtonPanel() {
        this.buttonPanel = new DefaultCreateItemButtonPanel();
        return this.buttonPanel;
    }

    public DefaultButtonPanel constructUpdateItemButtonPanel() {
        this.buttonPanel = new DefaultUpdateItemButtonPanel();
        return this.buttonPanel;
    }

    public DefaultButtonPanel constructDeleteItemButtonPanel() {
        this.buttonPanel = new DefaultDeleteItemButtonPanel();
        return buttonPanel;
    }

    public EisPropsPnlState getPanelState() {
        return panelState;
    }

    public void setPanelState(EisPropsPnlState newState) {
        this.panelState = newState;
    }

    // subclasses need to override this method to "apply" the panel state
    public void applyPanelState(EisPropsPnlState state) {
    }

    protected void addToPanelWithDefaultGridBagConstraint(JPanel gridBagPanel, JComponent componentToAdd, int x, int y) {
        addToPanelWithDefaultGridBagConstraint(gridBagPanel, componentToAdd, x, y, 1, 2);
    }

    protected void addToPanelWithDefaultGridBagConstraint(JPanel gridBagPanel, JComponent componentToAdd, int x, int y, int gridWidth, int insetLeft) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = x;
        gridBagConstraints.gridy = y;
        gridBagConstraints.gridwidth=gridWidth;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, insetLeft, 2, 2);
        gridBagPanel.add(componentToAdd, gridBagConstraints);
    }

    /**
     * <Code>OkCancelButtonPanel</Code> where the OK button creates an item
     * the Close Button closes this <Code>EisPropsPnl</Code>
     */
    private class DefaultCreateItemButtonPanel extends DefaultButtonPanel {

        protected AbstractAction getOkAction() {
            EisAbstractAction action = new EisAbstractAction() {
                public void doAction(ActionEvent evt) {
                    if (EisPropsPnl.this.createItem()) {
                        EisPropsPnl.this.doClose();
                    }
                }
            };
            action.setEnabled(false);
            return action;
        }
    }

    /**
     * <Code>OkCancelButtonPanel</Code> where the OK button updates the item
     * the Close Button closes this <Code>EisPropsPnl</Code>
     */
    private class DefaultUpdateItemButtonPanel extends DefaultButtonPanel {

        protected AbstractAction getOkAction() {
            EisAbstractAction action = new EisAbstractAction() {
                public void doAction(ActionEvent evt) {
                    if (EisPropsPnl.this.updateItem()) {
                        EisPropsPnl.this.doClose();
                    }
                }
            };
            action.setEnabled(false);
            return action;
        }
    }

    /**
     * <Code>OkCancelButtonPanel</Code> where the OK button deletes the item
     * the Close Button closes this <Code>EisPropsPnl</Code>
     */
    private class DefaultDeleteItemButtonPanel extends DefaultButtonPanel {

        /**
         * Method to be overridden by subclasses
         *
         * @return the action of the 'Cancel'-Button, by default returns null;
         */
        protected AbstractAction getOkAction() {
            return  new EisAbstractAction() {
                public void doAction(ActionEvent evt) {
                    if (EisPropsPnl.this.deleteItem()) {
                        EisPropsPnl.this.doClose();
                    }
                }
            };
        }
    }

    public String translate(String translationKey) {
        return TranslatorProvider.instance.get().getTranslator().getTranslation(translationKey);
    }

    public String translateError(String translationKey) {
        return TranslatorProvider.instance.get().getTranslator().getErrorMsg(translationKey);
    }
}