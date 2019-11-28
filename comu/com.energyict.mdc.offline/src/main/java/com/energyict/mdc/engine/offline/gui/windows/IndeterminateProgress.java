/*
 * IndeterminateProgress.java
 *
 * Created on 25 maart 2005, 10:06
 */

package com.energyict.mdc.engine.offline.gui.windows;

import com.energyict.mdc.engine.offline.gui.UiHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Koen, geert
 */
public class IndeterminateProgress extends JDialog {

    private IndeterminateProgressCancel ipc;

    private JButton jButtonCancel;
    private JPanel jPanel1;
    private JPanel jPanelControl;
    private JProgressBar jProgressBar;
    private String message;

    /**
     * Creates new form IndeterminateProgress
     * <p/>
     * Size 0 means indeterminate. Other positive values mean actual progress
     */
    public IndeterminateProgress(int size, JFrame parent, IndeterminateProgressCancel ipc, String message, boolean cancelable) {
        super(parent, true);
        setTitle(UiHelper.translate("fileChooser.pleaseWait")+"...");
        this.message = message;
        this.ipc = ipc;
        initComponents(cancelable);
        if (size == 0) {
            jProgressBar.setIndeterminate(true);
        } else {
            jProgressBar.setIndeterminate(false);
            jProgressBar.setMinimum(0);
            jProgressBar.setMaximum(size);
            jProgressBar.setValue(0);
        }
        jProgressBar.setString("");
        DialogLocalizer.place(parent, this);
    }

    public void setSize(int size) {
        jProgressBar.setIndeterminate(false);
        jProgressBar.setMinimum(0);
        jProgressBar.setMaximum(size);
        jProgressBar.setValue(0);
    }

    private void initComponents(boolean cancelable) {
        jPanel1 = new JPanel();
        jProgressBar = new JProgressBar();
        jPanelControl = new JPanel();
        jButtonCancel = new JButton();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        jPanel1.setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        if (message!=null) {
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.insets = new Insets(15, 15, 4, 15);
            gridBagConstraints.anchor = GridBagConstraints.CENTER;
            jPanel1.add(new JLabel(message), gridBagConstraints);
        }

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(4, 15, 4, 15);
        jPanel1.add(jProgressBar, gridBagConstraints);

        jButtonCancel.setText(UiHelper.translate("cancel"));
        jButtonCancel.setEnabled(cancelable);
        jButtonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onButtonCancelPressed();
            }
        });

        jPanelControl.add(jButtonCancel);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(4, 15, 4, 15);
        jPanel1.add(jPanelControl, gridBagConstraints);

        getContentPane().add(jPanel1, BorderLayout.CENTER);

        pack();
    }

    private void onButtonCancelPressed() {
        if (ipc != null) {
            ipc.cancel();
        }
        doClose();
    }

    public String getMessage() {
        return message;
    }

    public void close() {
        doClose();
    }

    private void doClose() {
        setVisible(false);
        dispose();
    }

    public void increaseProgress() {
        jProgressBar.setValue(jProgressBar.getValue() + 1);
        jProgressBar.repaint();
    }
}