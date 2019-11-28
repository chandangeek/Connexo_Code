/*
 * OptionsPnl.java
 *
 * Created on 11 april 2005, 13:18
 */

package com.energyict.mdc.engine.offline.gui.panels;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.core.Utils;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.beans.FormBuilder;
import com.energyict.mdc.engine.offline.gui.util.EisConst;
import com.energyict.mdc.engine.offline.gui.windows.EisPropsPnl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.prefs.Preferences;

public class OptionsPnl extends EisPropsPnl {

    private JPanel buttonPanel;
    private JButton cancelButton;
    private JPanel centerPanel;
    private JTextField fontSizeField;
    private JLabel fontSizeLabel;
    private JButton okButton;
    private JPanel propsPanel;
    private JPanel southPanel;

    private int fontSize = 11;
    private static int MAX_FONTSIZE = 40;
    private static int MIN_FONTSIZE = 8;

    private FormBuilder builder = new FormBuilder(this);
    private Preferences userPrefs;

    public OptionsPnl() {
        userPrefs = Preferences.userNodeForPackage(this.getClass());
        fontSize = userPrefs.getInt(EisConst.PREFKEY_FONTSIZE, 11);
        initComponents();
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fs) {
        fontSize = fs;
    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        centerPanel = new JPanel();
        propsPanel = new JPanel();
        fontSizeLabel = builder.getLabel("fontSize");
        fontSizeField = builder.getTextField("fontSize");
        fontSizeField.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
            }
            public void focusGained(FocusEvent e) {
                if (fontSizeField.getText().length() > 0) {
                    fontSizeField.setSelectionStart(0);
                    fontSizeField.setSelectionEnd(fontSizeField.getText().length());
                }
            }
        });

        southPanel = new JPanel();
        buttonPanel = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();

        setLayout(new BorderLayout());

        centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        propsPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        propsPanel.add(fontSizeLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        propsPanel.add(fontSizeField, gridBagConstraints);

        centerPanel.add(propsPanel);

        add(centerPanel, BorderLayout.CENTER);

        southPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        buttonPanel.setLayout(new GridLayout(1, 0, 6, 0));

        okButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("ok"));
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButtonActionPerformed();
            }
        });

        buttonPanel.add(okButton);

        cancelButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("cancel"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed();
            }
        });

        buttonPanel.add(cancelButton);

        southPanel.add(buttonPanel);

        add(southPanel, BorderLayout.SOUTH);

    }

    private void okButtonActionPerformed() {
        if (fontSize < MIN_FONTSIZE || fontSize > MAX_FONTSIZE) {
            String pattern = TranslatorProvider.instance.get().getTranslator().getTranslation("fontSizeUnacceptableXY");
            JOptionPane.showMessageDialog(UiHelper.getMainWindow(),
                Utils.format(pattern, new Object[]{new Integer(MIN_FONTSIZE), new Integer(MAX_FONTSIZE)}));
            return;
        }

        int previousFontSize = userPrefs.getInt(EisConst.PREFKEY_FONTSIZE, 11);
        // write the new font size in the registry(/ini file) so the method that applies it can find it:
        userPrefs.putInt(EisConst.PREFKEY_FONTSIZE, fontSize);
        if (!UiHelper.getMainWindow().applyNewFontSize()) {
            // User changed his/her mind, so write the previous font size again:
            userPrefs.putInt(EisConst.PREFKEY_FONTSIZE, previousFontSize);
        }
        doClose();
    }

    private void cancelButtonActionPerformed() {
        performEscapeAction();
    }

    public void performEscapeAction() {
        doClose();
    }

    public JButton getDefaultButton() {
        return okButton;
    }
}
