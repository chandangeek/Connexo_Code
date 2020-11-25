/*
 * TimePnl.java
 *
 * Created on 8 oktober 2003, 16:00
 */

package com.energyict.mdc.engine.offline.gui.panels;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author pasquien
 */
public class TimePnl extends javax.swing.JPanel {

    private class SpinnerRolloverNumberModel extends SpinnerNumberModel {

        public SpinnerRolloverNumberModel(int value, int min, int max, int step) {
            super(value, min, max, step);
        }

        public Object getNextValue() {
            int nextValue;
            if ((getValue()).equals(getMaximum())) {
                nextValue = ((Integer) getMinimum()).intValue();
            } else {
                nextValue = ((Integer) getValue()).intValue() + 1;
            }
            return (new Integer(nextValue));
        }

        public Object getPreviousValue() {
            int prevValue;
            if ((getValue()).equals(getMinimum())) {
                prevValue = ((Integer) getMaximum()).intValue();
            } else {
                prevValue = ((Integer) getValue()).intValue() - 1;
            }
            return (new Integer(prevValue));
        }
    }

    private boolean hoursVisible, minutesVisible, secondsVisible;

    public TimePnl(boolean hoursVisible, boolean minutesVisible, boolean secondsVisible) {
        this.hoursVisible = hoursVisible;
        this.minutesVisible = minutesVisible;
        this.secondsVisible = secondsVisible;
        initComponents();
        initSpinners();
    }

    public void setEnabled(boolean isEnabled) {
        hourSpinner.setEnabled(false);
        minuteSpinner.setEnabled(false);
        secondSpinner.setEnabled(false);
    }

    public void setFont(Font font) {
        super.setFont(font);
        if (hourSpinner != null) {
            ((JSpinner.NumberEditor) hourSpinner.getEditor()).getTextField().setFont(font);
            ((JSpinner.NumberEditor) minuteSpinner.getEditor()).getTextField().setFont(font);
            ((JSpinner.NumberEditor) secondSpinner.getEditor()).getTextField().setFont(font);
        }
    }

    public void setOpaque(boolean isOpaque) {
        super.setOpaque(isOpaque);
        if (alignmentPanel != null) {
            alignmentPanel.setOpaque(isOpaque);
        }
        if (hourSpinner != null) {
            hourSpinner.setOpaque(isOpaque);
            minuteSpinner.setOpaque(isOpaque);
            secondSpinner.setOpaque(isOpaque);
            // When used as a cellEditor we reshape the component a little bit...
            if (!isOpaque) {
                ((JSpinner.NumberEditor) hourSpinner.getEditor()).getTextField().setBorder(new EmptyBorder(0, 0, 0, 0));
                ((JSpinner.NumberEditor) minuteSpinner.getEditor()).getTextField().setBorder(new EmptyBorder(0, 0, 0, 0));
                ((JSpinner.NumberEditor) secondSpinner.getEditor()).getTextField().setBorder(new EmptyBorder(0, 0, 0, 0));

                GridBagConstraints gridBagConstraints;

                GridBagLayout layout = (GridBagLayout) alignmentPanel.getLayout();
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.insets = new Insets(0, 0, 0, 0);
                gridBagConstraints.anchor = GridBagConstraints.WEST;
                layout.setConstraints(hourSpinner, gridBagConstraints);
                layout.setConstraints(minuteSpinner, gridBagConstraints);
                layout.setConstraints(secondSpinner, gridBagConstraints);
                alignmentPanel.doLayout();
            }
        }
    }

    public void setHeight(int height) {
        Dimension dim, dimNew;

        dim = this.getSize();
        dimNew = new Dimension(new Double(dim.getWidth()).intValue(), height);
        this.setSize(dimNew);
        if (hourSpinner != null) {
            dim = hourSpinner.getPreferredSize();
            dimNew = new Dimension(new Double(dim.getWidth()).intValue(), height);
            hourSpinner.setPreferredSize(dimNew);
        }
        if (minuteSpinner != null) {
            dim = minuteSpinner.getPreferredSize();
            dimNew = new Dimension(new Double(dim.getWidth()).intValue(), height);
            minuteSpinner.setPreferredSize(dimNew);
        }
        if (secondSpinner != null) {
            dim = secondSpinner.getPreferredSize();
            dimNew = new Dimension(new Double(dim.getWidth()).intValue(), height);
            secondSpinner.setPreferredSize(dimNew);
        }

    }

    public int getValue() {
        int hour = 0;
        int minute = 0;
        int second = 0;

        // Geert (2003-nov-27)
        // Make sure the possibly manually edited value
        // is already "known" by the spinner(model)
        try {
            if (hoursVisible) {
                hourSpinner.commitEdit();
            }
            if (minutesVisible) {
                minuteSpinner.commitEdit();
            }
            if (secondsVisible) {
                secondSpinner.commitEdit();
            }
        } catch (java.text.ParseException e) {
        }

        if (hoursVisible) {
            hour = ((Integer) hourSpinner.getValue()).intValue();
        }
        if (minutesVisible) {
            minute = ((Integer) minuteSpinner.getValue()).intValue();
        }
        if (secondsVisible) {
            second = ((Integer) secondSpinner.getValue()).intValue();
        }
        return hour * 10000 + minute * 100 + second;
    }

    public void setValue(int value) {
        int hour = 0;
        int minute = 0;
        int second = 0;

        if (hoursVisible) {
            hour = Math.max(value / 10000, 0);
            if (hour > 23) {
                hour = 0;
            }
            hourSpinner.setValue(new Integer(hour));
        }

        if (minutesVisible) {
            minute = Math.max((value % 10000) / 100, 0);
            if (minute > 59) {
                minute = 0;
            }
            minuteSpinner.setValue(new Integer(minute));
        }

        if (secondsVisible) {
            second = Math.max(value % 100, 0);
            if (second > 59) {
                minute = 0;
            }
            secondSpinner.setValue(new Integer(second));
        }

    }

    private void initSpinners() {
        if (hourSpinner != null) {
            hourSpinner.setModel(new SpinnerRolloverNumberModel(0, 0, 23, 1));
            minuteSpinner.setModel(new SpinnerRolloverNumberModel(0, 0, 59, 1));
            secondSpinner.setModel(new SpinnerRolloverNumberModel(0, 0, 59, 1));
        }
    }


    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        GridBagConstraints gridBagConstraints;

        alignmentPanel = new javax.swing.JPanel();
        hourSpinner = new JSpinner();
        minuteSpinner = new JSpinner();
        secondSpinner = new JSpinner();

        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        alignmentPanel.setLayout(new GridBagLayout());

        hourSpinner.setFont(getFont());
        hourSpinner.setBorder(null);
        hourSpinner.setMinimumSize(new Dimension(31, 16));
        hourSpinner.setModel(new SpinnerNumberModel(0, 0, 23, 1));
        hourSpinner.setPreferredSize(new Dimension(38, 16));
        hourSpinner.setVisible(hoursVisible);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 0, 2);
        alignmentPanel.add(hourSpinner, gridBagConstraints);

        minuteSpinner.setFont(getFont());
        minuteSpinner.setBorder(null);
        minuteSpinner.setMinimumSize(new Dimension(31, 16));
        minuteSpinner.setModel(new SpinnerNumberModel(0, 0, 59, 1));
        minuteSpinner.setPreferredSize(new Dimension(38, 16));
        minuteSpinner.setVisible(minutesVisible);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 2, 0, 2);
        alignmentPanel.add(minuteSpinner, gridBagConstraints);

        secondSpinner.setFont(getFont());
        secondSpinner.setBorder(null);
        secondSpinner.setMinimumSize(new Dimension(31, 16));
        secondSpinner.setModel(new SpinnerNumberModel(0, 0, 59, 1));
        secondSpinner.setPreferredSize(new Dimension(38, 16));
        secondSpinner.setVisible(secondsVisible);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 2, 0, 0);
        alignmentPanel.add(secondSpinner, gridBagConstraints);

        add(alignmentPanel);

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel alignmentPanel;
    private JSpinner hourSpinner;
    private JSpinner minuteSpinner;
    private JSpinner secondSpinner;
    // End of variables declaration//GEN-END:variables

}
