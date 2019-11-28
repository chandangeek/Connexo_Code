package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import javax.swing.*;
import java.awt.*;

/**
 * JPanel having a 'OK' and 'Cancel'-Button to use in e.g. EisPropsPnl
 * Copyrights EnergyICT
 * Date: 10-mei-2010
 * Time: 13:04:19
 */
public class OkCancelButtonPanel extends JPanel {

    private JButton okButton;
    private JButton cancelButton;
    private JPanel buttonContainer;

    public OkCancelButtonPanel() {
        initComponents();
    }

    public void remove(JButton button) {
        buttonContainer.remove(button);
    }

    public void add(JButton button) {
        buttonContainer.add(button);
    }

    public void add(JButton button, int index) {
        buttonContainer.add(button, index);
    }

    public JButton getOkButton() {
        if (this.okButton == null) {
            okButton = new JButton();
            AbstractAction okAction = getOkAction();
            if (okAction != null) {
                okButton.setAction(okAction);
            }
            if (okButton.getText() == null || okButton.getText().length() == 0) {
                okButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("ok"));
            }
        }
        return okButton;
    }

    public JButton getCancelButton() {
        if (this.cancelButton == null) {
            cancelButton = new JButton();
            AbstractAction cancelAction = getCancelAction();
            if (cancelAction != null) {
                cancelButton.setAction(cancelAction);
            }
            if (cancelButton.getText() == null || cancelButton.getText().length() == 0) {
                cancelButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("cancel"));
            }
        }
        return cancelButton;
    }


    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.TRAILING));

        buttonContainer = new JPanel(new java.awt.GridLayout(1, 0, 6, 0));
        buttonContainer.add(getOkButton());
        buttonContainer.add(getCancelButton());
        this.add(buttonContainer);
    }

    /**
     * Method to be overridden by subclasses
     *
     * @return the action of the 'OK'-Button, by default returns null;
     */
    protected AbstractAction getOkAction() {
        return null;
    }

    /**
     * Method to be overridden by subclasses
     *
     * @return the action of the 'Cancel'-Button, by default returns null;
     */
    protected AbstractAction getCancelAction() {
        return null;
    }

}
