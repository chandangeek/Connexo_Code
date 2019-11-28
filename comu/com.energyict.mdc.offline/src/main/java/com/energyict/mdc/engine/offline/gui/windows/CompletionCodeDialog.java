package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.config.LookupEntry;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.dialogs.EisDialog;
import com.energyict.mdc.engine.offline.model.CustomCompletionCode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jme, gde
 */
public class CompletionCodeDialog extends EisDialog {

    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel jLabelMessage;
    private JScrollPane scrollPane1;
    private JTextArea jTextFieldCode;
    private JPanel buttonBar;
    private JButton jButtonOk;
    private JButton jButtonCancel;
    private JPanel completionCodePnl;
    private JComboBox<String> completionCodeCombo;
    private String completionCode;
    private String reasonCode = null;
    private boolean isCanceled = false;
    private final List<LookupEntry> completionCodes;

    public CompletionCodeDialog(Frame parent, List<LookupEntry> completionCodes, boolean modal) {
        super(parent, UiHelper.translate("errorCodeDlgTitle"), modal);
        this.completionCodes = (completionCodes == null ? new ArrayList<LookupEntry>() : completionCodes);
        initComponents();
    }

    private void initComponents() {
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        jLabelMessage = new JLabel();
        scrollPane1 = new JScrollPane();
        jTextFieldCode = new JTextArea();
        buttonBar = new JPanel();
        jButtonOk = new JButton();
        jButtonCancel = new JButton();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        jLabelMessage.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("errorCodeDlgLabel"));
        jButtonOk.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("ok"));
        jButtonCancel.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("cancel"));


        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());
            if (!completionCodes.isEmpty()) {
                dialogPane.add(getCompletionCodePnl(), BorderLayout.NORTH);
            }

            //======== contentPanel ========
            {
                contentPanel.setLayout(new GridBagLayout());
                ((GridBagLayout) contentPanel.getLayout()).columnWidths = new int[]{400, 0};
                ((GridBagLayout) contentPanel.getLayout()).rowHeights = new int[]{0, 150, 0};
                ((GridBagLayout) contentPanel.getLayout()).columnWeights = new double[]{1.0, 1.0E-4};
                ((GridBagLayout) contentPanel.getLayout()).rowWeights = new double[]{0.0, 1.0, 1.0E-4};

                //---- jLabelMessage ----
                contentPanel.add(jLabelMessage, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 2, 0), 0, 0));

                //======== scrollPane1 ========
                {

                    //---- textArea1 ----
                    jTextFieldCode.setLineWrap(true);
                    jTextFieldCode.setTabSize(4);
                    scrollPane1.setViewportView(jTextFieldCode);
                }
                contentPanel.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[]{0, 85, 80};
                ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[]{1.0, 0.0, 0.0};

                //---- okButton ----
                jButtonOk.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        jButtonOkActionPerformed(e);
                    }
                });
                buttonBar.add(jButtonOk, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                buttonBar.add(jButtonCancel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
                jButtonCancel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        performEscapeAction(null);
                    }
                });
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());

        // To set the initial focus on the OK button
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                UiHelper.setDefaultFocusInWindow(completionCodes.isEmpty() ? jTextFieldCode : jButtonOk);
            }
        });
    }

    private void jButtonOkActionPerformed(ActionEvent evt) {
        String text = jTextFieldCode.getText();
        reasonCode = text == null ? "" : text;
        dispose();
    }

    @Override
    public void performEscapeAction(KeyEvent evt) {
        isCanceled = true;
        dispose();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Tabbing in the textarea => focus the OK button
        if (e.getKeyCode() == KeyEvent.VK_TAB && e.getSource().equals(jTextFieldCode)) {
            jButtonOk.requestFocus();
            return;
        }
        super.keyPressed(e);
    }

    private JPanel getCompletionCodePnl() {
        if (completionCodePnl == null) {
            completionCodePnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            JLabel label = new JLabel(UiHelper.translate("completionCode") + ":");
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            completionCodePnl.add(label);
            completionCodePnl.add(getCompletionCodeCombo());
            completionCodePnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
        }
        return completionCodePnl;
    }

    private JComboBox<String> getCompletionCodeCombo() {
        if (completionCodeCombo == null) {
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            for (LookupEntry each : completionCodes) {
                if (each.getValue().equals(getCompletionCode())) {
                    model.setSelectedItem(each.getValue());
                }
                model.addElement(each.getValue());
            }
            completionCodeCombo = new JComboBox<>(model);
            completionCodeCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    completionCode = (String) completionCodeCombo.getSelectedItem();
                }
            });
            completionCode = completionCodeCombo.getItemAt(0);
        }
        return completionCodeCombo;
    }

    private String getReasonCode() {
        return reasonCode;
    }

    private String getCompletionCode() {
        return isCanceled() ? null : completionCode;
    }

    private boolean isCanceled() {
        return isCanceled;
    }

    /*
     * Public getters and setters
	 */

    public CustomCompletionCode getCustomCompletionCode() {
        return isCanceled() ? null : new CustomCompletionCode(getCompletionCode(), getReasonCode());
    }


}
