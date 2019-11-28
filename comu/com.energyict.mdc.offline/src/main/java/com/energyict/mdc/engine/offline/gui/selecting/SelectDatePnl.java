/*
 * SelectDatePnl.java
 *
 * Created on 15 november 2004, 10:47
 */

package com.energyict.mdc.engine.offline.gui.selecting;


import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.beans.FormBuilder;
import com.energyict.mdc.engine.offline.gui.editors.DateAspectEditor;
import com.energyict.mdc.engine.offline.gui.windows.EisPropsPnl;
import com.energyict.mdc.engine.offline.gui.windows.WizardPnl;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Geert
 */
public class SelectDatePnl extends EisPropsPnl implements WizardPnl {

    private boolean canceled = false;
    private Date theDate = null;
    private FormBuilder builder;
    private DateAspectEditor editor;

    // Multiple dates mode
    private List theLabels = null;
    private List theDates = null; // List of DateWrappers
    private List builders = null;
    private boolean useFinishButton = false;
    private String helpId = "eiserver.nohelpyet";

    public class DateWrapper {

        private Date date = null;

        public DateWrapper(Date d) {
            date = d;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date d) {
            date = d;
        }
    }

    /**
     * Creates new form SelectDatePnl
     */
    public SelectDatePnl(String label, boolean showNone, boolean dateOnly) {
        this(label, showNone, dateOnly, null);
    }

    public SelectDatePnl(String label, boolean showNone, boolean dateOnly, Date initDate) {
        this(label, showNone, dateOnly, false, initDate);
    }

    public SelectDatePnl(String label, boolean showNone, boolean dateOnly, boolean showSeconds, Date initDate) {
        theDate = initDate;
        builder = new FormBuilder(this);
        editor = (DateAspectEditor) builder.getEditor("date");
        editor.setShowNoneButton(showNone);
        editor.setDateOnly(dateOnly);
        editor.setShowSeconds(showSeconds);
        initComponents();
        if (label != null) {
            theLabel.setText(label);
        } else {
            mainPanel.remove(theLabel);
        }
    }

    // Multiple dates mode:

    public SelectDatePnl(List<String> labels, List<Boolean> showNone, List<Boolean> showSeconds, List<Boolean> dateOnly, List<Date> initDates, boolean showButtons) {
        this.theDates = new ArrayList();
        for (int i = 0, m = initDates.size(); i < m; i++) {
            theDates.add(new DateWrapper((Date) initDates.get(i)));
        }
        this.theLabels = labels;
        this.builders = new ArrayList();

        for (int i = 0, m = theLabels.size(); i < m; i++) {
            builder = new FormBuilder(theDates.get(i));
            builders.add(builder);
            editor = (DateAspectEditor) builder.getEditor("date");
            editor.setDateOnly(dateOnly.get(i).booleanValue());
            editor.setShowNoneButton(showNone.get(i).booleanValue());
            if (!dateOnly.get(i).booleanValue()) {
                editor.setShowSeconds(showSeconds.get(i).booleanValue());
            }
        }
        initComponents();
        if (!showButtons) {
            remove(southPanel);
        }
        initForMultiDates();
    }

    // Multiple dates mode

    private void initForMultiDates() {
        mainPanel.removeAll();

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        for (int i = 0, m = theLabels.size(); i < m; i++) {
            String labelText = (String) theLabels.get(i);

            if (labelText != null) {
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = i;
                gridBagConstraints.anchor = GridBagConstraints.WEST;
                gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
                mainPanel.add(new JLabel(labelText), gridBagConstraints);
            }
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = i;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
            FormBuilder aBuilder = (FormBuilder) builders.get(i);
            JPanel panel = aBuilder.getPanel("date");
            mainPanel.add(panel, gridBagConstraints);
        }
    }

    public boolean isCanceled() {
        return canceled;
    }

    public Date getDate() {
        return theDate;
    }

    public void setDate(Date d) {
        theDate = d;
    }

    // Multiple dates mode:

    public List getDates() {
        List dates = new ArrayList();
        for (int i = 0, m = theDates.size(); i < m; i++) {
            dates.add(((DateWrapper) theDates.get(i)).getDate());
        }
        return dates;
    }

    // WizardPnl interface:

    public boolean useFinishButton() {
        return useFinishButton;
    }
    // Multiple dates mode

    public void setUseFinishButton(boolean useFinishButton) {
        this.useFinishButton = useFinishButton;
    }

    public String getHelpId() {
        return helpId;
    }

    public void setHelpId(String helpId) {
        this.helpId = helpId;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        GridBagConstraints gridBagConstraints;

        centerPanel = new JPanel();
        mainPanel = new JPanel();
        theLabel = new JLabel();
        datePanel = builder.getPanel("date");
        editor = (DateAspectEditor) builder.getEditor("date");
        southPanel = new JPanel();
        buttonPanel = new JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        centerPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        mainPanel.setLayout(new java.awt.GridBagLayout());

        mainPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(2, 2, 2, 2)));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mainPanel.add(theLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mainPanel.add(datePanel, gridBagConstraints);

        centerPanel.add(mainPanel);

        add(centerPanel, java.awt.BorderLayout.CENTER);

        southPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 6, 0));

        okButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("ok"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(okButton);

        cancelButton.setText(TranslatorProvider.instance.get().getTranslator().getTranslation("cancel"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(cancelButton);

        southPanel.add(buttonPanel);

        add(southPanel, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        performEscapeAction();
    }//GEN-LAST:event_cancelButtonActionPerformed

    public void performEscapeAction() {
        canceled = true;
        doClose();
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose();
    }//GEN-LAST:event_okButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private JPanel centerPanel;
    private JPanel datePanel;
    private JPanel mainPanel;
    private javax.swing.JButton okButton;
    private JPanel southPanel;
    private JLabel theLabel;
    // End of variables declaration//GEN-END:variables

}
